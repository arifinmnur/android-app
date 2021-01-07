package com.kelsos.mbrc.features.library.presentation.screens

import androidx.lifecycle.LifecycleOwner
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.Meta
import com.kelsos.mbrc.common.utilities.nonNullObserver
import com.kelsos.mbrc.features.library.MenuItemSelectedListener
import com.kelsos.mbrc.features.library.PopupActionHandler
import com.kelsos.mbrc.features.library.data.Artist
import com.kelsos.mbrc.features.library.presentation.LibraryViewHolder
import com.kelsos.mbrc.features.library.presentation.adapters.ArtistAdapter
import com.kelsos.mbrc.features.library.presentation.viewmodels.ArtistViewModel
import com.kelsos.mbrc.features.queue.Queue.Default
import com.kelsos.mbrc.features.work.WorkHandler

typealias OnArtistPressed = (artist: Artist) -> Unit

class ArtistScreen(
  private val adapter: ArtistAdapter,
  private val workHandler: WorkHandler,
  private val viewModel: ArtistViewModel,
  private val actionHandler: PopupActionHandler
) : LibraryScreen,
  MenuItemSelectedListener<Artist> {
  private var viewHolder: LibraryViewHolder? = null
  private var onArtistPressedListener: OnArtistPressed? = null

  fun setOnArtistPressedListener(onArtistPressedListener: OnArtistPressed? = null) {
    this.onArtistPressedListener = onArtistPressedListener
  }

  override fun bind(viewHolder: LibraryViewHolder) {
    this.viewHolder = viewHolder
    viewHolder.setup(R.string.artists_list_empty, adapter)
    adapter.setMenuItemSelectedListener(this)
  }

  override fun observe(viewLifecycleOwner: LifecycleOwner) {
    viewModel.artists.nonNullObserver(viewLifecycleOwner) {
      adapter.submitList(it)
      viewHolder?.refreshingComplete(it.isEmpty())
    }
  }

  override fun onMenuItemSelected(itemId: Int, item: Artist) {
    val action = actionHandler.genreSelected(itemId)
    if (action == Default) {
      onItemClicked(item)
    } else {
      workHandler.queue(item.id, Meta.Artist, action)
    }
  }

  override fun onItemClicked(item: Artist) {
    onArtistPressedListener?.invoke(item)
  }
}
