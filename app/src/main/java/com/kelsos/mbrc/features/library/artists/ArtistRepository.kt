package com.kelsos.mbrc.features.library.artists

import androidx.paging.DataSource
import com.kelsos.mbrc.interfaces.data.Repository

interface ArtistRepository : Repository<Artist> {
  fun getArtistByGenre(genre: String): DataSource.Factory<Int, Artist>
  fun getAlbumArtistsOnly(): DataSource.Factory<Int, Artist>
}