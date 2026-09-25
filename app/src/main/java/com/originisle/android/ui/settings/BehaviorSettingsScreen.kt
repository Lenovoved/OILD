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
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.originisle.android.service.NotificationCastListener
import com.originisle.android.ui.PREFS_NAME

@Composable
fun BehaviorSettingsScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var forceShow by remember {
        mutableStateOf(prefs.getBoolean("cast_force_show", false))
    }
    var keepScreenOn by remember {
        mutableStateOf(prefs.getBoolean("cast_keep_screen_on", false))
    }
    var soundEnabled by remember {
        mutableStateOf(prefs.getBoolean("cast_sound", false))
    }
    var dismissWhenKill by remember {
        mutableStateOf(prefs.getBoolean("cast_dismiss_when_kill", true))
    }
    var islandClickAction by remember {
        mutableIntStateOf(prefs.getInt("cast_island_click_action", 0))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(
            title = "Поведение и отклик",
            subtitle = "Параметры реакции островка при появлении и нажатиях",
            onBack = onBack,
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Popup and Screen Wake
            SettingsCard {
                SettingsSectionHeader("Поведение при появлении", Icons.Default.OpenInFull)

                SettingsToggleRow(
                    title = "Принудительное раскрытие карточки",
                    subtitle = "Раскрывать большую карточку островка сразу при поступлении события вместо свернутой капсулы (forceShow)",
                    checked = forceShow,
                    onCheckedChange = { checked ->
                        forceShow = checked
                        prefs.edit().putBoolean("cast_force_show", checked).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

                SettingsToggleRow(
                    title = "Держать экран включенным",
                    subtitle = "Предотвращать автовыключение экрана, пока на островке отображается важное событие (keepScreenOn)",
                    checked = keepScreenOn,
                    onCheckedChange = { checked ->
                        keepScreenOn = checked
                        prefs.edit().putBoolean("cast_keep_screen_on", checked).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

                SettingsToggleRow(
                    title = "Фирменный звук SuperX",
                    subtitle = "Воспроизводить системный звуковой сигнал OriginOS при анимации появления островка (sound)",
                    checked = soundEnabled,
                    onCheckedChange = { checked ->
                        soundEnabled = checked
                        prefs.edit().putBoolean("cast_sound", checked).apply()
                        NotificationCastListener.instance?.reload()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

                SettingsToggleRow(
                    title = "Закрывать при завершении приложения",
                    subtitle = "Убирать карточку с островка, если исходное приложение завершило свою работу (dismissWhenKill)",
                    checked = dismissWhenKill,
                    onCheckedChange = { checked ->
                        dismissWhenKill = checked
                        prefs.edit().putBoolean("cast_dismiss_when_kill", checked).apply()
                        NotificationCastListener.instance?.reload()
                    },
                )
            }

            // Click Interaction
            SettingsCard {
                SettingsSectionHeader("Действие по нажатию на капсулу", Icons.Default.TouchApp)

                Text(
                    text = "Определяет поведение системы при касании пользователем свернутой капсулы островка (ключ `island.click` в протоколе SuperX).",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsChipsRow(
                    items = listOf(
                        0 to "Развернуть карточку",
                        1 to "Открыть приложение",
                        2 to "Тактильный отклик",
                    ),
                    selectedItem = islandClickAction,
                    onSelect = { action ->
                        islandClickAction = action
                        prefs.edit().putInt("cast_island_click_action", action).apply()
                        NotificationCastListener.instance?.reload()
                    },
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
