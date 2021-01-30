package com.example.oyunmerkezi3.database

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GamesViewModelFactory(
    private val dataSource: GameDatabaseDao,
    private val application: Application,
    private val platform:String) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamesViewModel::class.java)) {
            return GamesViewModel(dataSource, application,platform) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}