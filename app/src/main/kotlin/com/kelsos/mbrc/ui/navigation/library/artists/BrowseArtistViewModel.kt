package com.kelsos.mbrc.ui.navigation.library.artists

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.content.library.artists.ArtistRepository

class BrowseArtistViewModel(
  private val repository: ArtistRepository
) : ViewModel() {

  private lateinit var artists: LiveData<PagedList<ArtistEntity>>

  fun reload() {
    launch { repository.getRemote() }
  }
}