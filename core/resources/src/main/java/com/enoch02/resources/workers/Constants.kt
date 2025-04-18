package com.enoch02.resources.workers

import java.util.UUID

// shared prefs keys
const val APP_PREFS_KEY = "app_prefs"
const val DOCUMENT_DIR_KEY = "directory_uri"

// UUIDs for when scanning
const val SAVED_WORK_IDS_KEY = "work_ids"
val FILE_SCAN_WORKER_ID: UUID = UUID.randomUUID()
val COVER_SCAN_WORKER_ID: UUID = UUID.randomUUID()
const val FILE_SCAN_WORKER_KEY = "file_scan_work_id"
const val COVER_SCAN_WORKER_KEY = "cover_scan_work_id"

// Notification constants
const val BACKUP_NOTIFICATION_ID = 1001
const val RESTORE_NOTIFICATION_ID = 1002
const val COVER_SCAN_NOTIFICATION_ID = 1003
const val FILE_SCAN_NOTIFICATION_ID = 1003
const val PERIODIC_FILE_SCAN_NOTIFICATION_ID = 1003

// Name of Notification Channel for completion notifications of background work
val COMPLETION_NOTIFICATION_CHANNEL_NAME: CharSequence = "Background Task Completion"
const val COMPLETION_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications for LiteraryLinc background tasks"
val NOTIFICATION_TITLE: CharSequence = "LiteraryLinc"
const val CHANNEL_ID = "APP_NOTIFICATIONS"
//const val NOTIFICATION_ID = 1

// For progress notifications
val PROGRESS_NOTIFICATION_CHANNEL_NAME: CharSequence = "Background Task Progress"
const val PROGRESS_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications for LiteraryLinc background tasks"
const val PROGRESS_CHANNEL_ID = "PROGRESS_NOTIFICATION"
//const val PROGRESS_NOTIFICATION_ID = 2

//BackupWorker
val CREATE_BACKUP_WORKER_ID: UUID = UUID.randomUUID()
val RESTORE_BACKUP_WORKER_ID: UUID = UUID.randomUUID()
const val BACKUP_FILE_URI_KEY = "BACKUP_KEY"
const val EXCEL_FRIENDLY_KEY = "EXCEL_FRIENDLY"
