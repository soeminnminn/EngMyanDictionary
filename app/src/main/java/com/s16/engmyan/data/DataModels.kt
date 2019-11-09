package com.s16.engmyan.data

import androidx.lifecycle.*
import com.s16.app.SingletonHolder

class DictionaryModel(private val provider: DataAccess): ViewModel() {
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

    @Suppress("UNCHECKED_CAST")
    class Factory(private val provider: DataAccess): ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DictionaryModel(provider) as T
        }

    }

    companion object: SingletonHolder<Factory, DataAccess>({ Factory(it) })
}

class DefinitionModel(private val provider: DataAccess): ViewModel() {
    private val idData = MutableLiveData<Long>()

    var data: LiveData<DefinitionItem> = Transformations.switchMap(idData) { id ->
        provider.queryDefinition(id)
    }

    fun fetch(id: Long) {
        idData.value = id
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val provider: DataAccess): ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DefinitionModel(provider) as T
        }

    }

    companion object: SingletonHolder<Factory, DataAccess>({ Factory(it) })
}

class FavoriteModel(provider: DataAccess): ViewModel() {

    var data : LiveData<List<FavoriteItem>> = provider.queryFavorites()

    @Suppress("UNCHECKED_CAST")
    class Factory(private val provider: DataAccess): ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FavoriteModel(provider) as T
        }

    }

    companion object: SingletonHolder<Factory, DataAccess>({ Factory(it) })
}

class RecentModel(provider: DataAccess): ViewModel() {

    var data : LiveData<List<HistoryItem>> = provider.queryHistories()

    @Suppress("UNCHECKED_CAST")
    class Factory(private val provider: DataAccess): ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RecentModel(provider) as T
        }

    }

    companion object: SingletonHolder<Factory, DataAccess>({ Factory(it) })
}