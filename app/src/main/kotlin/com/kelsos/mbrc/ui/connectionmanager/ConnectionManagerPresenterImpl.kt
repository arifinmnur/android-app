package com.kelsos.mbrc.ui.connectionmanager

import androidx.lifecycle.LiveData
import com.kelsos.mbrc.events.ConnectionSettingsChanged
import com.kelsos.mbrc.events.DiscoveryStopped
import com.kelsos.mbrc.events.NotifyUser
import com.kelsos.mbrc.events.bus.RxBus
import com.kelsos.mbrc.mvp.BasePresenter
import com.kelsos.mbrc.networking.StartServiceDiscoveryEvent
import com.kelsos.mbrc.networking.connections.ConnectionRepository
import com.kelsos.mbrc.networking.connections.ConnectionSettingsEntity
import com.kelsos.mbrc.preferences.DefaultSettingsChangedEvent
import com.kelsos.mbrc.utilities.SchedulerProvider
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ConnectionManagerPresenterImpl
@Inject
constructor(
  private val repository: ConnectionRepository,
  private val schedulerProvider: SchedulerProvider,
  private val bus: RxBus
) : BasePresenter<ConnectionManagerView>(), ConnectionManagerPresenter {

  private lateinit var settings: LiveData<List<ConnectionSettingsEntity>>

  override fun attach(view: ConnectionManagerView) {
    super.attach(view)
    addDisposable(bus.observe(ConnectionSettingsChanged::class)
      .subscribeOn(schedulerProvider.io())
      .observeOn(schedulerProvider.main())
      .subscribe { view().onConnectionSettingsChange(it) })

    addDisposable(bus.observe(DiscoveryStopped::class)
      .subscribeOn(schedulerProvider.io())
      .observeOn(schedulerProvider.main())
      .subscribe { view().onDiscoveryStopped(it) })

    addDisposable(bus.observe(NotifyUser::class)
      .subscribeOn(schedulerProvider.io())
      .observeOn(schedulerProvider.main())
      .subscribe { view().onUserNotification(it) })
  }

  override fun startDiscovery() {
    bus.post(StartServiceDiscoveryEvent())
  }

  override fun load() {
    checkIfAttached()
    scope.launch {
      try {
        val model = repository.getModel()
        settings = model.settings
        view().updateDefault(model.defaultId)

        settings.observe(this@ConnectionManagerPresenterImpl, {
          it?.let { data ->
            view().updateData(data)
          }
        })
      } catch (e: Exception) {
        Timber.v(e, "Failure")
      }
    }
  }

  override fun setDefault(settings: ConnectionSettingsEntity) {
    checkIfAttached()
    scope.launch {
      repository.setDefault(settings)
      bus.post(DefaultSettingsChangedEvent())
      load()
    }
  }

  override fun save(settings: ConnectionSettingsEntity) {
    checkIfAttached()

    scope.launch {
      try {
        repository.save(settings)

        if (settings.id == repository.defaultId) {
          bus.post(DefaultSettingsChangedEvent())
        }

        load()
      } catch (e: Exception) {
        Timber.v(e)
      }
    }
  }

  override fun delete(settings: ConnectionSettingsEntity) {
    checkIfAttached()

    scope.launch {
      repository.delete(settings)

      if (settings.id == repository.defaultId) {
        bus.post(DefaultSettingsChangedEvent())
      }
    }
  }
}