package vn.id.tozydev.tusu.ui.format

import vn.id.tozydev.tusu.domain.error.AppLockError
import vn.id.tozydev.tusu.domain.error.BackupError
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.error.LabelError
import vn.id.tozydev.tusu.domain.error.LocationError
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.PermissionError
import vn.id.tozydev.tusu.domain.error.PlaceError
import vn.id.tozydev.tusu.domain.error.RichTextError
import vn.id.tozydev.tusu.domain.error.SettingsError
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.error_app_lock_cancelled
import vn.id.tozydev.tusu.generated.resources.error_app_lock_failed
import vn.id.tozydev.tusu.generated.resources.error_app_lock_unavailable
import vn.id.tozydev.tusu.generated.resources.error_backup_insufficient_storage
import vn.id.tozydev.tusu.generated.resources.error_backup_invalid_archive
import vn.id.tozydev.tusu.generated.resources.error_backup_read_failed
import vn.id.tozydev.tusu.generated.resources.error_backup_unsupported_schema
import vn.id.tozydev.tusu.generated.resources.error_backup_write_failed
import vn.id.tozydev.tusu.generated.resources.error_entry_not_found
import vn.id.tozydev.tusu.generated.resources.error_entry_storage_failed
import vn.id.tozydev.tusu.generated.resources.error_entry_unreadable
import vn.id.tozydev.tusu.generated.resources.error_label_blank_name
import vn.id.tozydev.tusu.generated.resources.error_label_duplicate_name
import vn.id.tozydev.tusu.generated.resources.error_label_group_not_found
import vn.id.tozydev.tusu.generated.resources.error_label_not_found
import vn.id.tozydev.tusu.generated.resources.error_label_storage_failed
import vn.id.tozydev.tusu.generated.resources.error_location_service_disabled
import vn.id.tozydev.tusu.generated.resources.error_location_unavailable
import vn.id.tozydev.tusu.generated.resources.error_media_file_unreadable
import vn.id.tozydev.tusu.generated.resources.error_media_insufficient_storage
import vn.id.tozydev.tusu.generated.resources.error_media_not_found
import vn.id.tozydev.tusu.generated.resources.error_media_processing_failed
import vn.id.tozydev.tusu.generated.resources.error_media_recording_failed
import vn.id.tozydev.tusu.generated.resources.error_media_storage_failed
import vn.id.tozydev.tusu.generated.resources.error_media_unsupported_format
import vn.id.tozydev.tusu.generated.resources.error_permission_denied
import vn.id.tozydev.tusu.generated.resources.error_permission_denied_permanently
import vn.id.tozydev.tusu.generated.resources.error_place_blank_name
import vn.id.tozydev.tusu.generated.resources.error_place_not_found
import vn.id.tozydev.tusu.generated.resources.error_place_storage_failed
import vn.id.tozydev.tusu.generated.resources.error_rich_text_malformed
import vn.id.tozydev.tusu.generated.resources.error_rich_text_unsupported_version
import vn.id.tozydev.tusu.generated.resources.error_settings_storage_failed

// Each `when` lists every case without `else`, so a new error case fails to compile until it has a
// message.

fun AppLockError.toUiText(): UiText =
    when (this) {
        AppLockError.Unavailable -> UiText.of(Res.string.error_app_lock_unavailable)
        AppLockError.Cancelled -> UiText.of(Res.string.error_app_lock_cancelled)
        AppLockError.Failed -> UiText.of(Res.string.error_app_lock_failed)
    }

fun BackupError.toUiText(): UiText =
    when (this) {
        BackupError.InvalidArchive -> UiText.of(Res.string.error_backup_invalid_archive)
        is BackupError.UnsupportedSchemaVersion ->
            UiText.of(Res.string.error_backup_unsupported_schema)
        BackupError.InsufficientStorage -> UiText.of(Res.string.error_backup_insufficient_storage)
        BackupError.ReadFailed -> UiText.of(Res.string.error_backup_read_failed)
        BackupError.WriteFailed -> UiText.of(Res.string.error_backup_write_failed)
    }

fun EntryError.toUiText(): UiText =
    when (this) {
        EntryError.NotFound -> UiText.of(Res.string.error_entry_not_found)
        EntryError.UnreadableBody -> UiText.of(Res.string.error_entry_unreadable)
        EntryError.StorageFailed -> UiText.of(Res.string.error_entry_storage_failed)
    }

fun LabelError.toUiText(): UiText =
    when (this) {
        LabelError.NotFound -> UiText.of(Res.string.error_label_not_found)
        LabelError.GroupNotFound -> UiText.of(Res.string.error_label_group_not_found)
        LabelError.BlankName -> UiText.of(Res.string.error_label_blank_name)
        LabelError.DuplicateName -> UiText.of(Res.string.error_label_duplicate_name)
        LabelError.StorageFailed -> UiText.of(Res.string.error_label_storage_failed)
    }

fun LocationError.toUiText(): UiText =
    when (this) {
        is LocationError.PermissionRequired -> error.toUiText()
        LocationError.ServiceDisabled -> UiText.of(Res.string.error_location_service_disabled)
        LocationError.Unavailable -> UiText.of(Res.string.error_location_unavailable)
    }

fun MediaError.toUiText(): UiText =
    when (this) {
        MediaError.NotFound -> UiText.of(Res.string.error_media_not_found)
        MediaError.FileUnreadable -> UiText.of(Res.string.error_media_file_unreadable)
        MediaError.UnsupportedFormat -> UiText.of(Res.string.error_media_unsupported_format)
        MediaError.InsufficientStorage -> UiText.of(Res.string.error_media_insufficient_storage)
        MediaError.ProcessingFailed -> UiText.of(Res.string.error_media_processing_failed)
        MediaError.RecordingFailed -> UiText.of(Res.string.error_media_recording_failed)
        is MediaError.PermissionRequired -> error.toUiText()
        MediaError.StorageFailed -> UiText.of(Res.string.error_media_storage_failed)
    }

fun PermissionError.toUiText(): UiText =
    when (this) {
        is PermissionError.Denied -> UiText.of(Res.string.error_permission_denied)
        is PermissionError.DeniedPermanently ->
            UiText.of(Res.string.error_permission_denied_permanently)
    }

fun PlaceError.toUiText(): UiText =
    when (this) {
        PlaceError.NotFound -> UiText.of(Res.string.error_place_not_found)
        PlaceError.BlankName -> UiText.of(Res.string.error_place_blank_name)
        PlaceError.StorageFailed -> UiText.of(Res.string.error_place_storage_failed)
    }

fun RichTextError.toUiText(): UiText =
    when (this) {
        RichTextError.MalformedJson -> UiText.of(Res.string.error_rich_text_malformed)
        is RichTextError.UnsupportedVersion ->
            UiText.of(Res.string.error_rich_text_unsupported_version)
    }

fun SettingsError.toUiText(): UiText =
    when (this) {
        SettingsError.StorageFailed -> UiText.of(Res.string.error_settings_storage_failed)
    }
