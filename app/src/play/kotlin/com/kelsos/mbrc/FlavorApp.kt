package com.kelsos.mbrc

import com.kelsos.mbrc.di.module
import com.kelsos.mbrc.di.scope
import com.kelsos.mbrc.metrics.SyncMetrics
import com.kelsos.mbrc.metrics.SyncMetricsImpl

class PlayRemoteApplication : RemoteApplication() {
  override fun onCreate() {
    super.onCreate()

    scope(applicationContext, {
      module {
        bindSingletonClass<SyncMetrics> { SyncMetricsImpl::class }
      }
    })
  }
}