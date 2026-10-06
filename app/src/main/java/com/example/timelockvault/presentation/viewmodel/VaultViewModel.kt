package com.example.timelockvault.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timelockvault.data.repository.VaultRepository
import com.example.timelockvault.domain.usecase.GeneratePasswordConfig
import com.example.timelockvault.domain.usecase.GeneratePasswordUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VaultUiState(
    val password: String = "",
    val isLocked: Boolean = false,
    val remainingMs: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)

class VaultViewModel(
    private val repository: VaultRepository,
    private val generatePasswordUseCase: GeneratePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        loadVaultState()
        startTimerUpdater()
    }

    fun savePassword(password: String) {
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Password cannot be empty")
            return
        }
        repository.savePassword(password)
        loadVaultState()
    }

    fun generatePassword(config: GeneratePasswordConfig): String {
        return generatePasswordUseCase(config)
    }

    fun lockVault(durationMs: Long) {
        repository.lockVault(durationMs)
        loadVaultState()
    }

    fun unlockVault() {
        repository.unlockVault()
        loadVaultState()
    }

    fun clearVault() {
        repository.clearVault()
        loadVaultState()
    }

    private fun loadVaultState() {
        viewModelScope.launch {
            try {
                val data = repository.getVaultData()
                _uiState.value = VaultUiState(
                    password = data.password,
                    isLocked = repository.isLocked(),
                    remainingMs = if (repository.isLocked()) repository.getRemainingTime() else 0L,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun startTimerUpdater() {
        viewModelScope.launch {
            while (true) {
                if (repository.isLocked()) {
                    val remaining = repository.getRemainingTime()
                    _uiState.value = _uiState.value.copy(remainingMs = remaining)

                    if (remaining <= 0L) {
                        repository.unlockVault()
                        loadVaultState()
                    }
                }
                delay(500L)
            }
        }
    }
}
