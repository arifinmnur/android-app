package com.kelsos.mbrc.di.modules

import kotlinx.coroutines.CoroutineDispatcher

data class AppCoroutineDispatchers(
  val main: CoroutineDispatcher,
  val disk: CoroutineDispatcher,
  val database: CoroutineDispatcher,
  val network: CoroutineDispatcher
)