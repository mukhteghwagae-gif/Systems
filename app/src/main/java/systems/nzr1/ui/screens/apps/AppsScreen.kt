package systems.nzr1.ui.screens.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import systems.nzr1.domain.model.InstalledApp
import systems.nzr1.ui.components.GridBackground
import systems.nzr1.ui.components.NeonProgressBar
import systems.nzr1.ui.components.SectionHeader
import systems.nzr1.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun AppsScreen() {
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()
    var apps     by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var loading  by remember { mutableStateOf(true) }
    var search   by remember { mutableStateOf("") }
    var filter   by remember { mutableStateOf(AppFilter.ALL) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            apps = loadApps(context)
            loading = false
        }
    }

    val filtered = apps.filter { app ->
        (filter == AppFilter.ALL ||
         (filter == AppFilter.USER && !app.isSystemApp) ||
         (filter == AppFilter.SYSTEM && app.isSystemApp)) &&
        (search.isBlank() || app.name.contains(search, ignoreCase = true) ||
         app.packageName.contains(search, ignoreCase = true))
    }

    Box(Modifier.fillMaxSize().background(DeepVoid)) {
        GridBackground(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().padding(bottom = 80.dp)) {

            Column(Modifier.padding(16.dp)) {
                Text("APPS", color = GreenCore, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text("${apps.size} installed · ${apps.count { !it.isSystemApp }} user apps", color = TextMuted, fontSize = 9.sp)
            }

            // Search
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search apps…", color = TextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = GreenCore, modifier = Modifier.size(18.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenCore, unfocusedBorderColor = BorderDim,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard, unfocusedContainerColor = SurfaceCard,
                ),
                singleLine = true, shape = RoundedCornerShape(10.dp),
            )

            Spacer(Modifier.height(8.dp))

            // Filter chips
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppFilter.values().forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick  = { filter = f },
                        label    = { Text(f.label, fontSize = 10.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenCore.copy(0.2f),
                            selectedLabelColor     = GreenCore,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenCore)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filtered) { app -> AppRow(app) }
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: InstalledApp) {
    val fmt = SimpleDateFormat("dd MMM yyyy")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(1.dp, if (app.isSystemApp) BorderDim else GreenCore.copy(0.1f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                .background(if (app.isSystemApp) SurfaceHover else GreenCore.copy(0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Android, "app",
                tint = if (app.isSystemApp) TextMuted else GreenCore,
                modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(app.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(app.packageName, color = TextMuted, fontSize = 9.sp, maxLines = 1)
            Text("v${app.versionName} · ${formatSize(app.sizeBytes)}", color = TextMuted, fontSize = 9.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            if (app.isSystemApp) {
                Text("SYS", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Text("SDK ${app.targetSdk}", color = TextMuted, fontSize = 9.sp)
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 * 1024   -> "${bytes / 1024} KB"
    else                  -> "${"%.1f".format(bytes / (1024f * 1024))} MB"
}

private fun loadApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    return pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        .map { pkg ->
            val ai   = pkg.applicationInfo
            val name = pm.getApplicationLabel(ai).toString()
            val isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val size = try {
                pm.getPackageSizeInfo(pkg.packageName) ?: 0L
            } catch (e: Exception) { 0L }

            InstalledApp(
                name         = name,
                packageName  = pkg.packageName,
                versionName  = pkg.versionName ?: "—",
                versionCode  = pkg.longVersionCode,
                installDate  = pkg.firstInstallTime,
                updateDate   = pkg.lastUpdateTime,
                sizeBytes    = size as Long,
                isSystemApp  = isSystem,
                permissions  = pkg.requestedPermissions?.toList() ?: emptyList(),
                targetSdk    = ai.targetSdkVersion,
            )
        }
        .sortedWith(compareBy({ it.isSystemApp }, { it.name.lowercase() }))
}

@Suppress("DEPRECATION")
private fun PackageManager.getPackageSizeInfo(pkg: String): Long? = try {
    val ai = getApplicationInfo(pkg, 0)
    java.io.File(ai.sourceDir).length()
} catch (e: Exception) { null }

enum class AppFilter(val label: String) { ALL("All"), USER("User"), SYSTEM("System") }
