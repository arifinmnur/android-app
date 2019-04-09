package com.kelsos.mbrc.content.playlists

import com.kelsos.mbrc.interfaces.data.Mapper

class PlaylistEntityMapper : Mapper<PlaylistEntity, Playlist> {
  override fun map(from: PlaylistEntity): Playlist {
    return Playlist(
      name = from.name,
      url = from.url,
      id = from.id
    )
  }
}