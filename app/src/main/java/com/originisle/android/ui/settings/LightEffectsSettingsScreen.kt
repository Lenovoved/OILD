package com.originisle.android.ui.settings

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.originisle.android.service.NotificationCastListener
import com.originisle.android.ui.PREFS_NAME

@Composable
fun LightEffectsSettingsScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var lightEnabled by remember {
        mutableStateOf(prefs.getBoolean("cast_light_enabled", false))
    }
    var lightColor by remember {
        mutableIntStateOf(prefs.getInt("cast_light_color", android.graphics.Color.parseColor("#1677FF")))
    }
    var lightMode by remember {
        mutableIntStateOf(prefs.getInt("cast_light_mode", 0))
    }
    var sosWarn by remember {
        mutableStateOf(prefs.getBoolean("cast_sos_warn", false))
    }
    var sosWarnRepeat by remember {
        mutableIntStateOf(prefs.getInt("cast_sos_warn_repeat", 2))
    }

    val lightColorList = listOf(
        android.graphics.Color.parseColor("#1677FF") to "Синий Origin",
        android.graphics.Color.parseColor("#10B981") to "Изумруд",
        android.graphics.Color.parseColor("#8B5CF6") to "Неон Фиолет",
        android.graphics.Color.parseColor("#F59E0B") to "Золотой",
        android.graphics.Color.parseColor("#EF4444") to "Алый",
        android.graphics.Color.parseColor("#06B6D4") to "Циан",
        android.graphics.Color.parseColor("#FFFFFF") to "Белый",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(
            title = "Световые эффекты",
            subtitle = "Боковая подсветка экрана и световые сигналы OriginOS SuperX",
            onBack = onBack,
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Main Light Switch
            SettingsCard {
                SettingsSectionHeader("Боковая подсветка экрана (Edge Glow)", Icons.Default.Highlight)

                Text(
                    text = "Активирует фирменную световую рамку по контуру экрана смартфона vivo при получении островных уведомлений (параметр `effectIsLight` в пакете SuperX).",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsToggleRow(
                    title = "Включить подсветку контура экрана",
                    subtitle = "Мягкое свечение граней устройства при входящих событиях",
                    checked = lightEnabled,
                    onCheckedChange = { checked ->
                        lightEnabled = checked
                        prefs.edit().putBoolean("cast_light_enabled", checked).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            if (lightEnabled) {
                // Color Selection
                SettingsCard {
                    SettingsSectionHeader("Цвет подсветки", Icons.Default.Highlight)

                    SettingsColorPalette(
                        colors = lightColorList,
                        selectedColor = lightColor,
                        onColorSelect = { c ->
                            lightColor = c
                            prefs.edit().putInt("cast_light_color", c).apply()
                            NotificationCastListener.instance?.reload()
                            NotificationCastListener.instance?.recastAll()
                        },
                    )
                }

                // Animation Mode
                SettingsCard {
                    SettingsSectionHeader("Режим световой анимации", Icons.Default.Animation)

                    SettingsChipsRow(
                        items = listOf(
                            0 to "Дыхание (Плавное)",
                            1 to "Бегущая волна",
                            2 to "Пульсация",
                            3 to "Вспышка",
                        ),
                        selectedItem = lightMode,
                        onSelect = { m ->
                            lightMode = m
                            prefs.edit().putInt("cast_light_mode", m).apply()
                            NotificationCastListener.instance?.reload()
                            NotificationCastListener.instance?.recastAll()
                        },
                    )
                }

                // SOS / Warning
                SettingsCard {
                    SettingsSectionHeader("Режим тревоги / Внимание", Icons.Default.Warning)

                    SettingsToggleRow(
                        title = "Световой сигнал тревоги (SOS Warn)",
                        subtitle = "Усиленная серия миганий для критических уведомлений",
                        checked = sosWarn,
                        onCheckedChange = { checked ->
                            sosWarn = checked
                            prefs.edit().putBoolean("cast_sos_warn", checked).apply()
                        },
                    )

                    if (sosWarn) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Количество повторений сигнала: $sosWarnRepeat",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SettingsChipsRow(
                            items = listOf(
                                1 to "1 раз",
                                2 to "2 раза",
                                3 to "3 раза",
                                5 to "5 раз",
                            ),
                            selectedItem = sosWarnRepeat,
                            onSelect = { r ->
                                sosWarnRepeat = r
                                prefs.edit().putInt("cast_sos_warn_repeat", r).apply()
                            },
                        )
                    }
                }

                // Live Glowing Edge Preview
                SettingsCard {
                    SettingsSectionHeader("Предпросмотр свечения граней", Icons.Default.Highlight)

                    val infiniteTransition = rememberInfiniteTransition(label = "glow")
                    val alphaAnim by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.95f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "alpha",
                    )

                    val activeColor = Color(lightColor).copy(alpha = alphaAnim)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F172A))
                            .border(
                                width = 3.dp,
                                color = activeColor,
                                shape = RoundedCornerShape(20.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Анимация подсветки OriginOS",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Цвет: ${lightColorList.find { it.first == lightColor }?.second ?: "Выбранный"}",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
