package vn.id.tozydev.tusu.di

import dev.zacsweers.metro.Qualifier

@Qualifier annotation class AppDir

@Qualifier annotation class MediaDir

/** Folder for files that can be rebuilt; the platform may clear it. */
@Qualifier annotation class CacheDir
