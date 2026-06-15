package vn.io.tozyworks.tusu

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
