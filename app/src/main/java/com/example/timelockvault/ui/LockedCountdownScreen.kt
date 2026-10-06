package com.example.timelockvault.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timelockvault.TimeLockManager
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun LockedCountdownScreen(
    timeLockManager: TimeLockManager,
    onCloseApp: () -> Unit
) {
    val uiState by timeLockManager.state.collectAsState()
    var arcProgress by remember { mutableFloatStateOf(360f) }

    LaunchedEffect(uiState.isLocked, uiState.remainingMs) {
        while (uiState.isLocked && uiState.remainingMs > 0L) {
            delay(500L)
            timeLockManager.refreshState()
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = arcProgress,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "arc_progress"
    )

    LaunchedEffect(uiState.remainingMs) {
        val totalLockTime = 86400000L
        arcProgress = 360f * (uiState.remainingMs.toFloat() / totalLockTime)
    }

    BackHandler { }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFF090C12)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔒 APP LOCKED",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                Canvas(
                    modifier = Modifier.size(240.dp)
                ) {
                    drawArc(
                        color = Color(0xFF7D9BFF),
                        startAngle = -90f,
                        sweepAngle = animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round),
                        topLeft = Offset(15f, 15f),
                        size = Size(size.width - 30f, size.height - 30f)
                    )
                    
                    drawArc(
                        color = Color(0xFF1F2937),
                        startAngle = animatedProgress - 90f,
                        sweepAngle = 360f - animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round),
                        topLeft = Offset(15f, 15f),
                        size = Size(size.width - 30f, size.height - 30f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF0D1117)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFB9A7FF),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF1A1F2A), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = formatCountdown(uiState.remainingMs),
                    color = Color(0xFF7D9BFF),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Vault is locked. Access denied until countdown completes.",
                color = Color(0xFFB8C0CF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onCloseApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB9A7FF),
                    contentColor = Color(0xFF111111)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("OK", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

private fun formatCountdown(remainingMs: Long): String {
    var remaining = remainingMs
    val days = TimeUnit.MILLISECONDS.toDays(remaining)
    remaining -= TimeUnit.DAYS.toMillis(days)

    val hours = TimeUnit.MILLISECONDS.toHours(remaining)
    remaining -= TimeUnit.HOURS.toMillis(hours)

    val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
    remaining -= TimeUnit.MINUTES.toMillis(minutes)

    val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining)

    return String.format("%02dd : %02dh : %02dm : %02ds", days, hours, minutes, seconds)
}
