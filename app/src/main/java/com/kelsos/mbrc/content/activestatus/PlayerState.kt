package com.kelsos.mbrc.content.activestatus

sealed class PlayerState(val state: String) {
  object Playing : PlayerState(PLAYING)
  object Paused : PlayerState(PAUSED)
  object Stopped : PlayerState(STOPPED)
  object Undefined : PlayerState(UNDEFINED)

  companion object {
    const val PLAYING = "playing"
    const val PAUSED = "paused"
    const val STOPPED = "stopped"
    const val UNDEFINED = "undefined"

    fun fromString(state: String): PlayerState = when (state) {
      PLAYING -> Playing
      PAUSED -> Paused
      STOPPED -> Stopped
      else -> Undefined
    }
  }
}
