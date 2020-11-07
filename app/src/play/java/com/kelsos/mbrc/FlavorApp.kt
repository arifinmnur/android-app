package com.kelsos.mbrc

import com.kelsos.mbrc.metrics.SyncMetrics
import com.kelsos.mbrc.metrics.SyncMetricsImpl
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.module.Module

open class FlavorApp : App() {
  override fun onCreate() {
    super.onCreate()
  }

  override fun appModules(): List<Module> {
    val playModule = module {
      single<SyncMetrics> { SyncMetricsImpl() }
    }
    return super.appModules().plus(playModule)
  }
}