package com.lenovoved.android.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.lenovoved.android.service.NotificationCastListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppItem(
    val pkg: String,
    val label: String,
    val icon: ImageBitmap?,
)

suspend fun loadInstalledApps(context: Context): List<AppItem> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    pm.getInstalledApplications(0)
        .filter { pm.getLaunchIntentForPackage(it.packageName) != null && it.packageName != context.packageName }
        .map { info ->
            AppItem(
                pkg = info.packageName,
                label = pm.getApplicationLabel(info).toString(),
                icon = runCatching { pm.getApplicationIcon(info).toBitmap(96, 96).asImageBitmap() }.getOrNull(),
            )
        }
        .sortedBy { it.label.lowercase() }
}

enum class AppFilterMode { ALL, ALLOWED, BLOCKED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFilterSheet(
    context: Context,
    prefs: SharedPreferences,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf(AppFilterMode.ALL) }

    val ignored = remember {
        mutableStateListOf<String>().apply {
            addAll(prefs.getStringSet("cast_ignored_apps", emptySet()).orEmpty())
        }
    }

    LaunchedEffect(Unit) {
        apps = loadInstalledApps(context)
        loading = false
    }

    val dark = isSystemInDarkTheme()

    val filteredApps = remember(apps, query, filterMode, ignored.size) {
        apps.filter { app ->
            val matchesQuery = if (query.isBlank()) true else {
                app.label.contains(query, ignoreCase = true) || app.pkg.contains(query, ignoreCase = true)
            }
            val matchesMode = when (filterMode) {
                AppFilterMode.ALL -> true
                AppFilterMode.ALLOWED -> app.pkg !in ignored
                AppFilterMode.BLOCKED -> app.pkg in ignored
            }
            matchesQuery && matchesMode
        }
    }

    val allowedCount = apps.count { it.pkg !in ignored }
    val blockedCount = apps.size - allowedCount

    // Polished OriginOS dark theme color palette
    val sheetBg = if (dark) Color(0xFF141416) else Color(0xFFF7F7FA)
    val cardBg = if (dark) Color(0xFF1C1C1F) else Color.White
    val cardBorder = if (dark) Color(0xFF2E2E33) else Color(0xFFEBEBF0)
    val searchBg = if (dark) Color(0xFF222227) else Color(0xFFEBEBF0)
    val searchBorder = if (dark) Color(0xFF33333A) else Color.Transparent
    val textColor = if (dark) Color(0xFFF3F4F6) else Color(0xFF1C1C1E)
    val subTextColor = if (dark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val dividerColor = if (dark) Color(0xFF2C2C30) else Color(0xFFE5E5EA)
    val iconContainerBg = if (dark) Color(0xFF26262B) else Color(0xFFF0F0F4)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = if (dark) Color(0xFF48484E) else Color(0xFFD1D1D6),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0066FF).copy(alpha = if (dark) 0.22f else 0.12f))
                            .border(1.dp, Color(0xFF0066FF).copy(alpha = if (dark) 0.35f else 0.15f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Фильтр приложений",
                            tint = Color(0xFF0066FF),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "Фильтр приложений",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        )
                        Text(
                            text = if (loading) "Загрузка списка..." else "Разрешено: $allowedCount • Заблокировано: $blockedCount",
                            fontSize = 12.5.sp,
                            color = subTextColor,
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (dark) Color(0xFF242428) else Color(0xFFEAEAEE)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = textColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // OriginOS Pill Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(searchBg)
                    .border(1.dp, searchBorder, CircleShape),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск",
                        tint = subTextColor,
                        modifier = Modifier.size(20.dp),
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Поиск по названию или пакету...",
                                fontSize = 14.sp,
                                color = subTextColor,
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = textColor,
                            ),
                            cursorBrush = SolidColor(Color(0xFF0066FF)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Очистить",
                            tint = subTextColor,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { query = "" },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter chips: Все / Разрешено / Заблокировано
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = filterMode == AppFilterMode.ALL,
                    onClick = { filterMode = AppFilterMode.ALL },
                    label = { Text("Все (${apps.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0066FF),
                        selectedLabelColor = Color.White,
                        containerColor = if (dark) Color(0xFF222227) else Color(0xFFEBEBF0),
                        labelColor = subTextColor,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )

                FilterChip(
                    selected = filterMode == AppFilterMode.ALLOWED,
                    onClick = { filterMode = AppFilterMode.ALLOWED },
                    label = { Text("Разрешено ($allowedCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0066FF),
                        selectedLabelColor = Color.White,
                        containerColor = if (dark) Color(0xFF222227) else Color(0xFFEBEBF0),
                        labelColor = subTextColor,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )

                FilterChip(
                    selected = filterMode == AppFilterMode.BLOCKED,
                    onClick = { filterMode = AppFilterMode.BLOCKED },
                    label = { Text("Запрещено ($blockedCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0066FF),
                        selectedLabelColor = Color.White,
                        containerColor = if (dark) Color(0xFF222227) else Color(0xFFEBEBF0),
                        labelColor = subTextColor,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bulk actions styled as OriginOS pills with icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0066FF).copy(alpha = if (dark) 0.18f else 0.1f))
                        .clickable {
                            ignored.removeAll(filteredApps.map { it.pkg }.toSet())
                            prefs.edit().putStringSet("cast_ignored_apps", ignored.toSet()).commit()
                            NotificationCastListener.onAppFilterChanged(context, null, true)
                            NotificationCastListener.notifySettingsChanged(context)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF0066FF),
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = "Включить все",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0066FF),
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = if (dark) 0.18f else 0.1f))
                        .clickable {
                            filteredApps.forEach { if (it.pkg !in ignored) ignored.add(it.pkg) }
                            prefs.edit().putStringSet("cast_ignored_apps", ignored.toSet()).commit()
                            NotificationCastListener.onAppFilterChanged(context, null, false)
                            NotificationCastListener.notifySettingsChanged(context)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.NotInterested,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = "Отключить все",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444),
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor, thickness = 0.8.dp)

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color(0xFF0066FF))
                }
            } else if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (query.isNotBlank()) "Приложения по запросу «$query» не найдены" else "В этом списке нет приложений",
                        fontSize = 13.5.sp,
                        color = subTextColor,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 280.dp, max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredApps, key = { it.pkg }) { app ->
                        val isAllowed = app.pkg !in ignored
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(iconContainerBg)
                                        .border(0.8.dp, cardBorder, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (app.icon != null) {
                                        Image(
                                            bitmap = app.icon,
                                            contentDescription = app.label,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                        )
                                    } else {
                                        Text(
                                            text = app.label.take(1).uppercase(),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0066FF),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.label,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = app.pkg,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = subTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                OriginOSSwitch(
                                    checked = isAllowed,
                                    onCheckedChange = { allowed ->
                                        if (allowed) {
                                            ignored.remove(app.pkg)
                                        } else {
                                            if (app.pkg !in ignored) ignored.add(app.pkg)
                                        }
                                        prefs.edit().putStringSet("cast_ignored_apps", ignored.toSet()).commit()
                                        NotificationCastListener.onAppFilterChanged(context, app.pkg, allowed)
                                        NotificationCastListener.notifySettingsChanged(context)
                                    },
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
