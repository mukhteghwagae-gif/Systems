package systems.nzr1.ui.screens.network

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import systems.nzr1.domain.model.NetworkDevice
import systems.nzr1.ui.components.*
import systems.nzr1.ui.theme.*

@Composable
fun NetworkScreen(viewModel: NetworkViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {
        GridBackground(Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(bottom = 80.dp)) {

            // Header
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("NETWORK", color = CyanCore, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                    Text("WiFi · Scanner · Diagnostics", color = TextMuted, fontSize = 9.sp)
                }
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Filled.Refresh, "Refresh", tint = CyanCore)
                }
            }

            Spacer(Modifier.height(12.dp))

            // WiFi card
            HoloCard(glowColor = CyanCore) {
                SectionHeader("Active Connection", color = CyanCore)
                Spacer(Modifier.height(8.dp))
                StatRow("SSID",     state.wifiInfo.ssid)
                Spacer(Modifier.height(3.dp))
                StatRow("BSSID",    state.wifiInfo.bssid)
                Spacer(Modifier.height(3.dp))
                StatRow("Local IP", state.wifiInfo.ipAddress)
                Spacer(Modifier.height(3.dp))
                StatRow("Public IP",state.publicIp)
                Spacer(Modifier.height(3.dp))
                StatRow("Gateway",  state.wifiInfo.gatewayIp)
                Spacer(Modifier.height(3.dp))
                StatRow("Speed",    "${state.wifiInfo.linkSpeed} Mbps")
                Spacer(Modifier.height(3.dp))
                StatRow("Ch / Freq","Ch ${state.wifiInfo.channel} · ${state.wifiInfo.frequency} MHz")
                Spacer(Modifier.height(3.dp))
                StatRow("Security", state.wifiInfo.securityType)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Signal: ${state.wifiInfo.signalStrength}%", color = TextSecondary, fontSize = 10.sp)
                }
                Spacer(Modifier.height(4.dp))
                NeonProgressBar(state.wifiInfo.signalStrength / 100f, color = CyanCore)
            }

            Spacer(Modifier.height(12.dp))

            // Scanner
            HoloCard(glowColor = PurpleCore) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    SectionHeader("Network Scanner", color = PurpleCore)
                    Button(
                        onClick  = { viewModel.startScan() },
                        enabled  = !state.isScanning,
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = PurpleCore.copy(0.2f),
                            contentColor   = PurpleCore,
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Icon(Icons.Filled.Search, "Scan", Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (state.isScanning) "SCANNING..." else "SCAN", fontSize = 10.sp,
                            letterSpacing = 1.sp)
                    }
                }

                if (state.isScanning) {
                    Spacer(Modifier.height(8.dp))
                    NeonProgressBar(
                        state.scanProgress.toFloat() / state.scanTotal.coerceAtLeast(1),
                        color = PurpleCore,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Scanning ${state.scanProgress} / ${state.scanTotal} hosts…",
                        color = TextMuted, fontSize = 10.sp)
                }

                if (state.devices.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("${state.devices.size} devices found", color = GreenCore,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    state.devices.forEach { device ->
                        DeviceRow(device)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(device: NetworkDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.DeviceHub, "Device",
            tint = CyanCore, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(device.ip, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(device.hostname, color = TextMuted, fontSize = 10.sp)
            if (device.mac != "Unknown") Text(device.mac, color = TextMuted, fontSize = 9.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${device.latencyMs}ms", color = GreenCore, fontSize = 10.sp)
        }
    }
}
