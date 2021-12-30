package com.example.teretamaapp.room

import androidx.lifecycle.*
import kotlinx.coroutines.launch

enum class Sort {
    DEFAULT, TITLE, YEAR, TITLE_DESC, YEAR_DESC
}

class AnimeViewModel(private val repository: AppRepository): ViewModel() {
    private val channelId = MutableLiveData<Int>()
    private val sort = MutableLiveData<Sort>()

    val anime: LiveData<List<Anime>> = Transformations.switchMap(channelId) {
        channel -> repository.getAnimeByChannel(channel).asLiveData()
    }

    val sortedAnime: LiveData<List<Anime>> = Transformations.switchMap(anime) { anime ->
        Transformations.map(sort) { sorting ->
            when (sorting) {
                Sort.TITLE -> anime.sortedBy { it.title }
                Sort.YEAR -> anime.sortedBy { it.releaseYear }
                Sort.TITLE_DESC -> anime.sortedBy { it.title }.reversed()
                Sort.YEAR_DESC -> anime.sortedBy { it.releaseYear }.reversed()
                else -> anime
            }
        }
    }

    fun setSort(newSort: Sort) {
        sort.value = newSort
    }

    fun setChannel(id: Int) {
        channelId.value = id
    }

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