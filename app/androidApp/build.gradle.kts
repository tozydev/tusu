import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.metro)
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_25 } }

dependencies {
    implementation(projects.app.shared)

    // Named by TusuApplication: its Configuration.Provider type and the coroutine launch on the
    // app scope come from these libraries, which the shared module keeps off this classpath.
    implementation(libs.androidx.work.runtime)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.activity.compose)

    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "vn.id.tozydev.tusu"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "vn.id.tozydev.tusu"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = calculateVersionCode(version.toString())
        versionName = version.toString()
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    buildFeatures { buildConfig = true }

    signingConfigs {
        create("release") {
            val keystoreFile = System.getenv("KEYSTORE_FILE")?.let { file(it) }
            if (keystoreFile != null && keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug { applicationIdSuffix = ".debug" }
        release {
            isMinifyEnabled = false
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning?.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}

androidComponents {
    // The sample photos are for the debug-only SampleDataSeeder. A `packaging` block inside the
    // `release` build type would still configure the whole app, so the exclusion is set on the
    // release variant itself.
    onVariants(selector().withBuildType("release")) { variant ->
        variant.packaging.resources.excludes.add("/test-data/**")
    }
}

/**
 * Android `versionCode` of a CalVer [version] `YYYY.M.MICRO[-preview]`, laid out as `YY MM MICRO
 * MODIFIER` with two digits each.
 *
 * MODIFIER is reserved and always `00`: MICRO grows with every release of a month, so a preview and
 * a stable build never share a version.
 */
fun calculateVersionCode(version: String): Int {
    val calVer = Regex("""(\d{4})\.(1[0-2]|[1-9])\.(0|[1-9]\d?)(-preview)?""")
    val match =
        requireNotNull(calVer.matchEntire(version)) {
            "Invalid version '$version': expected YYYY.M.MICRO[-preview] with M in 1..12 and " +
                "MICRO in 0..99, without leading zeros (e.g. 2027.1.0-preview)"
        }
    val (year, month, micro) = match.destructured

    return (year.toInt() % 100) * 1_000_000 + month.toInt() * 10_000 + micro.toInt() * 100
}
