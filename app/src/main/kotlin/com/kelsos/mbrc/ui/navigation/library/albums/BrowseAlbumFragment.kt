package com.kelsos.mbrc.ui.navigation.library.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.albums.Album
import com.kelsos.mbrc.content.nowplaying.queue.Queue
import com.kelsos.mbrc.extensions.fail
import com.kelsos.mbrc.ui.dialogs.SortingDialog
import com.kelsos.mbrc.ui.navigation.library.LibraryActivity.Companion.LIBRARY_SCOPE
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import com.kelsos.mbrc.ui.widgets.EmptyRecyclerView
import com.raizlabs.android.dbflow.list.FlowCursorList
import kotterknife.bindView
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieActivityModule
import javax.inject.Inject

class BrowseAlbumFragment : Fragment(),
  BrowseAlbumView,
  AlbumEntryAdapter.MenuItemSelectedListener {

  private val recycler: EmptyRecyclerView by bindView(R.id.library_data_list)

  private val emptyView: View by bindView(R.id.empty_view)
  private val emptyViewTitle: TextView by bindView(R.id.list_empty_title)
  private val emptyViewIcon: ImageView by bindView(R.id.list_empty_icon)
  private val emptyViewSubTitle: TextView by bindView(R.id.list_empty_subtitle)
  private val emptyViewProgress: ProgressBar by bindView(R.id.empty_view_progress_bar)

  @Inject
  lateinit var adapter: AlbumEntryAdapter

  @Inject
  lateinit var actionHandler: PopupActionHandler

  @Inject
  lateinit var presenter: BrowseAlbumPresenter

  private lateinit var syncButton: Button;

  override fun search(term: String) {
    syncButton.isGone = term.isNotEmpty()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflater.inflate(R.layout.fragment_browse, container, false)
    emptyViewTitle.setText(R.string.albums_list_empty)
    syncButton = view.findViewById(R.id.list_empty_sync);
    syncButton.setOnClickListener {
      presenter.sync()
    }
    return view
  }


  override fun onStart() {
    super.onStart()
    presenter.attach(this)
    adapter.refresh()
  }

  override fun onResume() {
    super.onResume()
    adapter.refresh()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    val activity = activity ?: fail("null activity")
    val scope = Toothpick.openScopes(requireActivity().application, LIBRARY_SCOPE, activity, this)
    scope.installModules(SmoothieActivityModule(requireActivity()), BrowseAlbumModule())
    super.onCreate(savedInstanceState)
    Toothpick.inject(this, scope)
    presenter.attach(this)
    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.browse_album__menu, menu)
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.browse_album__sort_albums) {
      presenter.showSorting()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun showSorting(order: Int, selection: Int) {
    SortingDialog.create(parentFragmentManager, order, selection, {
      presenter.order(it)
    }, {
      presenter.sortBy(it)
    }
    ).show()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    emptyViewTitle.setText(R.string.albums_list_empty)
    recycler.adapter = adapter
    recycler.emptyView = emptyView
    recycler.layoutManager = LinearLayoutManager(recycler.context)
    recycler.setHasFixedSize(true)
    adapter.setMenuItemSelectedListener(this)
    presenter.attach(this)
    presenter.load()
  }

  override fun onMenuItemSelected(menuItem: MenuItem, album: Album) {
    val action = actionHandler.albumSelected(menuItem, album, requireActivity())
    if (action != Queue.PROFILE) {
      presenter.queue(action, album)
    }
  }

  override fun onItemClicked(album: Album) {
    actionHandler.albumSelected(album, requireActivity())
  }

  override fun onStop() {
    super.onStop()

  }

  override fun update(cursor: FlowCursorList<Album>) {
    adapter.update(cursor)
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

  override fun showLoading() {
    emptyViewProgress.visibility = View.VISIBLE
    emptyViewIcon.visibility = View.GONE
    emptyViewTitle.visibility = View.GONE
    emptyViewSubTitle.visibility = View.GONE
  }

  override fun hideLoading() {
    emptyViewProgress.visibility = View.GONE
    emptyViewIcon.visibility = View.VISIBLE
    emptyViewTitle.visibility = View.VISIBLE
    emptyViewSubTitle.visibility = View.VISIBLE
  }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.detach()
  }

  override fun onDestroy() {
    Toothpick.closeScope(this)
    super.onDestroy()
  }
}
