package com.kelsos.mbrc.content.library.albums

import com.kelsos.mbrc.interfaces.data.Repository
import com.raizlabs.android.dbflow.list.FlowCursorList

interface AlbumRepository : Repository<Album> {
  suspend fun getAlbumsByArtist(artist: String): FlowCursorList<Album>
}
