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
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.ui.navigation.library.LibraryFragmentDirections
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import kotterknife.bindView
import org.koin.android.ext.android.inject

class BrowseArtistFragment : Fragment(),
  BrowseArtistView,
  MenuItemSelectedListener<ArtistEntity> {

  private val recycler: RecyclerView by bindView(R.id.library_browser__content)

  private val emptyView: Group by bindView(R.id.library_browser__empty_group)
  private val emptyViewTitle: TextView by bindView(R.id.library_browser__text_title)
  private val emptyViewProgress: ProgressBar by bindView(R.id.library_browser__loading_bar)

  private val adapter: ArtistEntryAdapter by inject()
  private val actionHandler: PopupActionHandler by inject()
  private val presenter: BrowseArtistPresenter by inject()

  private lateinit var syncButton: Button

  override fun search(term: String) {
    syncButton.isGone = term.isNotEmpty()
  }

  override fun queue(success: Boolean, tracks: Int) {
    val message = if (success) {
      getString(R.string.queue_result__success, tracks)
    } else {
      getString(R.string.queue_result__failure)
    }
    Snackbar.make(recycler, R.string.queue_result__success, Snackbar.LENGTH_SHORT)
      .setText(message)
      .show()
  }

  override fun onStart() {
    super.onStart()
    presenter.attach(this)
  }

  override fun onStop() {
    super.onStop()
    presenter.detach()
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
      presenter.sync()
    }
    recycler.setHasFixedSize(true)
    recycler.adapter = adapter
    recycler.layoutManager = LinearLayoutManager(recycler.context)
    adapter.setMenuItemSelectedListener(this)
    presenter.attach(this)
    presenter.load()
  }

  override fun onMenuItemSelected(@IdRes itemId: Int, item: ArtistEntity) {
    val action = actionHandler.artistSelected(itemId)
    if (action == LibraryPopup.PROFILE) {
      onItemClicked(item)
    } else {
      presenter.queue(action, item)
    }
  }

  override fun onItemClicked(item: ArtistEntity) {
    val directions = LibraryFragmentDirections.actionLibraryFragmentToArtistAlbumsFragment(
      item.artist
    )
    findNavController().navigate(directions)
  }

  override fun update(pagedList: PagedList<ArtistEntity>) {
    emptyView.isVisible = pagedList.isEmpty()
    adapter.submitList(pagedList)
  }

  override fun hideLoading() {
    emptyViewProgress.isVisible = false
  }
}