package systems.nzr1.ui.screens.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import systems.nzr1.ui.components.GridBackground
import systems.nzr1.ui.components.HoloCard
import systems.nzr1.ui.components.SectionHeader
import systems.nzr1.ui.components.StatRow
import systems.nzr1.ui.theme.*
import android.os.Build

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val prefs by viewModel.prefs.collectAsState()

    Box(Modifier.fillMaxSize().background(DeepVoid)) {
        GridBackground(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Text("SETTINGS", color = CyanCore, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text("App configuration", color = TextMuted, fontSize = 9.sp)

            Spacer(Modifier.height(16.dp))

            // Display
            HoloCard(glowColor = CyanCore) {
                SectionHeader("Display", color = CyanCore)
                Spacer(Modifier.height(12.dp))
                ToggleRow("Show Grid Background", prefs.showGrid) {
                    viewModel.setShowGrid(it)
                }
                Spacer(Modifier.height(8.dp))
                ToggleRow("Boot Animation", prefs.bootAnimation) {
                    viewModel.setBootAnimation(it)
                }
                Spacer(Modifier.height(8.dp))
                ToggleRow("Haptic Feedback", prefs.haptics) {
                    viewModel.setHaptics(it)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Performance
            HoloCard(glowColor = PurpleCore) {
                SectionHeader("Performance", color = PurpleCore)
                Spacer(Modifier.height(12.dp))
                Text("Monitor Refresh Interval", color = TextSecondary, fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(500L to "0.5s", 1000L to "1s", 2000L to "2s", 5000L to "5s").forEach { (ms, label) ->
                        val selected = prefs.monitorInterval == ms
                        OutlinedButton(
                            onClick = { viewModel.setMonitorInterval(ms) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) PurpleCore.copy(0.2f) else SurfaceCard,
                                contentColor = if (selected) PurpleCore else TextMuted,
                            ),
                            border = BorderStroke(1.dp, if (selected) PurpleCore else BorderDim),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(label, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // AI
            HoloCard(glowColor = PinkCore) {
                SectionHeader("AI Assistant", color = PinkCore)
                Spacer(Modifier.height(8.dp))
                Text("API key is stored securely in DataStore.", color = TextMuted, fontSize = 10.sp)
                Spacer(Modifier.height(4.dp))
                StatRow("Provider", "Anthropic Claude")
                Spacer(Modifier.height(4.dp))
                StatRow("Model",    "claude-3-haiku-20240307")
                Spacer(Modifier.height(4.dp))
                StatRow("Key Status", if (prefs.apiKeySet) "● SET" else "○ NOT SET",
                    valueColor = if (prefs.apiKeySet) GreenCore else OrangeCore)
            }

            Spacer(Modifier.height(12.dp))

            // About
            HoloCard(glowColor = TextMuted) {
                SectionHeader("About", color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                StatRow("App",       "Systems NZR1")
                Spacer(Modifier.height(3.dp))
                StatRow("Version",   "1.0.0")
                Spacer(Modifier.height(3.dp))
                StatRow("Package",   "systems.nzr1")
                Spacer(Modifier.height(3.dp))
                StatRow("Android",   Build.VERSION.RELEASE)
                Spacer(Modifier.height(3.dp))
                StatRow("SDK",       "${Build.VERSION.SDK_INT}")
                Spacer(Modifier.height(3.dp))
                StatRow("Device",    "${Build.MANUFACTURER} ${Build.MODEL}")
                Spacer(Modifier.height(3.dp))
                StatRow("Build",     Build.TYPE)
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(label, color = TextPrimary, fontSize = 12.sp)
        Switch(
            checked = value, onCheckedChange = onChange,
            colors  = SwitchDefaults.colors(
                checkedThumbColor  = CyanCore,
                checkedTrackColor  = CyanCore.copy(0.3f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceHover,
            )
        )
    }
}
