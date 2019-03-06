package com.kelsos.mbrc.ui.navigation.library.tracks

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.tracks.TrackEntity
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.popup

class TrackEntryAdapter : PagedListAdapter<TrackEntity, TrackViewHolder>(DIFF_CALLBACK) {

  private var listener: MenuItemSelectedListener<TrackEntity>? = null

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

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener<TrackEntity>) {
    this.listener = listener
  }

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrackEntity>() {
      override fun areItemsTheSame(oldItem: TrackEntity, newItem: TrackEntity): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: TrackEntity, newItem: TrackEntity): Boolean {
        return oldItem == newItem
      }
    }
  }
}