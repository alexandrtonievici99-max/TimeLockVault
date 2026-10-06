package com.example.timelockvault

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timelockvault.data.repository.SecureVaultRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                val repository = SecureVaultRepository(context)
                // Verify lock state after boot
                repository.getVaultData()
            }
        }
    }
}
