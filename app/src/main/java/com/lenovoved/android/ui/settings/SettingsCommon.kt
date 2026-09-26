package com.lenovoved.android.ui.settings

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenovoved.android.ui.OriginOSSlider
import com.lenovoved.android.ui.OriginOSSwitch
import kotlin.math.roundToInt

@Composable
fun SettingsTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val btnBg = if (dark) Color(0xFF1C1C1E) else Color.White
    val titleColor = if (dark) Color.White else Color(0xFF1C1C1E)
    val subColor = if (dark) Color(0xFF8E8E93) else Color(0xFF6B7280)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(btnBg),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = titleColor,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = subColor,
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val cardBg = if (dark) Color(0xFF1C1C1E) else Color.White

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (dark) 0.dp else 1.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x0A000000),
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            content()
        }
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: ImageVector? = null,
    iconTint: Color = Color(0xFF0066FF),
) {
    val dark = isSystemInDarkTheme()
    val titleColor = if (dark) Color.White else Color(0xFF1C1C1E)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 10.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
        )
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    val titleColor = if (dark) Color.White else Color(0xFF1C1C1E)
    val subColor = if (dark) Color(0xFF8E8E93) else Color(0xFF6B7280)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = subColor,
                )
            }
        }
        OriginOSSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun SettingsSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueDisplay: String,
    minLabel: String = "",
    maxLabel: String = "",
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val dark = isSystemInDarkTheme()
    val labelColor = if (dark) Color.White else Color(0xFF1C1C1E)
    val inactiveTrackColor = if (dark) Color(0xFF2C2C2E) else Color(0xFFEAEAEE)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = labelColor,
            )
            Text(
                text = valueDisplay,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0066FF),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        OriginOSSlider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            activeColor = Color(0xFF0066FF),
            inactiveColor = inactiveTrackColor,
            startIcon = startIcon,
            endIcon = endIcon,
        )
        if (minLabel.isNotBlank() || maxLabel.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = minLabel, fontSize = 11.sp, color = Color(0xFF8E8E93))
                Text(text = maxLabel, fontSize = 11.sp, color = Color(0xFF8E8E93))
            }
        }
    }
}

@Composable
fun <T> SettingsChipsRow(
    items: List<Pair<T, String>>,
    selectedItem: T,
    onSelect: (T) -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val unselectedBg = if (dark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
    val unselectedLabel = if (dark) Color(0xFFE5E5EA) else Color(0xFF3A3A3C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { (item, label) ->
            val isSelected = selectedItem == item
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(item) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0066FF),
                    selectedLabelColor = Color.White,
                    containerColor = unselectedBg,
                    labelColor = unselectedLabel,
                ),
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Composable
fun SettingsColorPalette(
    colors: List<Pair<Int, String>>,
    selectedColor: Int,
    onColorSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        colors.forEach { (colorInt, name) ->
            val isSelected = selectedColor == colorInt
            val color = if (colorInt == 0) Color(0xFF1E293B) else Color(colorInt)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onColorSelect(colorInt) }
                    .padding(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color(0xFF1677FF) else Color(0x3394A3B8),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Выбрано",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    fontSize = 11.sp,
                    color = if (isSelected) Color(0xFF1677FF) else Color(0xFF64748B),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}
