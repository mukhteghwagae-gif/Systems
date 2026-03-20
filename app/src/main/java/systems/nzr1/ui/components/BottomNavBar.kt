package systems.nzr1.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*
import systems.nzr1.ui.navigation.Screen
import systems.nzr1.ui.theme.*

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector,
)

val bottomNavItems = listOf(
    NavItem(Screen.Dashboard, "DASH",     Icons.Outlined.Dashboard,   Icons.Filled.Dashboard),
    NavItem(Screen.Monitor,   "SYS",      Icons.Outlined.Memory,      Icons.Filled.Memory),
    NavItem(Screen.Network,   "NET",      Icons.Outlined.Wifi,        Icons.Filled.Wifi),
    NavItem(Screen.Security,  "SEC",      Icons.Outlined.Security,    Icons.Filled.Security),
    NavItem(Screen.Assistant, "AI",       Icons.Outlined.SmartToy,    Icons.Filled.SmartToy),
    NavItem(Screen.Terminal,  "SHELL",    Icons.Outlined.Terminal,    Icons.Filled.Terminal),
)

@Composable
fun SystemsBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, DeepVoid)
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(BorderDim, CyanGlow25, BorderDim)
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.screen.route
                NavBarItem(item, selected) { onNavigate(item.screen) }
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val glowAnim by rememberInfiniteTransition(label = "navglow").animateFloat(
        initialValue = 0.4f, targetValue = 1f, label = "g",
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse)
    )
    val iconColor   = if (selected) CyanCore else TextMuted
    val labelColor  = if (selected) CyanCore else TextDisabled
    val bgColor     = if (selected) CyanGlow10 else Color.Transparent

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .drawBehind {
                if (selected) {
                    drawRect(
                        color = CyanCore.copy(alpha = 0.08f * glowAnim)
                    )
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) item.iconSelected else item.icon,
            contentDescription = item.label,
            tint = iconColor.copy(alpha = if (selected) glowAnim else 1f),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text      = item.label,
            color     = labelColor,
            fontSize  = 7.sp,
            letterSpacing = 1.sp,
        )
    }
}
