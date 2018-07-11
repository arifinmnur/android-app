package com.kelsos.mbrc.ui.navigation.library.genres

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.genres.GenreEntity
import com.kelsos.mbrc.content.library.genres.GenreRepository
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers

class BrowseGenreViewModel(
  private val repository: GenreRepository,
  private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

  val genres: LiveData<PagedList<GenreEntity>>

  fun reload() {
    launch(dispatchers.network) {
      repository.getRemote()
    }
  }
}