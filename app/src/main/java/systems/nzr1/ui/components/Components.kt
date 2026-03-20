package systems.nzr1.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import systems.nzr1.ui.theme.*

// ── Holographic Card ─────────────────────────────────────────────────────────
@Composable
fun HoloCard(
    modifier: Modifier = Modifier,
    glowColor: Color = CyanCore,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "holo")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f, label = "glow",
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val shape = RoundedCornerShape(12.dp)
    var baseModifier = modifier
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(SurfaceCard, SurfaceElevated)
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    glowColor.copy(alpha = glowAlpha),
                    glowColor.copy(alpha = 0.1f),
                    glowColor.copy(alpha = glowAlpha * 0.5f),
                )
            ),
            shape = shape
        )
        .drawBehind {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.05f), Color.Transparent),
                    center = Offset(size.width / 2, 0f),
                    radius = size.width * 0.8f
                )
            )
        }

    if (onClick != null) baseModifier = baseModifier.clickable(onClick = onClick)

    Column(modifier = baseModifier.padding(16.dp), content = content)
}

// ── Circular Gauge ────────────────────────────────────────────────────────────
@Composable
fun CircularGauge(
    value: Float,           // 0..100
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = CyanCore,
    showValue: Boolean = true,
) {
    val animValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, 100f),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "gauge"
    )
    val sweepAngle = animValue / 100f * 270f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke  = size.toPx() * 0.1f
            val inset   = stroke / 2
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            val startAngle = 135f

            // Background arc
            drawArc(
                color      = color.copy(alpha = 0.1f),
                startAngle = startAngle,
                sweepAngle = 270f,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(stroke, cap = StrokeCap.Round)
            )
            // Filled arc
            if (sweepAngle > 0f) {
                drawArc(
                    brush      = Brush.sweepGradient(
                        listOf(color.copy(0.5f), color),
                        center = Offset(this.size.width / 2, this.size.height / 2)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(stroke, cap = StrokeCap.Round)
                )
                // Glow dot at tip
                val angle  = Math.toRadians((startAngle + sweepAngle).toDouble())
                val cx     = this.size.width / 2
                val cy     = this.size.height / 2
                val radius = (this.size.minDimension - stroke) / 2
                val dotX   = (cx + radius * Math.cos(angle)).toFloat()
                val dotY   = (cy + radius * Math.sin(angle)).toFloat()
                drawCircle(color = color, radius = stroke * 0.6f, center = Offset(dotX, dotY))
                drawCircle(color = color.copy(0.4f), radius = stroke, center = Offset(dotX, dotY))
            }
        }

        if (showValue) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text  = "${animValue.toInt()}%",
                    color = color,
                    fontSize = (size.value * 0.18f).sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text  = label,
                    color = TextMuted,
                    fontSize = (size.value * 0.1f).sp,
                )
            }
        }
    }
}

// ── Mini Spark Line ───────────────────────────────────────────────────────────
@Composable
fun SparkLine(
    data: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = CyanCore,
    filled: Boolean = true,
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val max   = data.max().coerceAtLeast(1f)
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        val points = data.mapIndexed { i, v ->
            Offset(i * stepX, size.height - (v / max * size.height))
        }

        if (filled) {
            val fillPath = Path().apply {
                moveTo(points.first().x, size.height)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, size.height)
                close()
            }
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    listOf(color.copy(0.3f), Color.Transparent)
                )
            )
        }

        val linePath = Path().apply {
            points.forEachIndexed { i, p ->
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
        }
        drawPath(linePath, color = color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
}

// ── Grid Background ───────────────────────────────────────────────────────────
@Composable
fun GridBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val spacing = 40.dp.toPx()
        val color   = GridLine

        var x = 0f
        while (x <= size.width) {
            drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.5f)
            x += spacing
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
            y += spacing
        }
    }
}

// ── Neon Linear Bar ───────────────────────────────────────────────────────────
@Composable
fun NeonProgressBar(
    value: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color = CyanCore,
) {
    val animValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "progress"
    )
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        // Track
        drawRoundRect(
            color = color.copy(0.15f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height.toPx() / 2)
        )
        // Fill
        if (animValue > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(color.copy(0.7f), color)
                ),
                size  = Size(size.width * animValue, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height.toPx() / 2)
            )
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    color: Color = CyanCore,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(3.dp, 18.dp)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = title.uppercase(),
                color      = color,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
        }
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, color = TextMuted, fontSize = 10.sp)
        }
    }
}

// ── Stat Item ─────────────────────────────────────────────────────────────────
@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: Color = TextSecondary,
    valueColor: Color = TextPrimary,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = labelColor, fontSize = 11.sp)
        Text(value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
