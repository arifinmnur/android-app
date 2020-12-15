package com.kelsos.mbrc.features.library.presentation.screens

import androidx.lifecycle.LifecycleOwner
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.Meta.TRACK
import com.kelsos.mbrc.common.utilities.nonNullObserver
import com.kelsos.mbrc.features.library.MenuItemSelectedListener
import com.kelsos.mbrc.features.library.PopupActionHandler
import com.kelsos.mbrc.features.library.data.Track
import com.kelsos.mbrc.features.library.presentation.LibraryViewHolder
import com.kelsos.mbrc.features.library.presentation.adapters.TrackAdapter
import com.kelsos.mbrc.features.library.presentation.viewmodels.TrackViewModel
import com.kelsos.mbrc.features.queue.Queue
import com.kelsos.mbrc.features.work.WorkHandler

class TrackScreen(
  private val adapter: TrackAdapter,
  private val workHandler: WorkHandler,
  private val viewModel: TrackViewModel,
  private val actionHandler: PopupActionHandler
) : LibraryScreen, MenuItemSelectedListener<Track> {

  private lateinit var viewHolder: LibraryViewHolder

  override fun bind(viewHolder: LibraryViewHolder) {
    this.viewHolder = viewHolder
    viewHolder.setup(R.string.albums_list_empty, adapter)
    adapter.setMenuItemSelectedListener(this)
  }

  override fun observe(viewLifecycleOwner: LifecycleOwner) {
    viewModel.tracks.nonNullObserver(viewLifecycleOwner) {
      adapter.submitList(it)
      viewHolder.refreshingComplete(it.isEmpty())
    }
  }

  override fun onMenuItemSelected(itemId: Int, item: Track) {
    val action = actionHandler.genreSelected(itemId)
    if (action === Queue.DEFAULT) {
      onItemClicked(item)
    } else {
      workHandler.queue(item.id, TRACK, action = action)
    }
  }

  override fun onItemClicked(item: Track) {
    workHandler.queue(item.id, TRACK)
  }
}