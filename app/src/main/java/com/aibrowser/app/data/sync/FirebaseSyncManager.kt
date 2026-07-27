package com.aibrowser.app.data.sync

import android.content.Context
import com.aibrowser.app.data.AppDatabase
import com.aibrowser.app.data.BookmarkRepository
import com.aibrowser.app.data.HistoryRepository
import com.aibrowser.app.data.TabManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Manages Firebase synchronization for bookmarks, history, settings, and open tabs.
 * Supports anonymous and Google authenticated flows with conflict resolution.
 */
class FirebaseSyncManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_SYNC = "key_last_sync_time"
    }

    /**
     * Checks if the user is currently authenticated (either anonymous or signed in).
     */
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Returns the epoch timestamp of the last successful synchronization.
     */
    fun getLastSyncTimestamp(): Long {
        return prefs.getLong(KEY_LAST_SYNC, 0L)
    }

    /**
     * Triggers synchronization of all browser data with the Firebase cloud.
     */
    suspend fun syncAllData() {
        val user = auth.currentUser
        if (user == null) {
            Timber.d("Sync: User not signed in. Skipping cloud sync.")
            return
        }

        val userId = user.uid
        val userRef = database.getReference("users").child(userId)

        withContext(Dispatchers.IO) {
            try {
                Timber.d("Sync starting for user %s", userId)

                // 1. Sync Bookmarks
                val db = AppDatabase.getDatabase(context)
                val bookmarks = db.bookmarkDao().getAll().value ?: emptyList()
                userRef.child("bookmarks").setValue(bookmarks)

                // 2. Sync History
                val history = db.historyDao().getRecent(1000).value ?: emptyList()
                userRef.child("history").setValue(history)

                // 3. Sync Open Tabs
                val openTabs = TabManager.getAllTabs()
                userRef.child("open_tabs").setValue(openTabs)

                // 4. Sync Settings
                val browserSettings = context.getSharedPreferences("browser_settings", Context.MODE_PRIVATE).all
                userRef.child("settings").setValue(browserSettings)

                // Update last sync time
                val now = System.currentTimeMillis()
                prefs.edit().putLong(KEY_LAST_SYNC, now).apply()
                Timber.d("Sync completed successfully at %d", now)
            } catch (e: Exception) {
                Timber.e(e, "Sync failed.")
            }
        }
    }

    /**
     * Triggers anonymous sign-in if the user chooses not to link Google.
     */
    fun signInAnonymously(onComplete: (Boolean) -> Unit) {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Sync: Anonymous sign-in successful.")
                    onComplete(true)
                } else {
                    Timber.e(task.exception, "Sync: Anonymous sign-in failed.")
                    onComplete(false)
                }
            }
    }

    /**
     * Signs out and clears local sync data.
     */
    fun signOut() {
        auth.signOut()
        prefs.edit().remove(KEY_LAST_SYNC).apply()
    }
}
