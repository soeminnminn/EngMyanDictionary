package com.s16.engmyan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DictionaryItem::class, ConvertedItem::class, FavoriteItem::class, HistoryItem::class], version = 2)
abstract class DbManager: RoomDatabase() {
    abstract fun provider(): DataAccess

    companion object {
        @Volatile private var instance: DbManager? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            DbManager::class.java,
            "dictionary"
        ).createFromAsset("database/dictionary.db")
            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""CREATE TABLE IF NOT EXISTS `converted` 
                        (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `refrence_id` INTEGER, 
                        `mode` TEXT, `value` TEXT, `timestamp` INTEGER)""")

                database.execSQL("""CREATE TABLE IF NOT EXISTS `favorites` 
                        (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT, `refrence_id` INTEGER, 
                        `timestamp` INTEGER)""")

                database.execSQL("""CREATE TRIGGER IF NOT EXISTS `limit_favorites` AFTER INSERT ON `favorites`
                        FOR EACH ROW 
                        WHEN (SELECT COUNT(`_id`) FROM `favorites`) > 100 
                        BEGIN 
                        DELETE FROM `favorites` 
                        WHERE `timestamp` IS (SELECT MIN(`timestamp`) FROM `favorites`);
                        END""")

                database.execSQL("""CREATE TABLE IF NOT EXISTS `histories` 
                    (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT, `refrence_id` INTEGER, 
                    `timestamp` INTEGER)""")

                database.execSQL("""CREATE TRIGGER IF NOT EXISTS `limit_histories` AFTER INSERT ON `histories`
                        FOR EACH ROW
                        WHEN (SELECT COUNT(`_id`) FROM `histories`) > 100
                        BEGIN 
                        DELETE FROM `histories`
                        WHERE `timestamp` IS (SELECT MIN(`timestamp`) FROM `histories`);
                        END""")
            }
        }
    }
}