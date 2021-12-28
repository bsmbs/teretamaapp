package com.example.teretamaapp.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "anime_table")
data class Anime(
    var channelId: Int,
    val anilistId: Int,
    val title: String,
    var imageUri: String,
    val releaseYear: Int = -1,
    val episodeCount: Int = -1,
    val studios: String = "Unknown",
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Dao
interface AnimeDao {
    @Query("SELECT * FROM anime_table WHERE channelId=:channelId")
    fun getByChannel(channelId: Int): Flow<List<Anime>>

    @Query("SELECT * FROM anime_table")
    fun getAll(): Flow<List<Anime>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg anime: Anime)

    @Update
    suspend fun update(anime: Anime)

    @Delete
    suspend fun delete(anime: Anime)

    @Query("DELETE FROM anime_table")
    suspend fun deleteAll()
}