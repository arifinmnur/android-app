package com.kelsos.mbrc.ui.navigation.library.tracks

import com.kelsos.mbrc.content.library.tracks.TrackEntity
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.mvp.BaseView
import com.kelsos.mbrc.mvp.Presenter

interface BrowseTrackView : BaseView {
  fun update(list: List<TrackEntity>)
  fun search(term: String)
  fun queue(success: Boolean, tracks: Int)
  fun hideLoading()
}

interface BrowseTrackPresenter : Presenter<BrowseTrackView> {
  fun load()
  fun sync()
  fun queue(track: TrackEntity, @LibraryPopup.Action action: String? = null)
}
