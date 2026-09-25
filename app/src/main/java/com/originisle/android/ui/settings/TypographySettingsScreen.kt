package com.originisle.android.ui.settings

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.originisle.android.service.NotificationCastListener
import com.originisle.android.ui.PREFS_NAME
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TypographySettingsScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var notifStyle by remember {
        mutableStateOf(prefs.getString("notification_display_style", "classic") ?: "classic")
    }
    var notifFont by remember {
        mutableStateOf(prefs.getString("notification_font_family", "default") ?: "default")
    }
    var notifFontSize by remember {
        mutableStateOf(prefs.getString("notification_font_size", "normal") ?: "normal")
    }
    var capsuleChars by remember {
        mutableIntStateOf(prefs.getInt("notification_capsule_chars", 16))
    }
    var bodyChars by remember {
        mutableIntStateOf(prefs.getInt("notification_body_chars", 80))
    }
    val showTimestamp = prefs.getBoolean("notification_show_timestamp", true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(
            title = "Стиль, шрифты и лимиты",
            subtitle = "Настройка типографики, шаблона карточки и длины текста",
            onBack = onBack,
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Card Template Style
            SettingsCard {
                SettingsSectionHeader("Стиль карточки", Icons.Default.FormatPaint)

                Text(
                    text = "Выбирает компоновку элементов в развернутом островке OriginOS.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsChipsRow(
                    items = listOf(
                        "classic" to "Классический",
                        "compact" to "Компактный",
                        "expanded" to "Расширенный",
                        "minimal" to "Минималистичный",
                    ),
                    selectedItem = notifStyle,
                    onSelect = { s ->
                        notifStyle = s
                        prefs.edit().putString("notification_display_style", s).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            // Font Family
            SettingsCard {
                SettingsSectionHeader("Гарнитура шрифта", Icons.Default.TextFields)

                SettingsChipsRow(
                    items = listOf(
                        "default" to "Системный",
                        "sans" to "Sans-Serif",
                        "serif" to "Serif",
                        "mono" to "Моноширинный",
                        "rounded" to "Скругленный OriginOS",
                    ),
                    selectedItem = notifFont,
                    onSelect = { f ->
                        notifFont = f
                        prefs.edit().putString("notification_font_family", f).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            // Font Size
            SettingsCard {
                SettingsSectionHeader("Размер текста", Icons.Default.FormatSize)

                SettingsChipsRow(
                    items = listOf(
                        "small" to "Мелкий (12sp)",
                        "normal" to "Обычный (14sp)",
                        "large" to "Крупный (16sp)",
                    ),
                    selectedItem = notifFontSize,
                    onSelect = { sz ->
                        notifFontSize = sz
                        prefs.edit().putString("notification_font_size", sz).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            // Character Limits
            SettingsCard {
                SettingsSectionHeader("Количество отображаемых символов", Icons.Default.TextFields)

                Text(
                    text = "Максимум символов в капсуле островка: $capsuleChars",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155),
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsChipsRow(
                    items = listOf(
                        8 to "8",
                        12 to "12",
                        16 to "16",
                        24 to "24",
                        32 to "32",
                    ),
                    selectedItem = capsuleChars,
                    onSelect = { c ->
                        capsuleChars = c
                        prefs.edit().putInt("notification_capsule_chars", c).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (bodyChars == 0) "Символов в теле сообщения: Без ограничений" else "Символов в теле сообщения: $bodyChars",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155),
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsChipsRow(
                    items = listOf(
                        40 to "40",
                        60 to "60",
                        80 to "80",
                        120 to "120",
                        200 to "200",
                        0 to "Все",
                    ),
                    selectedItem = bodyChars,
                    onSelect = { b ->
                        bodyChars = b
                        prefs.edit().putInt("notification_body_chars", b).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            // Live Preview Card
            SettingsCard {
                SettingsSectionHeader("Предварительный просмотр", Icons.Default.FormatPaint)

                val sampleTitle = "Telegram • Александр"
                val sampleBody = "Привет! Встречаемся сегодня в 18:30 в кофейне у парка?"
                val timeString = if (showTimestamp) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) else "18:30"

                val font = when (notifFont) {
                    "sans" -> FontFamily.SansSerif
                    "serif" -> FontFamily.Serif
                    "mono" -> FontFamily.Monospace
                    else -> FontFamily.Default
                }

                val textSize = when (notifFontSize) {
                    "small" -> 12.sp
                    "large" -> 16.sp
                    else -> 14.sp
                }

                val chip = if (capsuleChars > 0 && timeString.length > capsuleChars) timeString.take(capsuleChars) + "…" else timeString
                val body = if (bodyChars > 0 && sampleBody.length > bodyChars) sampleBody.take(bodyChars) + "…" else sampleBody

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F172A))
                        .padding(14.dp),
                ) {
                    // Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("TG", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            if (notifStyle != "minimal") {
                                Text(
                                    text = sampleTitle,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = font,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF334155))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = chip,
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontFamily = font,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expanded Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B))
                            .padding(12.dp),
                    ) {
                        Text(
                            text = sampleTitle,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            fontFamily = font,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = body,
                            color = Color.White,
                            fontSize = textSize,
                            fontFamily = font,
                            lineHeight = (textSize.value + 4).sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
