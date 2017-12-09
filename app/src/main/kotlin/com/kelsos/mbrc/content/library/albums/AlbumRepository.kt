package com.kelsos.mbrc.content.library.albums

import com.kelsos.mbrc.interfaces.data.Repository

interface AlbumRepository : Repository<Album> {
  suspend fun getAlbumsByArtist(artist: String): List<Album>

  /**
   * Retrieves the albums ordered by
   */
  suspend fun getAlbumsSorted(
    @Sorting.Fields order: Int = Sorting.ALBUM_ARTIST__ALBUM,
    ascending: Boolean = true
  ): List<Album>
}
