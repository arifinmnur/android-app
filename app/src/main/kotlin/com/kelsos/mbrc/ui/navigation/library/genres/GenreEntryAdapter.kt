package com.kelsos.mbrc.ui.navigation.library.genres

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.genres.GenreEntity
import com.kelsos.mbrc.extensions.string
import kotterknife.bindView
import javax.inject.Inject

class GenreEntryAdapter
@Inject
constructor() : PagedListAdapter<GenreEntity, GenreEntryAdapter.ViewHolder>(DIFF_CALLBACK) {

  private var listener: MenuItemSelectedListener? = null

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener) {
    this.listener = listener
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder.create(parent)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val genre = getItem(holder.adapterPosition)

    genre?.let {
      holder.title.text = if (it.genre.isBlank()) holder.empty else genre.genre
      holder.indicator.setOnClickListener { createPopup(it, genre) }
      holder.itemView.setOnClickListener { listener?.onItemClicked(genre) }
    }

  }

  private fun createPopup(it: View, genre: GenreEntity) {
    val popupMenu = PopupMenu(it.context, it)
    popupMenu.inflate(R.menu.popup_genre)
    popupMenu.setOnMenuItemClickListener { menuItem ->
      return@setOnMenuItemClickListener listener?.onMenuItemSelected(menuItem.itemId, genre) ?: false
    }
    popupMenu.show()
  }

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GenreEntity>() {
      override fun areItemsTheSame(oldItem: GenreEntity, newItem: GenreEntity): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: GenreEntity, newItem: GenreEntity): Boolean {
        return oldItem == newItem
      }
    }
  }

  interface MenuItemSelectedListener {
    fun onMenuItemSelected(@IdRes itemId: Int, genre: GenreEntity): Boolean

    fun onItemClicked(genre: GenreEntity)
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView by bindView(R.id.line_one)
    val indicator: ImageView by bindView(R.id.ui_item_context_indicator)
    val empty: String by lazy { string(R.string.empty) }

    companion object {
      fun create(parent: ViewGroup): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.listitem_single, parent, false)
        return ViewHolder(view)
      }
    }
  }
}
