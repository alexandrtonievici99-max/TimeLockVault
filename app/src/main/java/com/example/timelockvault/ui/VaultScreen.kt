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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
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
    val sheetState = rememberModalBottomSheetState()

    BackHandler { }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F2F6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TIMELOCK VAULT",
                color = Color(0xFF191F29),
                fontSize = 11.sp,
                letterSpacing = 2.5.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Generate Password",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1117)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp))
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
                            fontSize = 9.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = password,
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(password))
                        },
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C313D), RoundedCornerShape(12.dp))
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy password",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = manualPassword,
                onValueChange = {
                    manualPassword = it
                    if (it.isNotEmpty()) {
                        password = it
                        timeLockManager.savePassword(it)
                    }
                },
                label = { Text("Enter or paste password", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    Icons.Default.VpnKey,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("CREATE PASSWORD", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (password.isNotBlank()) {
                        timeLockManager.savePassword(password)
                        showLockDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBAC8FF),
                    contentColor = Color(0xFF111111)
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE AND LOCK", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .background(Color(0xFF191E27), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Length",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$length chars",
                        color = Color(0xFFD7DBE5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = length.toFloat(),
                    onValueChange = { length = it.toInt() },
                    valueRange = 8f..32f,
                    steps = 23,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingRow("Uppercase (A-Z)", includeUppercase) { includeUppercase = it }
                Spacer(modifier = Modifier.height(6.dp))
                SettingRow("Lowercase (a-z)", includeLowercase) { includeLowercase = it }
                Spacer(modifier = Modifier.height(6.dp))
                SettingRow("Numbers (0-9)", includeNumbers) { includeNumbers = it }
                Spacer(modifier = Modifier.height(6.dp))
                SettingRow("Symbols (!@#$)", includeSymbols) { includeSymbols = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔧 Debug 30s Lock", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showLockDialog) {
        ModalBottomSheet(
            onDismissRequest = { showLockDialog = false },
            sheetState = sheetState,
            containerColor = Color(0xFF111827),
            scrimColor = Color.Black.copy(alpha = 0.7f)
        ) {
            LockDurationContent(
                selectedMs = selectedDuration,
                onDurationChanged = { selectedDuration = it },
                onConfirm = {
                    showLockDialog = false
                    onLockApp(selectedDuration)
                }
            )
        }
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(width = 48.dp, height = 24.dp)
        )
    }
}

@Composable
private fun LockDurationContent(
    selectedMs: Long,
    onDurationChanged: (Long) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Set Lock Duration",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        val options = listOf(
            "5 min" to TimeUnit.MINUTES.toMillis(5),
            "30 min" to TimeUnit.MINUTES.toMillis(30),
            "1 hour" to TimeUnit.HOURS.toMillis(1),
            "6 hours" to TimeUnit.HOURS.toMillis(6),
            "1 day" to TimeUnit.DAYS.toMillis(1)
        )

        options.forEach { (label, value) ->
            val isSelected = selectedMs == value
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) Color(0xFFB9A7FF) else Color(0xFF1E2733)
                    )
                    .clickable { onDurationChanged(value) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color(0xFF111111) else Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF111111),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2B1D20), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "⚠️ Once activated, the app will self-lock. You will NOT be able to access this password until the timer expires.",
                color = Color(0xFFFFE7E8),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB9A7FF),
                contentColor = Color(0xFF111111)
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Confirm & Lock App", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
