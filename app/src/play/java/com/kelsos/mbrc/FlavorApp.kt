package com.kelsos.mbrc

import com.kelsos.mbrc.metrics.SyncMetrics
import com.kelsos.mbrc.metrics.SyncMetricsImpl
import org.koin.dsl.module.Module

open class FlavorApp : App() {
  override fun onCreate() {
    super.onCreate()
  }

  override fun modules(): List<Module> {
    val playModule = org.koin.dsl.module.applicationContext {
      bean<SyncMetrics> { SyncMetricsImpl() }
    }
    return super.modules().plus(playModule)
  }
}