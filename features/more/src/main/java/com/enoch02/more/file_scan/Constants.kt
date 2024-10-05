package com.enoch02.more.file_scan

import java.util.UUID

// shared prefs keys
const val APP_PREFS_KEY = "app_prefs"
const val DOCUMENT_DIR_KEY = "directory_uri"
const val DOCUMENT_COUNT_KEY = "document_count"

// UUIDs for when scanning
const val SAVED_WORK_IDS_KEY = "work_ids"
val FILE_SCAN_WORKER_ID: UUID = UUID.randomUUID()
val COVER_SCAN_WORKER_ID: UUID = UUID.randomUUID()
const val FILE_SCAN_WORKER_KEY = "file_scan_work_id"
const val COVER_SCAN_WORKER_KEY = "cover_scan_work_id"

// Notification Channel constants

// Name of Notification Channel for completion notifications of background work
val COMPLETION_NOTIFICATION_CHANNEL_NAME: CharSequence = "LiteraryLinc Notifications"
const val COMPLETION_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications for LiteraryLinc background tasks"
val NOTIFICATION_TITLE: CharSequence = "LiteraryLinc"
const val CHANNEL_ID = "APP_NOTIFICATIONS"
const val NOTIFICATION_ID = 1

// For progress notifications
val PROGRESS_NOTIFICATION_CHANNEL_NAME: CharSequence = "LiteraryLinc Progress Notifications"
const val PROGRESS_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications for LiteraryLinc background tasks"
const val PROGRESS_CHANNEL_ID = "PROGRESS_NOTIFICATION"
const val PROGRESS_NOTIFICATION_ID = 2

//BackupWorker
val CREATE_BACKUP_WORKER_ID: UUID = UUID.randomUUID()
val RESTORE_BACKUP_WORKER_ID: UUID = UUID.randomUUID()
const val BACKUP_FILE_URI_KEY = "BACKUP_KEY"