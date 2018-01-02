package com.kelsos.mbrc.content.library.artists

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.interfaces.data.Repository

interface ArtistRepository : Repository<ArtistEntity> {
  suspend fun getArtistByGenre(genre: String): LiveData<List<ArtistEntity>>
  suspend fun getAlbumArtistsOnly(): LiveData<List<ArtistEntity>>
  suspend fun getAllRemoteAndShowAlbumArtist(): LiveData<List<ArtistEntity>>
}
