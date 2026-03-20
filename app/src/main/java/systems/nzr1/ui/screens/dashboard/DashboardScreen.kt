package systems.nzr1.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import systems.nzr1.ui.components.*
import systems.nzr1.ui.navigation.Screen
import systems.nzr1.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val stats       by viewModel.stats.collectAsState()
    val wifi        by viewModel.wifiInfo.collectAsState()
    val internet    by viewModel.internetAvailable.collectAsState()
    val time        by viewModel.currentTime.collectAsState()
    val date        by viewModel.currentDate.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {
        GridBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 80.dp),
        ) {
            // ── Header ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text("SYSTEMS", color = CyanCore, fontSize = 22.sp,
                        fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                    Text("COMMAND CENTER", color = TextMuted, fontSize = 9.sp,
                        letterSpacing = 2.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(time, color = CyanCore, fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text(date, color = TextMuted, fontSize = 9.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Status Row ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusChip("ONLINE", GreenCore, Modifier.weight(1f))
                StatusChip(wifi.ssid.take(12), CyanCore, Modifier.weight(1f))
                StatusChip(
                    if (internet) "INTERNET" else "NO NET",
                    if (internet) GreenCore else RedCore,
                    Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Circular Gauges ───────────────────────────────────────
            SectionHeader("System Vitals")
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CircularGauge(
                    value = stats.cpuUsagePercent,
                    label = "CPU",
                    color = CyanCore,
                    size  = 100.dp,
                )
                CircularGauge(
                    value = stats.ramUsagePercent,
                    label = "RAM",
                    color = PurpleCore,
                    size  = 100.dp,
                )
                CircularGauge(
                    value = stats.storagePercent,
                    label = "DISK",
                    color = PinkCore,
                    size  = 100.dp,
                )
                CircularGauge(
                    value = stats.batteryPercent.toFloat(),
                    label = "BATT",
                    color = if (stats.batteryPercent > 20) GreenCore else RedCore,
                    size  = 100.dp,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Quick Stats ───────────────────────────────────────────
            HoloCard(glowColor = CyanCore) {
                SectionHeader("Quick Stats", subtitle = "Real-time snapshot")
                Spacer(Modifier.height(12.dp))
                StatRow("CPU Cores", "${stats.cpuCores} cores")
                Spacer(Modifier.height(4.dp))
                StatRow("CPU Frequency", "${stats.cpuFreqMHz} MHz")
                Spacer(Modifier.height(4.dp))
                StatRow("RAM Used", "${stats.ramUsedMB} / ${stats.ramTotalMB} MB")
                Spacer(Modifier.height(4.dp))
                StatRow("Storage", "${"%.1f".format(stats.storageUsedGB)} / ${"%.1f".format(stats.storageTotalGB)} GB")
                Spacer(Modifier.height(4.dp))
                StatRow("Battery Temp", "${stats.batteryTempC}°C")
                Spacer(Modifier.height(4.dp))
                StatRow("Uptime", formatUptime(stats.uptimeSeconds))
                Spacer(Modifier.height(4.dp))
                StatRow("Net RX", "${"%.1f".format(stats.networkRxKbps)} KB/s")
                Spacer(Modifier.height(4.dp))
                StatRow("Net TX", "${"%.1f".format(stats.networkTxKbps)} KB/s")
            }

            Spacer(Modifier.height(12.dp))

            // ── CPU History Sparkline ─────────────────────────────────
            HoloCard(glowColor = PurpleCore) {
                SectionHeader("CPU Activity", color = PurpleCore)
                Spacer(Modifier.height(8.dp))
                SparkLine(
                    data     = stats.cpuHistory,
                    color    = CyanCore,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text("${stats.cpuUsagePercent.roundToInt()}% current usage",
                    color = TextMuted, fontSize = 10.sp)
            }

            Spacer(Modifier.height(12.dp))

            // ── WiFi Info ─────────────────────────────────────────────
            HoloCard(glowColor = CyanCore) {
                SectionHeader("WiFi", color = CyanCore)
                Spacer(Modifier.height(12.dp))
                StatRow("SSID",    wifi.ssid)
                Spacer(Modifier.height(4.dp))
                StatRow("IP",      wifi.ipAddress)
                Spacer(Modifier.height(4.dp))
                StatRow("Gateway", wifi.gatewayIp)
                Spacer(Modifier.height(4.dp))
                StatRow("Signal",  "${wifi.signalStrength}%")
                Spacer(Modifier.height(4.dp))
                StatRow("Speed",   "${wifi.linkSpeed} Mbps")
                Spacer(Modifier.height(4.dp))
                StatRow("Channel", "Ch ${wifi.channel} · ${wifi.frequency} MHz")
                Spacer(Modifier.height(8.dp))
                NeonProgressBar(value = wifi.signalStrength / 100f, color = CyanCore)
            }

            Spacer(Modifier.height(12.dp))

            // ── Quick Launch ──────────────────────────────────────────
            SectionHeader("Quick Launch")
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val items = listOf(
                    Triple("Monitor",  Icons.Filled.Memory,    Screen.Monitor),
                    Triple("Network",  Icons.Filled.Wifi,      Screen.Network),
                    Triple("Security", Icons.Filled.Security,  Screen.Security),
                    Triple("AI",       Icons.Filled.SmartToy,  Screen.Assistant),
                    Triple("Files",    Icons.Filled.Folder,    Screen.Files),
                    Triple("Apps",     Icons.Filled.Apps,      Screen.Apps),
                    Triple("Terminal", Icons.Filled.Terminal,  Screen.Terminal),
                    Triple("Settings", Icons.Filled.Settings,  Screen.Settings),
                )
                items(items.size) { i ->
                    val (label, icon, screen) = items[i]
                    QuickLaunchItem(label, icon) { navController.navigate(screen.route) }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(0.1f))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(color, shape = RoundedCornerShape(50))
        )
        Spacer(Modifier.width(4.dp))
        Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp, maxLines = 1)
    }
}

@Composable
private fun QuickLaunchItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ql")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f, label = "g",
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse)
    )
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, CyanCore.copy(0.2f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, label, tint = CyanCore.copy(glow), modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextSecondary, fontSize = 9.sp, letterSpacing = 0.5.sp)
    }
}

private fun formatUptime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
