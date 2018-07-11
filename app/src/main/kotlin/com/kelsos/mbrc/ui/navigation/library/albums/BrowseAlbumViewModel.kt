package com.kelsos.mbrc.ui.navigation.library.albums

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.content.library.albums.AlbumRepository
import com.kelsos.mbrc.content.library.albums.Sorting
import com.kelsos.mbrc.preferences.AlbumSortingStore

class BrowseAlbumViewModel(
  private val repository: AlbumRepository,
  private val albumSortingStore: AlbumSortingStore
) : ViewModel() {

  val albums: MediatorLiveData<PagedList<AlbumEntity>> = MediatorLiveData()

  init {
  }

  fun showSorting() {
  }

  fun order(@Sorting.Order order: Int) {
    albumSortingStore.setSortingOrder(order)

    val ascending = order == Sorting.ORDER_ASCENDING
    val sortingSelection = albumSortingStore.getSortingSelection()
  }

  fun sortBy(@Sorting.Fields selection: Int) {
    albumSortingStore.setSortingSelection(selection)
    val ascending = albumSortingStore.getSortingOrder() == Sorting.ORDER_ASCENDING
  }
}