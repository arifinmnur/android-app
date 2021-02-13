package com.kelsos.mbrc.ui.navigation.library.genre_artists

import android.os.Bundle
import android.view.MenuItem
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.artists.Artist
import com.kelsos.mbrc.content.now_playing.queue.Queue
import com.kelsos.mbrc.extensions.enableHome
import com.kelsos.mbrc.ui.activities.FontActivity
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import com.kelsos.mbrc.ui.navigation.library.artists.ArtistEntryAdapter
import com.kelsos.mbrc.ui.navigation.library.artists.ArtistEntryAdapter.MenuItemSelectedListener
import com.kelsos.mbrc.ui.widgets.EmptyRecyclerView
import com.raizlabs.android.dbflow.list.FlowCursorList
import kotterknife.bindView
import toothpick.Scope
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieActivityModule
import javax.inject.Inject

class GenreArtistsActivity : FontActivity(),
  GenreArtistsView,
  MenuItemSelectedListener {

  private val recyclerView: EmptyRecyclerView by bindView(R.id.genre_artists_recycler)
  private val toolbar: MaterialToolbar by bindView(R.id.toolbar)
  private val emptyView: ConstraintLayout by bindView(R.id.empty_view)

  @Inject
  lateinit var adapter: ArtistEntryAdapter

  @Inject
  lateinit var actionHandler: PopupActionHandler

  @Inject
  lateinit var presenter: GenreArtistsPresenter

  private var genre: String? = null
  private var scope: Scope? = null

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_genre_artists)
    scope = Toothpick.openScopes(application, this)
    scope!!.installModules(
      SmoothieActivityModule(this),
      GenreArtistsModule()
    )
    Toothpick.inject(this, scope)

    genre = intent?.extras?.getString(GENRE_NAME)

    if (genre == null) {
      finish()
      return
    }

    setSupportActionBar(toolbar)
    supportActionBar?.enableHome(genre)
    if (genre.isNullOrEmpty()) {
      supportActionBar?.setTitle(R.string.empty)
    }
    adapter.setMenuItemSelectedListener(this)
    recyclerView.adapter = adapter
    recyclerView.emptyView = emptyView
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    presenter.attach(this)
    presenter.load(genre!!)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val itemId = item.itemId

    if (itemId == android.R.id.home) {
      onBackPressed()
      return true
    }

    return super.onOptionsItemSelected(item)
  }

  override fun onMenuItemSelected(menuItem: MenuItem, artist: Artist) {
    val action = actionHandler.artistSelected(menuItem, artist, this)
    if (action != Queue.PROFILE) {
      presenter.queue(action, artist)
    }
  }

  override fun onItemClicked(artist: Artist) {
    actionHandler.artistSelected(artist, this)
  }

  override fun update(data: FlowCursorList<Artist>) {
    adapter.update(data)
  }

  override fun queue(success: Boolean, tracks: Int) {
    val message = if (success) {
      getString(R.string.queue_result__success, tracks)
    } else {
      getString(R.string.queue_result__failure)
    }
    Snackbar.make(recyclerView, R.string.queue_result__success, Snackbar.LENGTH_SHORT)
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

  override fun onDestroy() {
    super.onDestroy()
    Toothpick.closeScope(this)
  }

  override fun onBackPressed() {
    finish()
  }

  companion object {
    const val GENRE_NAME = "genre_name"
  }
}

