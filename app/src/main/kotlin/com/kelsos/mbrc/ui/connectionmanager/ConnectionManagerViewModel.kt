package com.kelsos.mbrc.ui.connectionmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kelsos.mbrc.networking.connections.ConnectionRepository
import com.kelsos.mbrc.networking.connections.ConnectionSettingsEntity
import com.kelsos.mbrc.networking.discovery.ServiceDiscoveryUseCase
import com.kelsos.mbrc.utilities.AppRxSchedulers
import kotlinx.coroutines.runBlocking

class ConnectionManagerViewModel(
  private val repository: ConnectionRepository,
  private val serviceDiscoveryUseCase: ServiceDiscoveryUseCase,
  private val appRxSchedulers: AppRxSchedulers
) : ViewModel() {

  var settings: LiveData<List<ConnectionSettingsEntity>> = runBlocking { repository.getAll() }

  fun startDiscovery() {
    serviceDiscoveryUseCase.discover {}
  }

  fun setDefault(settings: ConnectionSettingsEntity) {
    repository.setDefault(settings)
  }

  fun save(settings: ConnectionSettingsEntity) {
    repository.save(settings)

    if (settings.id == repository.defaultId) {
    }
  }

  fun delete(settings: ConnectionSettingsEntity) {
    repository.delete(settings)

    if (settings.id == repository.defaultId) {
    }
  }
}