package com.kelsos.mbrc.ui.navigation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.ProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.sync.SyncResult
import com.kelsos.mbrc.metrics.SyncedData
import com.kelsos.mbrc.ui.navigation.library.albums.AlbumScreen
import com.kelsos.mbrc.ui.navigation.library.artists.ArtistScreen
import com.kelsos.mbrc.ui.navigation.library.genres.GenreScreen
import com.kelsos.mbrc.ui.navigation.library.tracks.TrackScreen
import com.kelsos.mbrc.utilities.nonNullObserver
import kotterknife.bindView
import org.koin.android.ext.android.inject

class LibraryFragment : Fragment(), OnQueryTextListener {

  private val pager: RecyclerView by bindView(R.id.search_pager)
  private val tabs: TabLayout by bindView(R.id.pager_tab_strip)

  private var searchView: SearchView? = null
  private var searchMenuItem: MenuItem? = null
  private var albumArtistOnly: MenuItem? = null
  private var searchClear: MenuItem? = null
  private var pagerAdapter: LibraryPagerAdapter? = null

  private val viewModel: LibraryViewModel by inject()

  override fun onQueryTextSubmit(query: String): Boolean {
    val search = query.trim()
    if (search.isNotEmpty()) {
      closeSearch()
      searchMenuItem?.isVisible = false
      searchClear?.isVisible = true
    }

    return true
  }

  private fun onSyncResult(result: Int) {
    when (result) {
      SyncResult.NO_OP -> Unit
      SyncResult.FAILED -> Unit
      SyncResult.SUCCESS -> Unit
    }
  }

  private fun closeSearch(): Boolean {
    searchView?.let {
      if (it.isShown) {
        it.isIconified = true
        it.isFocusable = false
        it.clearFocus()
        searchMenuItem?.collapseActionView()
        return true
      }
    }
    return false
  }

  override fun onQueryTextChange(newText: String): Boolean = false

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    viewModel.events.nonNullObserver(viewLifecycleOwner) { event ->
      event.getContentIfNotHandled()?.let { onSyncResult(it) }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    setHasOptionsMenu(true)
    return inflater.inflate(R.layout.fragment_library, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupTabs()

    val pagerAdapter = LibraryPagerAdapter(viewLifecycleOwner).also {
      this.pagerAdapter = it
      pager.adapter = it
      it.submit(
        listOf(
          GenreScreen(),
          ArtistScreen(),
          AlbumScreen(),
          TrackScreen()
        )
      )
    }
    val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
    pager.layoutManager = layoutManager
    val snapHelper = PagerSnapHelper()
    snapHelper.attachToRecyclerView(pager)
    pager.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val itemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        pagerAdapter.setVisiblePosition(itemPosition)

        tabs.getTabAt(itemPosition)?.select()
      }
    })
    tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabReselected(tab: TabLayout.Tab?) {
      }

      override fun onTabUnselected(tab: TabLayout.Tab?) {
      }

      override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.run {
          layoutManager.scrollToPosition(position)
        }
      }
    })
  }

  private fun setupTabs() {
    tabs.addTab(tabs.newTab().setText(R.string.label_genres))
    tabs.addTab(tabs.newTab().setText(R.string.label_artists))
    tabs.addTab(tabs.newTab().setText(R.string.label_albums))
    tabs.addTab(tabs.newTab().setText(R.string.label_tracks))
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater.inflate(R.menu.library_search, menu)
    searchMenuItem = menu.findItem(R.id.library_screen__action_search)?.apply {
      searchView = actionView as SearchView
    }

    albumArtistOnly = menu.findItem(R.id.library_album_artist)

    searchView?.apply {
      queryHint = getString(R.string.library_search_hint)
      setIconifiedByDefault(true)
      setOnQueryTextListener(this@LibraryFragment)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.library_screen__action_refresh -> {
        viewModel.refresh()
        return true
      }
      R.id.library_album_artist -> {
        albumArtistOnly?.let {
          it.isChecked = !it.isChecked
        }

        return true
      }
      R.id.library_search_clear -> {
        searchMenuItem?.isVisible = true
        searchClear?.isVisible = false
        return true
      }
      R.id.library_sync_state -> {
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  fun showStats(stats: SyncedData) {
    val dialog = MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.library_stats__title)
      .setView(R.layout.library_stats__layout)
      .setPositiveButton(android.R.string.ok) { md, _ -> md.dismiss() }
      .show()

    dialog.findViewById<TextView>(R.id.library_stats__genre_value)?.text = "${stats.genres}"
    dialog.findViewById<TextView>(R.id.library_stats__artist_value)?.text = "${stats.artists}"
    dialog.findViewById<TextView>(R.id.library_stats__album_value)?.text = "${stats.albums}"
    dialog.findViewById<TextView>(R.id.library_stats__track_value)?.text = "${stats.tracks}"
    dialog.findViewById<TextView>(R.id.library_stats__playlist_value)?.text = "${stats.playlists}"
  }

  fun syncComplete(stats: SyncedData) {
    val message = getString(
      R.string.library__sync_complete,
      stats.genres,
      stats.artists,
      stats.albums,
      stats.tracks,
      stats.playlists
    )
    Snackbar.make(pager, R.string.library__sync_complete, Snackbar.LENGTH_LONG)
      .setText(message)
      .show()
  }

  override fun onDestroy() {
    pagerAdapter = null
    super.onDestroy()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    // outState.putInt(PAGER_POSITION, pager.currentItem)
  }

  override fun onViewStateRestored(savedInstanceState: Bundle?) {
    super.onViewStateRestored(savedInstanceState)
    // pager.currentItem = savedInstanceState?.getInt(PAGER_POSITION, 0) ?: 0
  }

  fun syncFailure() {
    Snackbar.make(pager, R.string.library__sync_failed, Snackbar.LENGTH_LONG).show()
  }

  fun showSyncProgress() {
    view?.apply {
      findViewById<ProgressIndicator>(R.id.sync_progress).isGone = false
      findViewById<TextView>(R.id.sync_progress_text).isGone = false
    }
  }

  fun hideSyncProgress() {
    view?.apply {
      findViewById<ProgressIndicator>(R.id.sync_progress).isGone = true
      findViewById<TextView>(R.id.sync_progress_text).isGone = true
    }
  }

  companion object {
    private const val PAGER_POSITION = "com.kelsos.mbrc.ui.activities.nav.PAGER_POSITION"
  }
}