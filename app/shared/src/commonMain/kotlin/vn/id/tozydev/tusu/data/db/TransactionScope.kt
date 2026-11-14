package vn.id.tozydev.tusu.data.db

import androidx.room3.withWriteTransaction

/**
 * Proof that the caller runs inside [transaction]. Helpers that must not run outside a write
 * transaction take it as a context parameter; only [transaction] creates one.
 */
class TransactionScope internal constructor()

/** Runs [block] in one immediate write transaction; it rolls back if [block] throws. */
suspend fun <R> TusuDatabase.transaction(block: suspend context(TransactionScope) () -> R): R =
    withWriteTransaction {
        block(TransactionScope())
    }
