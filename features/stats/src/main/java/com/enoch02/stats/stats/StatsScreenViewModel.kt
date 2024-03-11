package com.enoch02.stats.stats

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.dao.StatsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsScreenViewModel @Inject constructor(private val statsDao: StatsDao) : ViewModel() {
    val total = mutableIntStateOf(0)
    val completed = mutableIntStateOf(0)

    suspend fun get() {
        viewModelScope.launch(Dispatchers.IO) {
            total.value = statsDao.getTotalBooks()
            completed.value = statsDao.getCompletedBooks()
        }
    }
}