package com.kelsos.mbrc.features.library.albums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlbumInfo(val album: String, val artist: String) : Parcelable