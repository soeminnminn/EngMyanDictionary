package com.s16.engmyan.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary")
data class DictionaryItem(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    var id: Long,
    @ColumnInfo(index = true)
    var word: String? = null,
    @ColumnInfo(name = "stripword", index = true)
    var stripWord: String? = null,
    var title: String? = null,
    var definition: String? = null,
    var keywords: String? = null,
    var synonym: String? = null,
    var picture: String? = null
) {
    fun getImage() : Bitmap? {
        return if (picture != null) {
            val pic = picture!!.replace("^data:image\\/[\\S]+;base64,[\\s]+(.*)\$".toRegex(), "$1")
            val decodedString = Base64.decode(pic, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } else {
            null
        }
    }
}

@Entity(tableName = "converted")
data class ConvertedItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0,
    @ColumnInfo(name = "refrence_id")
    var refId: Long? = null,
    var mode: String? = null,
    var value: String? = null,
    var timestamp: Long? = null
)

@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0,
    var word: String? = null,
    @ColumnInfo(name = "refrence_id")
    var refId: Long? = null,
    var timestamp: Long? = null
)

@Entity(tableName = "histories")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0,
    var word: String? = null,
    @ColumnInfo(name = "refrence_id")
    var refId: Long? = null,
    var timestamp: Long? = null
)

data class DefinitionItem(
    @Embedded
    var dictionary: DictionaryItem? = null,

    @ColumnInfo(name = "def_zawgyi")
    var convertedZawgyi: String? = null,

    @ColumnInfo(name = "favorite_id")
    var favoriteId: Long? = 0
) {
    val id: Long
        get() = dictionary?.id ?: 0

    val word: String?
        get() = dictionary?.word

    val title: String?
        get() = dictionary?.title

    val definition: String?
        get() = dictionary?.definition

    val keywords: String?
        get() = dictionary?.keywords

    val synonym: String?
        get() = dictionary?.synonym

    val hasImage: Boolean
        get() = (dictionary?.picture ?: "").isNotEmpty()

    val image: Bitmap?
        get() = dictionary?.getImage()

    val isFavorite: Boolean
        get() = (favoriteId ?: 0L) != 0L
}