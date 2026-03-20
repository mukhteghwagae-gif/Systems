package systems.nzr1.domain.model

// ── System Stats ─────────────────────────────────────────────────────────────
data class SystemStats(
    val cpuUsagePercent: Float       = 0f,
    val cpuCores: Int                = 0,
    val cpuFreqMHz: Long             = 0L,
    val ramUsedMB: Long              = 0L,
    val ramTotalMB: Long             = 0L,
    val ramUsagePercent: Float       = 0f,
    val storageUsedGB: Float         = 0f,
    val storageTotalGB: Float        = 0f,
    val storagePercent: Float        = 0f,
    val batteryPercent: Int          = 0,
    val batteryCharging: Boolean     = false,
    val batteryTempC: Float          = 0f,
    val batteryVoltage: Float        = 0f,
    val uptimeSeconds: Long          = 0L,
    val cpuHistory: List<Float>      = emptyList(),
    val ramHistory: List<Float>      = emptyList(),
    val networkRxKbps: Float         = 0f,
    val networkTxKbps: Float         = 0f,
    val networkHistory: List<Float>  = emptyList(),
)

// ── Network Device ────────────────────────────────────────────────────────────
data class NetworkDevice(
    val ip: String,
    val mac: String         = "Unknown",
    val hostname: String    = "Unknown",
    val vendor: String      = "Unknown",
    val isOnline: Boolean   = true,
    val latencyMs: Long     = 0L,
    val lastSeen: Long      = System.currentTimeMillis(),
)

// ── WiFi Info ─────────────────────────────────────────────────────────────────
data class WifiInfo(
    val ssid: String          = "Unknown",
    val bssid: String         = "Unknown",
    val ipAddress: String     = "0.0.0.0",
    val gatewayIp: String     = "0.0.0.0",
    val signalStrength: Int   = 0,
    val frequency: Int        = 0,
    val linkSpeed: Int        = 0,
    val channel: Int          = 0,
    val securityType: String  = "Unknown",
)

// ── Installed App ─────────────────────────────────────────────────────────────
data class InstalledApp(
    val name: String,
    val packageName: String,
    val versionName: String      = "",
    val versionCode: Long        = 0L,
    val installDate: Long        = 0L,
    val updateDate: Long         = 0L,
    val sizeBytes: Long          = 0L,
    val isSystemApp: Boolean     = false,
    val permissions: List<String> = emptyList(),
    val targetSdk: Int           = 0,
)

// ── Security Item ─────────────────────────────────────────────────────────────
data class SecurityItem(
    val id: String,
    val title: String,
    val description: String,
    val severity: Severity,
    val category: String,
    val resolved: Boolean = false,
)

enum class Severity { CRITICAL, HIGH, MEDIUM, LOW, INFO }

// ── AI Message ────────────────────────────────────────────────────────────────
data class ChatMessage(
    val id: String           = java.util.UUID.randomUUID().toString(),
    val content: String,
    val role: MessageRole,
    val timestamp: Long      = System.currentTimeMillis(),
    val isError: Boolean     = false,
)

enum class MessageRole { USER, ASSISTANT, SYSTEM }
