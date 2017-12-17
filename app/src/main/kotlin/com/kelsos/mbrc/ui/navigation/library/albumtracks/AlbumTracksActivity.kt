package com.kelsos.mbrc.ui.navigation.library.albumtracks

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.albums.AlbumInfo
import com.kelsos.mbrc.content.library.tracks.TrackEntity
import com.kelsos.mbrc.ui.activities.BaseActivity
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import com.kelsos.mbrc.ui.navigation.library.tracks.TrackEntryAdapter
import com.kelsos.mbrc.ui.widgets.EmptyRecyclerView
import kotterknife.bindView
import toothpick.Scope
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieActivityModule
import javax.inject.Inject

class AlbumTracksActivity : BaseActivity(),
  AlbumTracksView,
  TrackEntryAdapter.MenuItemSelectedListener {

  private val listTracks: EmptyRecyclerView by bindView(R.id.list_tracks)
  private val emptyView: LinearLayout by bindView(R.id.empty_view)

  @Inject
  lateinit var adapter: TrackEntryAdapter

  @Inject
  lateinit var actionHandler: PopupActionHandler

  @Inject
  lateinit var presenter: AlbumTracksPresenter

  private var album: AlbumInfo? = null
  private lateinit var scope: Scope

  public override fun onCreate(savedInstanceState: Bundle?) {
    scope = Toothpick.openScopes(application, this)
    scope.installModules(
      SmoothieActivityModule(this),
      AlbumTracksModule()
    )
    super.onCreate(savedInstanceState)
    Toothpick.inject(this, scope)
    setContentView(R.layout.activity_album_tracks)
    val extras = intent.extras

    if (extras != null) {
      album = extras.getParcelable(ALBUM)
    }

    val selectedAlbum = album
    if (selectedAlbum == null) {
      finish()
      return
    }

    val albumTitle = album?.album ?: ""
    val title = if (albumTitle.isBlank()) {
      getString(R.string.non_album_tracks)
    } else {
      albumTitle
    }

    setupToolbar(title, subtitle = selectedAlbum.artist)

    adapter.setMenuItemSelectedListener(this)
    listTracks.layoutManager = LinearLayoutManager(baseContext)
    listTracks.adapter = adapter
    listTracks.emptyView = emptyView

    val fab = findViewById<FloatingActionButton>(R.id.play_album)
    fab.isVisible = true
    fab.setOnClickListener {
      presenter.queueAlbum(selectedAlbum.artist, selectedAlbum.album)
    }

    presenter.attach(this)
    presenter.load(album!!)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val itemId = item.itemId

    if (itemId == android.R.id.home) {
      onBackPressed()
      return true
    }

    return super.onOptionsItemSelected(item)
  }

  override fun onMenuItemSelected(menuItem: MenuItem, track: TrackEntity) {
    presenter.queue(track, actionHandler.trackSelected(menuItem))
  }

  override fun onItemClicked(track: TrackEntity) {
    presenter.queue(track)
  }

  override fun update(cursor: List<TrackEntity>) {
    adapter.update(cursor)
  }

  override fun queue(success: Boolean, tracks: Int) {
    val message = if (success) {
      getString(R.string.queue_result__success, tracks)
    } else {
      getString(R.string.queue_result__failure)
    }
    Snackbar.make(listTracks, R.string.queue_result__success, Snackbar.LENGTH_SHORT)
      .setText(message)
      .show()
  }

  override fun onDestroy() {
    presenter.detach()
    Toothpick.closeScope(this)
    super.onDestroy()
  }

  override fun onBackPressed() {
    finish()
  }

  companion object {
    const val ALBUM = "albumName"
  }
}
