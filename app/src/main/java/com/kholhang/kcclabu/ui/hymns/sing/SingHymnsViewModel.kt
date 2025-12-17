package com.kholhang.kcclabu.ui.hymns.sing

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SingHymnsViewModel @Inject constructor(
    private val repository: HymnalRepository
) : ViewModel() {

    private val mutableViewState = SingleLiveEvent<Status>()
    val statusLiveData: LiveData<Status> = mutableViewState.asLiveData()

    private val mutableHymnal = MutableLiveData<String>()
    val hymnalTitleLiveData: LiveData<String> = mutableHymnal.asLiveData()

    private val mutableHymnsList = MutableLiveData<List<Hymn>>()
    val hymnListLiveData: LiveData<List<Hymn>> get() = mutableHymnsList.asLiveData()

    fun loadData(collectionId: Int) {
        viewModelScope.launch {
            if (collectionId == -1) {
                repository.getHymns().collectLatest { resource ->
                    mutableViewState.postValue(resource.status)
                    resource.data?.let {
                        mutableHymnsList.postValue(it.hymns)
                        mutableHymnal.postValue(it.title)
                    }
                }
            } else {
                val resource = repository.getCollection(collectionId)
                mutableViewState.postValue(resource.status)
                resource.data?.let {
                    mutableHymnsList.postValue(it.hymns)
                    mutableHymnal.postValue(it.collection.title)
                }
            }
        }
    }

    fun switchHymnal(hymnal: Hymnal) {
        if (mutableHymnal.value == hymnal.title) {
            return
        }
        viewModelScope.launch {
            repository.getHymns(hymnal).collectLatest { resource ->
                mutableViewState.postValue(resource.status)
                resource.data?.let {
                    mutableHymnsList.postValue(it.hymns)
                    mutableHymnal.postValue(it.title)
                }
            }
        }
    }
}
