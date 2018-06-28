package com.kelsos.mbrc

import android.annotation.SuppressLint
import androidx.annotation.CallSuper
import androidx.multidex.MultiDexApplication
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kelsos.mbrc.di.modules.appModule
import com.kelsos.mbrc.di.modules.uiModule
import com.kelsos.mbrc.utilities.CustomLoggingTree
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.Module
import timber.log.Timber

@SuppressLint("Registered")
open class App : MultiDexApplication() {

  @CallSuper
  override fun onCreate() {
    super.onCreate()
    initialize()
  }

  protected open fun modules(): List<Module> {
    return listOf(appModule, uiModule)
  }

  protected open fun initialize() {
    if (!testMode()) {
      AndroidThreeTen.init(this)
    }

    startKoin(this, modules())

    initializeTimber()
  }

  private fun initializeTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(CustomLoggingTree.create())
    }
  }

  internal open fun testMode(): Boolean = false
}