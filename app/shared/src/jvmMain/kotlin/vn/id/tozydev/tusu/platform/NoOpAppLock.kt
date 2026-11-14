package vn.id.tozydev.tusu.platform

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import vn.id.tozydev.tusu.domain.error.AppLockError
import vn.id.tozydev.tusu.domain.error.Outcome

/** JVM is a build and test target only, so the app lock is never available. */
@ContributesBinding(AppScope::class)
@Inject
class NoOpAppLock : AppLock {
    override suspend fun isAvailable(): Boolean = false

    override suspend fun authenticate(): Outcome<Unit, AppLockError> =
        Outcome.Failure(AppLockError.Unavailable)
}
