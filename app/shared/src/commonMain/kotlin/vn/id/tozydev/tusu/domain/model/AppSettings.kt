package vn.id.tozydev.tusu.domain.model

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** ARGB seed of the colour scheme; null follows the system wallpaper where supported. */
    val seedColor: Int? = null,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val locationMode: LocationMode = LocationMode.ALWAYS,
    /** Keep the uncompressed file next to the compressed one. */
    val keepOriginalMedia: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val isReminderEnabled: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
)
