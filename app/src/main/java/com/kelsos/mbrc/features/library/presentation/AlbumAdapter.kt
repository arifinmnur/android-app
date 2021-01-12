package com.kelsos.mbrc.features.library.presentation

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.features.library.data.Album
import com.kelsos.mbrc.features.library.popup

class AlbumAdapter : LibraryAdapter<Album, AlbumViewHolder>(DIFF_CALLBACK) {

  private val indicatorPressed: (View, Int) -> Unit = { view, position ->
    view.popup(R.menu.popup_album) {
      requireListener().onMenuItemSelected(it, checkNotNull(getItem(position)))
    }
  }

  private val pressed: (View, Int) -> Unit = { _, position ->
    val listener = requireListener()
    getItem(position)?.run {
      listener.onItemClicked(this)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
    return AlbumViewHolder.create(
      parent,
      indicatorPressed,
      pressed
    )
  }

  override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
    val album = getItem(position)

    if (album != null) {
      holder.bindTo(album)
    } else {
      holder.clear()
    }
  }

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Album>() {
      override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem == newItem
      }
    }
  }
}
