package vn.io.tozydev.tusu

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*

class ApplicationTest : ShouldSpec() {
    init {
        should("return hello message from root endpoint") {
            testApplication {
                application {
                    module()
                }
                val response = client.get("/")
                response.status shouldBe HttpStatusCode.OK
                response.bodyAsText() shouldBe "Hello, Ktor!"
            }
        }
    }
}
