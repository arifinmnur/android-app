package com.kelsos.mbrc.di.modules

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.kelsos.mbrc.RemoteDb
import com.kelsos.mbrc.content.activestatus.ModelCache
import com.kelsos.mbrc.content.activestatus.ModelCacheImpl
import com.kelsos.mbrc.content.library.albums.AlbumDao
import com.kelsos.mbrc.content.library.albums.AlbumRepository
import com.kelsos.mbrc.content.library.albums.AlbumRepositoryImpl
import com.kelsos.mbrc.content.library.artists.ArtistDao
import com.kelsos.mbrc.content.library.artists.ArtistRepository
import com.kelsos.mbrc.content.library.artists.ArtistRepositoryImpl
import com.kelsos.mbrc.content.library.genres.GenreDao
import com.kelsos.mbrc.content.library.genres.GenreRepository
import com.kelsos.mbrc.content.library.genres.GenreRepositoryImpl
import com.kelsos.mbrc.content.library.tracks.TrackDao
import com.kelsos.mbrc.content.library.tracks.TrackRepository
import com.kelsos.mbrc.content.library.tracks.TrackRepositoryImpl
import com.kelsos.mbrc.content.nowplaying.NowPlayingDao
import com.kelsos.mbrc.content.nowplaying.NowPlayingRepository
import com.kelsos.mbrc.content.nowplaying.NowPlayingRepositoryImpl
import com.kelsos.mbrc.content.output.OutputApi
import com.kelsos.mbrc.content.output.OutputApiImpl
import com.kelsos.mbrc.content.playlists.PlaylistDao
import com.kelsos.mbrc.content.playlists.PlaylistRepository
import com.kelsos.mbrc.content.playlists.PlaylistRepositoryImpl
import com.kelsos.mbrc.content.radios.RadioRepository
import com.kelsos.mbrc.content.radios.RadioRepositoryImpl
import com.kelsos.mbrc.content.radios.RadioStationDao
import com.kelsos.mbrc.content.sync.LibrarySyncInteractor
import com.kelsos.mbrc.content.sync.LibrarySyncInteractorImpl
import com.kelsos.mbrc.di.providers.AlbumDaoProvider
import com.kelsos.mbrc.di.providers.ArtistDaoProvider
import com.kelsos.mbrc.di.providers.ConnectionDaoProvider
import com.kelsos.mbrc.di.providers.DatabaseProvider
import com.kelsos.mbrc.di.providers.GenreDaoProvider
import com.kelsos.mbrc.di.providers.NowPlayingDaoProvider
import com.kelsos.mbrc.di.providers.PlaylistDaoProvider
import com.kelsos.mbrc.di.providers.RadioStationDaoProvider
import com.kelsos.mbrc.di.providers.TrackDaoProvider
import com.kelsos.mbrc.events.bus.RxBus
import com.kelsos.mbrc.events.bus.RxBusImpl
import com.kelsos.mbrc.helper.QueueHandler
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.RequestManager
import com.kelsos.mbrc.networking.RequestManagerImpl
import com.kelsos.mbrc.networking.connections.ConnectionDao
import com.kelsos.mbrc.networking.connections.ConnectionRepository
import com.kelsos.mbrc.networking.connections.ConnectionRepositoryImpl
import com.kelsos.mbrc.networking.protocol.VolumeInteractor
import com.kelsos.mbrc.networking.protocol.VolumeInteractorImpl
import com.kelsos.mbrc.platform.ServiceChecker
import com.kelsos.mbrc.platform.ServiceCheckerImpl
import com.kelsos.mbrc.preferences.AlbumSortingStore
import com.kelsos.mbrc.preferences.AlbumSortingStoreImpl
import com.kelsos.mbrc.preferences.ClientInformationStore
import com.kelsos.mbrc.preferences.ClientInformationStoreImpl
import com.kelsos.mbrc.preferences.SettingsManager
import com.kelsos.mbrc.preferences.SettingsManagerImpl
import com.kelsos.mbrc.utilities.SchedulerProvider
import com.kelsos.mbrc.utilities.SchedulerProviderImpl
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import toothpick.config.Binding
import toothpick.config.Module
import java.util.concurrent.Executors
import kotlin.reflect.KClass

class RemoteModule : Module() {
  init {
    val mapper = ObjectMapper()
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.registerModule(KotlinModule())

    bind(RxBus::class.java).to(RxBusImpl::class.java).singletonInScope()
    bind(ObjectMapper::class.java).toInstance(mapper)

    bind(ConnectionRepository::class.java).to(ConnectionRepositoryImpl::class.java)
    bind(Scheduler::class.java).withName("main")
      .toProviderInstance { AndroidSchedulers.mainThread() }
    bind(Scheduler::class.java).withName("io").toProviderInstance { Schedulers.io() }
    bind(SchedulerProvider::class.java).to(SchedulerProviderImpl::class.java).singletonInScope()

    bind(TrackRepository::class.java).to(TrackRepositoryImpl::class.java)
    bind(AlbumRepository::class.java).to(AlbumRepositoryImpl::class.java)
    bind(ArtistRepository::class.java).to(ArtistRepositoryImpl::class.java)
    bind(GenreRepository::class.java).to(GenreRepositoryImpl::class.java)

    bind(NowPlayingRepository::class.java).to(NowPlayingRepositoryImpl::class.java)
    bind(PlaylistRepository::class.java).to(PlaylistRepositoryImpl::class.java)

    bind(RemoteDb::class).toProvider(DatabaseProvider::class.java).providesSingletonInScope()
    bind(GenreDao::class).toProvider(GenreDaoProvider::class.java).providesSingletonInScope()
    bind(ArtistDao::class).toProvider(ArtistDaoProvider::class.java).providesSingletonInScope()
    bind(AlbumDao::class).toProvider(AlbumDaoProvider::class.java).providesSingletonInScope()
    bind(TrackDao::class).toProvider(TrackDaoProvider::class.java).providesSingletonInScope()
    bind(NowPlayingDao::class).toProvider(NowPlayingDaoProvider::class.java).providesSingletonInScope()
    bind(PlaylistDao::class).toProvider(PlaylistDaoProvider::class.java).providesSingletonInScope()
    bind(RadioStationDao::class).toProvider(RadioStationDaoProvider::class.java).providesSingletonInScope()
    bind(ConnectionDao::class).toProvider(ConnectionDaoProvider::class.java).providesSingletonInScope()

    bind(SettingsManager::class.java).to(SettingsManagerImpl::class.java).singletonInScope()
    bind(ModelCache::class.java).to(ModelCacheImpl::class.java).singletonInScope()
    bind(ServiceChecker::class.java).to(ServiceCheckerImpl::class.java).singletonInScope()

    bind(LibrarySyncInteractor::class.java).to(LibrarySyncInteractorImpl::class.java)
      .singletonInScope()
    bind(ApiBase::class.java).singletonInScope()
    bind(RequestManager::class.java).to(RequestManagerImpl::class.java).singletonInScope()
    bind(OutputApi::class.java).to(OutputApiImpl::class.java).singletonInScope()
    bind(AppDispatchers::class.java).toInstance(
      AppDispatchers(
        Dispatchers.Main,
        Dispatchers.IO,
        Executors.newSingleThreadExecutor { Thread(it, "db") }.asCoroutineDispatcher()
      )
    )
    bind(QueueHandler::class.java).singletonInScope()
    bind(RadioRepository::class.java).to(RadioRepositoryImpl::class.java).singletonInScope()
    bind(ClientInformationStore::class).to(ClientInformationStoreImpl::class).singletonInScope()
    bind(VolumeInteractor::class).to(VolumeInteractorImpl::class).singletonInScope()
    bind(OutputApi::class).to(OutputApiImpl::class).singletonInScope()
    bind(AlbumSortingStore::class).to(AlbumSortingStoreImpl::class)
  }
}

fun <T : Any> Module.bind(clazz: KClass<T>): Binding<T> = bind(clazz.java)

fun <T : Any, K : T> Binding<T>.to(clazz: KClass<K>): Binding<T>.BoundStateForClassBinding =
  this.to(clazz.java)