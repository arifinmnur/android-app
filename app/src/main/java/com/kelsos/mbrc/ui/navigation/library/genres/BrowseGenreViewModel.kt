package com.kelsos.mbrc.ui.navigation.library.genres

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.genres.Genre
import com.kelsos.mbrc.content.library.genres.GenreRepository
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BrowseGenreViewModel(
  private val repository: GenreRepository,
  private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {
  private val viewModelJob: Job = Job()
  private val networkScope = CoroutineScope(dispatchers.network + viewModelJob)

  val genres: LiveData<PagedList<Genre>>

  init {
    this.genres = repository.getAll().paged()
  }

  fun reload() {
    networkScope.launch {
      repository.getRemote()
    }
  }

  override fun onCleared() {
    viewModelJob.cancel()
    super.onCleared()
  }
}