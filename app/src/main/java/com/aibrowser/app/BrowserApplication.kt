package com.aibrowser.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import timber.log.Timber

/**
 * Custom [Application] class for setting up application-wide configurations,
 * initializing core libraries like Timber, Firebase, and Google Mobile Ads (AdMob)
 * right at the start.
 */
class BrowserApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for structured logging
        initTimber()

        // Initialize Firebase SDK
        initFirebase()

        // Initialize Google Mobile Ads SDK
        initAdMob()
    }

    /**
     * Initializes Timber logging.
     * In debug builds, we plant a DebugTree to output logs to logcat.
     */
    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
        Timber.d("Timber has been initialized successfully.")
    }

    /**
     * Initializes the Firebase App.
     */
    private fun initFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            Timber.d("Firebase has been initialized successfully.")
        } catch (e: Exception) {
            Timber.e(e, "Firebase initialization failed.")
        }
    }

    /**
     * Initializes the Google Mobile Ads SDK in a background thread.
     */
    private fun initAdMob() {
        try {
            MobileAds.initialize(this) { status ->
                Timber.d("Google Mobile Ads (AdMob) initialized: %s", status.toString())
            }
        } catch (e: Exception) {
            Timber.e(e, "Google Mobile Ads (AdMob) initialization failed.")
        }
    }
}
