package com.example.teretamaapp.room

import androidx.lifecycle.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChannelViewModel(private val repository: AppRepository): ViewModel() {
    val channels: LiveData<List<Channel>> = repository.channels.asLiveData()

    fun insertAll(vararg channels: Channel) = viewModelScope.launch {
        repository.insertAll(*channels)
    }

    fun update(channel: Channel) = viewModelScope.launch {
        repository.update(channel)
    }

    fun autoDelete(channel: Channel) = viewModelScope.launch {
        repository.delete(channel)
        repository.deleteAnimeFromChannel(channel.id)
    }

    fun delete(channel: Channel) = viewModelScope.launch {
        repository.delete(channel)
    }
}

class ChannelViewModelFactory(private val repository: AppRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChannelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChannelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}