package systems.nzr1.ui.screens.files

import android.os.Environment
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
import systems.nzr1.ui.components.GridBackground
import systems.nzr1.ui.components.SectionHeader
import systems.nzr1.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun FilesScreen() {
    var currentPath by remember {
        mutableStateOf(Environment.getExternalStorageDirectory().absolutePath)
    }
    val files by remember(currentPath) {
        derivedStateOf {
            try {
                File(currentPath).listFiles()
                    ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    ?: emptyList()
            } catch (e: Exception) { emptyList() }
        }
    }
    var search by remember { mutableStateOf("") }
    val filtered = if (search.isBlank()) files else files.filter {
        it.name.contains(search, ignoreCase = true)
    }

    Box(Modifier.fillMaxSize().background(DeepVoid)) {
        GridBackground(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().padding(bottom = 80.dp)) {

            // Header
            Column(Modifier.padding(16.dp)) {
                Text("FILES", color = CyanCore, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text(currentPath, color = TextMuted, fontSize = 9.sp, maxLines = 2)
            }

            // Search
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search files…", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = CyanCore, modifier = Modifier.size(18.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanCore,
                    unfocusedBorderColor = BorderDim,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                ),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )

            Spacer(Modifier.height(8.dp))

            // Path controls
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val parent = File(currentPath).parentFile
                if (parent != null) {
                    OutlinedButton(
                        onClick = { currentPath = parent.absolutePath },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanCore),
                        border = BorderStroke(1.dp, CyanCore.copy(0.3f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Icon(Icons.Filled.ArrowUpward, "Up", Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("UP", fontSize = 10.sp, letterSpacing = 1.sp)
                    }
                }
                Text("${filtered.size} items", color = TextMuted, fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterVertically))
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                items(filtered) { file ->
                    FileRow(file) {
                        if (file.isDirectory) currentPath = file.absolutePath
                    }
                }
            }
        }
    }
}

@Composable
private fun FileRow(file: File, onClick: () -> Unit) {
    val isDir    = file.isDirectory
    val icon     = when {
        isDir                           -> Icons.Filled.Folder
        file.extension in listOf("jpg","png","jpeg","gif","webp") -> Icons.Filled.Image
        file.extension in listOf("mp4","mkv","avi","mov") -> Icons.Filled.VideoFile
        file.extension in listOf("mp3","wav","flac","ogg") -> Icons.Filled.AudioFile
        file.extension in listOf("apk") -> Icons.Filled.Android
        file.extension in listOf("pdf") -> Icons.Filled.PictureAsPdf
        file.extension in listOf("zip","tar","gz") -> Icons.Filled.FolderZip
        else                            -> Icons.Filled.InsertDriveFile
    }
    val iconColor = when {
        isDir                           -> CyanCore
        file.extension == "apk"         -> GreenCore
        file.extension in listOf("jpg","png","jpeg") -> PinkCore
        else                            -> TextSecondary
    }
    val dateFmt = SimpleDateFormat("dd MMM, HH:mm")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(1.dp, if (isDir) CyanCore.copy(0.1f) else BorderDim, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, file.name, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(file.name, color = if (isDir) TextPrimary else TextSecondary,
                fontSize = 12.sp, fontWeight = if (isDir) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1)
            Text(dateFmt.format(Date(file.lastModified())), color = TextMuted, fontSize = 9.sp)
        }
        if (!isDir) {
            Text(formatSize(file.length()), color = TextMuted, fontSize = 9.sp)
        } else {
            Icon(Icons.Filled.ChevronRight, "open", tint = CyanCore.copy(0.5f), modifier = Modifier.size(16.dp))
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024          -> "${bytes} B"
    bytes < 1024 * 1024   -> "${"%.1f".format(bytes / 1024f)} KB"
    bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024f * 1024))} MB"
    else                  -> "${"%.2f".format(bytes / (1024f * 1024 * 1024))} GB"
}
