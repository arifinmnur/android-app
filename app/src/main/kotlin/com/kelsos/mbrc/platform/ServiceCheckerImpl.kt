package com.kelsos.mbrc.platform

import android.app.Application
import android.content.Intent
import android.os.Build
import timber.log.Timber

class ServiceCheckerImpl(
  private val application: Application
) : ServiceChecker {

  override fun startServiceIfNotRunning() {
    if (RemoteService.SERVICE_RUNNING) {
      return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Timber.v("Starting foreground service")
      application.startForegroundService(Intent(application, RemoteService::class.java))
    } else {
      application.startService(Intent(application, RemoteService::class.java))
    }
  }
}