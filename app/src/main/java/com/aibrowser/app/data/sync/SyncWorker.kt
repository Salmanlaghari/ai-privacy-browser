package com.aibrowser.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import timber.log.Timber

/**
 * Background WorkManager [CoroutineWorker] job for automatic cloud data synchronization.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("SyncWorker: background sync job started.")
        val manager = FirebaseSyncManager(applicationContext)

        return try {
            manager.syncAllData()
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker background job failed.")
            Result.retry()
        }
    }
}
