package com.kelsos.mbrc.ui.navigation.library

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.isGone
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.ProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kelsos.mbrc.R
import com.kelsos.mbrc.metrics.SyncedData
import com.kelsos.mbrc.ui.activities.BaseNavigationActivity
import kotterknife.bindView
import toothpick.Scope
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieActivityModule
import javax.inject.Inject

class LibraryActivity : BaseNavigationActivity(),
  LibraryView,
  OnQueryTextListener {

  private val pager: ViewPager2 by bindView(R.id.search_pager)
  private val tabs: TabLayout by bindView(R.id.pager_tab_strip)

  private var searchView: SearchView? = null
  private var searchMenuItem: MenuItem? = null
  private var albumArtistOnly: MenuItem? = null
  private var searchClear: MenuItem? = null
  private var pagerAdapter: LibraryPagerAdapter? = null

  private lateinit var scope: Scope

  @Inject
  lateinit var presenter: LibraryPresenter

  override fun onQueryTextSubmit(query: String): Boolean {
    val search = query.trim()
    if (search.isNotEmpty()) {
      closeSearch()
      presenter.search(search)
      supportActionBar?.title = search
      supportActionBar?.setSubtitle(R.string.library_search_subtitle)
      searchMenuItem?.isVisible = false
      searchClear?.isVisible = true
    } else {
      presenter.search("")
    }

    return true
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

  public override fun onCreate(savedInstanceState: Bundle?) {
    Toothpick.openScope(PRESENTER_SCOPE).installModules(LibraryModule())
    scope = Toothpick.openScopes(application, PRESENTER_SCOPE, this)
    scope.installModules(SmoothieActivityModule(this))
    super.onCreate(savedInstanceState)
    Toothpick.inject(this, scope)
    setContentView(R.layout.activity_library)

    super.setup()
    pagerAdapter = LibraryPagerAdapter(this)
    pager.apply {
      adapter = pagerAdapter
    }

    TabLayoutMediator(tabs, pager) { currentTab, currentPosition ->
      currentTab.text = when (currentPosition) {
        Search.SECTION_ALBUM -> getString(R.string.label_albums)
        Search.SECTION_ARTIST -> getString(R.string.label_artists)
        Search.SECTION_GENRE -> getString(R.string.label_genres)
        Search.SECTION_TRACK -> getString(R.string.label_tracks)
        else -> throw IllegalArgumentException("invalid position")
      }
    }.attach()
    presenter.attach(this)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.library_search, menu)
    searchMenuItem = menu.findItem(R.id.library_search_item)
    searchClear = menu.findItem(R.id.library_search_clear)
    albumArtistOnly = menu.findItem(R.id.library_album_artist)
    searchView = searchMenuItem?.actionView as SearchView
    searchView!!.queryHint = getString(R.string.library_search_hint)
    searchView!!.setIconifiedByDefault(true)
    searchView!!.setOnQueryTextListener(this)
    presenter.loadArtistPreference()
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.library_refresh_item -> {
        presenter.refresh()
        return true
      }
      R.id.library_album_artist -> {
        albumArtistOnly?.let {
          it.isChecked = !it.isChecked
          presenter.setArtistPreference(it.isChecked)
        }

        return true
      }
      R.id.library_search_clear -> {
        supportActionBar?.setTitle(R.string.nav_library)
        supportActionBar?.subtitle = ""
        presenter.search("")
        searchMenuItem?.isVisible = true
        searchClear?.isVisible = false
        return true
      }
      R.id.library_sync_state -> {
        presenter.showStats()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun showStats(stats: SyncedData) {
    val dialog = MaterialAlertDialogBuilder(this)
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

  override fun syncComplete(stats: SyncedData) {
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

  public override fun onDestroy() {
    presenter.detach()
    pagerAdapter = null
    Toothpick.closeScope(this)

    if (isFinishing) {
      Toothpick.closeScope(PRESENTER_SCOPE)
    }
    super.onDestroy()
  }

  override fun onStart() {
    super.onStart()
    presenter.attach(this)
  }

  override fun onStop() {
    super.onStop()
    presenter.detach()
  }

  override fun onBackPressed() {
    if (closeSearch()) {
      return
    }
    super.onBackPressed()
  }

  override fun updateArtistOnlyPreference(albumArtistOnly: Boolean?) {
    this.albumArtistOnly?.isChecked = albumArtistOnly ?: false
  }

  override fun active(): Int {
    return R.id.nav_library
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(PAGER_POSITION, pager.currentItem)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    pager.currentItem = savedInstanceState.getInt(PAGER_POSITION, 0)
  }

  override fun syncFailure() {
    Snackbar.make(pager, R.string.library__sync_failed, Snackbar.LENGTH_LONG).show()
  }

  override fun showSyncProgress() {
    findViewById<ProgressIndicator>(R.id.sync_progress).isGone = false
    findViewById<TextView>(R.id.sync_progress_text).isGone = false
  }

  override fun hideSyncProgress() {
    findViewById<ProgressIndicator>(R.id.sync_progress).isGone = true
    findViewById<TextView>(R.id.sync_progress_text).isGone = true
  }

  @javax.inject.Scope
  @Retention(AnnotationRetention.RUNTIME)
  annotation class Presenter

  companion object {
    private const val PAGER_POSITION = "com.kelsos.mbrc.ui.activities.nav.PAGER_POSITION"
    private val PRESENTER_SCOPE: Class<*> = Presenter::class.java
  }
}