package com.kelsos.mbrc.ui.navigation.library.tracks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.tracks.Track
import com.kelsos.mbrc.content.library.tracks.TrackRepository
import com.kelsos.mbrc.utilities.paged

class TrackViewModel(
  repository: TrackRepository,
) : ViewModel() {
  val tracks: LiveData<PagedList<Track>> = repository.getAll().paged()
}