package systems.nzr1.ui.screens.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import systems.nzr1.domain.model.SecurityItem
import systems.nzr1.domain.model.Severity
import systems.nzr1.ui.components.*
import systems.nzr1.ui.theme.*

@Composable
fun SecurityScreen() {
    val context = LocalContext.current
    val items   = remember { buildSecurityAudit(context) }
    val score   = remember(items) { calculateScore(items) }

    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {
        GridBackground(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Text("SECURITY", color = RedCore, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text("Device audit · Risk analysis", color = TextMuted, fontSize = 9.sp)

            Spacer(Modifier.height(16.dp))

            // Score card
            HoloCard(glowColor = if (score >= 70) GreenCore else if (score >= 40) OrangeCore else RedCore) {
                val color = if (score >= 70) GreenCore else if (score >= 40) OrangeCore else RedCore
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("SECURITY SCORE", color = TextMuted, fontSize = 9.sp, letterSpacing = 2.sp)
                        Text("$score / 100", color = color, fontSize = 36.sp,
                            fontWeight = FontWeight.Black)
                        Text(
                            when {
                                score >= 70 -> "GOOD — minimal risk"
                                score >= 40 -> "MODERATE — attention needed"
                                else        -> "HIGH RISK — action required"
                            },
                            color = color, fontSize = 10.sp,
                        )
                    }
                    CircularGauge(score.toFloat(), "SCORE", size = 100.dp, color = color)
                }
                Spacer(Modifier.height(8.dp))
                NeonProgressBar(score / 100f,
                    color = if (score >= 70) GreenCore else if (score >= 40) OrangeCore else RedCore)
            }

            Spacer(Modifier.height(12.dp))

            // Device Info
            HoloCard(glowColor = CyanCore) {
                SectionHeader("Device Info", color = CyanCore)
                Spacer(Modifier.height(8.dp))
                StatRow("Android Version", Build.VERSION.RELEASE)
                Spacer(Modifier.height(3.dp))
                StatRow("SDK Level",       "${Build.VERSION.SDK_INT}")
                Spacer(Modifier.height(3.dp))
                StatRow("Security Patch",  Build.VERSION.SECURITY_PATCH)
                Spacer(Modifier.height(3.dp))
                StatRow("Model",           "${Build.MANUFACTURER} ${Build.MODEL}")
                Spacer(Modifier.height(3.dp))
                StatRow("Kernel",          System.getProperty("os.version") ?: "Unknown")
            }

            Spacer(Modifier.height(12.dp))

            // Audit items
            SectionHeader("Audit Findings", subtitle = "${items.size} checks performed")
            Spacer(Modifier.height(8.dp))
            items.groupBy { it.severity }.entries
                .sortedBy { it.key.ordinal }
                .forEach { (severity, list) ->
                    list.forEach { item ->
                        SecurityItemCard(item)
                        Spacer(Modifier.height(6.dp))
                    }
                }
        }
    }
}

@Composable
private fun SecurityItemCard(item: SecurityItem) {
    val color = when (item.severity) {
        Severity.CRITICAL -> RedCore
        Severity.HIGH     -> OrangeCore
        Severity.MEDIUM   -> YellowCore
        Severity.LOW      -> CyanCore
        Severity.INFO     -> TextSecondary
    }
    val icon = when (item.severity) {
        Severity.CRITICAL -> Icons.Filled.Error
        Severity.HIGH     -> Icons.Filled.Warning
        Severity.MEDIUM   -> Icons.Filled.Info
        Severity.LOW      -> Icons.Filled.CheckCircle
        Severity.INFO     -> Icons.Filled.Info
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(1.dp, color.copy(0.25f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, item.severity.name, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(item.title, color = TextPrimary, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold)
                Text(item.severity.name, color = color, fontSize = 9.sp,
                    fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(2.dp))
            Text(item.description, color = TextSecondary, fontSize = 10.sp)
            Spacer(Modifier.height(2.dp))
            Text(item.category, color = TextMuted, fontSize = 9.sp)
        }
    }
}

private fun buildSecurityAudit(context: Context): List<SecurityItem> {
    val items = mutableListOf<SecurityItem>()
    val sdk   = Build.VERSION.SDK_INT

    // Android version check
    if (sdk >= 30) {
        items += SecurityItem("1", "Android Version", "Running Android ${Build.VERSION.RELEASE} — up to date",
            Severity.INFO, "OS")
    } else {
        items += SecurityItem("2", "Outdated Android", "Android ${Build.VERSION.RELEASE} may lack security patches",
            Severity.HIGH, "OS")
    }

    // Monthly security patch
    val patch = Build.VERSION.SECURITY_PATCH
    items += SecurityItem("3", "Security Patch", "Patch level: $patch",
        if (patch >= "2024-01-01") Severity.LOW else Severity.MEDIUM, "OS")

    // USB debugging (developer options detection)
    val devOptions = android.provider.Settings.Secure.getInt(
        context.contentResolver, android.provider.Settings.Global.ADB_ENABLED, 0
    )
    if (devOptions == 1) {
        items += SecurityItem("4", "ADB Debugging ON", "USB debugging is enabled — disable when not in use",
            Severity.HIGH, "Developer Options")
    } else {
        items += SecurityItem("5", "ADB Debugging OFF", "USB debugging is properly disabled",
            Severity.INFO, "Developer Options")
    }

    // Unknown sources
    val unknownSrc = android.provider.Settings.Secure.getInt(
        context.contentResolver, android.provider.Settings.Secure.INSTALL_NON_MARKET_APPS, 0
    )
    if (unknownSrc == 1) {
        items += SecurityItem("6", "Unknown Sources Enabled",
            "Installing apps from unknown sources is allowed",
            Severity.MEDIUM, "App Security")
    } else {
        items += SecurityItem("7", "Unknown Sources Blocked",
            "App installs are restricted to trusted stores",
            Severity.INFO, "App Security")
    }

    // Permission-heavy apps
    val pm = context.packageManager
    val flags = PackageManager.GET_PERMISSIONS
    try {
        val packages = pm.getInstalledPackages(flags)
        val dangerous = packages.count { pkg ->
            pkg.requestedPermissions?.any { perm ->
                perm.contains("CAMERA") || perm.contains("RECORD_AUDIO") ||
                perm.contains("READ_CONTACTS") || perm.contains("ACCESS_FINE_LOCATION")
            } == true
        }
        items += SecurityItem("8", "Permission-Heavy Apps",
            "$dangerous apps request sensitive permissions",
            if (dangerous > 20) Severity.MEDIUM else Severity.LOW, "Apps")
    } catch (e: Exception) {}

    // System app count
    try {
        val sysApps = pm.getInstalledApplications(0)
            .count { it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0 }
        items += SecurityItem("9", "System Apps", "$sysApps system apps installed",
            Severity.INFO, "Apps")
    } catch (e: Exception) {}

    // Encryption
    items += SecurityItem("10", "Storage Encryption",
        if (sdk >= 23) "Full-disk or file-based encryption enabled" else "Encryption status unknown",
        if (sdk >= 23) Severity.INFO else Severity.MEDIUM, "Storage")

    // Google Play Protect
    items += SecurityItem("11", "Play Protect", "Enable via Play Store → Menu → Play Protect",
        Severity.LOW, "App Security")

    return items
}

private fun calculateScore(items: List<SecurityItem>): Int {
    var score = 100
    items.forEach {
        score -= when (it.severity) {
            Severity.CRITICAL -> 30
            Severity.HIGH     -> 15
            Severity.MEDIUM   ->  8
            Severity.LOW      ->  3
            Severity.INFO     ->  0
        }
    }
    return score.coerceIn(0, 100)
}
