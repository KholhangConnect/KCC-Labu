package com.tinashe.hymnal.ui.collections

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinashe.hymnal.data.model.TitleBody
import com.tinashe.hymnal.data.model.collections.CollectionHymns
import com.tinashe.hymnal.data.model.collections.HymnCollection
import com.tinashe.hymnal.data.repository.HymnalRepository
import com.tinashe.hymnal.extensions.arch.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val repository: HymnalRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.LOADING)
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()
    
    // Keep LiveData for fragments that haven't been migrated yet
    private val mutableViewState = MutableLiveData<ViewState>()
    val viewStateLiveData: LiveData<ViewState> = mutableViewState.asLiveData()

    private val _collections = MutableStateFlow<List<CollectionHymns>>(emptyList())
    val collections: StateFlow<List<CollectionHymns>> = _collections.asStateFlow()
    
    // Keep LiveData for fragments that haven't been migrated yet
    private val mutableCollections = MutableLiveData<List<CollectionHymns>>()
    val collectionsLiveData: LiveData<List<CollectionHymns>> = mutableCollections.asLiveData()

    private var collectionToDelete: Pair<Int, CollectionHymns>? = null

    init {
        loadData()
    }

    fun loadData() {
        _viewState.value = ViewState.LOADING
        mutableViewState.postValue(ViewState.LOADING)
        viewModelScope.launch {
            repository.getCollectionHymns().collect { collections ->
                _collections.value = collections
                mutableCollections.postValue(collections)
                _viewState.value = ViewState.HAS_RESULTS
                mutableViewState.postValue(ViewState.HAS_RESULTS)
            }
        }
    }

    fun performSearch(query: String?) {
        viewModelScope.launch {
            val collections = repository.searchCollections(query)
            _collections.value = collections
            mutableCollections.postValue(collections)
            val state = if (collections.isNotEmpty() || query.isNullOrEmpty()) {
                ViewState.HAS_RESULTS
            } else {
                ViewState.NO_RESULTS
            }
            _viewState.value = state
            mutableViewState.postValue(state)
        }
    }

    fun addCollection(content: TitleBody) {
        viewModelScope.launch {
            repository.addCollection(content)
        }
    }

    fun updateHymnCollections(hymnId: Int, collection: HymnCollection, add: Boolean) {
        viewModelScope.launch {
            repository.updateHymnCollections(hymnId, collection.collectionId, add)
        }
    }

    fun onIntentToDelete(position: Int) {
        val data = _collections.value.toMutableList()
        collectionToDelete = position to data.removeAt(position)
        _collections.value = data
        mutableCollections.postValue(data)
        _viewState.value = ViewState.HAS_RESULTS
        mutableViewState.postValue(ViewState.HAS_RESULTS)
    }

    fun undoDelete() {
        val data = _collections.value.toMutableList()
        val pair = collectionToDelete ?: return
        data.add(pair.first, pair.second)
        _collections.value = data
        mutableCollections.postValue(data)
        _viewState.value = ViewState.HAS_RESULTS
        mutableViewState.postValue(ViewState.HAS_RESULTS)
        collectionToDelete = null
    }

    fun deleteConfirmed() {
        collectionToDelete?.let {
            viewModelScope.launch {
                repository.deleteCollection(it.second)
                collectionToDelete = null
            }
        }
    }
}
