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
        namespace = "vn.id.tozydev.tusu.app.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions { jvmTarget = JvmTarget.JVM_25 }
        androidResources { enable = true }
        withHostTest { isIncludeAndroidResources = true }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(libs.metrox.viewmodel.compose)
            api(libs.kotlinLogging)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            // The graph's public accessors and factory parameters use these types, so the
            // androidApp entry point needs them on its compile classpath too.
            api(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.materialKolor)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.paging.compose)

            api(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)
            implementation(libs.androidx.sqlite.bundled)
            api(libs.androidx.datastore.preferences)

            implementation(libs.okio)

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.filekit.coil)
            implementation(libs.coil.core)
            implementation(libs.coil.compose)
            implementation(libs.composeMediaPlayer)
            implementation(libs.richeditor.compose)
            implementation(libs.richeditor.compose.json)
            implementation(libs.compass.geolocation)
            implementation(libs.compass.geocoder)
        }
        androidMain.dependencies {
            api(libs.metrox.android)

            implementation(libs.androidx.work.runtime)

            implementation(libs.compass.geolocation.mobile)
            implementation(libs.compass.geocoder.mobile)
            implementation(libs.compass.permissions.mobile)
        }
        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.room.testing)
            implementation(libs.androidx.paging.testing)
        }
        jvmTest.dependencies { implementation(libs.kotest.runner.junit6) }
        getByName("androidHostTest").dependencies { implementation(libs.kotest.runner.junit6) }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

room3 { schemaDirectory("$projectDir/schemas") }

compose.resources { packageOfResClass = "$group.generated.resources" }

tasks.withType<Test>().configureEach { useJUnitPlatform() }
