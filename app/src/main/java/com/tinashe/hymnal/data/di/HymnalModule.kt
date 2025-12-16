package com.tinashe.hymnal.data.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HymnalModule {

    @Provides
    fun provideContext(app: Application): Context = app

    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(app: Application): FirebaseDatabase? {
        // Firebase should be initialized by Google Services plugin if google-services.json exists
        // If not initialized, return null to allow app to work with default data
        return try {
            Firebase.database.also {
                try {
                    it.setPersistenceEnabled(true)
                } catch (ex: DatabaseException) {
                    Timber.e(ex, "Failed to enable persistence")
                }
            }
        } catch (e: IllegalStateException) {
            Timber.w(e, "Firebase is not initialized. App will use default data from raw resources.")
            null
        } catch (e: Exception) {
            Timber.w(e, "Error accessing Firebase Database. App will use default data from raw resources.")
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(app: Application): FirebaseAuth? {
        return try {
            Firebase.auth
        } catch (e: IllegalStateException) {
            Timber.w(e, "Firebase Auth is not initialized. App will use default data from raw resources.")
            null
        } catch (e: Exception) {
            Timber.w(e, "Error accessing Firebase Auth. App will use default data from raw resources.")
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(app: Application): FirebaseStorage? {
        return try {
            Firebase.storage
        } catch (e: IllegalStateException) {
            Timber.w(e, "Firebase Storage is not initialized. App will use default data from raw resources.")
            null
        } catch (e: Exception) {
            Timber.w(e, "Error accessing Firebase Storage. App will use default data from raw resources.")
            null
        }
    }

    @Provides
    fun provideConnectivityManager(context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}
