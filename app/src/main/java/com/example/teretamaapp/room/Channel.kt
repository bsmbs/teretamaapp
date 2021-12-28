package com.example.teretamaapp.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "channel_table")
data class Channel(
    var name: String,
    val description: String,
    var imageUri: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
    )

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channel_table")
    fun getAll(): Flow<List<Channel>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg channels: Channel)

    @Update
    suspend fun update(channel: Channel)

    @Delete
    suspend fun delete(channel: Channel)

    @Query("DELETE FROM channel_table")
    suspend fun deleteAll()

}

