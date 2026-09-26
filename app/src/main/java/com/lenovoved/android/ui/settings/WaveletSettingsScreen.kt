package com.lenovoved.android.ui.settings

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenovoved.android.ui.OriginOSSlider
import com.lenovoved.android.wavelet.WaveletAudioEngine
import kotlin.math.roundToInt

@Composable
fun WaveletSettingsScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val prefs = remember { WaveletAudioEngine.getPrefs(context) }
    val dark = isSystemInDarkTheme()
    val dividerColor = if (dark) Color(0xFF2C2C2E) else Color(0xFFF0F0F2)
    val textColor = if (dark) Color.White else Color(0xFF1C1C1E)
    val subTextColor = if (dark) Color(0xFF8E8E93) else Color(0xFF64748B)

    // Master switch
    var masterEnabled by remember { mutableStateOf(prefs.getBoolean("wavelet_master_enabled", true)) }

    // AutoEq
    var autoEqEnabled by remember { mutableStateOf(prefs.getBoolean("wavelet_autoeq_enabled", true)) }
    var selectedModelId by remember { mutableStateOf(prefs.getString("wavelet_autoeq_model", "vivo_tws_4") ?: "vivo_tws_4") }
    var showModelPicker by remember { mutableStateOf(false) }

    // 10 bands values
    val bandLevels = remember {
        (0 until 10).map { i ->
            mutableFloatStateOf(prefs.getFloat("wavelet_eq_band_$i", 0f))
        }
    }
    var selectedPreset by remember { mutableStateOf(prefs.getString("wavelet_eq_preset", "Плоский (Flat)") ?: "Плоский (Flat)") }

    // Bass Tuner
    var bassEnabled by remember { mutableStateOf(prefs.getBoolean("wavelet_bass_enabled", true)) }
    var bassType by remember { mutableStateOf(prefs.getString("wavelet_bass_type", "Natural") ?: "Natural") }
    var bassGain by remember { mutableFloatStateOf(prefs.getFloat("wavelet_bass_gain", 4.0f)) }
    var bassCutoff by remember { mutableFloatStateOf(prefs.getFloat("wavelet_bass_cutoff", 80f)) }

    // Virtualizer & Equal Loudness
    var equalLoudnessOn by remember { mutableStateOf(prefs.getBoolean("wavelet_equal_loudness_enabled", false)) }
    var virtualizerOn by remember { mutableStateOf(prefs.getBoolean("wavelet_virtualizer_enabled", false)) }
    var virtualizerStrength by remember { mutableFloatStateOf(prefs.getFloat("wavelet_virtualizer_strength", 35f)) }

    // Reverb
    var reverbOn by remember { mutableStateOf(prefs.getBoolean("wavelet_reverb_enabled", false)) }
    var reverbPreset by remember { mutableIntStateOf(prefs.getInt("wavelet_reverb_preset", 2)) }

    // Limiter & Island Sync
    var limiterOn by remember { mutableStateOf(prefs.getBoolean("wavelet_limiter_enabled", true)) }
    var channelBalance by remember { mutableFloatStateOf(prefs.getFloat("wavelet_channel_balance", 0f)) }
    var islandSyncOn by remember { mutableStateOf(prefs.getBoolean("wavelet_island_sync", true)) }

    val activeProfile = WaveletAudioEngine.HEADPHONE_DATABASE.find { it.id == selectedModelId }
        ?: WaveletAudioEngine.HEADPHONE_DATABASE.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(
            title = "Эквалайзер Wavelet",
            subtitle = "Звуковой движок AutoEq, 9-полосный EQ и DSP",
            onBack = onBack,
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Master Control Card
            SettingsCard {
                SettingsSectionHeader("Wavelet Audio DSP", Icons.Default.GraphicEq)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Главный переключатель",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                        )
                        Text(
                            text = if (masterEnabled) "Звуковой движок Wavelet активен для всех медиапотоков" else "Обработка звука отключена",
                            fontSize = 12.5.sp,
                            color = subTextColor,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (masterEnabled) Color(0x220066FF) else (if (dark) Color(0xFF2C2C2E) else Color(0xFFEAEAEE)))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = if (masterEnabled) "Активен" else "Выкл",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (masterEnabled) Color(0xFF0066FF) else subTextColor,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    title = "Включить обработку звука Wavelet",
                    subtitle = "Применять алгоритмы эквализации и улучшения звучания к музыке и видео",
                    checked = masterEnabled,
                    onCheckedChange = { checked ->
                        masterEnabled = checked
                        prefs.edit().putBoolean("wavelet_master_enabled", checked).commit()
                        WaveletAudioEngine.applyAllSettings(context)
                    },
                )
            }

            // 2. AutoEq Headphone Profile Card
            SettingsCard {
                SettingsSectionHeader("AutoEq • Профили наушников", Icons.Default.Headphones)

                Text(
                    text = "Автоматическая коррекция АЧХ по золотому стандарту Harman Target. Доступна оптимизация для популярных моделей vivo, Sony, Apple и др.",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = subTextColor,
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    title = "Включить компенсацию AutoEq",
                    subtitle = "Устраняет резонансы и выравнивает тональный баланс вашей модели наушников",
                    checked = autoEqEnabled,
                    onCheckedChange = { checked ->
                        autoEqEnabled = checked
                        prefs.edit().putBoolean("wavelet_autoeq_enabled", checked).commit()
                        WaveletAudioEngine.applyAllSettings(context)
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = dividerColor, thickness = 0.8.dp)

                // Selected Headphone Badge / Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (dark) Color(0xFF242426) else Color(0xFFF4F4F8))
                        .clickable { showModelPicker = !showModelPicker }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Выбранная модель:",
                            fontSize = 11.5.sp,
                            color = subTextColor,
                        )
                        Text(
                            text = activeProfile.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        )
                        Text(
                            text = "Кривая: ${activeProfile.targetCurve}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF0066FF),
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Выбрать",
                        tint = Color(0xFF0066FF),
                        modifier = Modifier.size(20.dp),
                    )
                }

                // Headphone Picker Expansion
                if (showModelPicker) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Выберите модель наушников из базы AutoEq:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (dark) Color(0xFF1F1F21) else Color(0xFFEEEEF2))
                            .padding(vertical = 4.dp),
                    ) {
                        WaveletAudioEngine.HEADPHONE_DATABASE.forEach { hp ->
                            val isSelected = hp.id == selectedModelId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedModelId = hp.id
                                        prefs.edit().putString("wavelet_autoeq_model", hp.id).commit()
                                        WaveletAudioEngine.applyAllSettings(context)
                                        showModelPicker = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(
                                        text = hp.displayName,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF0066FF) else textColor,
                                    )
                                    Text(
                                        text = hp.targetCurve,
                                        fontSize = 11.sp,
                                        color = subTextColor,
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF0066FF),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. 9-Band Graphic Equalizer Card
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SettingsSectionHeader("Графический эквалайзер", Icons.Default.Equalizer)

                    // Reset button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                WaveletAudioEngine.resetBands(context)
                                bandLevels.forEach { it.floatValue = 0f }
                                selectedPreset = "Плоский (Flat)"
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Сброс",
                            tint = Color(0xFF0066FF),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Сброс",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0066FF),
                        )
                    }
                }

                // Presets Chips
                Text(
                    text = "Пресеты звучания:",
                    fontSize = 12.sp,
                    color = subTextColor,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WaveletAudioEngine.EQ_PRESETS.keys.forEach { presetName ->
                        val isSelected = selectedPreset == presetName
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPreset = presetName
                                val presetValues = WaveletAudioEngine.EQ_PRESETS[presetName] ?: emptyList()
                                presetValues.forEachIndexed { i, v ->
                                    if (i < bandLevels.size) {
                                        bandLevels[i].floatValue = v
                                    }
                                }
                                WaveletAudioEngine.savePreset(context, presetName, presetValues)
                            },
                            label = {
                                Text(
                                    text = presetName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0066FF),
                                selectedLabelColor = Color.White,
                                containerColor = if (dark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
                                labelColor = if (dark) Color(0xFFE5E5EA) else Color(0xFF3A3A3C),
                            ),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sliders for 10 bands formatted exactly like OriginOS system sliders
                WaveletAudioEngine.EQ_BANDS.forEachIndexed { index, freqLabel ->
                    val bandState = bandLevels[index]
                    val dbVal = bandState.floatValue
                    val dbStr = if (dbVal > 0f) "+${String.format("%.1f", dbVal)} dB" else "${String.format("%.1f", dbVal)} dB"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = freqLabel,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor,
                            )
                            Text(
                                text = dbStr,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dbVal != 0f) Color(0xFF0066FF) else subTextColor,
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        OriginOSSlider(
                            value = dbVal,
                            onValueChange = { newVal ->
                                val rounded = (newVal * 2).roundToInt() / 2f
                                bandState.floatValue = rounded
                                selectedPreset = "Пользовательский"
                                WaveletAudioEngine.saveBandLevel(context, index, rounded)
                            },
                            valueRange = -10f..10f,
                            steps = 39,
                            activeColor = Color(0xFF0066FF),
                            startIcon = Icons.Default.Remove,
                            endIcon = Icons.Default.Add,
                        )
                    }

                    if (index < WaveletAudioEngine.EQ_BANDS.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 3.dp),
                            color = dividerColor,
                            thickness = 0.6.dp,
                        )
                    }
                }
            }

            // 4. Bass Tuner Card
            SettingsCard {
                SettingsSectionHeader("Тюнер баса (Bass Tuner)", Icons.Default.Speaker)

                Text(
                    text = "Технология точной калибровки низких частот из Wavelet. Разделение на панч (атака бочки) и сустейн (глубокий суб-бас).",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = subTextColor,
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    title = "Включить Bass Tuner",
                    subtitle = "Аппаратное усиление и компрессия низких частот",
                    checked = bassEnabled,
                    onCheckedChange = { checked ->
                        bassEnabled = checked
                        prefs.edit().putBoolean("wavelet_bass_enabled", checked).commit()
                        WaveletAudioEngine.applyAllSettings(context)
                    },
                )

                if (bassEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = dividerColor, thickness = 0.8.dp)

                    // Type Chips
                    Text(text = "Тип обработки баса:", fontSize = 12.sp, color = subTextColor)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Natural", "Transient", "Sustain").forEach { type ->
                            val isSelected = bassType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    bassType = type
                                    prefs.edit().putString("wavelet_bass_type", type).commit()
                                    WaveletAudioEngine.applyAllSettings(context)
                                },
                                label = {
                                    Text(
                                        text = when(type) {
                                            "Natural" -> "Естественный"
                                            "Transient" -> "Панч (Атака)"
                                            else -> "Сустейн (Гул)"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0066FF),
                                    selectedLabelColor = Color.White,
                                    containerColor = if (dark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
                                    labelColor = if (dark) Color(0xFFE5E5EA) else Color(0xFF3A3A3C),
                                ),
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bass Gain Slider
                    SettingsSliderRow(
                        label = "Усиление баса",
                        value = bassGain,
                        onValueChange = { newVal ->
                            val rounded = (newVal * 2).roundToInt() / 2f
                            bassGain = rounded
                            prefs.edit().putFloat("wavelet_bass_gain", rounded).commit()
                            WaveletAudioEngine.applyAllSettings(context)
                        },
                        valueRange = 0f..10f,
                        steps = 19,
                        valueDisplay = "+${String.format("%.1f", bassGain)} dB",
                        minLabel = "0 dB",
                        maxLabel = "+10 dB",
                        startIcon = Icons.Default.VolumeDown,
                        endIcon = Icons.Default.VolumeUp,
                    )

                    // Bass Cutoff Slider
                    SettingsSliderRow(
                        label = "Частота среза",
                        value = bassCutoff,
                        onValueChange = { newVal ->
                            val rounded = newVal.roundToInt().toFloat()
                            bassCutoff = rounded
                            prefs.edit().putFloat("wavelet_bass_cutoff", rounded).commit()
                            WaveletAudioEngine.applyAllSettings(context)
                        },
                        valueRange = 40f..150f,
                        steps = 21,
                        valueDisplay = "${bassCutoff.roundToInt()} Hz",
                        minLabel = "40 Hz",
                        maxLabel = "150 Hz",
                        startIcon = Icons.Default.GraphicEq,
                        endIcon = Icons.Default.GraphicEq,
                    )
                }
            }

            // 5. Virtualizer & Equal Loudness Card
            SettingsCard {
                SettingsSectionHeader("Пространство и восприятие (ISO 226)", Icons.Default.SurroundSound)

                SettingsToggleRow(
                    title = "Equal Loudness (ISO 226)",
                    subtitle = "Компенсация спада низких и высоких частот при прослушивании на малой громкости",
                    checked = equalLoudnessOn,
                    onCheckedChange = { checked ->
                        equalLoudnessOn = checked
                        prefs.edit().putBoolean("wavelet_equal_loudness_enabled", checked).commit()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Виртуализация стереобазы",
                    subtitle = "Расширение стереопанорамы и эмуляция объемного звучания в наушниках",
                    checked = virtualizerOn,
                    onCheckedChange = { checked ->
                        virtualizerOn = checked
                        prefs.edit().putBoolean("wavelet_virtualizer_enabled", checked).commit()
                        WaveletAudioEngine.applyAllSettings(context)
                    },
                )

                if (virtualizerOn) {
                    SettingsSliderRow(
                        label = "Интенсивность 3D-сцены",
                        value = virtualizerStrength,
                        onValueChange = { newVal ->
                            val rounded = newVal.roundToInt().toFloat()
                            virtualizerStrength = rounded
                            prefs.edit().putFloat("wavelet_virtualizer_strength", rounded).commit()
                            WaveletAudioEngine.applyAllSettings(context)
                        },
                        valueRange = 0f..100f,
                        steps = 19,
                        valueDisplay = "${virtualizerStrength.roundToInt()}%",
                        minLabel = "0%",
                        maxLabel = "100%",
                        startIcon = Icons.Default.SurroundSound,
                        endIcon = Icons.Default.SurroundSound,
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Реверберация (Reverberation)",
                    subtitle = "Эмуляция акустики комнаты или концертного зала",
                    checked = reverbOn,
                    onCheckedChange = { checked ->
                        reverbOn = checked
                        prefs.edit().putBoolean("wavelet_reverb_enabled", checked).commit()
                        WaveletAudioEngine.applyAllSettings(context)
                    },
                )

                if (reverbOn) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            1 to "Малая комната",
                            2 to "Средний зал",
                            3 to "Большой зал",
                            4 to "Студия"
                        ).forEach { (presetId, label) ->
                            val isSelected = reverbPreset == presetId
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    reverbPreset = presetId
                                    prefs.edit().putInt("wavelet_reverb_preset", presetId).commit()
                                    WaveletAudioEngine.applyAllSettings(context)
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0066FF),
                                    selectedLabelColor = Color.White,
                                    containerColor = if (dark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
                                    labelColor = if (dark) Color(0xFFE5E5EA) else Color(0xFF3A3A3C),
                                ),
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                    }
                }
            }

            // 6. Limiter & Island Sync Card
            SettingsCard {
                SettingsSectionHeader("Лимитер и интеграция с OriginOS", Icons.Default.Tune)

                SettingsToggleRow(
                    title = "Пост-гейн лимитер",
                    subtitle = "Автоматическое снижение пиков для предотвращения клиппинга и хрипов на максимальной громкости",
                    checked = limiterOn,
                    onCheckedChange = { checked ->
                        limiterOn = checked
                        prefs.edit().putBoolean("wavelet_limiter_enabled", checked).commit()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Транслировать Wavelet на Dynamic Island",
                    subtitle = "Отображать бейдж активного профиля наушников и эквалайзера в островке vivo при воспроизведении музыки",
                    checked = islandSyncOn,
                    onCheckedChange = { checked ->
                        islandSyncOn = checked
                        prefs.edit().putBoolean("wavelet_island_sync", checked).commit()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsSliderRow(
                    label = "Баланс стереоканалов (L / R)",
                    value = channelBalance,
                    onValueChange = { newVal ->
                        val rounded = newVal.roundToInt().toFloat()
                        channelBalance = rounded
                        prefs.edit().putFloat("wavelet_channel_balance", rounded).commit()
                    },
                    valueRange = -50f..50f,
                    steps = 19,
                    valueDisplay = when {
                        channelBalance < 0 -> "L ${-channelBalance.roundToInt()}%"
                        channelBalance > 0 -> "R ${channelBalance.roundToInt()}%"
                        else -> "Центр"
                    },
                    minLabel = "L 50%",
                    maxLabel = "R 50%",
                    startIcon = Icons.Default.Headphones,
                    endIcon = Icons.Default.Headphones,
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
