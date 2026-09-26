package com.lenovoved.android.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenovoved.android.ui.isBatteryUnrestricted
import com.lenovoved.android.ui.isListenerEnabled
import com.lenovoved.android.ui.openAutoStartSettings
import com.lenovoved.android.ui.rememberResumeTick
import com.lenovoved.android.ui.requestIgnoreBattery

@Composable
fun PermissionsSettingsScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val tick = rememberResumeTick()

    var listenerGranted by remember { mutableStateOf(isListenerEnabled(context)) }
    var batteryGranted by remember { mutableStateOf(isBatteryUnrestricted(context)) }

    LaunchedEffect(tick.intValue) {
        listenerGranted = isListenerEnabled(context)
        batteryGranted = isBatteryUnrestricted(context)
    }

    val dark = isSystemInDarkTheme()
    val dividerColor = if (dark) Color(0xFF2C2C2E) else Color(0xFFF0F0F2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(
            title = "Системные разрешения",
            subtitle = "Настройка прав доступа и фоновой службы OriginOS",
            onBack = onBack,
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsCard {
                SettingsSectionHeader("Статус работы службы", Icons.Default.Security)

                Text(
                    text = "Для бесперебойной трансляции уведомлений на островок в оболочке vivo OriginOS требуются следующие системные разрешения:",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = if (dark) Color(0xFF8E8E93) else Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(14.dp))

                PermissionItemRow(
                    title = "Доступ к уведомлениям",
                    subtitle = "Чтение входящих уведомлений и медиа-сессий плеера",
                    isGranted = listenerGranted,
                    onAction = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = dividerColor, thickness = 0.8.dp)

                PermissionItemRow(
                    title = "Работа в фоне без ограничений",
                    subtitle = "Исключение из оптимизации батареи для предотвращения засыпания службы",
                    isGranted = batteryGranted,
                    onAction = {
                        requestIgnoreBattery(context)
                    },
                )
            }

            // Vivo OriginOS Specific AutoStart
            SettingsCard {
                SettingsSectionHeader("Оптимизация vivo OriginOS", Icons.Default.SettingsSuggest)

                Text(
                    text = "Смартфоны vivo iQOO / X-series имеют агрессивный диспетчер задач. Рекомендуется включить «Автозапуск» и «Фоновую активность» в системном iManager.",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { openAutoStartSettings(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1677FF)),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Открыть настройки автозапуска vivo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun PermissionItemRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                }
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B),
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(start = 24.dp),
            )
        }

        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) Color(0xFFF1F5F9) else Color(0xFF1677FF),
                contentColor = if (isGranted) Color(0xFF475569) else Color.White,
            ),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(
                text = if (isGranted) "Настроено" else "Включить",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
