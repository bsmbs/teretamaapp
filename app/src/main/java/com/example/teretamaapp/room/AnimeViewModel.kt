package com.example.teretamaapp.room

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class AnimeViewModel(private val repository: AppRepository): ViewModel() {
    val anime: LiveData<List<Anime>> = repository.anime.asLiveData()

    fun getByChannel(id: Int): LiveData<List<Anime>> = repository.getAnimeByChannel(id).asLiveData()

    fun insert(anime: Anime) = viewModelScope.launch {
        repository.insertAnime(anime)
    }

    fun update(anime: Anime) = viewModelScope.launch {
        repository.updateAnime(anime)
    }

    fun delete(anime: Anime) = viewModelScope.launch {
        repository.deleteAnime(anime)
    }
}

class AnimeViewModelFactory(private val repository: AppRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnimeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnimeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}