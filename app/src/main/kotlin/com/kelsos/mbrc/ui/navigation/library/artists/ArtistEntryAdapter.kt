package com.kelsos.mbrc.ui.navigation.library.artists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.ui.navigation.library.popup
import com.kelsos.mbrc.utilities.Checks.ifNotNull
import kotterknife.bindView
import javax.inject.Inject

class ArtistEntryAdapter
@Inject constructor() : PagedListAdapter<ArtistEntity, ArtistEntryAdapter.ViewHolder>(DIFF_CALLBACK) {
  private var listener: MenuItemSelectedListener? = null
  private val indicatorPressed: (View, Int) -> Unit = { view, position ->
    view.popup(R.menu.popup_artist) {
      ifNotNull(listener, getItem(position)) { listener, artist ->
        listener.onMenuItemSelected(it, artist)
      }
    }
  }

  private val pressed: (View, Int) -> Unit = { _, position ->
    ifNotNull(listener, getItem(position)) { listener, artist ->
      listener.onItemClicked(artist)
    }
  }

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener) {
    this.listener = listener
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder.create(parent, indicatorPressed, pressed)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val artistEntity = getItem(position)
    if (artistEntity != null) {
      holder.bindTo(artistEntity)
    } else {
      holder.clear()
    }

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

  interface MenuItemSelectedListener {
    fun onMenuItemSelected(itemId: Int, artist: ArtistEntity)

    fun onItemClicked(artist: ArtistEntity)
  }

  class ViewHolder(
    itemView: View,
    indicatorPressed: (View, Int) -> Unit,
    pressed: (View, Int) -> Unit
  ) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView by bindView(R.id.line_one)
    private val indicator: ImageView by bindView(R.id.ui_item_context_indicator)
    private val empty: String = itemView.context.getString(R.string.empty)

    init {
      indicator.setOnClickListener { indicatorPressed(it, adapterPosition) }
      itemView.setOnClickListener { pressed(it, adapterPosition) }
    }

    companion object {
      fun create(
        parent: ViewGroup,
        indicatorPressed: (View, Int) -> Unit,
        pressed: (View, Int) -> Unit
      ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.listitem_single, parent, false)
        return ViewHolder(view, indicatorPressed, pressed)
      }
    }

    fun bindTo(artistEntity: ArtistEntity) {
      title.text = if (artistEntity.artist.isBlank()) {
        empty
      } else {
        artistEntity.artist
      }
    }

    fun clear() {
      title.text = ""
    }
  }
}
