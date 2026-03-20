package systems.nzr1.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import systems.nzr1.domain.model.SystemStats
import java.io.BufferedReader
import java.io.FileReader
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val activityManager by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    private var prevRxBytes = 0L
    private var prevTxBytes = 0L
    private var prevSampleTime = System.currentTimeMillis()

    private val cpuHistory   = ArrayDeque<Float>(60)
    private val ramHistory   = ArrayDeque<Float>(60)
    private val netHistory   = ArrayDeque<Float>(60)

    fun statsFlow(intervalMs: Long = 1000L): Flow<SystemStats> = flow {
        while (true) {
            emit(collectStats())
            delay(intervalMs)
        }
    }

    private fun collectStats(): SystemStats {
        val cpu = getCpuUsage()
        val (ramUsed, ramTotal) = getRam()
        val (stUsed, stTotal)   = getStorage()
        val bat                 = getBattery()
        val (rx, tx)            = getNetworkSpeed()

        cpuHistory.addLast(cpu)
        if (cpuHistory.size > 60) cpuHistory.removeFirst()
        ramHistory.addLast(ramUsed.toFloat() / ramTotal.coerceAtLeast(1) * 100)
        if (ramHistory.size > 60) ramHistory.removeFirst()
        netHistory.addLast((rx + tx))
        if (netHistory.size > 60) netHistory.removeFirst()

        return SystemStats(
            cpuUsagePercent   = cpu,
            cpuCores          = Runtime.getRuntime().availableProcessors(),
            cpuFreqMHz        = getCpuFreqMHz(),
            ramUsedMB         = ramUsed,
            ramTotalMB        = ramTotal,
            ramUsagePercent   = ramUsed.toFloat() / ramTotal.coerceAtLeast(1) * 100,
            storageUsedGB     = stUsed,
            storageTotalGB    = stTotal,
            storagePercent    = if (stTotal > 0) stUsed / stTotal * 100 else 0f,
            batteryPercent    = bat.first,
            batteryCharging   = bat.second,
            batteryTempC      = bat.third,
            batteryVoltage    = bat.fourth,
            uptimeSeconds     = SystemClock.elapsedRealtime() / 1000,
            cpuHistory        = cpuHistory.toList(),
            ramHistory        = ramHistory.toList(),
            networkRxKbps     = rx,
            networkTxKbps     = tx,
            networkHistory    = netHistory.toList(),
        )
    }

    private fun getCpuUsage(): Float {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load   = reader.readLine()
            reader.close()
            val toks   = load.split("\\s+".toRegex())
            val work   = toks[1].toLong() + toks[2].toLong() + toks[3].toLong()
            val total  = work + toks[4].toLong() + toks[5].toLong() +
                         toks[6].toLong() + toks[7].toLong()
            (work * 100f / total.coerceAtLeast(1)).coerceIn(0f, 100f)
        } catch (e: Exception) { 0f }
    }

    private fun getCpuFreqMHz(): Long {
        return try {
            val f = BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"))
            val freq = f.readLine().trim().toLong() / 1000
            f.close()
            freq
        } catch (e: Exception) { 0L }
    }

    private fun getRam(): Pair<Long, Long> {
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        val total = info.totalMem / (1024 * 1024)
        val avail = info.availMem  / (1024 * 1024)
        return Pair(total - avail, total)
    }

    private fun getStorage(): Pair<Float, Float> {
        val stat  = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes.toFloat() / (1024 * 1024 * 1024)
        val free  = stat.availableBytes.toFloat() / (1024 * 1024 * 1024)
        return Pair(total - free, total)
    }

    private data class BatteryInfo(
        val percent: Int,
        val charging: Boolean,
        val tempC: Float,
        val voltage: Float,
    )

    private fun getBattery(): Triple2 {
        val intent = context.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level   = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale   = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status  = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val tempRaw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val pct     = (level * 100 / scale)
        val chg     = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                      status == BatteryManager.BATTERY_STATUS_FULL
        return Triple2(pct, chg, tempRaw / 10f, voltage / 1000f)
    }

    private fun getNetworkSpeed(): Pair<Float, Float> {
        val rx    = TrafficStats.getTotalRxBytes()
        val tx    = TrafficStats.getTotalTxBytes()
        val now   = System.currentTimeMillis()
        val dt    = (now - prevSampleTime).coerceAtLeast(1)
        val rxKbps = (rx - prevRxBytes).coerceAtLeast(0) / dt.toFloat()
        val txKbps = (tx - prevTxBytes).coerceAtLeast(0) / dt.toFloat()
        prevRxBytes    = rx
        prevTxBytes    = tx
        prevSampleTime = now
        return Pair(rxKbps, txKbps)
    }
}

// Simple 4-tuple helper
data class Triple2(val first: Int, val second: Boolean, val third: Float, val fourth: Float)
val Triple2.component1 get() = first
val Triple2.component2 get() = second
val Triple2.component3 get() = third
val Triple2.component4 get() = fourth
