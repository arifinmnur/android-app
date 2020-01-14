package com.kelsos.mbrc.features.library.tracks

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.features.library.LibraryResult
import com.kelsos.mbrc.ui.BaseViewModel
import com.kelsos.mbrc.utilities.paged

class TrackViewModel(
  repository: TrackRepository,
  dispatchers: AppCoroutineDispatchers
) : BaseViewModel<LibraryResult>(dispatchers) {

  val tracks: LiveData<PagedList<Track>> = repository.getAll().paged()
}