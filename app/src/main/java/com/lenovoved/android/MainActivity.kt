package com.lenovoved.android

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.magnifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.lenovoved.android.cards.SportsCard
import com.lenovoved.android.equalizer.WaveletEqualizerScreen
import com.lenovoved.android.island.OriginIslandBuilder
import com.lenovoved.android.island.PlaygroundService
import com.lenovoved.android.service.NotificationCastListener
import com.lenovoved.android.ui.AppFilterSheet
import com.lenovoved.android.ui.OriginIsleTheme
import com.lenovoved.android.ui.PREFS_NAME
import com.lenovoved.android.ui.openAutoStartSettings
import com.lenovoved.android.ui.rememberResumeTick
import com.lenovoved.android.ui.samples.IslandSamples
import com.lenovoved.android.ui.settings.CategoriesSettingsScreen
import com.lenovoved.android.ui.settings.ColorsThemeSettingsScreen
import com.lenovoved.android.ui.settings.DurationSettingsScreen
import com.lenovoved.android.ui.settings.PermissionsSettingsScreen
import com.lenovoved.android.ui.settings.SurfacesSettingsScreen
import com.lenovoved.android.ui.settings.TypographySettingsScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SettingsSubScreen {
    CATEGORIES,
    DURATION,
    SURFACES,
    TYPOGRAPHY,
    COLORS_THEME,
    PERMISSIONS,
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        when (intent?.getStringExtra("autofire")) {
            "football" -> SportsCard.post(this, "ARS", 1, "MAN", 1, "65'")
        }
        intent?.getIntExtra("autofire_sample", -1)?.takeIf { it >= 0 }?.let {
            IslandSamples.all.getOrNull(it)?.post(this)
        }

        setContent {
            OriginIsleTheme {
                OriginSpaceApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val hasAnyCasting = prefs.getBoolean("cast_media_sessions", false) ||
            prefs.getBoolean("cast_normal_notifications", false) ||
            prefs.getBoolean("cast_messenger_notifications", false) ||
            prefs.getBoolean("cast_nav_notifications", false) ||
            prefs.getBoolean("cast_sports_enabled", false) ||
            prefs.getBoolean("cast_payments_enabled", false) ||
            prefs.getBoolean("cast_notifications", false)

        if (hasAnyCasting) {
            NotificationCastListener.forceRebind(this)
        }
    }
}

@Composable
private fun OriginSpaceApp() {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableIntStateOf(0) } // 0: Управление, 1: Эквалайзер
    var activeSubScreen by remember { mutableStateOf<SettingsSubScreen?>(null) }

    // Grant Island scenes on launch
    remember { OriginIslandBuilder.grantScenes(context) }

    Scaffold(
        containerColor = Color(0xFFF4F6F9),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            AnimatedContent(
                targetState = selectedBottomTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> fullWidth },
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                            ) + fadeOut(animationSpec = tween(200)),
                        )
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> -fullWidth },
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> fullWidth / 3 },
                            ) + fadeOut(animationSpec = tween(200)),
                        )
                    }
                },
                label = "TabTransition",
            ) { tabIndex ->
                if (tabIndex == 0) {
                    AnimatedContent(
                        targetState = activeSubScreen,
                        transitionSpec = {
                            if (targetState != null) {
                                (slideInHorizontally(
                                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                                    initialOffsetX = { fullWidth -> fullWidth },
                                ) + fadeIn(animationSpec = tween(300))).togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                                    ) + fadeOut(animationSpec = tween(200)),
                                )
                            } else {
                                (slideInHorizontally(
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                                ) + fadeIn(animationSpec = tween(300))).togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(320, easing = FastOutSlowInEasing),
                                        targetOffsetX = { fullWidth -> fullWidth },
                                    ) + fadeOut(animationSpec = tween(200)),
                                )
                            }
                        },
                        label = "SubScreenTransition",
                    ) { subScreen ->
                        when (subScreen) {
                            SettingsSubScreen.CATEGORIES -> CategoriesSettingsScreen(onBack = { activeSubScreen = null })
                            SettingsSubScreen.DURATION -> DurationSettingsScreen(onBack = { activeSubScreen = null })
                            SettingsSubScreen.SURFACES -> SurfacesSettingsScreen(onBack = { activeSubScreen = null })
                            SettingsSubScreen.TYPOGRAPHY -> TypographySettingsScreen(onBack = { activeSubScreen = null })
                            SettingsSubScreen.COLORS_THEME -> ColorsThemeSettingsScreen(onBack = { activeSubScreen = null })
                            SettingsSubScreen.PERMISSIONS -> PermissionsSettingsScreen(onBack = { activeSubScreen = null })
                            null -> OriginSpaceMainHub(
                                onNavigateTo = { screen -> activeSubScreen = screen },
                            )
                        }
                    }
                } else {
                    WaveletEqualizerScreen()
                }
            }

            // Floating Bottom Pill Dock
            FloatingBottomPill(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                selectedTab = selectedBottomTab,
                onSelectTab = { tabIndex ->
                    selectedBottomTab = tabIndex
                    if (tabIndex == 1) {
                        activeSubScreen = null
                    }
                },
            )
        }
    }
}

@Composable
private fun OriginSpaceMainHub(
    onNavigateTo: (SettingsSubScreen) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val tick = rememberResumeTick()

    var showInfoDialog by remember { mutableStateOf(false) }
    var showAppFilterSheet by remember { mutableStateOf(false) }

    // Count active categories
    val normalOn = prefs.getBoolean("cast_normal_notifications", false) || prefs.getBoolean("cast_notifications", false)
    val messengerOn = prefs.getBoolean("cast_messenger_notifications", false)
    val navOn = prefs.getBoolean("cast_nav_notifications", false)
    val mediaOn = prefs.getBoolean("cast_media_sessions", false)
    val sportsOn = prefs.getBoolean("cast_sports_enabled", false)
    val paymentsOn = prefs.getBoolean("cast_payments_enabled", false)
    val activeCategoriesCount = listOf(normalOn, messengerOn, navOn, mediaOn, sportsOn, paymentsOn).count { it }

    val autoDismissSec = prefs.getInt("cast_auto_dismiss_seconds", 0)
    val notifStyle = prefs.getString("notification_display_style", "classic") ?: "classic"
    val notifFont = prefs.getString("notification_font_family", "default") ?: "default"
    val lightEnabled = prefs.getBoolean("cast_light_enabled", false)

    val postNotifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { tick.intValue++ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            val hasNotif = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasNotif) {
                postNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (activeCategoriesCount > 0) {
            NotificationCastListener.forceRebind(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(
                    onClick = { activity?.finish() },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color(0xFF1A1C1E),
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = "Пространство Origin",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E),
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9ECF0))
                    .clickable { showInfoDialog = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Информация",
                    tint = Color(0xFF49454F),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Phone Island Mockup
        PhoneIslandMockup()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Пространство Origin",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Интеграция с динамическим островом OriginOS. Каждый параметр вынесен в отдельную страницу для детальной настройки.",
            fontSize = 13.5.sp,
            lineHeight = 18.sp,
            color = Color(0xFF5F6368),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Settings Navigation Menu Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color(0x10000000),
                ),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                // 1. Categories
                SettingsHubRow(
                    icon = Icons.Default.Notifications,
                    iconBg = Color(0xFF1677FF),
                    title = "Категории уведомлений",
                    subtitle = "Обычные, мессенджеры, навигаторы, плеер, спорт, чеки",
                    badge = if (activeCategoriesCount == 0) "Выкл" else "$activeCategoriesCount акт.",
                    badgeColor = if (activeCategoriesCount == 0) Color(0xFF94A3B8) else Color(0xFF1677FF),
                    onClick = { onNavigateTo(SettingsSubScreen.CATEGORIES) },
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // 2. Duration
                SettingsHubRow(
                    icon = Icons.Default.Schedule,
                    iconBg = Color(0xFF0EA5E9),
                    title = "Время отображения",
                    subtitle = "Задержка удержания карточки и капсулы, таймстемп",
                    badge = if (autoDismissSec == 0) "Бессрочно" else "$autoDismissSec сек",
                    badgeColor = Color(0xFF0EA5E9),
                    onClick = { onNavigateTo(SettingsSubScreen.DURATION) },
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // 3. Surfaces
                SettingsHubRow(
                    icon = Icons.Default.Layers,
                    iconBg = Color(0xFF8B5CF6),
                    title = "Поверхности отображения",
                    subtitle = "Статус-бар, экран блокировки, AOD, виджет, Jovi",
                    badge = "Экраны",
                    badgeColor = Color(0xFF8B5CF6),
                    onClick = { onNavigateTo(SettingsSubScreen.SURFACES) },
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // 4. Typography & Style
                SettingsHubRow(
                    icon = Icons.Default.FormatPaint,
                    iconBg = Color(0xFF10B981),
                    title = "Стиль и лимиты символов",
                    subtitle = "Шаблоны карточки, лимит символов в капсуле и теле сообщения",
                    badge = notifStyle.replaceFirstChar { it.uppercase() },
                    badgeColor = Color(0xFF10B981),
                    onClick = { onNavigateTo(SettingsSubScreen.TYPOGRAPHY) },
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // 5. Colors & Theme
                SettingsHubRow(
                    icon = Icons.Default.ColorLens,
                    iconBg = Color(0xFF6366F1),
                    title = "Цветовая палитра и темы",
                    subtitle = "Запрет инверсии темы, цвет фона карточки и капсулы",
                    badge = "Цвета",
                    badgeColor = Color(0xFF6366F1),
                    onClick = { onNavigateTo(SettingsSubScreen.COLORS_THEME) },
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // 6. App Filter
                SettingsHubRow(
                    icon = Icons.Default.FilterList,
                    iconBg = Color(0xFF64748B),
                    title = "Фильтр приложений",
                    subtitle = "Индивидуальный выбор приложений для трансляции",
                    badge = "Список",
                    badgeColor = Color(0xFF64748B),
                    onClick = { showAppFilterSheet = true },
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // 7. Permissions
                SettingsHubRow(
                    icon = Icons.Default.Security,
                    iconBg = Color(0xFF14B8A6),
                    title = "Системные разрешения",
                    subtitle = "Доступ к уведомлениям, батарея, автозапуск",
                    badge = "Служба",
                    badgeColor = Color(0xFF14B8A6),
                    onClick = { onNavigateTo(SettingsSubScreen.PERMISSIONS) },
                )
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }

    if (showAppFilterSheet) {
        AppFilterSheet(
            context = context,
            prefs = prefs,
            onDismiss = { showAppFilterSheet = false },
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "О приложении Пространство Origin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "«Пространство Origin» транслирует уведомления и медиаплеер Android на встроенный динамический остров (OriginIsland) в смартфонах vivo на базе OriginOS.",
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563),
                    )
                    Text(
                        text = "• Все параметры сгруппированы в отдельные страницы настроек.\n• Тумблеры по умолчанию выключены.\n• Поддерживаются все системные возможности vivo SuperX.",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                    )
                    Button(
                        onClick = {
                            openAutoStartSettings(context)
                            showInfoDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    ) {
                        Text("Настройки автозапуска vivo")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Понятно")
                }
            },
        )
    }
}

@Composable
private fun SettingsHubRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF64748B),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = badge,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Открыть",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/**
 * Phone Screen Mockup with top camera cutout pill and soft real-time clock.
 */
@Composable
private fun PhoneIslandMockup() {
    val currentTime = remember {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date())
    }

    Box(
        modifier = Modifier
            .width(170.dp)
            .height(155.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0x12000000),
                ambientColor = Color(0x08000000),
            )
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFFE5E8EB), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Island cutout pill (Black pill with camera lenses)
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(15.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1E20)),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF323236)),
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF28282B)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentTime,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFCCD1D7),
                letterSpacing = 1.sp,
            )
        }
    }
}

/**
 * Translucent Glass Floating Pill Bar at the bottom of the screen (Управление / Эквалайзер).
 */
@Composable
private fun FloatingBottomPill(
    modifier: Modifier = Modifier,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
) {
    var tab0Center by remember { mutableStateOf(Offset.Unspecified) }
    var tab1Center by remember { mutableStateOf(Offset.Unspecified) }
    var isMagnifying0 by remember { mutableStateOf(false) }
    var isMagnifying1 by remember { mutableStateOf(false) }

    val tab0Scale by animateFloatAsState(targetValue = if (selectedTab == 0 || isMagnifying0) 1.20f else 1.0f, label = "Tab0Scale")
    val tab1Scale by animateFloatAsState(targetValue = if (selectedTab == 1 || isMagnifying1) 1.20f else 1.0f, label = "Tab1Scale")

    Surface(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0x20000000),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(32.dp),
            ),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.75f),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        tab0Center = coordinates.positionInRoot() + Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
                    }
                    .then(
                        if (isMagnifying0 && tab0Center.isSpecified) {
                            Modifier.magnifier(
                                sourceCenter = { tab0Center },
                                magnifierCenter = { tab0Center - Offset(0f, 110f) },
                                zoom = 1.4f
                            )
                        } else Modifier
                    )
                    .graphicsLayer {
                        scaleX = tab0Scale
                        scaleY = tab0Scale
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (selectedTab == 0) Color(0xFF1677FF).copy(alpha = 0.15f) else Color.Transparent,
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isMagnifying0 = true
                                try {
                                    awaitRelease()
                                } finally {
                                    isMagnifying0 = false
                                }
                            },
                            onTap = { onSelectTab(0) }
                        )
                    }
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Управление",
                    fontSize = 14.5.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedTab == 0) Color(0xFF1677FF) else Color(0xFF334155),
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        tab1Center = coordinates.positionInRoot() + Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
                    }
                    .then(
                        if (isMagnifying1 && tab1Center.isSpecified) {
                            Modifier.magnifier(
                                sourceCenter = { tab1Center },
                                magnifierCenter = { tab1Center - Offset(0f, 110f) },
                                zoom = 1.4f
                            )
                        } else Modifier
                    )
                    .graphicsLayer {
                        scaleX = tab1Scale
                        scaleY = tab1Scale
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (selectedTab == 1) Color(0xFF1677FF).copy(alpha = 0.15f) else Color.Transparent,
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isMagnifying1 = true
                                try {
                                    awaitRelease()
                                } finally {
                                    isMagnifying1 = false
                                }
                            },
                            onTap = { onSelectTab(1) }
                        )
                    }
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Эквалайзер",
                    fontSize = 14.5.sp,
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedTab == 1) Color(0xFF1677FF) else Color(0xFF334155),
                )
            }
        }
    }
}
