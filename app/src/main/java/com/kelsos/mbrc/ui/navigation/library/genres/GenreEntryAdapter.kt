package com.kelsos.mbrc.ui.navigation.library.genres

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.genres.Genre
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.popup

class GenreEntryAdapter : PagedListAdapter<Genre, GenreViewHolder>(DIFF_CALLBACK) {

  private var listener: MenuItemSelectedListener<Genre>? = null

  private val indicatorPressed: (View, Int) -> Unit = { view, position ->
    view.popup(R.menu.popup_genre) {
      val listener = checkNotNull(listener)
      getItem(position)?.run {
        listener.onMenuItemSelected(it, this)
      }
    }
  }

  private val pressed: (View, Int) -> Unit = { _, position ->
    val listener = checkNotNull(listener)
    getItem(position)?.let {
      listener.onItemClicked(it)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
    return GenreViewHolder.create(parent, indicatorPressed, pressed)
  }

  override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
    val genre = getItem(holder.adapterPosition)
    if (genre != null) {
      holder.bindTo(genre)
    } else {
      holder.clear()
    }
  }

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener<Genre>) {
    this.listener = listener
  }

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Genre>() {
      override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean {
        return oldItem == newItem
      }
    }
  }
}