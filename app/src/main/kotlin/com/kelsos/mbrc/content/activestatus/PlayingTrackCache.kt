package com.kelsos.mbrc.content.activestatus

import android.app.Application
import androidx.datastore.DataStore
import androidx.datastore.createDataStore
import com.kelsos.mbrc.content.library.tracks.PlayingTrackModel
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.store.Store
import com.kelsos.mbrc.store.StoreSerializer
import com.kelsos.mbrc.store.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class PlayingTrackCacheImpl
@Inject
constructor(
  context: Application,
  private val dispatchers: AppCoroutineDispatchers
) : PlayingTrackCache {

  private val dataStore: DataStore<Store> =
    context.createDataStore("cache_store.db", StoreSerializer)
  private val storeFlow: Flow<Store> = dataStore.data
    .catch { exception ->
      // dataStore.data throws an IOException when an error is encountered when reading data
      if (exception is IOException) {
        Timber.e(exception, "Error reading sort order preferences.")
        emit(Store.getDefaultInstance())
      } else {
        throw exception
      }
    }

  override suspend fun persistInfo(trackInfo: PlayingTrackModel) = withContext(dispatchers.network) {
    dataStore.updateData { store ->
      val track = Track.newBuilder()
        .setAlbum(trackInfo.album)
        .setArtist(trackInfo.artist)
        .setPath(trackInfo.path)
        .setTitle(trackInfo.title)
        .setYear(trackInfo.year)
        .build()

      store.toBuilder()
        .setTrack(track)
        .build()
    }
    return@withContext
  }

  override suspend fun restoreInfo(): PlayingTrackModel = withContext(dispatchers.network) {
    val track = storeFlow.first().track

    return@withContext PlayingTrackModel(
      track.artist,
      track.title,
      track.album,
      track.year,
      track.path
    )
  }

  override suspend fun persistCover(cover: String) {
    dataStore.updateData { store ->
      store.toBuilder().setCover(cover).build()
    }
  }

  override suspend fun restoreCover(): String {
    return storeFlow.first().cover
  }
}

interface PlayingTrackCache {
  suspend fun persistInfo(trackInfo: PlayingTrackModel)
  suspend fun restoreInfo(): PlayingTrackModel
  suspend fun persistCover(cover: String)
  suspend fun restoreCover(): String
}