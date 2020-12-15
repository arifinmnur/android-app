package com.kelsos.mbrc.features.library.presentation.screens

import androidx.lifecycle.LifecycleOwner
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.Meta.GENRE
import com.kelsos.mbrc.common.utilities.nonNullObserver
import com.kelsos.mbrc.features.library.MenuItemSelectedListener
import com.kelsos.mbrc.features.library.PopupActionHandler
import com.kelsos.mbrc.features.library.data.Genre
import com.kelsos.mbrc.features.library.presentation.LibraryViewHolder
import com.kelsos.mbrc.features.library.presentation.adapters.GenreAdapter
import com.kelsos.mbrc.features.library.presentation.viewmodels.GenreViewModel
import com.kelsos.mbrc.features.queue.Queue.DEFAULT
import com.kelsos.mbrc.features.work.WorkHandler

class GenreScreen(
  private val adapter: GenreAdapter,
  private val workHandler: WorkHandler,
  private val viewModel: GenreViewModel,
  private val actionHandler: PopupActionHandler
) : LibraryScreen,
  MenuItemSelectedListener<Genre> {

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
    if (action == DEFAULT) {
      onItemClicked(item)
    } else {
      workHandler.queue(item.id, GENRE, action)
    }
  }

  override fun onItemClicked(item: Genre) {
    workHandler.queue(item.id, GENRE)
  }
}