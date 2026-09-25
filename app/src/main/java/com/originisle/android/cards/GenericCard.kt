package com.originisle.android.cards

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import androidx.core.app.NotificationCompat
import com.originisle.android.R
import com.originisle.android.island.OriginIslandConstants
import com.originisle.android.island.PlaygroundService
import com.originisle.android.service.IconCache
import com.originisle.android.ui.PREFS_NAME
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Casts an "ordinary" notification — downloads, calls, progress, plain messages, and messenger chats —
 * as an island card with customizable appearance, fonts, character limits, and accurate display duration.
 */
object GenericCard {

    /**
     * @param isLive whether [NotificationCastListener] classified this as a live/persistent
     * notification (ongoing, call, or progress) rather than a one-off message.
     * @param category notification category: "normal", "messenger", "navigation", etc.
     */
    fun post(
        context: Context,
        sbn: StatusBarNotification,
        isLive: Boolean,
        category: String = "normal",
    ) {
        val n = sbn.notification
        val extras = n.extras
        val id = IconCache.castIdFor(sbn)
        val isCall = n.category == NotificationCompat.CATEGORY_CALL ||
            extras.getString(NotificationCompat.EXTRA_TEMPLATE) == "android.app.Notification\$CallStyle"

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val maxCapsuleChars = prefs.getInt("notification_capsule_chars", 16)
        val maxBodyChars = prefs.getInt("notification_body_chars", 80)
        val notifStyle = prefs.getString("notification_display_style", "classic") ?: "classic"
        val notifFont = prefs.getString("notification_font_family", "default") ?: "default"
        val notifFontSize = prefs.getString("notification_font_size", "normal") ?: "normal"
        val notifShowTimestamp = prefs.getBoolean("notification_show_timestamp", true)
        val autoDismissSec = prefs.getInt("cast_auto_dismiss_seconds", 0)

        val rawText = extras.getCharSequence(NotificationCompat.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        val rawBigText = extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT)?.toString()?.trim().orEmpty()
        val rawSubText = extras.getCharSequence(NotificationCompat.EXTRA_SUB_TEXT)?.toString()?.trim().orEmpty()
        val appLabel = runCatching {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(sbn.packageName, 0),
            ).toString()
        }.getOrDefault(sbn.packageName)

        val rawTitle = extras.getCharSequence(NotificationCompat.EXTRA_TITLE)?.toString()?.trim().orEmpty()
            .ifBlank { appLabel }

        // Trim body according to user's character limit preference
        val rawBody = rawText.ifBlank { rawBigText }.ifBlank { rawSubText }
        val bodyText = if (maxBodyChars > 0 && rawBody.length > maxBodyChars) {
            rawBody.take(maxBodyChars) + "…"
        } else {
            rawBody
        }

        // Format real timestamp if present
        val timeString = if (notifShowTimestamp && (sbn.postTime > 0 || n.`when` > 0)) {
            val ts = if (sbn.postTime > 0) sbn.postTime else n.`when`
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))
        } else ""

        val progress = extras.getInt(NotificationCompat.EXTRA_PROGRESS, 0)
        val progressMax = extras.getInt(NotificationCompat.EXTRA_PROGRESS_MAX, 0)
        val indeterminate = extras.getBoolean(NotificationCompat.EXTRA_PROGRESS_INDETERMINATE, false)
        val hasProgress = progressMax > 0 && !indeterminate

        val showChrono = extras.getBoolean(NotificationCompat.EXTRA_SHOW_CHRONOMETER, false)
        val percentText = if (hasProgress) "${progress * 100 / progressMax}%" else ""
        val shortCritical = extras.getCharSequence("android.shortCriticalText")?.toString()?.trim().orEmpty()

        val rawChip = when {
            showChrono && n.`when` > 0 -> formatElapsed(abs(System.currentTimeMillis() - n.`when`) / 1000)
            shortCritical.isNotBlank() -> shortCritical
            hasProgress -> percentText
            category == "messenger" && timeString.isNotBlank() -> timeString
            timeString.isNotBlank() -> timeString
            else -> ""
        }

        // Trim capsule / chip according to user's capsule character limit
        val chip = if (maxCapsuleChars > 0 && rawChip.length > maxCapsuleChars) {
            rawChip.take(maxCapsuleChars) + "…"
        } else {
            rawChip
        }

        n.smallIcon?.let { IconCache.activeSmallIcons[id] = it }
        when (val large = extras.get(NotificationCompat.EXTRA_LARGE_ICON)) {
            is Icon -> IconCache.activeLargeIcons[id] = large
            is Bitmap -> IconCache.activeLargeBitmaps[id] = shrinkForIcon(large)
            else -> runCatching {
                val info = context.packageManager.getApplicationInfo(sbn.packageName, 0)
                IconCache.activeLargeIcons[id] = Icon.createWithResource(sbn.packageName, info.icon)
            }
        }

        val pillIcon = if (isCall) callIcon(context, sbn) else null

        val style = styleFor(sbn.packageName, rawTitle, "$rawText $rawBigText $rawSubText")
        val isMaps = sbn.packageName == "com.google.android.apps.maps"
        val finalChip = if (isMaps) rawText.ifBlank { chip } else style?.chip ?: chip
        val ringSaysItAll = finalChip.isBlank() || finalChip == percentText

        // Apply font family and size to text
        val styledTitle = applyFontStyling(rawTitle, notifFont, notifFontSize)
        val styledText = applyFontStyling(bodyText.ifBlank { finalChip }.ifBlank { appLabel }, notifFont, notifFontSize)

        val intent = Intent(context, PlaygroundService::class.java).apply {
            action = PlaygroundService.ACTION_START
            putExtra("id", id)
            putExtra("category", category)
            putExtra("is_ongoing", isLive)
            putExtra("oi_scene", if (category == "navigation") "NAVIGATION" else "NAVIGATION")
            putExtra("title", styledTitle.toString())
            putExtra("text", styledText.toString())
            putExtra("subtext", rawSubText)
            putExtra("source_app", appLabel)
            putExtra("source_pkg", sbn.packageName)
            putExtra("oi_left_content", if (notifStyle == "minimal") "" else rawTitle)
            putExtra("oi_right_content", finalChip)
            putExtra("status_chip_text", finalChip)
            putExtra("icon_res", R.mipmap.ic_launcher_round)
            putExtra("when", n.`when`)
            putExtra("oi_keep_duration", autoDismissSec)
            putExtra("oi_island_show_time", autoDismissSec)
            putExtra("source_content_intent", n.contentIntent ?: fallbackClickIntent(context, sbn.packageName))
            n.actions?.let { putParcelableArrayListExtra("actions", ArrayList(it.toList())) }
            style?.iconStatus?.let { putExtra("oi_icon_status_type", it) }
            pillIcon?.let {
                putExtra("oi_left_icon", it)
                putExtra("oi_large_icon", it)
            }
            when {
                notifStyle == "compact" -> {
                    putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                    putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT)
                }
                notifStyle == "minimal" -> {
                    putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                    putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_TEXT_ICON)
                    putExtra("oi_show_right_icon", false)
                }
                notifStyle == "expanded" -> {
                    putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                    putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT)
                    if (rawTitle.isNotBlank() && bodyText.isNotBlank()) {
                        putStringArrayListExtra(
                            "oi_left_doubleline",
                            arrayListOf(rawTitle, bodyText.take(24)),
                        )
                    }
                }
                style != null -> {
                    putExtra("oi_template", style.template)
                    putExtra("oi_right_template", style.rightTemplate)
                }
                hasProgress && ringSaysItAll -> {
                    putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                    putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_PROGRESS)
                    putExtra("show_progress", true)
                    putExtra("progress", progress)
                    putExtra("progress_max", progressMax)
                }
                hasProgress -> {
                    putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                    putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT)
                }
                else -> {
                    putExtra("oi_left_content", "")
                    if (!isCall) callIcon(context, sbn)?.let { putExtra("oi_left_icon", it) }
                    putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                    if (finalChip.isNotBlank()) {
                        putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT)
                    } else {
                        putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_TEXT_ICON)
                        putExtra("oi_show_right_icon", false)
                        val combined = if (rawTitle.isNotBlank() && rawTitle != appLabel && bodyText.isNotBlank()) {
                            "$rawTitle: $bodyText"
                        } else {
                            bodyText
                        }
                        val trimmed = if (maxCapsuleChars > 0 && combined.length > maxCapsuleChars) {
                            combined.take(maxCapsuleChars) + "…"
                        } else {
                            combined
                        }
                        putExtra("oi_right_content", trimmed)
                    }
                }
            }
        }
        context.startService(intent)
    }

    /** Applies font family and size scaling to text using SpannableString. */
    fun applyFontStyling(text: String, font: String, size: String): CharSequence {
        if (text.isEmpty()) return text
        val span = SpannableString(text)
        val typefaceFamily = when (font) {
            "sans" -> "sans-serif"
            "serif" -> "serif"
            "mono" -> "monospace"
            "rounded" -> "sans-serif-rounded"
            else -> null
        }
        typefaceFamily?.let {
            span.setSpan(TypefaceSpan(it), 0, text.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val sizeFactor = when (size) {
            "small" -> 0.85f
            "large" -> 1.15f
            else -> 1.0f
        }
        if (sizeFactor != 1.0f) {
            span.setSpan(RelativeSizeSpan(sizeFactor), 0, text.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return span
    }

    /** A colourful icon for a call: the caller's avatar if the notification carries one, else the app icon. */
    private fun callIcon(context: Context, sbn: StatusBarNotification): Icon? {
        when (val large = sbn.notification.extras.get(NotificationCompat.EXTRA_LARGE_ICON)) {
            is Icon -> return large
            is Bitmap -> return Icon.createWithBitmap(shrinkForIcon(large))
        }
        return runCatching {
            val info = context.packageManager.getApplicationInfo(sbn.packageName, 0)
            Icon.createWithResource(sbn.packageName, info.icon)
        }.getOrNull()
    }

    private data class CastStyle(
        val template: Int,
        val rightTemplate: Int = OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT,
        val iconStatus: Int = -1,
        val chip: String? = null,
    )

    private fun styleFor(pkg: String, title: String, body: String): CastStyle? {
        val t = "$title $body".lowercase()
        val base = OriginIslandConstants.TEMPLATE_BASE
        return when (pkg) {
            "com.google.android.apps.walletnfcrel",
            "com.google.android.apps.nbu.paisa.user",
            -> when {
                listOf("boarding", "flight", "gate", "depart", "terminal", "airline")
                    .any { t.contains(it) } -> CastStyle(base, chip = "✈")
                listOf("train", "platform", "rail", "track", "sncf", "coach", "wagon")
                    .any { t.contains(it) } -> CastStyle(base, chip = "🚆")
                else -> CastStyle(base, iconStatus = OriginIslandConstants.ICON_STATUS_SUCCESS, chip = "✓")
            }
            else -> null
        }
    }
}
