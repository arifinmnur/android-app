package com.kelsos.mbrc.ui.navigation.library.genreartists

import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.mvp.BaseView
import com.kelsos.mbrc.mvp.Presenter

interface GenreArtistsView : BaseView {
  fun update(pagedList: PagedList<ArtistEntity>)
  fun queue(success: Boolean, tracks: Int)
}

interface GenreArtistsPresenter : Presenter<GenreArtistsView> {
  fun load(genre: String)
  fun queue(@LibraryPopup.Action action: String, entry: ArtistEntity)
}