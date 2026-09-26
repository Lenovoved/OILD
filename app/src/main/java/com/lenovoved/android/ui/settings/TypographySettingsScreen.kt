package com.lenovoved.android.ui.settings

import android.content.Context
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
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.lenovoved.android.service.NotificationCastListener
import com.lenovoved.android.ui.OriginOSSlider
import com.lenovoved.android.ui.PREFS_NAME
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TypographySettingsScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var notifStyle by remember {
        mutableStateOf(prefs.getString("notification_display_style", "classic") ?: "classic")
    }
    var capsuleCharsFloat by remember {
        mutableFloatStateOf(prefs.getInt("notification_capsule_chars", 24).coerceIn(1, 100).toFloat())
    }
    var bodyCharsFloat by remember {
        mutableFloatStateOf(prefs.getInt("notification_body_chars", 120).coerceIn(1, 500).toFloat())
    }
    val showTimestamp = prefs.getBoolean("notification_show_timestamp", true)

    val capsuleChars = capsuleCharsFloat.roundToInt()
    val bodyChars = bodyCharsFloat.roundToInt()
    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Color.White else Color(0xFF1C1C1E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(
            title = "Стиль и лимиты символов",
            subtitle = "Настройка шаблона карточки и длины текста в капсуле и теле сообщения",
            onBack = onBack,
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Card Template Style
            SettingsCard {
                SettingsSectionHeader("Стиль карточки", Icons.Default.FormatPaint)

                Text(
                    text = "Выбирает компоновку элементов в развернутом островке OriginOS.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsChipsRow(
                    items = listOf(
                        "classic" to "Классический",
                        "compact" to "Компактный",
                        "expanded" to "Расширенный",
                        "minimal" to "Минималистичный",
                    ),
                    selectedItem = notifStyle,
                    onSelect = { s ->
                        notifStyle = s
                        prefs.edit().putString("notification_display_style", s).commit()
                        NotificationCastListener.notifySettingsChanged(context)
                    },
                )
            }

            // Capsule Character Limit Slider (1..100)
            SettingsCard {
                SettingsSectionHeader("Символы в капсуле", Icons.Default.TextFields)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Лимит символов в заголовке / капсуле",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (dark) Color(0x330066FF) else Color(0xFFE0EDFF))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "$capsuleChars симв.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0066FF),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OriginOSSlider(
                    value = capsuleCharsFloat,
                    onValueChange = { newVal ->
                        capsuleCharsFloat = newVal
                        val rounded = newVal.roundToInt().coerceIn(1, 100)
                        prefs.edit().putInt("notification_capsule_chars", rounded).commit()
                        NotificationCastListener.notifySettingsChanged(context)
                    },
                    valueRange = 1f..100f,
                    steps = 98, // (100 - 1 - 1) = 98 steps for integer precision 1..100
                    startIcon = Icons.Default.TextFields,
                    endIcon = Icons.Default.TextFields,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("1 (мин)", fontSize = 11.5.sp, color = Color(0xFF8E8E93))
                    Text("24 (стандарт)", fontSize = 11.5.sp, color = Color(0xFF8E8E93))
                    Text("100 (макс)", fontSize = 11.5.sp, color = Color(0xFF8E8E93))
                }
            }

            // Message Body Character Limit Slider (1..500)
            SettingsCard {
                SettingsSectionHeader("Символы в теле сообщения", Icons.Default.TextFields)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Лимит текста сообщения",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (dark) Color(0x330066FF) else Color(0xFFE0EDFF))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "$bodyChars симв.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0066FF),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OriginOSSlider(
                    value = bodyCharsFloat,
                    onValueChange = { newVal ->
                        bodyCharsFloat = newVal
                        val rounded = newVal.roundToInt().coerceIn(1, 500)
                        prefs.edit().putInt("notification_body_chars", rounded).commit()
                        NotificationCastListener.notifySettingsChanged(context)
                    },
                    valueRange = 1f..500f,
                    startIcon = Icons.Default.ShortText,
                    endIcon = Icons.Default.Notes,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("1 (мин)", fontSize = 11.5.sp, color = Color(0xFF8E8E93))
                    Text("120 (стандарт)", fontSize = 11.5.sp, color = Color(0xFF8E8E93))
                    Text("500 (полный)", fontSize = 11.5.sp, color = Color(0xFF8E8E93))
                }
            }



            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
