package com.kelsos.mbrc.features.library.albums

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.features.library.LibraryResult
import com.kelsos.mbrc.ui.BaseViewModel
import com.kelsos.mbrc.utilities.paged

class AlbumViewModel(
  repository: AlbumRepository,
  dispatchers: AppCoroutineDispatchers
) : BaseViewModel<LibraryResult>(dispatchers) {

  val albums: LiveData<PagedList<Album>> = repository.getAll().paged()
}