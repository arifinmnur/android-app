package com.kelsos.mbrc.ui.navigation.library.albums

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.popup
import javax.inject.Inject

class AlbumEntryAdapter
@Inject
constructor() : PagedListAdapter<AlbumEntity, AlbumViewHolder>(DIFF_CALLBACK) {

  private var listener: MenuItemSelectedListener<AlbumEntity>? = null

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

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener<AlbumEntity>) {
    this.listener = listener
  }

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AlbumEntity>() {
      override fun areItemsTheSame(oldItem: AlbumEntity, newItem: AlbumEntity): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: AlbumEntity, newItem: AlbumEntity): Boolean {
        return oldItem == newItem
      }
    }
  }
}