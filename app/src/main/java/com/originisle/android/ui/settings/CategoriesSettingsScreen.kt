package com.originisle.android.ui.settings

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.originisle.android.island.PlaygroundService
import com.originisle.android.service.NotificationCastListener
import com.originisle.android.ui.PREFS_NAME

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
    var hideSourceNotification by remember {
        mutableStateOf(prefs.getBoolean("cast_hide_source_notification", true))
    }
    var hideOriginShade by remember {
        mutableStateOf(prefs.getBoolean("cast_hide_origin_shade", true))
    }

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
                            PlaygroundService.keepAlive(context)
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

                SettingsToggleRow(
                    title = "Уведомления мессенджеров",
                    subtitle = "Telegram, WhatsApp, Viber, VK, SMS, диалоги и чаты",
                    checked = messengerOn,
                    onCheckedChange = { checked ->
                        messengerOn = checked
                        prefs.edit().putBoolean("cast_messenger_notifications", checked).apply()
                        NotificationCastListener.onCategorySettingsChanged(context, "messenger", checked)
                        if (checked) {
                            PlaygroundService.keepAlive(context)
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

                SettingsToggleRow(
                    title = "Уведомления навигаторов",
                    subtitle = "Google Карты, Яндекс Навигатор, Яндекс Карты, 2ГИС, дорожные подсказки",
                    checked = navOn,
                    onCheckedChange = { checked ->
                        navOn = checked
                        prefs.edit().putBoolean("cast_nav_notifications", checked).apply()
                        NotificationCastListener.onCategorySettingsChanged(context, "navigation", checked)
                        if (checked) {
                            PlaygroundService.keepAlive(context)
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

                SettingsToggleRow(
                    title = "Уведомления плеером",
                    subtitle = "Медиаплеер OriginOS, обложки треков, управление воспроизведением (пауза, след/пред)",
                    checked = mediaOn,
                    onCheckedChange = { checked ->
                        mediaOn = checked
                        prefs.edit().putBoolean("cast_media_sessions", checked).apply()
                        NotificationCastListener.onMediaSettingsChanged(context, checked)
                        if (checked) {
                            PlaygroundService.keepAlive(context)
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

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

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

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

            // Notification Shade Duplicate Hiding
            SettingsCard {
                SettingsSectionHeader("Скрытие дубликатов из шторки", Icons.Default.NotificationsOff)

                SettingsToggleRow(
                    title = "Скрывать исходные уведомления",
                    subtitle = "Автоматически удалять входящие сообщения и оповещения из обычной шторки Android после их отправки на Dynamic Island",
                    checked = hideSourceNotification,
                    onCheckedChange = { checked ->
                        hideSourceNotification = checked
                        prefs.edit().putBoolean("cast_hide_source_notification", checked).apply()
                        NotificationCastListener.instance?.reload()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

                SettingsToggleRow(
                    title = "Скрывать дубликат Origin OS из шторки",
                    subtitle = "Отключает показ системной копии карточки в раскрывающейся шторке (IslandNotify), отображая только Dynamic Island",
                    checked = hideOriginShade,
                    onCheckedChange = { checked ->
                        hideOriginShade = checked
                        prefs.edit()
                            .putBoolean("cast_hide_origin_shade", checked)
                            .putBoolean("cast_show_notify", !checked)
                            .putBoolean("cast_island_notify", checked)
                            .apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
