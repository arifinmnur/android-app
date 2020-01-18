package com.kelsos.mbrc.features.library.data

import com.kelsos.mbrc.interfaces.data.Mapper

class GenreEntityMapper : Mapper<GenreEntity, Genre> {
  override fun map(from: GenreEntity): Genre {
    return Genre(
      genre = from.genre,
      id = from.id
    )
  }
}