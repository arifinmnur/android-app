package com.kelsos.mbrc.ui.navigation.library.genres

import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.genres.GenreEntity
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.mvp.BaseView
import com.kelsos.mbrc.mvp.Presenter

interface BrowseGenrePresenter : Presenter<BrowseGenreView> {
  fun load()
  fun sync()
  fun queue(@LibraryPopup.Action action: String, genre: GenreEntity)
}

interface BrowseGenreView : BaseView {
  fun update(pagedList: PagedList<GenreEntity>)
  fun search(term: String)
  fun queue(success: Boolean, tracks: Int)
  fun hideLoading()
}
