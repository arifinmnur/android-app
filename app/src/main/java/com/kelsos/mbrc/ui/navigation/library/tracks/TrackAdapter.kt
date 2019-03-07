package com.kelsos.mbrc.ui.navigation.library.tracks

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.tracks.Track
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.popup

class TrackAdapter : PagedListAdapter<Track, TrackViewHolder>(DIFF_CALLBACK) {

  private var listener: MenuItemSelectedListener<Track>? = null

  private val indicatorPressed: (View, Int) -> Unit = { view, position ->
    view.popup(R.menu.popup_track) {
      val listener = checkNotNull(listener)
      listener.onMenuItemSelected(it, checkNotNull(getItem(position)))
    }
  }

  private val pressed: (View, Int) -> Unit = { _, position ->
    val listener = checkNotNull(listener)
    getItem(position)?.run {
      listener.onItemClicked(this)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
    return TrackViewHolder.create(parent, indicatorPressed, pressed)
  }

  override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
    val trackEntity = getItem(holder.adapterPosition)

    if (trackEntity != null) {
      holder.bindTo(trackEntity)
    } else {
      holder.clear()
    }
  }

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener<Track>) {
    this.listener = listener
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