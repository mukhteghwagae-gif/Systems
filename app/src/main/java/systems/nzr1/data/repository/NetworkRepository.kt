package systems.nzr1.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import systems.nzr1.domain.model.NetworkDevice
import systems.nzr1.domain.model.WifiInfo
import java.net.InetAddress
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val wifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    private val connManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun getWifiInfo(): WifiInfo {
        val info = wifiManager.connectionInfo ?: return WifiInfo()
        val dhcp = wifiManager.dhcpInfo
        val ip   = formatIp(info.ipAddress)
        val gw   = formatIp(dhcp.gateway)
        val rssi = WifiManager.calculateSignalLevel(info.rssi, 100)
        val freq = info.frequency
        return WifiInfo(
            ssid          = info.ssid.removeSurrounding("\""),
            bssid         = info.bssid ?: "Unknown",
            ipAddress     = ip,
            gatewayIp     = gw,
            signalStrength = rssi,
            frequency     = freq,
            linkSpeed     = info.linkSpeed,
            channel       = frequencyToChannel(freq),
            securityType  = "WPA2",
        )
    }

    suspend fun scanNetwork(onProgress: (Int, Int) -> Unit = { _, _ -> }): List<NetworkDevice> =
        withContext(Dispatchers.IO) {
            val subnet = getSubnet() ?: return@withContext emptyList()
            val devices = mutableListOf<NetworkDevice>()
            val total = 254
            var scanned = 0

            (1..254).map { i ->
                async {
                    val host = "$subnet.$i"
                    try {
                        val addr    = InetAddress.getByName(host)
                        val reachable = addr.isReachable(800)
                        scanned++
                        onProgress(scanned, total)
                        if (reachable) {
                            val hostname = try { addr.canonicalHostName } catch (e: Exception) { host }
                            NetworkDevice(
                                ip       = host,
                                hostname = hostname,
                                mac      = getMacFromArp(host),
                                latencyMs = pingMs(host),
                            )
                        } else null
                    } catch (e: Exception) { null }
                }
            }.awaitAll().filterNotNull().sortedBy {
                it.ip.substringAfterLast(".").toIntOrNull() ?: 0
            }.also { devices.addAll(it) }

            devices
        }

    fun isInternetAvailable(): Boolean {
        val network = connManager.activeNetwork ?: return false
        val caps    = connManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun getPublicIp(): String {
        return try {
            NetworkInterface.getNetworkInterfaces()?.toList()
                ?.flatMap { it.inetAddresses.toList() }
                ?.firstOrNull { !it.isLoopbackAddress && it.hostAddress?.contains(':') == false }
                ?.hostAddress ?: "Unknown"
        } catch (e: Exception) { "Unknown" }
    }

    private fun getSubnet(): String? {
        return try {
            val dhcp = wifiManager.dhcpInfo
            val ip   = formatIp(dhcp.ipAddress)
            ip.substringBeforeLast(".")
        } catch (e: Exception) { null }
    }

    private fun formatIp(ip: Int): String {
        return "${ip and 0xff}.${ip shr 8 and 0xff}.${ip shr 16 and 0xff}.${ip shr 24 and 0xff}"
    }

    private fun frequencyToChannel(freq: Int): Int = when {
        freq in 2412..2484 -> (freq - 2412) / 5 + 1
        freq in 5170..5825 -> (freq - 5170) / 5 + 34
        else               -> 0
    }

    private fun getMacFromArp(ip: String): String {
        return try {
            val f = java.io.File("/proc/net/arp")
            f.readLines().firstOrNull { it.contains(ip) }
                ?.split("\\s+".toRegex())?.getOrNull(3) ?: "Unknown"
        } catch (e: Exception) { "Unknown" }
    }

    private fun pingMs(host: String): Long {
        val start = System.currentTimeMillis()
        return try {
            InetAddress.getByName(host).isReachable(800)
            System.currentTimeMillis() - start
        } catch (e: Exception) { -1L }
    }
}
