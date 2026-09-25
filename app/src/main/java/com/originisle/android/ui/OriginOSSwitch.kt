package com.originisle.android.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Custom OriginOS-style switch matching vivo OriginOS UI design:
 * Soft light-blue pill track with a white circular thumb accented by a vivid blue ring.
 */
@Composable
fun OriginOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "switchOffset",
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFFD6E4FF) else Color(0xFFE2E8F0),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "trackColor",
    )

    val trackBorderColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF1677FF).copy(alpha = 0.35f) else Color(0xFFCBD5E1),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "trackBorderColor",
    )

    val ringColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF0066FF) else Color(0xFF94A3B8),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ringColor",
    )

    Box(
        modifier = modifier
            .width(50.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor)
            .border(
                width = 1.dp,
                color = trackBorderColor,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(24.dp)
                .shadow(elevation = 2.dp, shape = CircleShape, spotColor = Color(0x33000000))
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = if (checked) 3.5.dp else 1.5.dp,
                    color = ringColor,
                    shape = CircleShape,
                ),
        )
    }
}
