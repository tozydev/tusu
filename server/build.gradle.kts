plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotest)
}

application {
    mainClass = "vn.id.tozydev.tusu.ApplicationKt"
}

dependencies {
    api(projects.core)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotest.runner.junit6)
    testImplementation(libs.kotest.assertions.core)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
