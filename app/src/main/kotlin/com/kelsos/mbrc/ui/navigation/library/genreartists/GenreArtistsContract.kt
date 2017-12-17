package com.kelsos.mbrc.ui.navigation.library.genreartists

import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.content.nowplaying.queue.Queue
import com.kelsos.mbrc.mvp.BaseView
import com.kelsos.mbrc.mvp.Presenter

interface GenreArtistsView : BaseView {
  fun update(data: List<ArtistEntity>)
  fun queue(success: Boolean, tracks: Int)
}

interface GenreArtistsPresenter : Presenter<GenreArtistsView> {
  fun load(genre: String)
  fun queue(@Queue.Action action: String, entry: ArtistEntity)
}


