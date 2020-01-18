package com.kelsos.mbrc.features.library.genres

import androidx.lifecycle.LifecycleOwner
import com.kelsos.mbrc.R
import com.kelsos.mbrc.features.library.LibraryScreen
import com.kelsos.mbrc.features.library.LibraryViewHolder
import com.kelsos.mbrc.features.library.MenuItemSelectedListener
import com.kelsos.mbrc.features.library.PopupActionHandler
import com.kelsos.mbrc.features.queue.LibraryPopup
import com.kelsos.mbrc.utilities.nonNullObserver
import org.koin.core.KoinComponent
import org.koin.core.inject

class GenreScreen : LibraryScreen,
  KoinComponent,
  MenuItemSelectedListener<Genre> {

  private val adapter: GenreAdapter by inject()
  private val actionHandler: PopupActionHandler by inject()
  private val viewModel: GenreViewModel by inject()

  private lateinit var viewHolder: LibraryViewHolder

  override fun bind(viewHolder: LibraryViewHolder) {
    this.viewHolder = viewHolder
    viewHolder.setup(R.string.albums_list_empty, adapter)
    adapter.setMenuItemSelectedListener(this)
  }

  override fun observe(viewLifecycleOwner: LifecycleOwner) {
    viewModel.genres.nonNullObserver(viewLifecycleOwner) {
      adapter.submitList(it)
      viewHolder.refreshingComplete(it.isEmpty())
    }
  }

  override fun onMenuItemSelected(itemId: Int, item: Genre) {
    val action = actionHandler.genreSelected(itemId)
    if (action === LibraryPopup.PROFILE) {
      onItemClicked(item)
    }
  }

  override fun onItemClicked(item: Genre) {
  }
}