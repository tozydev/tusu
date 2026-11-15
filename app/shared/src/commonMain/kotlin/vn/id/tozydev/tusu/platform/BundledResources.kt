package vn.id.tozydev.tusu.platform

import kotlinx.io.RawSource

/**
 * Opens a file packaged as a Java resource, e.g. `test-data/files.txt`, or returns null when it is
 * not packaged. Android and the desktop both read it through the class loader.
 */
expect fun openBundledResource(path: String): RawSource?
