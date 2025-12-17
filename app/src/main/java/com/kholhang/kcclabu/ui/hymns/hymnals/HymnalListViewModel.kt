package com.kholhang.kcclabu.ui.hymns.hymnals

import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kholhang.kcclabu.data.model.Hymnal
import com.kholhang.kcclabu.data.repository.HymnalRepository
import com.kholhang.kcclabu.data.repository.RemoteHymnsRepository
import com.kholhang.kcclabu.extensions.arch.asLiveData
import com.kholhang.kcclabu.extensions.connectivity.isConnected
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HymnalListViewModel @Inject constructor(
    private val remoteHymnsRepository: RemoteHymnsRepository,
    private val repository: HymnalRepository,
    private val connectivityManager: ConnectivityManager,
    private val prefs: HymnalPrefs
) : ViewModel() {

    private val _hymnalList = MutableStateFlow<List<Hymnal>>(emptyList())
    val hymnalList: StateFlow<List<Hymnal>> = _hymnalList.asStateFlow()
    
    // Keep LiveData for fragments that haven't been migrated yet
    private val mutableHymnalList = MutableLiveData<List<Hymnal>>()
    val hymnalListLiveData: LiveData<List<Hymnal>> = mutableHymnalList.asLiveData()

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
            _hymnalList.value = list
            mutableHymnalList.postValue(list)
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
                // Update the list with remote data (which may include default data if Firebase unavailable)
                _hymnalList.value = list
                mutableHymnalList.postValue(list)
            }
            // If remote loading fails, keep the local hymnals that were already loaded
        }
    }
}
