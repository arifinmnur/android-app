package com.kelsos.mbrc.features.playlists

import com.kelsos.mbrc.features.playlists.data.PlaylistEntity
import com.kelsos.mbrc.interfaces.data.Mapper

object PlaylistDtoMapper : Mapper<PlaylistDto, PlaylistEntity> {
  override fun map(from: PlaylistDto): PlaylistEntity = PlaylistEntity(from.name, from.url)
}