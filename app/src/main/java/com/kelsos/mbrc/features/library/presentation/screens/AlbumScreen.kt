package com.kelsos.mbrc.features.library.presentation.screens

import androidx.lifecycle.LifecycleOwner
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.Meta.ALBUM
import com.kelsos.mbrc.common.utilities.nonNullObserver
import com.kelsos.mbrc.features.library.MenuItemSelectedListener
import com.kelsos.mbrc.features.library.PopupActionHandler
import com.kelsos.mbrc.features.library.data.Album
import com.kelsos.mbrc.features.library.presentation.LibraryViewHolder
import com.kelsos.mbrc.features.library.presentation.adapters.AlbumAdapter
import com.kelsos.mbrc.features.library.presentation.viewmodels.AlbumViewModel
import com.kelsos.mbrc.features.queue.Queue
import com.kelsos.mbrc.features.work.WorkHandler
import org.koin.core.KoinComponent
import org.koin.core.inject

class AlbumScreen :
  LibraryScreen,
  KoinComponent,
  MenuItemSelectedListener<Album> {

  private val adapter: AlbumAdapter by inject()
  private val workHandler: WorkHandler by inject()
  private val actionHandler: PopupActionHandler by inject()
  private val viewModel: AlbumViewModel by inject()

  private lateinit var viewHolder: LibraryViewHolder

  override fun observe(viewLifecycleOwner: LifecycleOwner) {
    viewModel.albums.nonNullObserver(viewLifecycleOwner) {
      adapter.submitList(it)
      viewHolder.refreshingComplete(it.isEmpty())
    }
  }

  override fun bind(viewHolder: LibraryViewHolder) {
    this.viewHolder = viewHolder
    viewHolder.setup(R.string.albums_list_empty, adapter)
    adapter.setMenuItemSelectedListener(this)
  }

  override fun onMenuItemSelected(itemId: Int, item: Album) {
    val action = actionHandler.genreSelected(itemId)
    if (action === Queue.DEFAULT) {
      onItemClicked(item)
    } else {
      workHandler.queue(item.id, ALBUM, action)
    }
  }

  override fun onItemClicked(item: Album) {
    workHandler.queue(item.id, ALBUM)
  }
}