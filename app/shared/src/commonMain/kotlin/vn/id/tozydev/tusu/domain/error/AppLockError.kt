package vn.id.tozydev.tusu.domain.error

sealed interface AppLockError {
    /** No secure lock is set up or the device cannot authenticate. */
    data object Unavailable : AppLockError

    /** The user dismissed the authentication prompt. */
    data object Cancelled : AppLockError

    /**
     * Authentication ran but did not succeed, for example a wrong fingerprint or too many attempts.
     */
    data object Failed : AppLockError
}
