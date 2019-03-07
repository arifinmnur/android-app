package com.kelsos.mbrc.ui.navigation.library.albums

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.albums.Album
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.popup

class AlbumAdapter : PagedListAdapter<Album, AlbumViewHolder>(DIFF_CALLBACK) {

  private var listener: MenuItemSelectedListener<Album>? = null

  private val indicatorPressed: (View, Int) -> Unit = { view, position ->
    view.popup(R.menu.popup_album) {
      checkNotNull(listener).onMenuItemSelected(it, checkNotNull(getItem(position)))
    }
  }

  private val pressed: (View, Int) -> Unit = { _, position ->
    val listener = checkNotNull(listener)
    getItem(position)?.run {
      listener.onItemClicked(this)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
    return AlbumViewHolder.create(parent, indicatorPressed, pressed)
  }

  override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
    val albumEntity = getItem(position)

    if (albumEntity != null) {
      holder.bindTo(albumEntity)
    } else {
      holder.clear()
    }
  }

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener<Album>) {
    this.listener = listener
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