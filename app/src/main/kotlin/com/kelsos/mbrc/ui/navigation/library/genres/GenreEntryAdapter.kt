package com.kelsos.mbrc.ui.navigation.library.genres

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
import com.kelsos.mbrc.content.library.genres.GenreEntity
import com.kelsos.mbrc.extensions.string
import com.kelsos.mbrc.ui.navigation.library.popup
import kotterknife.bindView
import javax.inject.Inject

class GenreEntryAdapter
@Inject
constructor() : PagedListAdapter<GenreEntity, GenreEntryAdapter.ViewHolder>(DIFF_CALLBACK) {

  private var listener: MenuItemSelectedListener? = null
  private val indicatorPressed: (View, Int) -> Unit = { view, position ->
    view.popup(R.menu.popup_genre) {
      val genreEntity = getItem(position)

      genreEntity?.run {
        listener?.onMenuItemSelected(it, this)
      }
    }
  }

  private val pressed: (View, Int) -> Unit = { _, position ->
    val genreEntity = getItem(position)
    genreEntity?.let {
      listener?.onItemClicked(it)
    }
  }

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener) {
    this.listener = listener
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder.create(parent, indicatorPressed, pressed)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val genre = getItem(holder.adapterPosition)
    if (genre != null) {
      holder.bindTo(genre)
    } else {
      holder.clear()
    }
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

  class ViewHolder(
      itemView: View,
      indicatorPressed: (view: View, position: Int) -> Unit,
      pressed: (view: View, position: Int) -> Unit
  ) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView by bindView(R.id.line_one)
    private val indicator: ImageView by bindView(R.id.ui_item_context_indicator)
    private val empty: String by lazy { string(R.string.empty) }

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
        val view = inflater.inflate(R.layout.listitem_single, parent, false)
        return ViewHolder(view, indicatorPressed, pressed)
      }
    }

    fun bindTo(genre: GenreEntity) {
      title.text = if (genre.genre.isBlank()) empty else genre.genre
    }

    fun clear() {
      title.text = ""
    }
  }
}
