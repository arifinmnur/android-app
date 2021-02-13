package com.kelsos.mbrc.networking.connections

interface ConnectionRepository {
  suspend fun save(settings: ConnectionSettings)

  suspend fun delete(settings: ConnectionSettings)

  suspend fun update(settings: ConnectionSettings): Boolean

  suspend fun getAll(): List<ConnectionSettings>

  suspend fun count(): Long

  suspend fun setDefault(settings: ConnectionSettings)

  suspend fun getDefault(): ConnectionSettings?

  val defaultId: Long
}
