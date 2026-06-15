plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
}

application {
    mainClass = "vn.io.tozyworks.tusu.ApplicationKt"
}

dependencies {
    api(projects.core)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}