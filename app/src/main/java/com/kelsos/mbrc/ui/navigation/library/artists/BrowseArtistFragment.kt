package com.kelsos.mbrc.ui.navigation.library.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.artists.Artist
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.ui.navigation.library.LibraryFragmentDirections
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import kotterknife.bindView
import org.koin.android.ext.android.inject

class BrowseArtistFragment : Fragment(), MenuItemSelectedListener<Artist> {

  private val recycler: RecyclerView by bindView(R.id.library_browser__content)

  private val emptyView: Group by bindView(R.id.library_browser__empty_group)
  private val emptyViewTitle: TextView by bindView(R.id.library_browser__text_title)
  private val emptyViewProgress: ProgressBar by bindView(R.id.library_browser__loading_bar)

  private val adapter: ArtistAdapter by inject()
  private val actionHandler: PopupActionHandler by inject()
  private val presenter: BrowseArtistViewModel by inject()

  private lateinit var syncButton: Button

  fun search(term: String) {
    syncButton.isGone = term.isNotEmpty()
  }

  fun queue(success: Boolean, tracks: Int) {
    val message = if (success) {
      getString(R.string.queue_result__success, tracks)
    } else {
      getString(R.string.queue_result__failure)
    }
    Snackbar.make(recycler, R.string.queue_result__success, Snackbar.LENGTH_SHORT)
      .setText(message)
      .show()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_browse, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    emptyViewTitle.setText(R.string.artists_list_empty)
    syncButton = view.findViewById(R.id.list_empty_sync)
    syncButton.setOnClickListener {
    }
    recycler.setHasFixedSize(true)
    recycler.adapter = adapter
    recycler.layoutManager = LinearLayoutManager(recycler.context)
    adapter.setMenuItemSelectedListener(this)
  }

  override fun onMenuItemSelected(@IdRes itemId: Int, item: Artist) {
    val action = actionHandler.artistSelected(itemId)
    if (action == LibraryPopup.PROFILE) {
      onItemClicked(item)
    }
  }

  override fun onItemClicked(item: Artist) {
    val directions = LibraryFragmentDirections.actionLibraryFragmentToArtistAlbumsFragment(
      item.artist
    )
    findNavController().navigate(directions)
  }

  fun update(pagedList: PagedList<Artist>) {
    emptyView.isVisible = pagedList.isEmpty()
    adapter.submitList(pagedList)
  }

  fun hideLoading() {
    emptyViewProgress.isVisible = false
  }
}