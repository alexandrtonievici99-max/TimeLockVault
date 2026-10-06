package com.example.timelockvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.timelockvault.ui.LockedCountdownScreen
import com.example.timelockvault.ui.TimeLockVaultTheme
import com.example.timelockvault.ui.VaultScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val timeLockManager = TimeLockManager.getInstance(applicationContext)

        setContent {
            TimeLockVaultTheme {
                val uiState by timeLockManager.state.collectAsState()

                LaunchedEffect(uiState.isLocked, uiState.remainingMs) {
                    if (!uiState.isLocked || uiState.remainingMs <= 0L) {
                        timeLockManager.unlockVault()
                    }
                }

                when {
                    timeLockManager.isVaultLocked() -> {
                        LockedCountdownScreen(
                            timeLockManager = timeLockManager,
                            onCloseApp = { finishAffinity() }
                        )
                    }
                    else -> {
                        VaultScreen(
                            timeLockManager = timeLockManager,
                            onLockApp = { durationMs ->
                                timeLockManager.lockFor(durationMs)
                                finishAffinity()
                            }
                        )
                    }
                }
            }
        }
    }
}
