package com.example.teretamaapp.room

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class AppRepository(private val channelDao: ChannelDao, private val animeDao: AnimeDao) {
    var channels: Flow<List<Channel>> = channelDao.getAll()
    var anime: Flow<List<Anime>> = animeDao.getAll()

    fun getAnimeByChannel(channelId: Int): Flow<List<Anime>> {
        return animeDao.getByChannel(channelId)
    }

    @WorkerThread
    suspend fun insertAnime(anime: Anime) {
        animeDao.insertAll(anime)
    }

    @WorkerThread
    suspend fun updateAnime(anime: Anime) {
        animeDao.update(anime)
    }

    @WorkerThread
    suspend fun deleteAnime(anime: Anime) {
        animeDao.delete(anime)
    }

    @WorkerThread
    suspend fun insertAll(vararg channels: Channel) {
        channelDao.insertAll(*channels)
    }

    @WorkerThread
    suspend fun update(channel: Channel) {
        channelDao.update(channel)
    }

    @WorkerThread
    suspend fun delete(channel: Channel) {
        channelDao.delete(channel)
    }
}