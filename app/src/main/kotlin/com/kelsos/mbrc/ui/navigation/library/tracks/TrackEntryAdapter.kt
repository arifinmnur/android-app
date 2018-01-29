package com.kelsos.mbrc.ui.navigation.library.tracks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.tracks.TrackEntity
import com.kelsos.mbrc.extensions.string
import com.kelsos.mbrc.ui.navigation.library.popup
import com.kelsos.mbrc.utilities.Checks.ifNotNull
import kotterknife.bindView
import javax.inject.Inject

class TrackEntryAdapter
@Inject
constructor() : PagedListAdapter<TrackEntity, TrackEntryAdapter.ViewHolder>(DIFF_CALLBACK) {
  private var listener: MenuItemSelectedListener? = null
  private val indicatorPressed: (View, Int) -> Unit = { view, position ->
    view.popup(R.menu.popup_track) {
      ifNotNull(listener, getItem(position)) { listener, track ->
        listener.onMenuItemSelected(it, track)
      }
    }
  }

  private val pressed: (View, Int) -> Unit = { _, position ->
    ifNotNull(listener, getItem(position)) { listener, track ->
      listener.onItemClicked(track)
    }
  }

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener) {
    this.listener = listener
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder.create(parent, indicatorPressed, pressed)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val trackEntity = getItem(holder.adapterPosition)

    if (trackEntity != null) {
      holder.bindTo(trackEntity)
    } else {
      holder.clear()
    }
  }

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrackEntity>() {
      override fun areItemsTheSame(oldItem: TrackEntity, newItem: TrackEntity): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: TrackEntity, newItem: TrackEntity): Boolean {
        return oldItem == newItem
      }
    }
  }

  interface MenuItemSelectedListener {
    fun onMenuItemSelected(@IdRes itemId: Int, track: TrackEntity)

    fun onItemClicked(track: TrackEntity)
  }

  class ViewHolder(
    itemView: View,
    indicatorPressed: (view: View, position: Int) -> Unit,
    pressed: (view: View, position: Int) -> Unit
  ) : RecyclerView.ViewHolder(itemView) {
    private val artist: TextView by bindView(R.id.line_two)
    private val title: TextView by bindView(R.id.line_one)
    private val indicator: ImageView by bindView(R.id.overflow_menu)
    private val unknownArtist: String by lazy { string(R.string.unknown_artist) }

    init {
      indicator.setOnClickListener { indicatorPressed(it, adapterPosition) }
      itemView.setOnClickListener { pressed(it, adapterPosition) }
    }

    companion object {
      fun create(
        parent: ViewGroup,
        indicatorPressed: (view: View, position: Int) -> Unit,
        pressed: (view: View, position: Int) -> Unit
      ): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.ui_list_dual, parent, false)
        return ViewHolder(view, indicatorPressed, pressed)
      }
    }

    fun clear() {
      artist.text = ""
      title.text = ""
    }

    fun bindTo(trackEntity: TrackEntity) {
      title.text = trackEntity.title
      artist.text = if (trackEntity.artist.isBlank()) unknownArtist else trackEntity.artist
    }
  }

}
