package com.kelsos.mbrc.ui.navigation.library.artists

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.kelsos.mbrc.content.library.artists.Artist
import com.kelsos.mbrc.content.library.artists.ArtistRepository
import com.kelsos.mbrc.utilities.paged

class ArtistViewModel(
  repository: ArtistRepository,
) : ViewModel() {
  val artists: LiveData<PagedList<Artist>> = repository.getAll().paged()
}