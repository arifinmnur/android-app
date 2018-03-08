package com.kelsos.mbrc.ui.navigation.library.artists

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.popup
import com.kelsos.mbrc.utilities.Checks.ifNotNull
import javax.inject.Inject

class ArtistEntryAdapter
@Inject constructor() : PagedListAdapter<ArtistEntity, ArtistViewHolder>(DIFF_CALLBACK) {

  private var listener: MenuItemSelectedListener<ArtistEntity>? = null

  private val indicatorPressed: (View, Int) -> Unit = { view, position ->
    view.popup(R.menu.popup_artist) {
      ifNotNull(listener, getItem(position)) { listener, artist ->
        listener.onMenuItemSelected(it, artist)
      }
    }
  }

  private val pressed: (View, Int) -> Unit = { _, position ->
    val listener = checkNotNull(listener)
    getItem(position)?.run {
      listener.onItemClicked(this)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
    return ArtistViewHolder.create(parent, indicatorPressed, pressed)
  }

  override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
    val artistEntity = getItem(position)
    if (artistEntity != null) {
      holder.bindTo(artistEntity)
    } else {
      holder.clear()
    }
  }

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener<ArtistEntity>) {
    this.listener = listener
  }

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ArtistEntity>() {
      override fun areItemsTheSame(oldItem: ArtistEntity, newItem: ArtistEntity): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: ArtistEntity, newItem: ArtistEntity): Boolean {
        return oldItem == newItem
      }
    }
  }
}