package com.kelsos.mbrc.content.library.albums

import androidx.paging.DataSource
import com.kelsos.mbrc.interfaces.data.Repository

interface AlbumRepository : Repository<Album> {
  fun getAlbumsByArtist(artist: String): DataSource.Factory<Int, Album>
}