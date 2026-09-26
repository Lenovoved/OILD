package com.lenovoved.android.ui.settings

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenovoved.android.service.NotificationCastListener
import com.lenovoved.android.ui.PREFS_NAME

@Composable
fun SurfacesSettingsScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var displayStatusbar by remember {
        mutableStateOf(prefs.getBoolean("cast_display_statusbar", true))
    }
    var displayAod by remember {
        mutableStateOf(prefs.getBoolean("cast_display_aod", false))
    }
    var displayLockscreen by remember {
        mutableStateOf(prefs.getBoolean("cast_lockscreen_live_card", false))
    }
    var displayWidget by remember {
        mutableStateOf(prefs.getBoolean("cast_display_widget", false))
    }
    var displayVSuggestion by remember {
        mutableStateOf(prefs.getBoolean("cast_display_v_suggestion", false))
    }

    val dark = isSystemInDarkTheme()
    val dividerColor = if (dark) Color(0xFF2C2C2E) else Color(0xFFF0F0F2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(
            title = "Область уведомлений",
            subtitle = "Выбор экранов системы vivo для показа элементов островка",
            onBack = onBack,
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsCard {
                SettingsSectionHeader("Системные экраны OriginOS", Icons.Default.Layers)

                Text(
                    text = "Протокол SuperX (битовая маска `displays`) определяет, на каких поверхностях системы vivo отображается островное событие. Вы можете независимо включать и отключать вывод на экран блокировки, выключенный экран AOD, рабочий стол или статус-бар.",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = if (dark) Color(0xFF8E8E93) else Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsToggleRow(
                    title = "Строка состояния (Статус-бар)",
                    subtitle = "Капсула островка рядом с вырезом фронтальной камеры при активном экране (DISPLAY_STATUSBAR)",
                    checked = displayStatusbar,
                    onCheckedChange = { checked ->
                        displayStatusbar = checked
                        prefs.edit().putBoolean("cast_display_statusbar", checked).commit()
                        NotificationCastListener.notifySettingsChanged(context)
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Экран блокировки (Lock Screen)",
                    subtitle = "Интерактивная живая карточка на экране блокировки с кнопками действий (DISPLAY_LOCKSCREEN)",
                    checked = displayLockscreen,
                    onCheckedChange = { checked ->
                        displayLockscreen = checked
                        prefs.edit().putBoolean("cast_lockscreen_live_card", checked).commit()
                        NotificationCastListener.notifySettingsChanged(context)
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Always-On Display (AOD)",
                    subtitle = "Вывод миниатюрного значка и статуса островка на всегда включенный экран AOD (DISPLAY_AOD)",
                    checked = displayAod,
                    onCheckedChange = { checked ->
                        displayAod = checked
                        prefs.edit().putBoolean("cast_display_aod", checked).commit()
                        NotificationCastListener.notifySettingsChanged(context)
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Виджет рабочего стола",
                    subtitle = "Трансляция карточки в рабочий стол OriginOS (DISPLAY_WIDGET)",
                    checked = displayWidget,
                    onCheckedChange = { checked ->
                        displayWidget = checked
                        prefs.edit().putBoolean("cast_display_widget", checked).commit()
                        NotificationCastListener.notifySettingsChanged(context)
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Подсказки Jovi / V-Suggestion",
                    subtitle = "Интеграция с умным ассистентом Jovi и карточками сценариев (DISPLAY_V_SUGGESTION)",
                    checked = displayVSuggestion,
                    onCheckedChange = { checked ->
                        displayVSuggestion = checked
                        prefs.edit().putBoolean("cast_display_v_suggestion", checked).commit()
                        NotificationCastListener.notifySettingsChanged(context)
                    },
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
