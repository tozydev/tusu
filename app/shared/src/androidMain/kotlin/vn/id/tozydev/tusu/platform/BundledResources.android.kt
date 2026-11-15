package vn.id.tozydev.tusu.platform

import kotlinx.io.RawSource
import kotlinx.io.asSource

private object ResourceAnchor

actual fun openBundledResource(path: String): RawSource? =
    ResourceAnchor::class.java.classLoader?.getResourceAsStream(path)?.asSource()
