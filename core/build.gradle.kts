import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
}

kotlin {
    jvm()

    android {
        namespace = "vn.id.tozydev.tusu.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_25
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit6)
        }
    }
}

tasks.withType<KotlinJvmTest>().configureEach {
    useJUnitPlatform()
}
