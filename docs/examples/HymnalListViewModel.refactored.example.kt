package com.kholhang.kcclabu.ui.hymns.hymnals

import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kholhang.kcclabu.data.model.Hymnal
import com.kholhang.kcclabu.data.repository.HymnalRepository
import com.kholhang.kcclabu.data.repository.RemoteHymnsRepository
import com.kholhang.kcclabu.extensions.connectivity.isConnected
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * REFACTORED EXAMPLE: Using StateFlow as Single Source of Truth
 * 
 * This is an example of how to refactor ViewModels for future upgradeability.
 * 
 * Key Changes:
 * 1. StateFlow is the ONLY source of truth
 * 2. LiveData is auto-converted from StateFlow (no manual sync needed)
 * 3. Only update StateFlow - LiveData updates automatically
 * 4. Easier migration path to full Compose
 */
@HiltViewModel
class HymnalListViewModelRefactored @Inject constructor(
    private val remoteHymnsRepository: RemoteHymnsRepository,
    private val repository: HymnalRepository,
    private val connectivityManager: ConnectivityManager,
    private val prefs: HymnalPrefs
) : ViewModel() {

    // SINGLE SOURCE OF TRUTH: StateFlow
    private val _hymnalList = MutableStateFlow<List<Hymnal>>(emptyList())
    val hymnalList: StateFlow<List<Hymnal>> = _hymnalList.asStateFlow()
    
    // AUTO-CONVERTED LiveData for backward compatibility (no manual sync needed!)
    // Uses built-in asLiveData() from androidx.lifecycle.asLiveData
    val hymnalListLiveData: LiveData<List<Hymnal>> = hymnalList.asLiveData()

    fun loadData() {
        // Always try to load local hymnals first, then try remote if available
        loadLocalHymnals()
        
        // If connected, also try to load remote hymnals (will fallback to default data if Firebase unavailable)
        if (connectivityManager.isConnected) {
            loadRemoteHymnals()
        }
    }

    private fun loadLocalHymnals() {
        viewModelScope.launch {
            val resource = repository.getHymnals()
            resource.data?.forEach {
                it.selected = it.code == prefs.getSelectedHymnal()
            }
            val list = resource.data ?: emptyList()
            // ONLY update StateFlow - LiveData updates automatically!
            _hymnalList.value = list
            // No need for: mutableHymnalList.postValue(list) ❌
        }
    }

    private fun loadRemoteHymnals() {
        viewModelScope.launch {
            val resource = remoteHymnsRepository.listHymnals()
            if (resource.isSuccessFul && resource.data != null) {
                val localHymnals = repository.getHymnals().data ?: emptyList()
                val hymnals = resource.data.sortedBy { it.title }
                val list = hymnals.map { remote ->
                    Hymnal(
                        remote.key,
                        remote.title,
                        remote.language
                    ).also { hymnal ->
                        hymnal.offline = localHymnals.find { it.code == remote.key } != null
                        hymnal.selected = hymnal.code == prefs.getSelectedHymnal()
                    }
                }
                // ONLY update StateFlow - LiveData updates automatically!
                _hymnalList.value = list
                // No need for: mutableHymnalList.postValue(list) ❌
            }
            // If remote loading fails, keep the local hymnals that were already loaded
        }
    }
}

/**
 * COMPARISON: Before vs After
 * 
 * BEFORE (Current - Duplicate State):
 * ```kotlin
 * private val _hymnalList = MutableStateFlow<List<Hymnal>>(emptyList())
 * val hymnalList: StateFlow<List<Hymnal>> = _hymnalList.asStateFlow()
 * 
 * private val mutableHymnalList = MutableLiveData<List<Hymnal>>()
 * val hymnalListLiveData: LiveData<List<Hymnal>> = mutableHymnalList.asLiveData()
 * 
 * // Must update both manually
 * _hymnalList.value = list
 * mutableHymnalList.postValue(list) // Manual sync required
 * ```
 * 
 * AFTER (Refactored - Single Source):
 * ```kotlin
 * private val _hymnalList = MutableStateFlow<List<Hymnal>>(emptyList())
 * val hymnalList: StateFlow<List<Hymnal>> = _hymnalList.asStateFlow()
 * 
 * // Auto-converted - no manual sync needed
 * val hymnalListLiveData: LiveData<List<Hymnal>> = hymnalList.asLiveData()
 * 
 * // Only update StateFlow
 * _hymnalList.value = list // LiveData updates automatically!
 * ```
 * 
 * BENEFITS:
 * ✅ Single source of truth (StateFlow)
 * ✅ No duplicate state management
 * ✅ No manual synchronization
 * ✅ Less code to maintain
 * ✅ Easier migration to Compose
 * ✅ Type-safe and modern
 */

