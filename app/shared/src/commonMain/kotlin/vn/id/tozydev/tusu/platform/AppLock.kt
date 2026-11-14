package vn.id.tozydev.tusu.platform

import vn.id.tozydev.tusu.domain.error.AppLockError
import vn.id.tozydev.tusu.domain.error.Outcome

interface AppLock {
    suspend fun isAvailable(): Boolean

    suspend fun authenticate(): Outcome<Unit, AppLockError>
}
