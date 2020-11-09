package com.kelsos.mbrc.features.library.presentation

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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.utilities.nonNullObserver
import com.kelsos.mbrc.databinding.FragmentLibraryBinding
import com.kelsos.mbrc.features.library.presentation.screens.AlbumScreen
import com.kelsos.mbrc.features.library.presentation.screens.ArtistScreen
import com.kelsos.mbrc.features.library.presentation.screens.GenreScreen
import com.kelsos.mbrc.features.library.presentation.screens.TrackScreen
import com.kelsos.mbrc.features.library.sync.SyncResult
import com.kelsos.mbrc.metrics.SyncedData
import org.koin.android.ext.android.inject

class LibraryFragment : Fragment(), OnQueryTextListener {

  private lateinit var pager: ViewPager2
  private lateinit var tabs: TabLayout
  private var dataBinding: FragmentLibraryBinding? = null

  private var searchView: SearchView? = null
  private var searchMenuItem: MenuItem? = null
  private var albumArtistOnly: MenuItem? = null
  private var searchClear: MenuItem? = null
  private var pagerAdapter: LibraryPagerAdapter? = null

  private val viewModel: LibraryViewModel by inject()

  override fun onQueryTextSubmit(query: String): Boolean {
    if (query.isNotEmpty() && query.trim { it <= ' ' }.isNotEmpty()) {
      closeSearch()
      searchMenuItem?.isVisible = false
      searchClear?.isVisible = true
    }

    return true
  }

  private fun onSyncResult(result: SyncResult) {
    when (result) {
      SyncResult.NOOP -> Unit
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

    viewModel.emitter.nonNullObserver(viewLifecycleOwner) { event ->
      event.contentIfNotHandled?.let { onSyncResult(it) }
    }
    dataBinding?.sync = viewModel.syncProgress
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    setHasOptionsMenu(true)
    val dataBinding: FragmentLibraryBinding = DataBindingUtil.inflate(
      inflater,
      R.layout.fragment_library,
      container,
      false
    )
    this.dataBinding = dataBinding
    dataBinding.lifecycleOwner = viewLifecycleOwner
    pager = dataBinding.searchPager
    tabs = dataBinding.pagerTabStrip
    return dataBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val pagerAdapter = LibraryPagerAdapter(
      viewLifecycleOwner
    ).also {
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

    pager.adapter = pagerAdapter

    TabLayoutMediator(tabs, pager) { tab, position ->
      val resId = when (position) {
        0 -> R.string.label_genres
        1 -> R.string.label_artists
        2 -> R.string.label_albums
        3 -> R.string.label_tracks
        else -> error("invalid position")
      }

      tab.setText(resId)
    }.attach()
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
    dataBinding = null
    super.onDestroy()
  }

  companion object {
    private const val PAGER_POSITION = "com.kelsos.mbrc.ui.activities.nav.PAGER_POSITION"
  }
}