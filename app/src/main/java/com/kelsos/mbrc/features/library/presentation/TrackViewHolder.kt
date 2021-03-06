package com.kelsos.mbrc.features.library.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.ui.extensions.string
import com.kelsos.mbrc.databinding.ListitemDualBinding
import com.kelsos.mbrc.features.library.data.Track
import com.kelsos.mbrc.ui.BindableViewHolder

class TrackViewHolder(
  binding: ListitemDualBinding,
  indicatorPressed: (view: View, position: Int) -> Unit,
  pressed: (view: View, position: Int) -> Unit
) : BindableViewHolder<Track>(binding.root) {
  private val artist: TextView = binding.lineTwo
  private val title: TextView = binding.lineOne
  private val indicator: ImageView = binding.overflowMenu
  private val unknownArtist: String by lazy { string(R.string.unknown_artist) }

  init {
    indicator.setOnClickListener { indicatorPressed(it, adapterPosition) }
    itemView.setOnClickListener { pressed(it, adapterPosition) }
  }

  override fun clear() {
    artist.text = ""
    title.text = ""
  }

  override fun bindTo(item: Track) {
    title.text = item.title
    artist.text = if (item.artist.isBlank()) unknownArtist else item.artist
  }

  companion object {
    fun create(
      parent: ViewGroup,
      indicatorPressed: (view: View, position: Int) -> Unit,
      pressed: (view: View, position: Int) -> Unit
    ): TrackViewHolder {
      val inflater: LayoutInflater = LayoutInflater.from(parent.context)
      val binding: ListitemDualBinding = DataBindingUtil.inflate(
        inflater,
        R.layout.listitem_dual,
        parent,
        false
      )
      return TrackViewHolder(
        binding,
        indicatorPressed,
        pressed
      )
    }
  }
}
