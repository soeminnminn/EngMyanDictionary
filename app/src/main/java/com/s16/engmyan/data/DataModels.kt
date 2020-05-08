package com.s16.engmyan.data

import android.app.Application
import androidx.lifecycle.*

class DictionaryModel(application: Application): AndroidViewModel(application) {
    private val provider: DataAccess = DbManager(application).provider()

    private val filterData = MutableLiveData<String?>()

    var data: LiveData<List<DictionaryItem>> = Transformations.switchMap(filterData) { constraint ->
        if (constraint == null || constraint.isEmpty()) {
            provider.querySuggestWord()
        } else {
            var searchWord = constraint.replace("'", "''").replace("[%\\-]".toRegex(), "").trim()
            if (searchWord.indexOf('*') > -1 || searchWord.indexOf('?') > -1) {
                searchWord = searchWord.replace('?', '_')
                searchWord = searchWord.replace('*', '%')
            } else {
                searchWord = "$searchWord%"
            }
            provider.query(searchWord)
        }
    }

    init {
        filterData.value = null
    }

    fun filter(constraint: String? = "") {
        filterData.value = constraint
    }
}

class DefinitionModel(application: Application): AndroidViewModel(application) {
    private val provider: DataAccess = DbManager(application).provider()

    private val idData = MutableLiveData<Long>()

    var data: LiveData<DefinitionItem> = Transformations.switchMap(idData) { id ->
        provider.queryDefinition(id)
    }

    fun fetch(id: Long) {
        idData.value = id
    }
}

class FavoriteModel(application: Application): AndroidViewModel(application) {
    private val provider: DataAccess = DbManager(application).provider()

    var data : LiveData<List<FavoriteItem>> = provider.queryFavorites()
}

class RecentModel(application: Application): AndroidViewModel(application) {
    private val provider: DataAccess = DbManager(application).provider()

    var data : LiveData<List<HistoryItem>> = provider.queryHistories()
}