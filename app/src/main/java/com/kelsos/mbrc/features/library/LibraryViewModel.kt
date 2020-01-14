package com.kelsos.mbrc.features.library

import com.kelsos.mbrc.content.sync.LibrarySyncUseCase
import com.kelsos.mbrc.content.sync.SyncResult
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.ui.BaseViewModel
import kotlinx.coroutines.launch

class LibraryViewModel(
  dispatchers: AppCoroutineDispatchers,
  private val librarySyncUseCase: LibrarySyncUseCase
) : BaseViewModel<SyncResult>(dispatchers) {

  fun refresh() {
    scope.launch {
      emit(librarySyncUseCase.sync())
    }
  }
}