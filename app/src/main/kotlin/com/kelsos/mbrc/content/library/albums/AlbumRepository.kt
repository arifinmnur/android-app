package com.kelsos.mbrc.content.library.albums

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.interfaces.data.Repository

interface AlbumRepository : Repository<AlbumEntity> {
  suspend fun getAlbumsByArtist(artist: String): LiveData<List<AlbumEntity>>

  /**
   * Retrieves the albums ordered by
   */
  suspend fun getAlbumsSorted(
    @Sorting.Fields order: Int = Sorting.ALBUM_ARTIST__ALBUM,
    ascending: Boolean = true
  ): LiveData<List<AlbumEntity>>
}
