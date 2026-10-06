package com.example.timelockvault.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    LaunchedEffect(uiState.isLocked, uiState.remainingMs) {
        while (uiState.isLocked && uiState.remainingMs > 0L) {
            delay(500L)
            timeLockManager.refreshState()
        }
    }

    BackHandler { }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C12)),
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
                text = "APP LOCKED",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(26.dp))

            Box(contentAlignment = Alignment.Center) {
                Canvas(
                    modifier = Modifier.size(220.dp)
                ) {
                    drawArc(
                        color = Color(0xFF7D9BFF),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10f, cap = StrokeCap.Round),
                        topLeft = Offset(20f, 20f),
                        size = Size(size.width - 40f, size.height - 40f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D1117)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFB9A7FF),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = formatCountdown(uiState.remainingMs),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Vault is locked. Access denied until countdown completes.",
                color = Color(0xFFB8C0CF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = onCloseApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB9A7FF),
                    contentColor = Color(0xFF111111)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
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
