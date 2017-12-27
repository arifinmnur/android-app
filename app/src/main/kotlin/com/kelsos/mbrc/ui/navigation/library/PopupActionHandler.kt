package com.kelsos.mbrc.ui.navigation.library

import android.content.Context
import android.content.Intent
import androidx.annotation.IdRes
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.content.library.albums.AlbumMapper
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.content.library.genres.GenreEntity
import com.kelsos.mbrc.content.nowplaying.queue.Queue
import com.kelsos.mbrc.ui.navigation.library.albumtracks.AlbumTracksActivity
import com.kelsos.mbrc.ui.navigation.library.artistalbums.ArtistAlbumsActivity
import com.kelsos.mbrc.ui.navigation.library.genreartists.GenreArtistsActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PopupActionHandler
@Inject
constructor() {

  @Queue.Action
  fun albumSelected(@IdRes itemId: Int, entry: AlbumEntity, context: Context): String {
    if (itemId == R.id.popup_album_tracks) {
      openProfile(entry, context)
      return Queue.PROFILE
    }

    return when (itemId) {
      R.id.popup_album_queue_next -> Queue.NEXT
      R.id.popup_album_queue_last -> Queue.LAST
      R.id.popup_album_play -> Queue.NOW
      else -> Queue.NOW
    }
  }

  @Queue.Action
  fun artistSelected(@IdRes itemId: Int, entry: ArtistEntity, context: Context): String {
    if (itemId == R.id.popup_artist_album) {
      openProfile(entry, context)
      return Queue.PROFILE
    }

    return when (itemId) {
      R.id.popup_artist_queue_next -> Queue.NEXT
      R.id.popup_artist_queue_last -> Queue.LAST
      R.id.popup_artist_play -> Queue.NOW
      else -> Queue.NOW
    }
  }

  @Queue.Action
  fun genreSelected(@IdRes itemId: Int, entry: GenreEntity, context: Context): String {
    if (R.id.popup_genre_artists == itemId) {
      openProfile(entry, context)
      return Queue.PROFILE
    }

    return when (itemId) {
      R.id.popup_genre_queue_next -> Queue.NEXT
      R.id.popup_genre_queue_last -> Queue.LAST
      R.id.popup_genre_play -> Queue.NOW
      else -> Queue.NOW
    }
  }

  @Queue.Action
  fun trackSelected(@IdRes itemId: Int): String =
    when (itemId) {
      R.id.popup_track_queue_next -> Queue.NEXT
      R.id.popup_track_queue_last -> Queue.LAST
      R.id.popup_track_play -> Queue.NOW
      R.id.popup_track_play_queue_all -> Queue.ADD_ALL
      R.id.popup_track_play_artist -> Queue.PLAY_ARTIST
      R.id.popup_track_play_album -> Queue.PLAY_ALBUM
      else -> Queue.NOW
    }

  fun albumSelected(album: AlbumEntity, context: Context) {
    openProfile(album, context)
  }

  fun artistSelected(artist: ArtistEntity, context: Context) {
    openProfile(artist, context)
  }

  fun genreSelected(genre: GenreEntity, context: Context) {
    openProfile(genre, context)
  }

  private fun openProfile(artist: ArtistEntity, context: Context) {
    val intent = Intent(context, ArtistAlbumsActivity::class.java)
    intent.putExtra(ArtistAlbumsActivity.ARTIST_NAME, artist.artist)
    context.startActivity(intent)
  }

  private fun openProfile(album: AlbumEntity, context: Context) {
    val mapper = AlbumMapper()
    val intent = Intent(context, AlbumTracksActivity::class.java)
    intent.putExtra(AlbumTracksActivity.ALBUM, mapper.map(album))
    context.startActivity(intent)
  }

  private fun openProfile(genre: GenreEntity, context: Context) {
    val intent = Intent(context, GenreArtistsActivity::class.java)
    intent.putExtra(GenreArtistsActivity.GENRE_NAME, genre.genre)
    context.startActivity(intent)
  }
}
