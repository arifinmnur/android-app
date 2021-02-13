package com.kelsos.mbrc.features.library.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.features.library.data.Track
import com.kelsos.mbrc.features.library.repositories.TrackRepository
import com.kelsos.mbrc.utilities.paged

class TrackViewModel(
  repository: TrackRepository
) : ViewModel() {

  val tracks: LiveData<PagedList<Track>> = repository.getAll().paged()
}