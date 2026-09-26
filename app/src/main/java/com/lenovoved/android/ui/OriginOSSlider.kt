package com.lenovoved.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * OriginOS-styled slider matching Vivo OriginOS:
 * - Solid, continuous, unbroken 6dp track (ZERO dots, ZERO tick marks, ZERO gaps)
 * - Vivid blue (#0066FF) solid active bar
 * - Soft subtle gray (#EAEAEE in light, #2C2C2E in dark) solid inactive bar
 * - White circular thumb (20dp) with vibrant blue (#0066FF, 3dp) accent ring and soft drop shadow
 * - Continuous unbroken track running seamlessly through the thumb
 * - Optional left and right icons matching system brightness/volume/level sliders
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginOSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    activeColor: Color = Color(0xFF0066FF),
    inactiveColor: Color? = null,
    thumbSize: Dp = 20.dp,
    trackHeight: Dp = 6.dp,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
) {
    val dark = isSystemInDarkTheme()
    val resolvedInactiveColor = inactiveColor ?: (if (dark) Color(0xFF2C2C2E) else Color(0xFFEAEAEE))
    val iconTint = if (dark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { (thumbSize / 2).toPx() }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (startIcon != null) {
            Icon(
                imageVector = startIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(19.dp),
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                valueRange = valueRange,
                steps = 0, // Always 0 to prevent Compose from rendering step/tick dots along the track
                interactionSource = interactionSource,
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(thumbSize)
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                spotColor = if (enabled) Color(0x350066FF) else Color(0x1F000000),
                                ambientColor = Color(0x15000000),
                            )
                            .background(Color.White, CircleShape)
                            .border(
                                width = 3.dp,
                                color = if (enabled) activeColor else Color(0xFF94A3B8),
                                shape = CircleShape,
                            ),
                    )
                },
                track = { sliderState ->
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(trackHeight)
                    ) {
                        val range = sliderState.valueRange.endInclusive - sliderState.valueRange.start
                        val fraction = if (range > 0f) {
                            ((sliderState.value - sliderState.valueRange.start) / range).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        val trackWidth = size.width
                        val trackH = size.height
                        val pillRadius = CornerRadius(trackH / 2f, trackH / 2f)

                        // 1. Inactive track: 100% solid, smooth, unbroken rounded pill across full width
                        drawRoundRect(
                            color = if (enabled) resolvedInactiveColor else resolvedInactiveColor.copy(alpha = 0.5f),
                            size = Size(trackWidth, trackH),
                            cornerRadius = pillRadius,
                        )

                        // 2. Active track: 100% solid, smooth, unbroken rounded pill from 0 to thumb position
                        val availableWidth = (trackWidth - 2 * thumbRadiusPx).coerceAtLeast(0f)
                        val activeWidth = (thumbRadiusPx + fraction * availableWidth).coerceIn(0f, trackWidth)
                        if (activeWidth > 0f) {
                            drawRoundRect(
                                color = if (enabled) activeColor else activeColor.copy(alpha = 0.4f),
                                size = Size(activeWidth, trackH),
                                cornerRadius = pillRadius,
                            )
                        }
                    }
                },
            )
        }

        if (endIcon != null) {
            Icon(
                imageVector = endIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
