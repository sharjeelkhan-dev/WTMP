package com.sharjeel.wtmp

import android.app.Application
import android.content.pm.ApplicationInfo
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WTMPApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            Firebase.initialize(context = this)
            
            val isDebuggable = (0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE))
            if (isDebuggable && FirebaseApp.getApps(this).isNotEmpty()) {
                Firebase.appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance(),
                )
            }
        } catch (e: Exception) {
            Log.e("WTMP", "Firebase initialization failed. Did you add google-services.json?", e)
        }
    }
}
