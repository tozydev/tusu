import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.metro)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
    }
}

dependencies {
    implementation(projects.app.shared)

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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        buildConfig = true
    }

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
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning?.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }
            packaging {
                resources {
                    excludes += "/test-data/**"
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}

/**
 * Calculates version code from [version] string and return version code in format
 * `0Y_0M_MICRO_MODIFIER`.
 */
fun calculateVersionCode(version: String): Int {
    val modifierCode = 0
    val parts = version.substringBefore('-').split('.').map { it.toIntOrNull() }
    require(parts.size == 3) {
        "Invalid version format, required: MAJOR.MINOR.MICRO-MODIFIER, got: $version"
    }

    val (year, month, micro) = parts
    require(year != null && month != null && micro != null) {
        "Invalid version format, required: MAJOR.MINOR.MICRO-MODIFIER, got: $version"
    }
    return "%02d_%02d_%02d_%02d"
        .format(year % 100, month, micro, modifierCode)
        .replace("_", "")
        .toInt()
}
