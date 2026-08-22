package com.sharjeel.wtmp.di

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.FirebaseApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(): FirebaseAnalytics? {
        return try {
            Firebase.analytics
        } catch (_: Exception) {
            null
        }
    }

    @Provides
    @Singleton
    fun provideGenerativeModel(@ApplicationContext context: Context): GenerativeModel? {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel("gemini-3.5-flash")
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}