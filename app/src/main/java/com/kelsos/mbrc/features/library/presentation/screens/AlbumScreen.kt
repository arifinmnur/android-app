package com.kelsos.mbrc.features.library.presentation.screens

import androidx.lifecycle.LifecycleOwner
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.Meta
import com.kelsos.mbrc.common.utilities.nonNullObserver
import com.kelsos.mbrc.features.library.MenuItemSelectedListener
import com.kelsos.mbrc.features.library.PopupActionHandler
import com.kelsos.mbrc.features.library.data.Album
import com.kelsos.mbrc.features.library.presentation.AlbumAdapter
import com.kelsos.mbrc.features.library.presentation.AlbumViewModel
import com.kelsos.mbrc.features.library.presentation.LibraryViewHolder
import com.kelsos.mbrc.features.queue.Queue
import com.kelsos.mbrc.features.work.WorkHandler

typealias OnAlbumPressed = (artist: Album) -> Unit

class AlbumScreen(
  private val adapter: AlbumAdapter,
  private val workHandler: WorkHandler,
  private val viewModel: AlbumViewModel,
  private val actionHandler: PopupActionHandler
) : LibraryScreen,
  MenuItemSelectedListener<Album> {
  private var viewHolder: LibraryViewHolder? = null
  private var onAlbumPressedListener: OnAlbumPressed? = null

  fun setOnAlbumPressedListener(onAlbumPressedListener: OnAlbumPressed? = null) {
    this.onAlbumPressedListener = onAlbumPressedListener
  }

  override fun observe(viewLifecycleOwner: LifecycleOwner) {
    viewModel.albums.nonNullObserver(viewLifecycleOwner) {
      adapter.submitList(it)
      viewHolder?.refreshingComplete(it.isEmpty())
    }
  }

  override fun bind(viewHolder: LibraryViewHolder) {
    this.viewHolder = viewHolder
    viewHolder.setup(R.string.albums_list_empty, adapter)
    adapter.setMenuItemSelectedListener(this)
  }

  override fun onMenuItemSelected(itemId: Int, item: Album) {
    val action = actionHandler.genreSelected(itemId)
    if (action === Queue.Default) {
      onItemClicked(item)
    } else {
      workHandler.queue(item.id, Meta.Album, action)
    }
  }

  override fun onItemClicked(item: Album) {
    onAlbumPressedListener?.invoke(item)
  }
}
