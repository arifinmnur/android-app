package com.kelsos.mbrc.ui.navigation.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelsos.mbrc.R
import kotterknife.bindView

class LibraryPagerAdapter(
  private val viewLifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<LibraryViewHolder>() {
  private var visiblePosition = 0
  private val screens: MutableList<LibraryScreen> = mutableListOf()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
    return LibraryViewHolder.create(parent)
  }

  fun submit(screens: List<LibraryScreen>) {
    this.screens.clear()
    this.screens.addAll(screens)
  }
  override fun getItemCount(): Int = 4

  override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
    val screen = screens[position]
    holder.bind(screen, visiblePosition == position)
    screen.observe(viewLifecycleOwner)
  }

  fun setVisiblePosition(itemPosition: Int) {
    visiblePosition = itemPosition
  }
}

class LibraryViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val recycler: RecyclerView by bindView(R.id.library_browser__content)

  private val emptyView: Group by bindView(R.id.library_browser__empty_group)
  private val emptyViewTitle: TextView by bindView(R.id.library_browser__text_title)

  fun bind(libraryScreen: LibraryScreen, visible: Boolean) {
    libraryScreen.bind(this)
  }

  fun refreshingComplete(empty: Boolean) {
    emptyView.isVisible = empty
  }

  fun setup(
    @StringRes empty: Int,
    adapter: RecyclerView.Adapter<*>
  ) {
    emptyViewTitle.setText(empty)
    recycler.adapter = adapter
    recycler.layoutManager = LinearLayoutManager(recycler.context)
    recycler.setHasFixedSize(true)
  }

  companion object {
    fun create(
      parent: ViewGroup
    ): LibraryViewHolder {
      val inflater: LayoutInflater = LayoutInflater.from(parent.context)
      val view = inflater.inflate(R.layout.fragment_browse, parent, false)
      return LibraryViewHolder(view)
    }
  }
}