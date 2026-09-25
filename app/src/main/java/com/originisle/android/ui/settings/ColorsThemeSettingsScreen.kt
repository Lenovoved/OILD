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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
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
fun ColorsThemeSettingsScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var disableInvert by remember {
        mutableStateOf(prefs.getBoolean("cast_disable_invert", true))
    }
    var cardBgColor by remember {
        mutableIntStateOf(prefs.getInt("cast_card_bg_color", 0))
    }
    var capsuleBgColor by remember {
        mutableIntStateOf(prefs.getInt("cast_capsule_bg_color", 0))
    }
    var rightTemplateOverride by remember {
        mutableStateOf(prefs.getString("cast_right_template", "auto") ?: "auto")
    }

    val cardColors = listOf(
        0 to "Origin Dark",
        android.graphics.Color.parseColor("#0F172A") to "Глубокий синий",
        android.graphics.Color.parseColor("#18181B") to "Графит",
        android.graphics.Color.parseColor("#000000") to "OLED Black",
        android.graphics.Color.parseColor("#064E3B") to "Изумруд",
        android.graphics.Color.parseColor("#4C0519") to "Бордо",
    )

    val capsuleColors = listOf(
        0 to "По умолчанию",
        android.graphics.Color.parseColor("#1677FF") to "Синий Origin",
        android.graphics.Color.parseColor("#7C3AED") to "Фиолетовый",
        android.graphics.Color.parseColor("#059669") to "Зеленый",
        android.graphics.Color.parseColor("#D97706") to "Оранжевый",
        android.graphics.Color.parseColor("#DC2626") to "Красный",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(
            title = "Цветовая палитра и темы",
            subtitle = "Настройка цветовых слоев, инверсии и шаблонов капсулы",
            onBack = onBack,
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Theme Inversion
            SettingsCard {
                SettingsSectionHeader("Инверсия цвета темы", Icons.Default.InvertColors)

                SettingsToggleRow(
                    title = "Запрет авто-инверсии (disableInvertColor)",
                    subtitle = "Запрещает системе OriginOS автоматически инвертировать цвета карточки при переключении между светлой и темной темой, сохраняя контрастный фирменный вид",
                    checked = disableInvert,
                    onCheckedChange = { checked ->
                        disableInvert = checked
                        prefs.edit().putBoolean("cast_disable_invert", checked).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            // Card Background Color
            SettingsCard {
                SettingsSectionHeader("Цвет фона большой карточки", Icons.Default.Palette)

                Text(
                    text = "Задает системную заливку фона развернутого островка (параметр `cardBgColor`).",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsColorPalette(
                    colors = cardColors,
                    selectedColor = cardBgColor,
                    onColorSelect = { c ->
                        cardBgColor = c
                        prefs.edit().putInt("cast_card_bg_color", c).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            // Capsule Chip Background Color
            SettingsCard {
                SettingsSectionHeader("Цвет правой капсулы (Chip)", Icons.Default.ColorLens)

                Text(
                    text = "Акцентный цвет фона для миниатюрного чипа/пилюли в правой части островка.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsColorPalette(
                    colors = capsuleColors,
                    selectedColor = capsuleBgColor,
                    onColorSelect = { c ->
                        capsuleBgColor = c
                        prefs.edit().putInt("cast_capsule_bg_color", c).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            // Right Template
            SettingsCard {
                SettingsSectionHeader("Шаблон правой части островка", Icons.Default.Widgets)

                Text(
                    text = "Принудительный выбор типа отображения правой зоны островка (`rightTemplate`).",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsChipsRow(
                    items = listOf(
                        "auto" to "Авто (По типу)",
                        "capsule" to "Текстовая капсула",
                        "text_icon" to "Текст + Иконка",
                        "wave" to "Звуковая волна",
                        "progress" to "Прогресс-бар",
                    ),
                    selectedItem = rightTemplateOverride,
                    onSelect = { tmpl ->
                        rightTemplateOverride = tmpl
                        prefs.edit().putString("cast_right_template", tmpl).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
