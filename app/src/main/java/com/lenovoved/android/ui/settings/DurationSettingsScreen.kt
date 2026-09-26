package com.lenovoved.android.ui.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenovoved.android.R
import com.lenovoved.android.island.OriginIslandConstants
import com.lenovoved.android.island.PlaygroundService
import com.lenovoved.android.service.NotificationCastListener
import com.lenovoved.android.ui.PREFS_NAME
import kotlin.math.roundToInt

@Composable
fun DurationSettingsScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var autoDismissSec by remember {
        mutableIntStateOf(prefs.getInt("cast_auto_dismiss_seconds", 0))
    }
    var capsuleShowTime by remember {
        mutableIntStateOf(prefs.getInt("cast_capsule_show_time", 0))
    }
    var showTimestamp by remember {
        mutableStateOf(prefs.getBoolean("notification_show_timestamp", true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(
            title = "Время отображения",
            subtitle = "Настройка таймингов удержания карточки и капсулы на островке",
            onBack = onBack,
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Main Card Display Duration
            SettingsCard {
                SettingsSectionHeader("Длительность показа карточки", Icons.Default.Schedule)

                Text(
                    text = "Задает фактическое время (в секундах), в течение которого карточка остается на динамическом островке до автоматического скрытия. Значение транслируется в системные параметры OriginOS SuperX (keepDuration и islandShowTime) и управляется внутренним таймером.",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsChipsRow(
                    items = listOf(
                        0 to "Бессрочно",
                        3 to "3 сек",
                        5 to "5 сек",
                        7 to "7 сек",
                        10 to "10 сек",
                        15 to "15 сек",
                        20 to "20 сек",
                        30 to "30 сек",
                        60 to "60 сек",
                    ),
                    selectedItem = autoDismissSec,
                    onSelect = { sec ->
                        autoDismissSec = sec
                        prefs.edit().putInt("cast_auto_dismiss_seconds", sec).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsSliderRow(
                    label = "Точная задержка удержания",
                    value = autoDismissSec.toFloat(),
                    onValueChange = { newVal ->
                        val s = newVal.roundToInt()
                        autoDismissSec = s
                        prefs.edit().putInt("cast_auto_dismiss_seconds", s).apply()
                    },
                    valueRange = 0f..60f,
                    steps = 59,
                    valueDisplay = if (autoDismissSec == 0) "Не скрывать" else "$autoDismissSec сек",
                    minLabel = "0 сек",
                    maxLabel = "60 сек",
                    startIcon = Icons.Default.HourglassTop,
                    endIcon = Icons.Default.HourglassFull,
                )
            }

            // Capsule Show Time
            SettingsCard {
                SettingsSectionHeader("Длительность правой капсулы (Chip)", Icons.Default.HourglassEmpty)

                Text(
                    text = "Системный таймер `capsuleShowTime` для свернутой мини-пилюли в строке состояния OriginOS (0 = системное время по умолчанию).",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsChipsRow(
                    items = listOf(
                        0 to "По умолчанию",
                        3 to "3 сек",
                        5 to "5 сек",
                        10 to "10 сек",
                        15 to "15 сек",
                        30 to "30 сек",
                    ),
                    selectedItem = capsuleShowTime,
                    onSelect = { sec ->
                        capsuleShowTime = sec
                        prefs.edit().putInt("cast_capsule_show_time", sec).apply()
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsSliderRow(
                    label = "Время показа капсулы",
                    value = capsuleShowTime.toFloat(),
                    onValueChange = { newVal ->
                        val s = newVal.roundToInt()
                        capsuleShowTime = s
                        prefs.edit().putInt("cast_capsule_show_time", s).apply()
                    },
                    valueRange = 0f..30f,
                    steps = 29,
                    valueDisplay = if (capsuleShowTime == 0) "По умолчанию" else "$capsuleShowTime сек",
                    minLabel = "0 сек",
                    maxLabel = "30 сек",
                    startIcon = Icons.Default.Timer,
                    endIcon = Icons.Default.Timelapse,
                )
            }

            // Real Time Clock & Timestamping
            SettingsCard {
                SettingsSectionHeader("Временная метка", Icons.Default.Timer)

                SettingsToggleRow(
                    title = "Фактическое время получения (HH:mm)",
                    subtitle = "Отображать в правой части капсулы реальное системное время поступления уведомления (например, 14:28 вместо абстрактных значков)",
                    checked = showTimestamp,
                    onCheckedChange = { checked ->
                        showTimestamp = checked
                        prefs.edit().putBoolean("notification_show_timestamp", checked).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            // Test Button
            SettingsCard {
                SettingsSectionHeader("Проверка таймингов", Icons.Default.PlayArrow)

                Text(
                    text = "Нажмите кнопку ниже, чтобы отправить тестовое уведомление на островок с текущим таймером ($autoDismissSec сек).",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val testIntent = Intent(context, PlaygroundService::class.java).apply {
                            action = PlaygroundService.ACTION_START
                            putExtra("id", 49999)
                            putExtra("oi_scene", "NAVIGATION")
                            putExtra("title", "Тест времени показа")
                            putExtra("text", "Карточка закроется через ${if (autoDismissSec == 0) "∞" else "$autoDismissSec с"}")
                            putExtra("source_app", "Пространство Origin")
                            putExtra("icon_res", R.mipmap.ic_launcher_round)
                            putExtra("oi_right_content", if (autoDismissSec == 0) "Live" else "${autoDismissSec}s")
                            putExtra("oi_keep_duration", autoDismissSec)
                            putExtra("oi_island_show_time", autoDismissSec)
                            putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                            putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT)
                        }
                        context.startService(testIntent)
                        Toast.makeText(context, "Тестовое уведомление отправлено", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1677FF)),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Запустить тестовое уведомление",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
