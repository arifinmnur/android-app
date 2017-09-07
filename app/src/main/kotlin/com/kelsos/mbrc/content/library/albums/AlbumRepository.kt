package com.kelsos.mbrc.content.library.albums

import com.kelsos.mbrc.interfaces.data.Repository
import com.raizlabs.android.dbflow.list.FlowCursorList

interface AlbumRepository : Repository<Album> {
  suspend fun getAlbumsByArtist(artist: String): FlowCursorList<Album>

  /**
   * Retrieves the albums ordered by
   */
  suspend fun getAlbumsSorted(
      @Sorting.Order order: Long = Sorting.ALBUM_ARTIST__ALBUM,
      ascending: Boolean = true
  ): FlowCursorList<Album>
}
