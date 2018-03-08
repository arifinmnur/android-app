package com.kelsos.mbrc.ui.navigation.library.artists

import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.mvp.BaseView
import com.kelsos.mbrc.mvp.Presenter

interface BrowseArtistView : BaseView {
  fun update(pagedList: PagedList<ArtistEntity>)
  fun search(term: String)
  fun queue(success: Boolean, tracks: Int)
  fun hideLoading()
}

interface BrowseArtistPresenter : Presenter<BrowseArtistView> {
  fun load()
  fun sync()
  fun queue(action: String, entry: ArtistEntity)
}