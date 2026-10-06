package com.example.timelockvault.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timelockvault.BuildConfig
import com.example.timelockvault.TimeLockManager
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    timeLockManager: TimeLockManager,
    onLockApp: (Long) -> Unit
) {
    var password by remember {
        mutableStateOf(
            timeLockManager.getPassword().ifBlank {
                timeLockManager.generatePassword(
                    length = 16,
                    includeUppercase = true,
                    includeLowercase = true,
                    includeNumbers = true,
                    includeSymbols = true
                )
            }
        )
    }

    var manualPassword by rememberSaveable { mutableStateOf(password) }
    var length by rememberSaveable { mutableIntStateOf(16) }
    var includeUppercase by rememberSaveable { mutableStateOf(true) }
    var includeLowercase by rememberSaveable { mutableStateOf(true) }
    var includeNumbers by rememberSaveable { mutableStateOf(true) }
    var includeSymbols by rememberSaveable { mutableStateOf(true) }
    var showLockDialog by remember { mutableStateOf(false) }
    var selectedDuration by rememberSaveable { mutableLongStateOf(TimeUnit.MINUTES.toMillis(30)) }

    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    BackHandler { }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F2F6))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "TIMELOCK VAULT",
                color = Color(0xFF191F29),
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Generate Password",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1F)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A1E27))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GENERATED PASSWORD",
                            color = Color(0xFF9AA3B8),
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = password,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(password))
                        },
                        modifier = Modifier
                            .background(Color(0xFF2C313D), RoundedCornerShape(12.dp))
                            .size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy password",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = manualPassword,
                onValueChange = {
                    manualPassword = it
                    password = it
                    timeLockManager.savePassword(it)
                },
                label = { Text("Password or paste your own") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val generated = timeLockManager.generatePassword(
                        length = length,
                        includeUppercase = includeUppercase,
                        includeLowercase = includeLowercase,
                        includeNumbers = includeNumbers,
                        includeSymbols = includeSymbols
                    )

                    password = generated
                    manualPassword = generated
                    timeLockManager.savePassword(generated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB99AF8),
                    contentColor = Color(0xFF111111)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.VpnKey, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CREATE PASSWORD", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    timeLockManager.savePassword(password)
                    showLockDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBAC8FF),
                    contentColor = Color(0xFF111111)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE AND LOCK", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF191E27), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Length",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$length characters",
                        color = Color(0xFFD7DBE5),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = length.toFloat(),
                    onValueChange = { length = it.toInt() },
                    valueRange = 8f..32f,
                    steps = 23,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingRow("Uppercase (A-Z)", includeUppercase) { includeUppercase = it }
                SettingRow("Lowercase (a-z)", includeLowercase) { includeLowercase = it }
                SettingRow("Numbers (0-9)", includeNumbers) { includeNumbers = it }
                SettingRow("Symbols (!@#$)", includeSymbols) { includeSymbols = it }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (BuildConfig.DEBUG) {
                Button(
                    onClick = {
                        val generated = timeLockManager.generatePassword(
                            length = length,
                            includeUppercase = includeUppercase,
                            includeLowercase = includeLowercase,
                            includeNumbers = includeNumbers,
                            includeSymbols = includeSymbols
                        )
                        password = generated
                        manualPassword = generated
                        timeLockManager.savePassword(generated)
                        timeLockManager.debugLockForSeconds(30L)
                        onLockApp(TimeUnit.SECONDS.toMillis(30L))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2D32),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Debug 30s Lock", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showLockDialog) {
        LockDurationBottomSheet(
            selectedMs = selectedDuration,
            onDurationChanged = { selectedDuration = it },
            onDismiss = { showLockDialog = false },
            onConfirm = {
                showLockDialog = false
                onLockApp(selectedDuration)
            }
        )
    }
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LockDurationBottomSheet(
    selectedMs: Long,
    onDurationChanged: (Long) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111827)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "Set lock duration",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val options = listOf(
                "Minutes" to TimeUnit.MINUTES.toMillis(5),
                "Minutes" to TimeUnit.MINUTES.toMillis(30),
                "Hours" to TimeUnit.HOURS.toMillis(1),
                "Hours" to TimeUnit.HOURS.toMillis(6),
                "Days" to TimeUnit.DAYS.toMillis(1)
            )

            options.forEach { (label, value) ->
                val isSelected = selectedMs == value
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            if (isSelected) Color(0xFFB9A7FF) else Color(0xFF1E2733),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onDurationChanged(value) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$label • ${formatDurationLabel(value)}",
                        color = if (isSelected) Color(0xFF111111) else Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B1D20), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Once activated, the app will self-lock. You will NOT be able to access this password until the timer expires.",
                    color = Color(0xFFFFE7E8),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB9A7FF),
                    contentColor = Color(0xFF111111)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Confirm & Lock App", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatDurationLabel(durationMs: Long): String {
    val days = durationMs / TimeUnit.DAYS.toMillis(1)
    val hours = (durationMs % TimeUnit.DAYS.toMillis(1)) / TimeUnit.HOURS.toMillis(1)
    val minutes = (durationMs % TimeUnit.HOURS.toMillis(1)) / TimeUnit.MINUTES.toMillis(1)

    return when {
        days > 0 -> "${days}d ${hours}h ${minutes}m"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
