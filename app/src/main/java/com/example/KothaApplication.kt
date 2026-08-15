package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class KothaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Protect Firebase AI Logic requests with Play Integrity in every
        // shipped variant. Firebase configuration is required for production.
        runCatching {
            FirebaseApp.initializeApp(this)
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}
