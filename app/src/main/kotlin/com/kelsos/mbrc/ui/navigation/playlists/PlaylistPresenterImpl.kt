package com.kelsos.mbrc.ui.navigation.playlists

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.kelsos.mbrc.content.playlists.PlaylistEntity
import com.kelsos.mbrc.content.playlists.PlaylistRepository
import com.kelsos.mbrc.events.UserAction
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.networking.client.UserActionUseCase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.launch

class PlaylistPresenterImpl(
  private val repository: PlaylistRepository,
  private val userActionUseCase: UserActionUseCase
) : BasePresenter<PlaylistView>(), PlaylistPresenter {

  private lateinit var playlists: LiveData<PagedList<PlaylistEntity>>

  override fun load() {
    scope.launch {
      view().showLoading()
      try {
        onPlaylistsLoad(repository.getAll())
      } catch (e: Exception) {
        view().failure(e)
      }
      view().hideLoading()
    }
  }

  private fun onPlaylistsLoad(it: DataSource.Factory<Int, PlaylistEntity>) {
    playlists = it.paged()
    playlists.observe(this@PlaylistPresenterImpl, {
      if (it != null) {
        view().update(it)
      }
    })
  }

  override fun play(path: String) {
    userActionUseCase.perform(UserAction(Protocol.PlaylistPlay, path))
  }

  override fun reload() {
    view().showLoading()
    scope.launch {
      try {
        onPlaylistsLoad(repository.getAndSaveRemote())
      } catch (e: Exception) {
        view().failure(e)
      }
      view().hideLoading()
    }
  }
}