package systems.nzr1.ui.screens.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import systems.nzr1.data.local.dao.TerminalDao
import systems.nzr1.data.local.entity.TerminalCommandEntity
import javax.inject.Inject

data class TerminalLine(
    val text: String,
    val type: LineType,
)
enum class LineType { INPUT, OUTPUT, ERROR, SYSTEM }

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val dao: TerminalDao,
) : ViewModel() {

    private val _lines  = MutableStateFlow<List<TerminalLine>>(listOf(
        TerminalLine("SYSTEMS NZR1 Terminal v1.0", LineType.SYSTEM),
        TerminalLine("Type 'help' for commands.", LineType.SYSTEM),
        TerminalLine("", LineType.SYSTEM),
    ))
    val lines: StateFlow<List<TerminalLine>> = _lines.asStateFlow()

    private val _workDir = MutableStateFlow("/")
    val workDir: StateFlow<String> = _workDir.asStateFlow()

    val history: StateFlow<List<String>> = dao.getHistory()
        .map { it.map { e -> e.command } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun execute(command: String) {
        if (command.isBlank()) return
        addLine("> $command", LineType.INPUT)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { runCommand(command.trim()) }
            if (result.first.isNotBlank()) addLine(result.first, LineType.OUTPUT)
            if (result.second.isNotBlank()) addLine(result.second, LineType.ERROR)
            dao.insert(TerminalCommandEntity(
                command  = command,
                output   = result.first,
                exitCode = if (result.second.isBlank()) 0 else 1,
            ))
        }
    }

    fun clear() { _lines.value = listOf(TerminalLine("Screen cleared.", LineType.SYSTEM)) }

    private fun addLine(text: String, type: LineType) {
        _lines.update { it + TerminalLine(text, type) }
    }

    private suspend fun runCommand(cmd: String): Pair<String, String> {
        val parts = cmd.split("\\s+".toRegex())
        return when (parts[0]) {
            "help"    -> helpText() to ""
            "clear"   -> { clear(); "" to "" }
            "pwd"     -> _workDir.value to ""
            "cd"      -> handleCd(parts) to ""
            "ls"      -> handleLs(parts) to ""
            "cat"     -> handleCat(parts) to ""
            "echo"    -> parts.drop(1).joinToString(" ") to ""
            "uname"   -> "Linux ${System.getProperty("os.version")} Android" to ""
            "whoami"  -> "u0_a${android.os.Process.myUid()}" to ""
            "date"    -> java.util.Date().toString() to ""
            "uptime"  -> "up ${android.os.SystemClock.elapsedRealtime() / 1000}s" to ""
            "id"      -> "uid=${android.os.Process.myUid()} gid=${android.os.Process.myPid()}" to ""
            "env"     -> System.getenv().entries.take(30).joinToString("\n") { "${it.key}=${it.value}" } to ""
            "ps"      -> "PID=${android.os.Process.myPid()} UID=${android.os.Process.myUid()} systems.nzr1" to ""
            "free"    -> handleFree() to ""
            "df"      -> handleDf() to ""
            "netstat" -> "Not available without root" to ""
            else      -> executeShell(cmd)
        }
    }

    private suspend fun executeShell(cmd: String): Pair<String, String> =
        withContext(Dispatchers.IO) {
            try {
                val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
                val out  = proc.inputStream.bufferedReader().readText()
                val err  = proc.errorStream.bufferedReader().readText()
                proc.waitFor()
                out.trim() to err.trim()
            } catch (e: Exception) {
                "" to "Error: ${e.message}"
            }
        }

    private fun handleCd(parts: List<String>): String {
        val target = parts.getOrNull(1) ?: "/"
        val dir    = if (target.startsWith("/")) java.io.File(target)
                     else java.io.File("${_workDir.value}/$target")
        return if (dir.exists() && dir.isDirectory) {
            _workDir.value = dir.canonicalPath; ""
        } else "cd: $target: No such directory"
    }

    private fun handleLs(parts: List<String>): String {
        val path   = parts.getOrNull(1) ?: _workDir.value
        val dir    = java.io.File(path)
        return if (!dir.exists()) "ls: $path: Not found"
        else dir.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            ?.joinToString("  ") { if (it.isDirectory) "${it.name}/" else it.name }
            ?: "Permission denied"
    }

    private fun handleCat(parts: List<String>): String {
        val path = parts.getOrNull(1) ?: return "Usage: cat <file>"
        val file = java.io.File(if (path.startsWith("/")) path else "${_workDir.value}/$path")
        return try {
            if (file.length() > 50_000) "[File too large — ${file.length()} bytes]"
            else file.readText()
        } catch (e: Exception) { "cat: ${e.message}" }
    }

    private fun handleFree(): String {
        val am   = android.app.ActivityManager.MemoryInfo()
        // Rough approximation without context
        val rt   = Runtime.getRuntime()
        val max  = rt.maxMemory() / 1024
        val used = (rt.totalMemory() - rt.freeMemory()) / 1024
        return "              total        used        free\nMem:   %8d    %8d    %8d".format(max, used, max - used)
    }

    private fun handleDf(): String {
        val stat  = android.os.StatFs(android.os.Environment.getDataDirectory().path)
        val total = stat.totalBytes / (1024 * 1024)
        val free  = stat.availableBytes / (1024 * 1024)
        return "Filesystem     1M-blocks    Used   Avail\n/data          %8d  %6d  %6d".format(total, total - free, free)
    }

    private fun helpText() = """
Available commands:
  ls [path]       - List directory
  cd <path>       - Change directory
  pwd             - Current directory
  cat <file>      - Read file
  echo <text>     - Print text
  free            - Memory info
  df              - Disk info
  uname           - System info
  whoami          - Current user
  id              - User/group IDs
  date            - Current date
  uptime          - System uptime
  ps              - Process info
  env             - Environment
  clear           - Clear screen
  <any sh cmd>    - Execute shell
""".trimIndent()
}
