package com.example.timelockvault.data.repository

import android.content.Context
import android.os.SystemClock
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlin.math.max

data class VaultData(
    val password: String = "",
    val isLocked: Boolean = false,
    val lockExpiresAt: Long = 0L,
    val bootToken: Long = 0L
)

interface VaultRepository {
    fun getVaultData(): VaultData
    fun savePassword(password: String)
    fun lockVault(durationMs: Long)
    fun unlockVault()
    fun clearVault()
    fun isLocked(): Boolean
    fun getRemainingTime(): Long
}

class SecureVaultRepository(
    private val context: Context
) : VaultRepository {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        PREFS_NAME,
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val PREFS_NAME = "secure_vault_prefs"
        private const val KEY_PASSWORD = "password"
        private const val KEY_LOCKED = "locked"
        private const val KEY_LOCK_EXPIRES = "lock_expires"
        private const val KEY_BOOT_TOKEN = "boot_token"
    }

    override fun getVaultData(): VaultData {
        return VaultData(
            password = encryptedPrefs.getString(KEY_PASSWORD, "") ?: "",
            isLocked = isLocked(),
            lockExpiresAt = encryptedPrefs.getLong(KEY_LOCK_EXPIRES, 0L),
            bootToken = encryptedPrefs.getLong(KEY_BOOT_TOKEN, 0L)
        )
    }

    override fun savePassword(password: String) {
        if (password.isNotBlank()) {
            encryptedPrefs.edit().putString(KEY_PASSWORD, password).apply()
        }
    }

    override fun lockVault(durationMs: Long) {
        val safeDuration = max(1_000L, durationMs)
        val expiresAt = SystemClock.elapsedRealtime() + safeDuration
        val bootToken = getBootToken()

        encryptedPrefs.edit()
            .putBoolean(KEY_LOCKED, true)
            .putLong(KEY_LOCK_EXPIRES, expiresAt)
            .putLong(KEY_BOOT_TOKEN, bootToken)
            .apply()
    }

    override fun unlockVault() {
        encryptedPrefs.edit()
            .putBoolean(KEY_LOCKED, false)
            .remove(KEY_LOCK_EXPIRES)
            .remove(KEY_BOOT_TOKEN)
            .apply()
    }

    override fun clearVault() {
        encryptedPrefs.edit().clear().apply()
    }

    override fun isLocked(): Boolean {
        val locked = encryptedPrefs.getBoolean(KEY_LOCKED, false)
        if (!locked) return false

        val persistedBootToken = encryptedPrefs.getLong(KEY_BOOT_TOKEN, 0L)
        val currentBootToken = getBootToken()
        if (persistedBootToken != 0L && currentBootToken != 0L && persistedBootToken != currentBootToken) {
            unlockVault()
            return false
        }

        val expiresAt = encryptedPrefs.getLong(KEY_LOCK_EXPIRES, 0L)
        if (expiresAt <= 0L || SystemClock.elapsedRealtime() >= expiresAt) {
            unlockVault()
            return false
        }

        return true
    }

    override fun getRemainingTime(): Long {
        if (!encryptedPrefs.getBoolean(KEY_LOCKED, false)) return 0L
        val expiresAt = encryptedPrefs.getLong(KEY_LOCK_EXPIRES, 0L)
        return max(0L, expiresAt - SystemClock.elapsedRealtime())
    }

    private fun getBootToken(): Long {
        val bootPrefs = context.getSharedPreferences("boot_tracking", Context.MODE_PRIVATE)
        var token = bootPrefs.getLong("boot_token", 0L)
        if (token == 0L) {
            token = System.currentTimeMillis()
            bootPrefs.edit().putLong("boot_token", token).apply()
        }
        return token
    }
}
