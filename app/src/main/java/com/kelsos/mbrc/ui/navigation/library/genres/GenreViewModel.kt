package com.kelsos.mbrc.ui.navigation.library.genres

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.genres.Genre
import com.kelsos.mbrc.content.library.genres.GenreRepository
import com.kelsos.mbrc.utilities.paged

class GenreViewModel(
  repository: GenreRepository
) : ViewModel() {
  val genres: LiveData<PagedList<Genre>> = repository.getAll().paged()
}