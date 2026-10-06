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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.timelockvault.domain.usecase.GeneratePasswordConfig
import com.example.timelockvault.presentation.viewmodel.VaultUiState
import com.example.timelockvault.presentation.viewmodel.VaultViewModel
import java.util.concurrent.TimeUnit

enum class TimeUnit(val label: String, val toMillis: (Long) -> Long) {
    MINUTES("Минуты", { it * 60 * 1000 }),
    HOURS("Часы", { it * 60 * 60 * 1000 }),
    DAYS("Дни", { it * 24 * 60 * 60 * 1000 }),
    WEEKS("Недели", { it * 7 * 24 * 60 * 60 * 1000 }),
    MONTHS("Месяцы", { it * 30 * 24 * 60 * 60 * 1000 })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    uiState: VaultUiState,
    viewModel: VaultViewModel,
    onLockApp: (Long) -> Unit
) {
    var password by rememberSaveable { mutableStateOf(uiState.password) }
    var length by rememberSaveable { mutableIntStateOf(16) }
    var includeUppercase by rememberSaveable { mutableStateOf(true) }
    var includeLowercase by rememberSaveable { mutableStateOf(true) }
    var includeNumbers by rememberSaveable { mutableStateOf(true) }
    var includeSymbols by rememberSaveable { mutableStateOf(true) }
    var showLockDialog by remember { mutableStateOf(false) }
    var selectedDuration by rememberSaveable { mutableLongStateOf(java.util.concurrent.TimeUnit.MINUTES.toMillis(30)) }
    
    // Custom time picker state
    var customTimeValue by rememberSaveable { mutableStateOf("") }
    var selectedTimeUnit by rememberSaveable { mutableStateOf(TimeUnit.HOURS) }
    var expandedTimeUnit by remember { mutableStateOf(false) }

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

            // Vault Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (uiState.isLocked) Color(0xFFFF6B6B) else Color(0xFF51CF66)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.isLocked) "🔒 ЗАБЛОКИРОВАН" else "🔓 РАЗБЛОКИРОВАН",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.isLocked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatTimeRemaining(uiState.remainingMs),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Password Section
            if (uiState.vaultPresent) {
                Text(
                    text = "Ваш пароль",
                    color = Color(0xFF191F29),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = password,
                            color = Color(0xFF191F29),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(password))
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Lock Options Section
                Text(
                    text = "Блокировка",
                    color = Color(0xFF191F29),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Preset Duration Buttons
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LockButton(
                            text = "5 мин",
                            onClick = {
                                selectedDuration = java.util.concurrent.TimeUnit.MINUTES.toMillis(5)
                                showLockDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LockButton(
                            text = "30 мин",
                            onClick = {
                                selectedDuration = java.util.concurrent.TimeUnit.MINUTES.toMillis(30)
                                showLockDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LockButton(
                            text = "1 час",
                            onClick = {
                                selectedDuration = java.util.concurrent.TimeUnit.HOURS.toMillis(1)
                                showLockDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LockButton(
                            text = "6 часов",
                            onClick = {
                                selectedDuration = java.util.concurrent.TimeUnit.HOURS.toMillis(6)
                                showLockDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LockButton(
                            text = "1 день",
                            onClick = {
                                selectedDuration = java.util.concurrent.TimeUnit.DAYS.toMillis(1)
                                showLockDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Time Input Section
                Text(
                    text = "Выбрать время вручную",
                    color = Color(0xFF191F29),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customTimeValue,
                        onValueChange = { customTimeValue = it },
                        label = { Text("Кол-во") },
                        modifier = Modifier
                            .weight(0.6f)
                            .height(56.dp),
                        singleLine = true
                    )

                    Box(modifier = Modifier.weight(0.4f)) {
                        Button(
                            onClick = { expandedTimeUnit = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF007AFF)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedTimeUnit.label,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expandedTimeUnit,
                            onDismissRequest = { expandedTimeUnit = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                        ) {
                            TimeUnit.values().forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.label) },
                                    onClick = {
                                        selectedTimeUnit = unit
                                        expandedTimeUnit = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (customTimeValue.isNotBlank()) {
                            try {
                                val timeValue = customTimeValue.toLong()
                                selectedDuration = selectedTimeUnit.toMillis(timeValue)
                                showLockDialog = true
                                customTimeValue = ""
                            } catch (e: Exception) {
                                // Invalid input
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34C759)
                    )
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Lock",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Применить",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Нет сохранённого пароля",
                    color = Color(0xFF999999),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Generate Password Section
            Text(
                text = "Генератор паролей",
                color = Color(0xFF191F29),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Length Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Длина: $length",
                        color = Color(0xFF191F29),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Slider(
                    value = length.toFloat(),
                    onValueChange = { length = it.toInt() },
                    valueRange = 8f..64f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Character Options
            Column(modifier = Modifier.fillMaxWidth()) {
                CheckboxOption(
                    label = "Заглавные буквы (A-Z)",
                    checked = includeUppercase,
                    onCheckedChange = { includeUppercase = it }
                )
                CheckboxOption(
                    label = "Строчные буквы (a-z)",
                    checked = includeLowercase,
                    onCheckedChange = { includeLowercase = it }
                )
                CheckboxOption(
                    label = "Цифры (0-9)",
                    checked = includeNumbers,
                    onCheckedChange = { includeNumbers = it }
                )
                CheckboxOption(
                    label = "Символы (!@#$...)",
                    checked = includeSymbols,
                    onCheckedChange = { includeSymbols = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Generate Button
            Button(
                onClick = {
                    viewModel.generatePassword(
                        GeneratePasswordConfig(
                            length = length,
                            includeUppercase = includeUppercase,
                            includeLowercase = includeLowercase,
                            includeNumbers = includeNumbers,
                            includeSymbols = includeSymbols
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                )
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Generate",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "Сгенерировать пароль",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Lock Confirmation Dialog
    if (showLockDialog) {
        LockConfirmationDialog(
            duration = selectedDuration,
            onConfirm = {
                onLockApp(selectedDuration)
                showLockDialog = false
                password = ""
                customTimeValue = ""
            },
            onDismiss = {
                showLockDialog = false
            }
        )
    }
}

@Composable
fun LockButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD60A)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF191F29),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun CheckboxOption(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFF191F29),
            fontSize = 12.sp
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun LockConfirmationDialog(
    duration: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp)
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Lock",
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Заблокировать хранилище?",
                    color = Color(0xFF191F29),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "На: ${formatTimeRemaining(duration)}",
                    color = Color(0xFF666666),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8E8E8)
                        )
                    ) {
                        Text(
                            text = "Отмена",
                            color = Color(0xFF191F29),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B6B)
                        )
                    ) {
                        Text(
                            text = "Блокировать",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun formatTimeRemaining(milliseconds: Long): String {
    return when {
        milliseconds <= 0 -> "Истекло"
        milliseconds < 60 * 1000 -> "${milliseconds / 1000}с"
        milliseconds < 60 * 60 * 1000 -> "${milliseconds / (60 * 1000)}м"
        milliseconds < 24 * 60 * 60 * 1000 -> "${milliseconds / (60 * 60 * 1000)}ч"
        else -> "${milliseconds / (24 * 60 * 60 * 1000)}д"
    }
}
