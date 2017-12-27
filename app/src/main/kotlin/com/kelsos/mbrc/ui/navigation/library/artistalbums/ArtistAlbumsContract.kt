package com.kelsos.mbrc.ui.navigation.library.artistalbums

import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.content.nowplaying.queue.Queue
import com.kelsos.mbrc.mvp.BaseView
import com.kelsos.mbrc.mvp.Presenter

interface ArtistAlbumsView : BaseView {
  fun update(albums: PagedList<AlbumEntity>)
  fun queue(success: Boolean, tracks: Int)
}

interface ArtistAlbumsPresenter : Presenter<ArtistAlbumsView> {
  fun load(artist: String)
  fun queue(@Queue.Action action: String, album: AlbumEntity)
}

