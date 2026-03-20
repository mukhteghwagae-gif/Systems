package systems.nzr1.ui.screens.monitor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import systems.nzr1.ui.components.*
import systems.nzr1.ui.theme.*

@Composable
fun MonitorScreen(viewModel: MonitorViewModel = hiltViewModel()) {
    val stats by viewModel.stats.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {
        GridBackground(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Text("SYSTEM MONITOR", color = CyanCore, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text("500 ms refresh · live telemetry", color = TextMuted, fontSize = 9.sp)

            Spacer(Modifier.height(16.dp))

            // CPU
            HoloCard(glowColor = CyanCore) {
                SectionHeader("CPU", color = CyanCore,
                    subtitle = "${stats.cpuCores} cores · ${stats.cpuFreqMHz} MHz")
                Spacer(Modifier.height(8.dp))
                NeonProgressBar(stats.cpuUsagePercent / 100f, color = CyanCore)
                Spacer(Modifier.height(4.dp))
                Text("Usage: ${stats.cpuUsagePercent.toInt()}%", color = TextPrimary, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                SparkLine(stats.cpuHistory, Modifier.fillMaxWidth().height(80.dp), CyanCore)
            }

            Spacer(Modifier.height(12.dp))

            // RAM
            HoloCard(glowColor = PurpleCore) {
                SectionHeader("RAM", color = PurpleCore,
                    subtitle = "${stats.ramUsedMB} MB used of ${stats.ramTotalMB} MB")
                Spacer(Modifier.height(8.dp))
                NeonProgressBar(stats.ramUsagePercent / 100f, color = PurpleCore)
                Spacer(Modifier.height(4.dp))
                Text("Usage: ${stats.ramUsagePercent.toInt()}%", color = TextPrimary, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                SparkLine(stats.ramHistory, Modifier.fillMaxWidth().height(80.dp), PurpleCore)
            }

            Spacer(Modifier.height(12.dp))

            // Storage
            HoloCard(glowColor = PinkCore) {
                SectionHeader("STORAGE", color = PinkCore,
                    subtitle = "${"%.2f".format(stats.storageUsedGB)} / ${"%.2f".format(stats.storageTotalGB)} GB")
                Spacer(Modifier.height(8.dp))
                NeonProgressBar(stats.storagePercent / 100f, color = PinkCore)
                Spacer(Modifier.height(4.dp))
                Text("Usage: ${stats.storagePercent.toInt()}%", color = TextPrimary, fontSize = 11.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Battery
            HoloCard(glowColor = if (stats.batteryPercent > 20) GreenCore else RedCore) {
                val batColor = if (stats.batteryPercent > 20) GreenCore else RedCore
                SectionHeader("BATTERY", color = batColor,
                    subtitle = if (stats.batteryCharging) "Charging" else "Discharging")
                Spacer(Modifier.height(8.dp))
                NeonProgressBar(stats.batteryPercent / 100f, color = batColor)
                Spacer(Modifier.height(4.dp))
                StatRow("Level",       "${stats.batteryPercent}%")
                Spacer(Modifier.height(4.dp))
                StatRow("Temperature", "${stats.batteryTempC}°C")
                Spacer(Modifier.height(4.dp))
                StatRow("Voltage",     "${stats.batteryVoltage} V")
            }

            Spacer(Modifier.height(12.dp))

            // Network throughput
            HoloCard(glowColor = OrangeCore) {
                SectionHeader("NETWORK I/O", color = OrangeCore)
                Spacer(Modifier.height(8.dp))
                StatRow("Download", "${"%.2f".format(stats.networkRxKbps)} KB/s", valueColor = GreenCore)
                Spacer(Modifier.height(4.dp))
                StatRow("Upload",   "${"%.2f".format(stats.networkTxKbps)} KB/s", valueColor = OrangeCore)
                Spacer(Modifier.height(8.dp))
                SparkLine(stats.networkHistory, Modifier.fillMaxWidth().height(60.dp), OrangeCore)
            }

            Spacer(Modifier.height(12.dp))

            // Uptime
            HoloCard(glowColor = YellowCore) {
                SectionHeader("UPTIME", color = YellowCore)
                Spacer(Modifier.height(8.dp))
                val h = stats.uptimeSeconds / 3600
                val m = (stats.uptimeSeconds % 3600) / 60
                val s = stats.uptimeSeconds % 60
                Text("%02d:%02d:%02d".format(h, m, s),
                    color = YellowCore, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp)
                Text("hours : minutes : seconds", color = TextMuted, fontSize = 9.sp)
            }
        }
    }
}
