package com.s16.engmyan.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DataAccess {

    // Dictionary
    @Query("SELECT _id, word, stripword FROM dictionary ORDER BY stripword ASC LIMIT :suggestLimit")
    fun querySuggestWord(suggestLimit: Int = 100): LiveData<List<DictionaryItem>>

    @Query("SELECT _id, word, stripword FROM dictionary WHERE stripword LIKE :searchWord ORDER BY stripword ASC LIMIT :limit")
    fun query(searchWord: String, limit: Int = 1000): LiveData<List<DictionaryItem>>

    @Query("SELECT _id FROM dictionary WHERE stripword LIKE :searchWord LIMIT 1")
    fun queryId(searchWord: String): Long

    @Query("""SELECT dictionary.*, converted.value AS def_zawgyi, favorites._id AS favorite_id 
        FROM dictionary 
        LEFT JOIN converted ON dictionary._id = converted.refrence_id AND mode = 'zawgyi'
        LEFT JOIN favorites ON dictionary._id = favorites.refrence_id
        WHERE dictionary._id IS :id LIMIT 1""")
    fun queryDefinition(id: Long): LiveData<DefinitionItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConverted(value: ConvertedItem): Long

    // Favorite
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorite(value: FavoriteItem): Long

    @Query("DELETE FROM favorites WHERE _id IS :id")
    fun deleteFavorite(id: Long)

    @Query("DELETE FROM favorites WHERE refrence_id IS :refId")
    fun deleteFavoriteByRef(refId: Long)

    @Transaction
    fun deleteFavoriteAll(ids: List<Long?>) {
        ids.forEach { pid ->
            if (pid != null) {
                deleteFavoriteByRef(pid)
            }
        }
    }

    @Query("SELECT * FROM favorites")
    fun queryFavorites() : LiveData<List<FavoriteItem>>

    @Query("SELECT * FROM favorites WHERE refrence_id IS :refId LIMIT 1")
    fun queryFavoriteByRef(refId: Long) : FavoriteItem?

    // History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHistory(value: HistoryItem): Long

    @Query("DELETE FROM histories WHERE _id IS :id")
    fun deleteHistory(id: Long)

    @Query("DELETE FROM histories")
    fun deleteAllHistory()

    @Query("SELECT * FROM histories")
    fun queryHistories() : LiveData<List<HistoryItem>>

    @Query("SELECT * FROM histories WHERE refrence_id IS :refId LIMIT 1")
    fun queryHistoryByRef(refId: Long) : HistoryItem?

    @Transaction
    fun createHistory(word: String, refId: Long) : Long {
        val item = queryHistoryByRef(refId) ?: HistoryItem(word = word, refId = refId)
        item.timestamp = System.currentTimeMillis()
        return insertHistory(item)
    }
}