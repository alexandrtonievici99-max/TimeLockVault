package com.example.timelockvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.timelockvault.data.repository.SecureVaultRepository
import com.example.timelockvault.domain.usecase.GeneratePasswordUseCase
import com.example.timelockvault.presentation.viewmodel.VaultViewModel
import com.example.timelockvault.ui.LockedCountdownScreen
import com.example.timelockvault.ui.TimeLockVaultTheme
import com.example.timelockvault.ui.VaultScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = SecureVaultRepository(applicationContext)
        val generatePasswordUseCase = GeneratePasswordUseCase()
        val viewModel = VaultViewModel(repository, generatePasswordUseCase)

        setContent {
            TimeLockVaultTheme {
                val uiState by viewModel.uiState.collectAsState()

                when {
                    repository.isLocked() -> {
                        LockedCountdownScreen(
                            uiState = uiState,
                            onCloseApp = { finishAffinity() }
                        )
                    }
                    else -> {
                        VaultScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onLockApp = { durationMs ->
                                viewModel.lockVault(durationMs)
                                finishAffinity()
                            }
                        )
                    }
                }
            }
        }
    }
}
