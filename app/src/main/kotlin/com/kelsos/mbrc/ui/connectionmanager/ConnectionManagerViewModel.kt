package com.kelsos.mbrc.ui.connectionmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kelsos.mbrc.di.modules.AppCoroutineDispatchers
import com.kelsos.mbrc.networking.connections.ConnectionRepository
import com.kelsos.mbrc.networking.connections.ConnectionSettingsEntity
import com.kelsos.mbrc.networking.discovery.ServiceDiscoveryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class ConnectionManagerViewModel(
  private val repository: ConnectionRepository,
  private val serviceDiscoveryUseCase: ServiceDiscoveryUseCase,
  private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

  private val viewmModelJob: Job = Job()
  private val databaseScope = CoroutineScope(dispatchers.database + viewmModelJob)

  var settings: LiveData<List<ConnectionSettingsEntity>> = runBlocking { repository.getAll() }

  fun startDiscovery() {
    serviceDiscoveryUseCase.discover {}
  }

  fun setDefault(settings: ConnectionSettingsEntity) {
    databaseScope.launch {
      repository.setDefault(settings)
    }
  }

  fun save(settings: ConnectionSettingsEntity) {
    databaseScope.launch {
      repository.save(settings)

      if (settings.id == repository.defaultId) {
      }
    }
  }

  fun delete(settings: ConnectionSettingsEntity) {
    databaseScope.launch {

      repository.delete(settings)

      if (settings.id == repository.defaultId) {
      }
    }
  }

  private fun onLoadError(throwable: Throwable) {
    Timber.v(throwable, "Failure")
  }

  override fun onCleared() {
    viewmModelJob.cancel()
    super.onCleared()
  }
}