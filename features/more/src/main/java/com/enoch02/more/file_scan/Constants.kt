package com.enoch02.more.file_scan

import java.util.UUID

// shared prefs keys
const val APP_PREFS_KEY = "app_prefs"
const val DOCUMENT_DIR_KEY = "directory_uri"
const val DOCUMENT_COUNT_KEY = "document_count"

// UUIDs for when scanning for both
val FILE_SCAN_WORKER_ID: UUID = UUID.randomUUID()
val COVER_SCAN_WORKER_ID: UUID = UUID.randomUUID()

// Notification Channel constants

// Name of Notification Channel for verbose notifications of background work
val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
    "LiteraryLinc Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications for LiteraryLinc background tasks"
val NOTIFICATION_TITLE: CharSequence = "LiteraryLinc"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1

//BackupWorker
val CREATE_BACKUP_WORKER_ID: UUID = UUID.randomUUID()
val RESTORE_BACKUP_WORKER_ID: UUID = UUID.randomUUID()
const val BACKUP_FILE_URI_KEY = "BACKUP_KEY"