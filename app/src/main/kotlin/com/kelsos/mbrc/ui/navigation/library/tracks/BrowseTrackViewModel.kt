package com.kelsos.mbrc.ui.navigation.library.tracks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.tracks.TrackEntity
import com.kelsos.mbrc.content.library.tracks.TrackRepository
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers

class BrowseTrackViewModel(
  private val repository: TrackRepository,
  private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

  val tracks: LiveData<PagedList<TrackEntity>>

  fun reload() {
    launch(dispatchers.network) {
      repository.getRemote()
    }
  }
}