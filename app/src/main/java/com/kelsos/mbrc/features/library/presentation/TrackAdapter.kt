package com.kelsos.mbrc.features.library.presentation

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.features.library.data.Track
import com.kelsos.mbrc.features.library.popup

class TrackAdapter : LibraryAdapter<Track, TrackViewHolder>(DIFF_CALLBACK) {

  private val indicatorPressed: (View, Int) -> Unit = { view, position ->
    view.popup(R.menu.popup_track) {
      val listener = requireListener()
      listener.onMenuItemSelected(it, checkNotNull(getItem(position)))
    }
  }

  private val pressed: (View, Int) -> Unit = { _, position ->
    val listener = requireListener()
    getItem(position)?.run {
      listener.onItemClicked(this)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
    return TrackViewHolder.create(
      parent,
      indicatorPressed,
      pressed
    )
  }

  override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
    val trackEntity = getItem(holder.adapterPosition)

    if (trackEntity != null) {
      holder.bindTo(trackEntity)
    } else {
      holder.clear()
    }
  }

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Track>() {
      override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
      }
    }
  }
}
