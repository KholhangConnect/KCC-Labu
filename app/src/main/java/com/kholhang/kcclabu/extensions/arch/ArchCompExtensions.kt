package com.kholhang.kcclabu.extensions.arch

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * Observe LiveData with null-safety check
 */
fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(
        owner,
        Observer {
            it?.let(observer)
        }
    )
}

/**
 * Convert MutableLiveData to read-only LiveData
 */
fun <T> MutableLiveData<T>.asLiveData() = this as LiveData<T>

/**
 * FUTURE UPGRADEABILITY NOTE:
 * 
 * To convert StateFlow to LiveData for backward compatibility, use the built-in
 * extension from lifecycle-runtime-ktx:
 * 
 * ```kotlin
 * import androidx.lifecycle.asLiveData
 * 
 * private val _data = MutableStateFlow<List<Item>>(emptyList())
 * val data: StateFlow<List<Item>> = _data.asStateFlow()
 * 
 * // Auto-converted LiveData (no manual sync needed)
 * val dataLiveData: LiveData<List<Item>> = data.asLiveData()
 * ```
 * 
 * This allows ViewModels to use StateFlow as the single source of truth,
 * while still providing LiveData for fragments that haven't been migrated to Compose yet.
 * 
 * Benefits:
 * - Single source of truth (StateFlow)
 * - Automatic synchronization (no manual postValue calls)
 * - Easier migration path to full Compose
 */
