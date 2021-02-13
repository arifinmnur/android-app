package com.kelsos.mbrc.ui.navigation.library.tracks

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.tracks.TrackEntity
import com.kelsos.mbrc.content.library.tracks.TrackRepository
import com.kelsos.mbrc.content.sync.LibrarySyncInteractor
import com.kelsos.mbrc.events.LibraryRefreshCompleteEvent
import com.kelsos.mbrc.events.bus.RxBus
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.ui.navigation.library.LibrarySearchModel
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class BrowseTrackPresenterImpl
@Inject
constructor(
  private val bus: RxBus,
  private val repository: TrackRepository,
  private val librarySyncInteractor: LibrarySyncInteractor,
  private val queue: QueueHandler,
  private val searchModel: LibrarySearchModel
) : BasePresenter<BrowseTrackView>(),
  BrowseTrackPresenter {

  private lateinit var tracks: LiveData<PagedList<TrackEntity>>

  init {
    searchModel.term.observe(this) { term -> updateUi(term) }
  }

  override fun attach(view: BrowseTrackView) {
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

  private fun updateUi(term: String) {
    scope.launch {
      view().showLoading()
      view().search(term)
      try {
        onTrackLoad(getData(term))
      } catch (e: Exception) {
        Timber.v(e, "Error while loading the data from the database")
      }
      view().hideLoading()
    }
  }

  private fun onTrackLoad(it: DataSource.Factory<Int, TrackEntity>) {
    tracks = it.paged()
    tracks.observe(this@BrowseTrackPresenterImpl, {
      if (it != null) {
        view().update(it)
      }
    })
  }


  private suspend fun getData(term: String): DataSource.Factory<Int, TrackEntity> {
    return if (term.isEmpty()) {
      repository.getAll()
    } else {
      repository.search(term)
    }
  }

  override fun sync() {
    scope.launch {
      librarySyncInteractor.sync()
    }
  }

  override fun queue(track: TrackEntity, action: String?) {
    scope.launch {
      val (success, tracks) = if (action == null) {
        queue.queueTrack(track)
      } else {
        queue.queueTrack(track, action)
      }
      view().queue(success, tracks)
    }
  }
}
