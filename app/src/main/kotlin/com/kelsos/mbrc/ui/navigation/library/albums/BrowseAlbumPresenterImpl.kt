package com.kelsos.mbrc.ui.navigation.library.albums

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.content.library.albums.AlbumRepository
import com.kelsos.mbrc.content.library.albums.Sorting
import com.kelsos.mbrc.content.sync.LibrarySyncInteractor
import com.kelsos.mbrc.events.LibraryRefreshCompleteEvent
import com.kelsos.mbrc.events.bus.RxBus
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.preferences.AlbumSortingStore
import com.kelsos.mbrc.ui.navigation.library.LibrarySearchModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class BrowseAlbumPresenterImpl
@Inject
constructor(
  private val bus: RxBus,
  private val repository: AlbumRepository,
  private val albumSortingStore: AlbumSortingStore,
  private val librarySyncInteractor: LibrarySyncInteractor,
  private val queueHandler: QueueHandler,
  private val searchModel: LibrarySearchModel
) : BasePresenter<BrowseAlbumView>(), BrowseAlbumPresenter {

  private lateinit var albums: LiveData<List<AlbumEntity>>

  init {
    searchModel.term.observe(this) { term -> updateUi(term) }
  }

  private fun observeAlbums(it: LiveData<List<AlbumEntity>>) {

    if (::albums.isInitialized) {
      albums.removeObservers(this)
    }

    albums = it
    albums.observe(this@BrowseAlbumPresenterImpl, {
      if (it != null) {
        view().update(it)
      }
    })
  }

  private fun updateUi(term: String) {
    scope.launch {
      view().search(term)
      try {
        observeAlbums(getData(term))
      } catch (e: Exception) {
        Timber.v(e)
      }
      view().hideLoading()
    }
  }

  private suspend fun getData(term: String) =
    if (term.isNotEmpty()) repository.search(term) else repository.getAlbumsSorted()

  override fun attach(view: BrowseAlbumView) {
    super.attach(view)
    bus.register(this, LibraryRefreshCompleteEvent::class.java) { load() }
  }

  override fun detach() {
    super.detach()
    bus.unregister(this)
  }

  override fun load() {
    updateUi(searchModel.term.value ?: "")
  }

  override fun showSorting() {
    view().showSorting(albumSortingStore.getSortingOrder(), albumSortingStore.getSortingSelection())
  }

  override fun order(@Sorting.Order order: Int) {
    albumSortingStore.setSortingOrder(order)

    val ascending = order == Sorting.ORDER_ASCENDING
    val sortingSelection = albumSortingStore.getSortingSelection()
    loadSorted(sortingSelection, ascending)
  }

  private fun loadSorted(sortingSelection: Int, ascending: Boolean) {
    scope.launch {
      try {
        observeAlbums(repository.getAlbumsSorted(sortingSelection, ascending))
      } catch (e: Exception) {
        Timber.e(e)
      }
      view().hideLoading()
    }
  }

  override fun sortBy(@Sorting.Fields selection: Int) {
    albumSortingStore.setSortingSelection(selection)
    val ascending = albumSortingStore.getSortingOrder() == Sorting.ORDER_ASCENDING
    loadSorted(selection, ascending)
  }

  override fun sync() {
    scope.launch {
      librarySyncInteractor.sync()
    }
  }

  override fun queue(action: String, entry: AlbumEntity) {
    scope.launch {
      val (success, tracks) = queueHandler.queueAlbum(action, entry.album, entry.artist)
      view().queue(success, tracks)
    }
  }
}
