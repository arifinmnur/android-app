package com.kelsos.mbrc.ui.navigation.nowplaying

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.kelsos.mbrc.content.activestatus.livedata.PlayingTrackLiveDataProvider
import com.kelsos.mbrc.content.nowplaying.NowPlayingEntity
import com.kelsos.mbrc.content.nowplaying.NowPlayingRepository
import com.kelsos.mbrc.events.UserAction
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.networking.client.UserActionUseCase
import com.kelsos.mbrc.networking.protocol.NowPlayingMoveRequest
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.nonNullObserver
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.launch
import javax.inject.Inject

class NowPlayingPresenterImpl
@Inject
constructor(
  playingTrackLiveDataProvider: PlayingTrackLiveDataProvider,
  private val repository: NowPlayingRepository,
  private val moveManager: MoveManager,
  private val userActionUseCase: UserActionUseCase
) : BasePresenter<NowPlayingView>(), NowPlayingPresenter {

  private lateinit var nowPlayingTracks: LiveData<PagedList<NowPlayingEntity>>

  init {
    moveManager.onMoveSubmit { originalPosition, finalPosition ->
      val data = NowPlayingMoveRequest(originalPosition, finalPosition)
      userActionUseCase.perform(UserAction(Protocol.NowPlayingListMove, data))
    }

    playingTrackLiveDataProvider.observe(this) {
      view().trackChanged(it)
    }
  }

  override fun reload(scrollToTrack: Boolean) {
    scope.launch {
      try {
        onNowPlayingTracksLoaded(repository.getAndSaveRemote())
      } catch (e: Exception) {
        view().failure(e)
      }
    }
  }

  private fun onNowPlayingTracksLoaded(it: DataSource.Factory<Int, NowPlayingEntity>) {
    nowPlayingTracks = it.paged()
    nowPlayingTracks.nonNullObserver(this) {
      view().update(it)
    }
  }

  override fun load() {
    scope.launch {
      try {
        onNowPlayingTracksLoaded(repository.getAll())
      } catch (e: Exception) {
        view().failure(e)
      }
    }
  }

  override fun search(query: String) {
    // todo: drop and upgrade to do this locally, bus.post(
  }

  override fun moveTrack(from: Int, to: Int) {
    moveManager.move(from, to)
  }

  override fun play(position: Int) {
    userActionUseCase.perform(UserAction(Protocol.NowPlayingListPlay, position))
  }

  override fun removeTrack(position: Int) {
    userActionUseCase.perform(UserAction(Protocol.NowPlayingListRemove, position))
  }
}