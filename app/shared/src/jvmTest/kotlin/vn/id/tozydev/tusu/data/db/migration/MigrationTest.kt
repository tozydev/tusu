package vn.id.tozydev.tusu.data.db.migration

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.TusuDatabaseConstructor

/**
 * Reads the exported schemas from `app/shared/schemas`, which the Gradle test working directory
 * resolves to. For a future version N: add the migration to `ALL_MIGRATIONS`, then add a test that
 * calls `helper.runMigrationsAndValidate(N, ALL_MIGRATIONS.toList())`.
 */
class MigrationTest :
    FunSpec(
        {
            test("database created from exported schema 1 opens with the current code") {
                val dir = Files.createTempDirectory("tusu-migration")
                try {
                    val path = dir.resolve("tusu.db")
                    val helper = migrationHelper(path)

                    helper.createDatabase(1).close()

                    val db =
                        Room.databaseBuilder<TusuDatabase>(
                                name = path.toString(),
                                factory = TusuDatabaseConstructor::initialize,
                            )
                            .setDriver(BundledSQLiteDriver())
                            .addMigrations(*ALL_MIGRATIONS)
                            .build()
                    try {
                        // The first query opens the file and validates it against the current
                        // entities.
                        db.entryDao().getById(Uuid.random()) shouldBe null
                    } finally {
                        db.close()
                    }
                } finally {
                    dir.toFile().deleteRecursively()
                }
            }
        },
    )

private fun migrationHelper(databasePath: Path) =
    MigrationTestHelper(
        schemaDirectoryPath = Path.of("schemas"),
        databasePath = databasePath,
        driver = BundledSQLiteDriver(),
        databaseClass = TusuDatabase::class,
        databaseFactory = TusuDatabaseConstructor::initialize,
    )
