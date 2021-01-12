package com.kelsos.mbrc.features.library.presentation

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.features.library.data.Artist
import com.kelsos.mbrc.features.library.popup

class ArtistAdapter : LibraryAdapter<Artist, ArtistViewHolder>(DIFF_CALLBACK) {

  private val indicatorPressed: (View, Int) -> Unit = { view, position ->
    view.popup(R.menu.popup_artist) {
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

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
    return ArtistViewHolder.create(
      parent,
      indicatorPressed,
      pressed
    )
  }

  override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
    val artistEntity = getItem(position)
    if (artistEntity != null) {
      holder.bindTo(artistEntity)
    } else {
      holder.clear()
    }
  }

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Artist>() {
      override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
        return oldItem == newItem
      }
    }
  }
}
