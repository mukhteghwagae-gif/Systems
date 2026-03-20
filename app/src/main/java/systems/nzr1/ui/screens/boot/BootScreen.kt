package systems.nzr1.ui.screens.boot

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import systems.nzr1.ui.navigation.Screen
import systems.nzr1.ui.theme.*
import kotlin.math.*

@Composable
fun BootScreen(navController: NavController) {
    var phase by remember { mutableStateOf(0) }
    var progressText by remember { mutableStateOf("") }
    var statusLines by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }

    val bootSequence = listOf(
        "INITIALIZING KERNEL INTERFACE" to 300L,
        "LOADING HARDWARE ABSTRACTION LAYER" to 400L,
        "MOUNTING SYSTEM PARTITIONS" to 250L,
        "STARTING NETWORK SUBSYSTEM" to 350L,
        "CALIBRATING SENSOR ARRAYS" to 300L,
        "LOADING SECURITY MODULE" to 280L,
        "INITIALIZING AI ENGINE" to 450L,
        "SYSTEMS ONLINE" to 200L,
    )

    val infiniteTransition = rememberInfiniteTransition(label = "boot")
    val scanLine by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, label = "scan",
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing))
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f, label = "pulse",
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, label = "rot",
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing))
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(800), label = "title"
    )

    LaunchedEffect(Unit) {
        delay(400)
        phase = 1
        bootSequence.forEachIndexed { i, (text, duration) ->
            statusLines = statusLines + (text to false)
            delay(duration)
            statusLines = statusLines.mapIndexed { idx, pair ->
                if (idx == i) pair.first to true else pair
            }
        }
        delay(600)
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(Screen.Boot.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepVoid),
        contentAlignment = Alignment.Center
    ) {
        // Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 40f
            var x = 0f
            while (x <= size.width) {
                drawLine(GridLine, Offset(x, 0f), Offset(x, size.height), 0.5f)
                x += spacing
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(GridLine, Offset(0f, y), Offset(size.width, y), 0.5f)
                y += spacing
            }
            // Scan line
            val scanY = size.height * scanLine
            drawLine(
                brush       = Brush.horizontalGradient(listOf(Color.Transparent, CyanCore.copy(0.3f), Color.Transparent)),
                start       = Offset(0f, scanY),
                end         = Offset(size.width, scanY),
                strokeWidth = 2f,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            // Logo rings
            Canvas(modifier = Modifier.size(160.dp).alpha(titleAlpha)) {
                val cx = size.width / 2
                val cy = size.height / 2

                for (ring in listOf(70f, 55f, 38f)) {
                    val alpha = if (ring == 70f) 0.3f else if (ring == 55f) 0.5f else 0.8f
                    drawCircle(
                        color = CyanCore.copy(alpha = alpha * pulse),
                        radius = ring,
                        center = Offset(cx, cy),
                        style = Stroke(width = if (ring == 38f) 2f else 1f)
                    )
                }

                // Rotating arc
                val path = Path()
                val sweepPath = android.graphics.Path()
                drawArc(
                    color = CyanCore,
                    startAngle = rotation,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(cx - 62f, cy - 62f),
                    size = androidx.compose.ui.geometry.Size(124f, 124f),
                    style = Stroke(3f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = PurpleCore,
                    startAngle = rotation + 180f,
                    sweepAngle = 80f,
                    useCenter = false,
                    topLeft = Offset(cx - 62f, cy - 62f),
                    size = androidx.compose.ui.geometry.Size(124f, 124f),
                    style = Stroke(3f, cap = StrokeCap.Round)
                )

                // Center hexagon
                val hexPath = Path()
                val hexR = 22f
                for (i in 0..5) {
                    val angle = Math.toRadians((60.0 * i - 30))
                    val px = (cx + hexR * cos(angle)).toFloat()
                    val py = (cy + hexR * sin(angle)).toFloat()
                    if (i == 0) hexPath.moveTo(px, py) else hexPath.lineTo(px, py)
                }
                hexPath.close()
                drawPath(hexPath, color = CyanCore.copy(0.15f))
                drawPath(hexPath, color = CyanCore, style = Stroke(2f))

                // S letter
                drawCircle(color = CyanCore.copy(alpha = pulse), radius = 8f, center = Offset(cx, cy))
            }

            Spacer(Modifier.height(24.dp))

            // Title
            Text(
                text = "SYSTEMS",
                color = CyanCore,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 12.sp,
                modifier = Modifier.alpha(titleAlpha)
            )
            Text(
                text = "NZR1 · ADVANCED CONTROL INTERFACE",
                color = TextMuted,
                fontSize = 10.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(Modifier.height(48.dp))

            // Boot log
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                statusLines.forEach { (text, done) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "  > $text",
                            color = if (done) TextSecondary else CyanCore,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                        )
                        Text(
                            text = if (done) "[ OK ]" else "[....]",
                            color = if (done) GreenCore else CyanCore.copy(0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
