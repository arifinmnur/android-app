package com.kelsos.mbrc.ui.navigation.library.albums

import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.content.library.albums.Sorting.Fields
import com.kelsos.mbrc.content.library.albums.Sorting.Order
import com.kelsos.mbrc.content.nowplaying.queue.Queue
import com.kelsos.mbrc.mvp.BaseView
import com.kelsos.mbrc.mvp.Presenter

interface BrowseAlbumView : BaseView {
  fun update(pagedList: PagedList<AlbumEntity>)
  fun search(term: String)
  fun queue(success: Boolean, tracks: Int)
  fun hideLoading()
  fun showLoading()
  fun showSorting(@Order order: Int, @Fields selection: Int)
}

interface BrowseAlbumPresenter : Presenter<BrowseAlbumView> {
  fun load()
  fun sync()
  fun queue(@Queue.Action action: String, entry: AlbumEntity)
  fun showSorting()
  fun order(@Order order: Int)
  fun sortBy(@Fields selection: Int)
}

