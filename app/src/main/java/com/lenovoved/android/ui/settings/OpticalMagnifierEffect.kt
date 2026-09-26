package com.lenovoved.android.ui.settings

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/**
 * AGSL source for an advanced physical convex optical lens magnifier.
 * Distorts coordinates spherically relative to the given bounding box coordinates.
 */
const val MAGNIFIER_SHADERS_SRC = """
    uniform shader composable;
    uniform float4 rect; // x=left, y=top, z=right, w=bottom
    uniform float zoom;

    half4 main(float2 coords) {
        if (coords.x >= rect.x && coords.x <= rect.z && coords.y >= rect.y && coords.y <= rect.w) {
            float2 center = float2((rect.x + rect.z) * 0.5, (rect.y + rect.w) * 0.5);
            float2 size = float2(rect.z - rect.x, rect.w - rect.y);
            float2 d = coords - center;
            
            // Normalize distance to the ellipse bounds
            float2 normD = d / (size * 0.5);
            float dist = length(normD);
            
            if (dist < 1.0) {
                // Spherical compression for natural-looking magnification at the center
                float scale = 1.0 - (1.0 - 1.0 / zoom) * (1.0 - dist * dist);
                float2 refractedCoords = center + d * scale;
                return composable.eval(refractedCoords);
            }
        }
        return composable.eval(coords);
    }
"""

/**
 * Composable modifier that applies a real-time hardware-accelerated convex optical magnification
 * to the specified bounding box coordinates on the applied container.
 */
@Composable
fun Modifier.opticalMagnifier(
    enabled: Boolean,
    rect: android.graphics.RectF?,
    zoom: Float = 1.25f
): Modifier {
    if (!enabled || Build.VERSION.SDK_INT < 33 || rect == null) return this

    val runtimeShader = remember {
        RuntimeShader(MAGNIFIER_SHADERS_SRC)
    }

    return this.graphicsLayer {
        runtimeShader.setFloatUniform("rect", rect.left, rect.top, rect.right, rect.bottom)
        runtimeShader.setFloatUniform("zoom", zoom)

        renderEffect = RenderEffect.createRuntimeShaderEffect(
            runtimeShader,
            "composable"
        ).asComposeRenderEffect()
    }
}
