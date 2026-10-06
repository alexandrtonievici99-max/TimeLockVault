package com.example.timelockvault.domain.usecase

data class GeneratePasswordConfig(
    val length: Int = 16,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSymbols: Boolean = true
)

class GeneratePasswordUseCase {
    operator fun invoke(config: GeneratePasswordConfig): String {
        require(config.length in 8..64) { "Password length must be 8-64 characters" }

        val charPool = StringBuilder()
        if (config.includeUppercase) charPool.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        if (config.includeLowercase) charPool.append("abcdefghijklmnopqrstuvwxyz")
        if (config.includeNumbers) charPool.append("0123456789")
        if (config.includeSymbols) charPool.append("!@#$%^&*()_-+=[]{}|;:,.<>?")

        require(charPool.isNotEmpty()) { "At least one character set must be enabled" }

        val random = java.util.Random(System.currentTimeMillis())
        return buildString {
            repeat(config.length) {
                append(charPool[random.nextInt(charPool.length)])
            }
        }
    }
}
