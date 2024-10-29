package com.enoch02.stats.stats

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
    val categories = mutableStateOf(CategoriesStats())

    fun get() {
        viewModelScope.launch(Dispatchers.IO) {
            total.intValue = statsDao.getTotalBooks()
            completed.intValue = statsDao.getCompletedBooks()
            categories.value = CategoriesStats(
                mangaCount = statsDao.getMangaCount(),
                lnCount = statsDao.getLNCount(),
                comicCount = statsDao.getComicCount(),
                novelCount = statsDao.getNovelCount(),
                anyCount = statsDao.getAnyTypeCount(),
            )
        }
    }
}

data class CategoriesStats(
    val mangaCount: Int = 0,
    val lnCount: Int = 0,
    val comicCount: Int = 0,
    val novelCount: Int = 0,
    val anyCount: Int = 0
)