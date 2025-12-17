package com.kholhang.kcclabu.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.TitleLanguage
import com.kholhang.kcclabu.data.model.remote.RemoteHymn
import com.kholhang.kcclabu.data.model.remote.RemoteHymnal
import com.kholhang.kcclabu.data.model.remote.RemoteHymnalJsonAdapter
import com.kholhang.kcclabu.data.model.response.Resource
import com.kholhang.kcclabu.utils.Helper
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.full.memberProperties

class RemoteHymnsRepository(
    private val database: FirebaseDatabase?,
    private val auth: FirebaseAuth?,
    private val storage: FirebaseStorage?,
    private val moshi: Moshi,
    private val context: Context
) {
    private val isFirebaseAvailable: Boolean
        get() = database != null && auth != null && storage != null

    fun getSample(): RemoteHymnal? {
        // Always load from raw resources as default/fallback
        // Try to load mtl_000_* first, then mtl_001_*, etc.
        val allHymnals = getAllLocalHymnals()
        return allHymnals.firstOrNull()
    }

    /**
     * Load all hymnals from raw resources matching pattern mtl_XXX_* where XXX is a three-digit number
     * Examples: mtl_001_english.json, mtl_002_cis.json, mtl_003_kukisong.json
     * Returns list sorted by the number in the prefix
     */
    fun getAllLocalHymnals(): List<RemoteHymnal> {
        val hymnals = mutableListOf<Pair<Int, RemoteHymnal>>()
        val adapter: JsonAdapter<RemoteHymnal> = RemoteHymnalJsonAdapter(moshi)
        
        // Use reflection to get all R.raw fields
        try {
            val rawClass = R.raw::class.java
            val fields = rawClass.fields
            
            for (field in fields) {
                val resourceName = field.name
                // Check if resource name matches pattern mtl_XXX_* where XXX is three digits
                val match = Regex("^mtl_(\\d{3})_.*").find(resourceName)
                if (match != null) {
                    val number = match.groupValues[1].toIntOrNull()
                    if (number != null) {
                        try {
                            val resId = field.getInt(null)
                            val jsonString = Helper.getJson(context.resources, resId)
                            val hymnal = adapter.fromJson(jsonString)
                            if (hymnal != null) {
                                hymnals.add(Pair(number, hymnal))
                                Timber.d("Loaded hymnal: $resourceName (Songbook #$number) - ${hymnal.title}")
                            }
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to load or parse $resourceName")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error accessing R.raw fields via reflection")
            // Fallback: try direct getIdentifier for common patterns
            for (i in 0..999) {
                val number = String.format("%03d", i)
                // Try common suffixes
                val commonSuffixes = listOf("english", "cis", "kukisong", "hymn", "songbook")
                for (suffix in commonSuffixes) {
                    val resourceName = "mtl_${number}_$suffix"
                    val resId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
                    if (resId != 0) {
                        try {
                            val jsonString = Helper.getJson(context.resources, resId)
                            val hymnal = adapter.fromJson(jsonString)
                            if (hymnal != null) {
                                hymnals.add(Pair(i, hymnal))
                                Timber.d("Loaded hymnal: $resourceName (Songbook #$number) - ${hymnal.title}")
                                break // Found one for this number, move to next
                            }
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to load or parse $resourceName")
                        }
                    }
                }
            }
        }
        
        // Sort by the number and return just the hymnals
        return hymnals.sortedBy { it.first }.map { it.second }
    }

    private suspend fun checkAuthState() {
        if (!isFirebaseAvailable) {
            throw IllegalStateException("Firebase is not available")
        }
        if (auth!!.currentUser == null) {
            val result = auth.signInAnonymously().await()
            if (result.user == null) {
                throw IllegalStateException("Could not authenticate user")
            }
        }
    }

    suspend fun listHymnals(): Resource<List<RemoteHymnal>> {
        if (!isFirebaseAvailable) {
            Timber.w("Firebase not available. Returning hymnals from raw resources.")
            // Return all hymnals from raw resources matching mtl_XXX pattern
            val localHymnals = getAllLocalHymnals()
            return if (localHymnals.isNotEmpty()) {
                Resource.success(localHymnals)
            } else {
                // Fallback to single sample if no mtl_XXX files found
                val sample = getSample()
                if (sample != null) {
                    Resource.success(listOf(sample))
                } else {
                    Resource.error(IllegalStateException("Firebase not available and no hymnals found in raw resources"))
                }
            }
        }
        
        return try {
            checkAuthState()
            val data: List<RemoteHymnal> = suspendCoroutine { continuation ->
                database!!.getReference(FOLDER)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            throw error.toException()
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val hymnals = snapshot.children.mapNotNull { child ->
                                child.key?.let { code ->
                                    child.getValue<TitleLanguage>()?.let {
                                        RemoteHymnal(code, it.title, it.language)
                                    }
                                }
                            }
                            continuation.resume(hymnals)
                        }
                    })
            }
            Resource.success(data)
        } catch (ex: Exception) {
            Timber.e(ex, "Error loading hymnals from Firebase, falling back to local data")
            // Fallback to local hymnals from raw resources
            val localHymnals = getAllLocalHymnals()
            if (localHymnals.isNotEmpty()) {
                Resource.success(localHymnals)
            } else {
                // Fallback to single sample if no mtl_XXX files found
                val sample = getSample()
                if (sample != null) {
                    Resource.success(listOf(sample))
                } else {
                    Resource.error(ex)
                }
            }
        }
    }

    suspend fun downloadHymns(code: String): Resource<List<RemoteHymn>?> {
        if (!isFirebaseAvailable) {
            Timber.w("Firebase not available. Returning hymns from raw resources.")
            // Try to find the hymnal in local resources
            val localHymnals = getAllLocalHymnals()
            val hymnal = localHymnals.find { it.key == code }
            if (hymnal != null) {
                return Resource.success(hymnal.hymns)
            }
            // Fallback to sample if not found
            val sample = getSample()
            return if (sample != null && sample.key == code) {
                Resource.success(sample.hymns)
            } else {
                Resource.error(IllegalStateException("Firebase not available and requested hymnal ($code) not found in local data"))
            }
        }
        
        return try {
            checkAuthState()

            val ref = storage!!.getReference(FOLDER)
                .child("$code.$FILE_SUFFIX")
            val localFile = createFile(code)
            val snapshot = ref.getFile(localFile).await()
            if (snapshot.error != null) {
                Timber.e(snapshot.error)
                Resource.error(snapshot.error!!)
            } else {
                val jsonString = Helper.getJson(localFile)
                val hymns = parseJson(jsonString)
                Resource.success(hymns)
            }
        } catch (ex: Exception) {
            Timber.e(ex, "Error downloading hymns from Firebase, falling back to local data")
            // Fallback to local hymnals from raw resources
            val localHymnals = getAllLocalHymnals()
            val hymnal = localHymnals.find { it.key == code }
            if (hymnal != null) {
                return Resource.success(hymnal.hymns)
            }
            // Fallback to sample if not found
            val sample = getSample()
            if (sample != null && sample.key == code) {
                Resource.success(sample.hymns)
            } else {
                Resource.error(ex)
            }
        }
    }

    private fun createFile(code: String): File = File.createTempFile(code, FILE_SUFFIX)

    private fun parseJson(jsonString: String): List<RemoteHymn>? {
        val listDataType: Type =
            Types.newParameterizedType(List::class.java, RemoteHymn::class.java)
        val adapter: JsonAdapter<List<RemoteHymn>> = moshi.adapter(listDataType)
        return adapter.fromJson(jsonString)
    }

    companion object {
        private const val FOLDER = "mtl"
        private const val FILE_SUFFIX = "json"
    }
}
