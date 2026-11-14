package vn.id.tozydev.tusu.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.system.ErrnoException
import android.system.OsConstants
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.outcome
import vn.id.tozydev.tusu.domain.error.raise

private val logger = KotlinLogging.logger {}

private const val JPEG_QUALITY = 85
private const val TEMP_SUFFIX = ".tmp"

@ContributesBinding(AppScope::class)
@Inject
class AndroidImageCompressor : ImageCompressor {
    override suspend fun compress(
        source: Path,
        destination: Path,
        maxLongEdgePx: Int,
    ): Outcome<ImageSize, MediaError> {
        require(maxLongEdgePx > 0) { "maxLongEdgePx must be positive, was $maxLongEdgePx" }
        val input = File(source.toString())
        val output = File(destination.toString())
        // Writing next to the destination and renaming keeps a same-file compress and a crash
        // mid-write from leaving a truncated image behind.
        val temp = File(output.path + TEMP_SUFFIX)
        return withContext(Dispatchers.IO) {
            try {
                encode(input, temp, output, maxLongEdgePx)
            } catch (e: CancellationException) {
                throw e
            } catch (e: OutOfMemoryError) {
                logger.warn(e) { "Image compression ran out of memory" }
                Outcome.Failure(MediaError.ProcessingFailed)
            } catch (e: IOException) {
                logger.warn(e) { "Image compression could not write the output" }
                Outcome.Failure(e.toStorageError())
            } finally {
                temp.delete()
            }
        }
    }

    private fun encode(
        input: File,
        temp: File,
        output: File,
        maxLongEdgePx: Int,
    ): Outcome<ImageSize, MediaError> = outcome {
        if (!input.canRead()) raise(MediaError.FileUnreadable)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(input.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) raise(MediaError.UnsupportedFormat)

        val orientation =
            try {
                ExifInterface(input)
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } catch (e: IOException) {
                logger.warn(e) { "Image metadata could not be read" }
                raise(MediaError.FileUnreadable)
            }

        // Decode at a power-of-two sample size that keeps the long edge at or above the target,
        // so the final scale-down only ever shrinks and never loses detail below the target.
        val sourceLongEdge = maxOf(bounds.outWidth, bounds.outHeight)
        val sampleSize = Integer.highestOneBit(maxOf(1, sourceLongEdge / maxLongEdgePx))
        val decoded =
            BitmapFactory.decodeFile(
                input.path,
                BitmapFactory.Options().apply { inSampleSize = sampleSize },
            ) ?: raise(MediaError.UnsupportedFormat)

        val decodedLongEdge = maxOf(decoded.width, decoded.height)
        val scale =
            if (decodedLongEdge > maxLongEdgePx) maxLongEdgePx.toFloat() / decodedLongEdge else 1f
        val result =
            Bitmap.createBitmap(
                decoded,
                0,
                0,
                decoded.width,
                decoded.height,
                orientationMatrix(orientation, scale),
                true,
            )
        try {
            FileOutputStream(temp).use { stream ->
                if (!result.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)) {
                    raise(MediaError.ProcessingFailed)
                }
            }
            Files.move(
                temp.toPath(),
                output.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
            ImageSize(result.width, result.height)
        } finally {
            // createBitmap returns the source itself when the matrix is the identity.
            if (result !== decoded) decoded.recycle()
            result.recycle()
        }
    }

    /**
     * Maps an EXIF orientation to a scale, a horizontal flip and a rotation. EXIF flips mean
     * "mirror, then rotate", so the flip is applied before the rotation.
     */
    private fun orientationMatrix(orientation: Int, scale: Float): Matrix {
        val rotation =
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_180,
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180f
                ExifInterface.ORIENTATION_ROTATE_270,
                ExifInterface.ORIENTATION_TRANSPOSE -> 270f
                ExifInterface.ORIENTATION_ROTATE_90,
                ExifInterface.ORIENTATION_TRANSVERSE -> 90f
                else -> 0f
            }
        return Matrix().apply {
            setScale(scale, scale)
            if (orientation in FLIPPED_ORIENTATIONS) postScale(-1f, 1f)
            if (rotation != 0f) postRotate(rotation)
        }
    }

    private fun IOException.toStorageError(): MediaError {
        val errno = (cause as? ErrnoException)?.errno
        return if (errno == OsConstants.ENOSPC || errno == OsConstants.EDQUOT) {
            MediaError.InsufficientStorage
        } else {
            MediaError.StorageFailed
        }
    }

    private companion object {
        val FLIPPED_ORIENTATIONS =
            setOf(
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
                ExifInterface.ORIENTATION_FLIP_VERTICAL,
                ExifInterface.ORIENTATION_TRANSPOSE,
                ExifInterface.ORIENTATION_TRANSVERSE,
            )
    }
}
