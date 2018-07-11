package com.kelsos.mbrc.ui.navigation.playlists

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.kelsos.mbrc.content.playlists.PlaylistEntity
import com.kelsos.mbrc.content.playlists.PlaylistRepository
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.events.UserAction
import com.kelsos.mbrc.networking.client.UserActionUseCase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.runBlocking

class PlaylistViewModel(
  private val repository: PlaylistRepository,
  private val dispatchers: AppCoroutineDispatchers,
  private val userActionUseCase: UserActionUseCase
) {
  val playlists: LiveData<PagedList<PlaylistEntity>> = runBlocking { repository.getAll() }.paged()

  fun play(path: String) {
    userActionUseCase.perform(UserAction(Protocol.PlaylistPlay, path))
  }

  fun reload() {
    runBlocking {
      repository.getRemote()
    }
  }
}