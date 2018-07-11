package com.kelsos.mbrc.ui.navigation.library

import androidx.lifecycle.ViewModel
import com.kelsos.mbrc.content.sync.LibrarySyncUseCase
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.preferences.SettingsManager

class LibraryViewModel(
  private val dispatchers: AppCoroutineDispatchers,
  private val settingsManager: SettingsManager,
  private val librarySyncUseCase: LibrarySyncUseCase,
) : ViewModel() {

  val displayOnlyAlbumArtists = settingsManager.shouldDisplayOnlyAlbumArtists()

  fun refresh() {
    launch(dispatchers.network) {
      librarySyncUseCase.sync()
    }
  }

  fun setArtistPreference(albumArtistOnly: Boolean) {
    settingsManager.setShouldDisplayOnlyAlbumArtist(albumArtistOnly)
  }
}