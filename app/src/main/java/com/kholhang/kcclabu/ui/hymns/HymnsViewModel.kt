package com.kholhang.kcclabu.ui.hymns

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kholhang.kcclabu.data.model.Hymn
import com.kholhang.kcclabu.data.model.Hymnal
import com.kholhang.kcclabu.data.model.constants.Status
import com.kholhang.kcclabu.data.repository.HymnalRepository
import com.kholhang.kcclabu.extensions.arch.SingleLiveEvent
import com.kholhang.kcclabu.extensions.arch.asLiveData
import com.kholhang.kcclabu.extensions.prefs.HymnalPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HymnsViewModel @Inject constructor(
    private val repository: HymnalRepository,
    private val prefs: HymnalPrefs
) : ViewModel() {

    private val _showHymnalsPrompt = MutableStateFlow<Boolean>(false)
    val showHymnalsPrompt: StateFlow<Boolean> = _showHymnalsPrompt.asStateFlow()
    
    // Keep LiveData for fragments that haven't been migrated yet
    private val mutableShowHymnalsPrompt = SingleLiveEvent<Any>()
    val showHymnalsPromptLiveData: LiveData<Any> = mutableShowHymnalsPrompt.asLiveData()

    private val _status = MutableStateFlow<Status>(Status.LOADING)
    val status: StateFlow<Status> = _status.asStateFlow()
    
    // Keep LiveData for fragments that haven't been migrated yet
    private val mutableViewState = SingleLiveEvent<Status>()
    val statusLiveData: LiveData<Status> = mutableViewState.asLiveData()

    private val _hymnalTitle = MutableStateFlow<String>("")
    val hymnalTitle: StateFlow<String> = _hymnalTitle.asStateFlow()
    
    // Keep LiveData for fragments that haven't been migrated yet
    private val mutableHymnal = MutableLiveData<String>()
    val hymnalTitleLiveData: LiveData<String> = mutableHymnal.asLiveData()

    private val _hymnsList = MutableStateFlow<List<Hymn>>(emptyList())
    val hymnsList: StateFlow<List<Hymn>> = _hymnsList.asStateFlow()
    
    // Keep LiveData for fragments that haven't been migrated yet
    private val mutableHymnsList: MutableLiveData<List<Hymn>> = MutableLiveData()
    val hymnListLiveData: LiveData<List<Hymn>> = mutableHymnsList.asLiveData()

    init {
        try {
            fetchData()
        } catch (e: Exception) {
            Timber.e(e, "Error initializing HymnsViewModel")
            _status.value = Status.ERROR
        }
    }

    fun hymnalSelected(hymnal: Hymnal) {
        fetchData(hymnal)
    }

    private fun fetchData(hymnal: Hymnal? = null) {
        viewModelScope.launch {
            try {
                repository.getHymns(hymnal).catch { e ->
                    Timber.e(e, "Error fetching hymns")
                    _status.value = Status.ERROR
                    mutableViewState.postValue(Status.ERROR)
                }.collectLatest { resource ->
                    _status.value = resource.status
                    mutableViewState.postValue(resource.status)
                    _hymnsList.value = resource.data?.hymns ?: emptyList()
                    mutableHymnsList.postValue(resource.data?.hymns ?: emptyList())
                    resource.data?.title?.let {
                        _hymnalTitle.value = it
                        mutableHymnal.postValue(it)

                        if (!prefs.isHymnalPromptSeen()) {
                            withContext(Dispatchers.Main) {
                                _showHymnalsPrompt.value = true
                                mutableShowHymnalsPrompt.call()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in fetchData")
                _status.value = Status.ERROR
                mutableViewState.postValue(Status.ERROR)
            }
        }
    }

    fun performSearch(query: String?) {
        viewModelScope.launch {
            val results = repository.searchHymns(query)
            _hymnsList.value = results
            mutableHymnsList.postValue(results)
        }
    }

    fun hymnalsPromptShown() {
        prefs.setHymnalPromptSeen()
        _showHymnalsPrompt.value = false
    }
}
