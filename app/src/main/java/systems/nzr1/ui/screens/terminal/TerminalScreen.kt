package systems.nzr1.ui.screens.terminal

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import systems.nzr1.ui.theme.*

@Composable
fun TerminalScreen(viewModel: TerminalViewModel = hiltViewModel()) {
    val lines    by viewModel.lines.collectAsState()
    val workDir  by viewModel.workDir.collectAsState()
    var input    by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope    = rememberCoroutineScope()

    val blink by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 0f, targetValue = 1f, label = "b",
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse)
    )

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020205))
            .padding(bottom = 80.dp)
    ) {
        // Title bar
        Row(
            Modifier.fillMaxWidth()
                .background(SurfaceCard)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(RedCore, RoundedCornerShape(50)))
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size(8.dp).background(YellowCore, RoundedCornerShape(50)))
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size(8.dp).background(GreenCore, RoundedCornerShape(50)))
                Spacer(Modifier.width(12.dp))
                Text("SYSTEMS SHELL — NZR1", color = TextSecondary,
                    fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            IconButton(onClick = { viewModel.clear() }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Delete, "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }

        // Output
        LazyColumn(
            state   = listState,
            modifier = Modifier.weight(1f).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            items(lines) { line ->
                val color = when (line.type) {
                    LineType.INPUT  -> CyanCore
                    LineType.OUTPUT -> GreenCore.copy(0.9f)
                    LineType.ERROR  -> RedCore
                    LineType.SYSTEM -> TextSecondary
                }
                Text(
                    text       = line.text,
                    color      = color,
                    fontSize   = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp,
                )
            }
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text       = "${workDir.take(20)} > ",
                color      = CyanCore,
                fontSize   = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            )
            BasicTextField(
                value         = input,
                onValueChange = { input = it },
                modifier      = Modifier.weight(1f),
                textStyle     = androidx.compose.ui.text.TextStyle(
                    color      = GreenCore,
                    fontSize   = 12.sp,
                    fontFamily = FontFamily.Monospace,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.execute(input); input = ""
                }),
                singleLine      = true,
                cursorBrush     = SolidColor(CyanCore),
            )
            Text("█", color = CyanCore.copy(alpha = blink), fontSize = 12.sp,
                fontFamily = FontFamily.Monospace)
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick  = { viewModel.execute(input); input = "" },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(Icons.Filled.Send, "Run", tint = CyanCore, modifier = Modifier.size(18.dp))
            }
        }
    }
}
