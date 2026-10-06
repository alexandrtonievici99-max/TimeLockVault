package com.example.timelockvault

import android.content.Context
import android.os.SystemClock
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

data class VaultUiState(
    val password: String = "",
    val isLocked: Boolean = false,
    val remainingMs: Long = 0L,
    val vaultPresent: Boolean = false
)

class TimeLockManager private constructor(
    private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        PREFS_NAME,
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _state = MutableStateFlow(VaultUiState())
    val state: StateFlow<VaultUiState> = _state.asStateFlow()

    init {
        refreshState()
    }

    companion object {
        @Volatile
        private var INSTANCE: TimeLockManager? = null

        private const val PREFS_NAME = "time_lock_vault_secure"
        private const val KEY_PASSWORD = "vault_password"
        private const val KEY_LOCKED = "vault_locked"
        private const val KEY_LOCK_UNTIL_ELAPSED = "vault_lock_until_elapsed"
        private const val KEY_BOOT_TOKEN = "vault_boot_token"

        fun getInstance(context: Context): TimeLockManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TimeLockManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun getPassword(): String {
        return prefs.getString(KEY_PASSWORD, "") ?: ""
    }

    fun savePassword(password: String) {
        if (password.isNotBlank()) {
            prefs.edit().putString(KEY_PASSWORD, password).apply()
            refreshState()
        }
    }

    fun clearVault() {
        prefs.edit()
            .remove(KEY_PASSWORD)
            .putBoolean(KEY_LOCKED, false)
            .remove(KEY_LOCK_UNTIL_ELAPSED)
            .remove(KEY_BOOT_TOKEN)
            .apply()
        refreshState()
    }

    fun unlockVault() {
        prefs.edit()
            .putBoolean(KEY_LOCKED, false)
            .remove(KEY_LOCK_UNTIL_ELAPSED)
            .remove(KEY_BOOT_TOKEN)
            .apply()
        refreshState()
    }

    fun isVaultLocked(): Boolean {
        val locked = prefs.getBoolean(KEY_LOCKED, false)
        if (!locked) return false

        val persistedBootToken = prefs.getLong(KEY_BOOT_TOKEN, 0L)
        val currentBootToken = getBootToken()
        if (persistedBootToken != 0L && currentBootToken != 0L && persistedBootToken != currentBootToken) {
            unlockVault()
            return false
        }

        val lockUntilElapsed = prefs.getLong(KEY_LOCK_UNTIL_ELAPSED, 0L)
        if (lockUntilElapsed <= 0L) {
            unlockVault()
            return false
        }

        val now = SystemClock.elapsedRealtime()
        if (now >= lockUntilElapsed) {
            unlockVault()
            return false
        }

        return true
    }

    fun getRemainingTimeMs(): Long {
        if (!prefs.getBoolean(KEY_LOCKED, false)) return 0L

        val lockUntilElapsed = prefs.getLong(KEY_LOCK_UNTIL_ELAPSED, 0L)
        if (lockUntilElapsed <= 0L) return 0L

        return max(0L, lockUntilElapsed - SystemClock.elapsedRealtime())
    }

    fun lockFor(durationMs: Long) {
        val safeDuration = max(1_000L, durationMs)
        val now = SystemClock.elapsedRealtime()
        val unlockAt = now + safeDuration

        prefs.edit()
            .putBoolean(KEY_LOCKED, true)
            .putLong(KEY_LOCK_UNTIL_ELAPSED, unlockAt)
            .putLong(KEY_BOOT_TOKEN, getBootToken())
            .apply()

        refreshState()
    }

    fun debugLockForSeconds(seconds: Long) {
        lockFor(seconds * 1000L)
    }

    fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        require(length in 8..64) { "Password length must be between 8 and 64" }

        val charPool = StringBuilder()
        if (includeUppercase) charPool.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        if (includeLowercase) charPool.append("abcdefghijklmnopqrstuvwxyz")
        if (includeNumbers) charPool.append("0123456789")
        if (includeSymbols) charPool.append("!@#$%^&*()_-+=[]{}|;:,.<>?")

        require(charPool.isNotEmpty()) { "At least one character set must be enabled" }

        val random = java.util.Random(System.currentTimeMillis())
        return buildString {
            repeat(length) {
                append(charPool[random.nextInt(charPool.length)])
            }
        }
    }

    fun onBootCompleted() {
        val bootToken = getBootToken()
        prefs.edit().putLong(KEY_BOOT_TOKEN, bootToken).apply()
        refreshState()
    }

    fun refreshState() {
        val password = getPassword()
        val locked = isVaultLocked()

        _state.value = VaultUiState(
            password = password,
            isLocked = locked,
            remainingMs = if (locked) getRemainingTimeMs() else 0L,
            vaultPresent = password.isNotBlank()
        )
    }

    private fun getBootToken(): Long {
        val bootPrefs = context.getSharedPreferences("boot_guard", Context.MODE_PRIVATE)
        var token = bootPrefs.getLong("boot_token", 0L)

        if (token == 0L) {
            token = System.currentTimeMillis()
            bootPrefs.edit().putLong("boot_token", token).apply()
        }

        return token
    }
}
