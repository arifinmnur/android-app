package com.kelsos.mbrc.preferences

import androidx.annotation.StringDef
import java.util.Date

interface SettingsManager {
  @CallAction
  fun getCallAction(): String
  suspend fun shouldDisplayOnlyAlbumArtists(): Boolean
  fun setShouldDisplayOnlyAlbumArtist(onlyAlbumArtist: Boolean)
  fun shouldShowChangeLog(): Boolean
  fun isPluginUpdateCheckEnabled(): Boolean
  fun getLastUpdated(): Date
  fun setLastUpdated(lastChecked: Date)

  @StringDef(
    NONE,
    PAUSE,
    STOP,
    REDUCE
  )
  @Retention(AnnotationRetention.SOURCE)
  annotation class CallAction

  companion object {
    const val NONE = "none"
    const val PAUSE = "pause"
    const val STOP = "stop"
    const val REDUCE = "reduce"
  }
}