package com.kholhang.kcclabu.ui.hymns.sing.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kholhang.kcclabu.data.model.Hymn
import com.kholhang.kcclabu.data.model.constants.Status
import com.kholhang.kcclabu.data.repository.HymnalRepository
import com.kholhang.kcclabu.extensions.arch.SingleLiveEvent
import com.kholhang.kcclabu.extensions.arch.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditHymnViewModel @Inject constructor(
    private val repository: HymnalRepository
) : ViewModel() {

    private val mutableViewState = SingleLiveEvent<Status>()
    val statusLiveData: LiveData<Status> = mutableViewState.asLiveData()

    private val mutableEditContent = MutableLiveData<Pair<String, Boolean>>()
    val editContentLiveData: LiveData<Pair<String, Boolean>> = mutableEditContent.asLiveData()

    private var editHymn: Hymn? = null

    fun setHymn(hymn: Hymn) {
        this.editHymn = hymn

        val content = if (hymn.editedContent.isNullOrEmpty()) {
            hymn.content to false
        } else {
            (hymn.editedContent ?: "") to true
        }
        mutableEditContent.postValue(content)
    }

    fun saveContent(content: String) {
        if (content.isNotEmpty()) {
            editHymn?.let { hymn ->

                // remove extra break lines added by aztec
                val suffix = "<br>"
                var parsed = content
                while (parsed.endsWith(suffix)) {
                    parsed = parsed.dropLast(suffix.length)
                }

                viewModelScope.launch {
                    hymn.editedContent = parsed
                    repository.updateHymn(hymn)

                    mutableViewState.postValue(Status.SUCCESS)
                }
            }
        } else {
            mutableViewState.postValue(Status.ERROR)
        }
    }

    fun undoChanges() {
        editHymn?.let { hymn ->
            viewModelScope.launch {
                hymn.editedContent = null
                repository.updateHymn(hymn)

                mutableViewState.postValue(Status.SUCCESS)
            }
        }
    }
}
