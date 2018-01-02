package com.kelsos.mbrc.ui.navigation.library.artistalbums

import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.mvp.BaseView
import com.kelsos.mbrc.mvp.Presenter

interface ArtistAlbumsView : BaseView {
  fun update(albums: List<AlbumEntity>)
  fun queue(success: Boolean, tracks: Int)
}

interface ArtistAlbumsPresenter : Presenter<ArtistAlbumsView> {
  fun load(artist: String)
  fun queue(@LibraryPopup.Action action: String, album: AlbumEntity)
}

