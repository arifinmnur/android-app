package com.kelsos.mbrc.ui.navigation.library.albums

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.content.library.albums.AlbumRepository
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers

class BrowseAlbumViewModel(
  private val repository: AlbumRepository,
  private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

  val albums: MediatorLiveData<PagedList<AlbumEntity>> = MediatorLiveData()
}