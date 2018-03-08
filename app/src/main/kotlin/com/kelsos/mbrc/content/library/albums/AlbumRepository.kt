package com.kelsos.mbrc.content.library.albums

import androidx.paging.DataSource
import com.kelsos.mbrc.interfaces.data.Repository

interface AlbumRepository : Repository<AlbumEntity> {
  suspend fun getAlbumsByArtist(artist: String): DataSource.Factory<Int, AlbumEntity>

  /**
   * Retrieves the albums ordered by
   */
  suspend fun getAlbumsSorted(
    @Sorting.Fields order: Int = Sorting.ALBUM_ARTIST__ALBUM,
    ascending: Boolean = true
  ): DataSource.Factory<Int, AlbumEntity>
}