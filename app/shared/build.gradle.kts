import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.metro)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    jvm()

    android {
        namespace = "vn.io.tozyworks.tusu.app.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            api(libs.metrox.android)

            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.emoji2.emojipicker)
        }
        commonMain.dependencies {
            api(projects.core)
            api(libs.metrox.viewmodel.compose)
            api(libs.kotlinLogging)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.navigationevent.compose)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.coil.compose)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.richeditor.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.materialKolor)
            implementation(libs.androidx.paging.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
        }
        @Suppress("unused")
        val androidHostTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit6)
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    kspAndroid(libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

compose.resources {
    packageOfResClass = "${group}.generated.resources"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
