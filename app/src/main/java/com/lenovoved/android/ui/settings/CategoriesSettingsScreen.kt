package com.lenovoved.android.ui.settings

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenovoved.android.service.NotificationCastListener
import com.lenovoved.android.ui.PREFS_NAME
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CategoriesSettingsScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var normalOn by remember {
        mutableStateOf(prefs.getBoolean("cast_normal_notifications", false) || prefs.getBoolean("cast_notifications", false))
    }
    var messengerOn by remember {
        mutableStateOf(prefs.getBoolean("cast_messenger_notifications", false))
    }
    var navOn by remember {
        mutableStateOf(prefs.getBoolean("cast_nav_notifications", false))
    }
    var mediaOn by remember {
        mutableStateOf(prefs.getBoolean("cast_media_sessions", false))
    }
    var sportsOn by remember {
        mutableStateOf(prefs.getBoolean("cast_sports_enabled", false))
    }
    var paymentsOn by remember {
        mutableStateOf(prefs.getBoolean("cast_payments_enabled", false))
    }
    var ignoreSilent by remember {
        mutableStateOf(prefs.getBoolean("cast_ignore_silent", false))
    }

    val dark = isSystemInDarkTheme()
    val dividerColor = if (dark) Color(0xFF2C2C2E) else Color(0xFFF0F0F2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(
            title = "Категории уведомлений",
            subtitle = "Управление типами транслируемых событий в OriginIsland",
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Phone Island Mockup
            PhoneIslandMockup()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Пространство Origin",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color.White else Color(0xFF1C1C1E),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Интеграция с динамическим островом OriginOS. Каждый параметр вынесен в отдельную страницу для детальной настройки.",
                fontSize = 13.5.sp,
                lineHeight = 18.sp,
                color = if (dark) Color(0xFF8E8E93) else Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Main Types
            SettingsCard {
                SettingsSectionHeader("Основные источники", Icons.Default.Notifications)

                SettingsToggleRow(
                    title = "Обычные уведомления",
                    subtitle = "Системные оповещения, загрузки, будильники, таймеры и установленные приложения",
                    checked = normalOn,
                    onCheckedChange = { checked ->
                        normalOn = checked
                        prefs.edit()
                            .putBoolean("cast_normal_notifications", checked)
                            .putBoolean("cast_notifications", checked)
                            .apply()
                        NotificationCastListener.onCategorySettingsChanged(context, "normal", checked)
                        if (checked) {
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Уведомления мессенджеров",
                    subtitle = "Telegram, WhatsApp, Viber, VK, SMS, диалоги и чаты",
                    checked = messengerOn,
                    onCheckedChange = { checked ->
                        messengerOn = checked
                        prefs.edit().putBoolean("cast_messenger_notifications", checked).apply()
                        NotificationCastListener.onCategorySettingsChanged(context, "messenger", checked)
                        if (checked) {
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Уведомления навигаторов",
                    subtitle = "Google Карты, Яндекс Навигатор, Яндекс Карты, 2ГИС, дорожные подсказки",
                    checked = navOn,
                    onCheckedChange = { checked ->
                        navOn = checked
                        prefs.edit().putBoolean("cast_nav_notifications", checked).apply()
                        NotificationCastListener.onCategorySettingsChanged(context, "navigation", checked)
                        if (checked) {
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Уведомления плеером",
                    subtitle = "Медиаплеер OriginOS, обложки треков, управление воспроизведением (пауза, след/пред)",
                    checked = mediaOn,
                    onCheckedChange = { checked ->
                        mediaOn = checked
                        prefs.edit().putBoolean("cast_media_sessions", checked).apply()
                        NotificationCastListener.onMediaSettingsChanged(context, checked)
                        if (checked) {
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )
            }

            // Specialized Streams
            SettingsCard {
                SettingsSectionHeader("Специализированные события", Icons.Default.Tune)

                SettingsToggleRow(
                    title = "Спортивные события и Live Score",
                    subtitle = "Счета футбольных матчей в реальном времени, эмблемы клубов и минуты (FotMob, SofaScore, FlashScore)",
                    checked = sportsOn,
                    onCheckedChange = { checked ->
                        sportsOn = checked
                        prefs.edit().putBoolean("cast_sports_enabled", checked).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Банковские чеки и платежи",
                    subtitle = "Карточка успешной оплаты в стиле Wallet/Apple Pay, билеты на самолеты ✈ и поезда 🚆",
                    checked = paymentsOn,
                    onCheckedChange = { checked ->
                        paymentsOn = checked
                        prefs.edit().putBoolean("cast_payments_enabled", checked).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Игнорировать беззвучные уведомления",
                    subtitle = "Не показывать на островке события из каналов, где отключены звук и вибрация",
                    checked = ignoreSilent,
                    onCheckedChange = { checked ->
                        ignoreSilent = checked
                        prefs.edit().putBoolean("cast_ignore_silent", checked).apply()
                        NotificationCastListener.instance?.reload()
                    },
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * Phone Screen Mockup with top camera cutout pill and soft real-time clock.
 */
@Composable
private fun PhoneIslandMockup() {
    val dark = isSystemInDarkTheme()
    val currentTime = remember {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date())
    }

    Box(
        modifier = Modifier
            .width(170.dp)
            .height(155.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = if (dark) Color(0x33000000) else Color(0x12000000),
                ambientColor = Color(0x08000000),
            )
            .clip(RoundedCornerShape(28.dp))
            .background(if (dark) Color(0xFF1C1C1E) else Color.White)
            .border(1.5.dp, if (dark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Island cutout pill (Black pill with camera lenses)
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(15.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1E20)),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF323236)),
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF28282B)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentTime,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFCCD1D7),
                letterSpacing = 1.sp,
            )
        }
    }
}
