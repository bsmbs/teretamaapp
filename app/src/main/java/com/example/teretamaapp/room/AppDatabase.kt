package com.example.teretamaapp.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Channel::class, Anime::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun animeDao(): AnimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "teretama_dbtest"
                    )
                        .fallbackToDestructiveMigration()
                        .addCallback(AppDatabaseCallback(context, scope))
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    private class AppDatabaseCallback(val context: Context, val scope: CoroutineScope): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.channelDao())
                }
            }
        }

        fun drawableToUri(name: String): String {
            return "android.resource://"+context.packageName+"/drawable/"+name
        }

        // Populate database with entries on first run
        suspend fun populateDatabase(channelDao: ChannelDao) {
            channelDao.deleteAll()

            // Channels

            channelDao.insertAll(
                Channel("Tokyo MX", "Tokyo MX", drawableToUri("tokyo_mx")),
                Channel("MBS", "毎日放送", drawableToUri("station_mbs")),
                Channel("Teretama", "TV Saitama", drawableToUri("station_teretama")),
                Channel("TBS", "BS-i [*]", drawableToUri("station_tbs")),
                Channel("Fuji TV", "noitaminA", drawableToUri("station_fuji")),
                Channel("TV Tokyo", "TV Tokyo", drawableToUri("station_tereto")),
                Channel("BS11", "BS11", drawableToUri("station_bs11")),
                Channel("TV Kanagawa", "Placeholder for unknown 2007 tv rips", drawableToUri("station_tvk"))
            )
        }
    }

}