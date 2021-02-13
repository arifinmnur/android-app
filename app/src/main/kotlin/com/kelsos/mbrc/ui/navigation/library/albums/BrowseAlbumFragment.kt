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
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.albums.Album
import com.kelsos.mbrc.content.now_playing.queue.Queue
import com.kelsos.mbrc.ui.navigation.library.LibraryActivity.Companion.LIBRARY_SCOPE
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import com.kelsos.mbrc.ui.widgets.EmptyRecyclerView
import com.raizlabs.android.dbflow.list.FlowCursorList
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieActivityModule
import javax.inject.Inject

class BrowseAlbumFragment : Fragment(),
  BrowseAlbumView,
  AlbumEntryAdapter.MenuItemSelectedListener {

  @BindView(R.id.library_data_list)
  lateinit var recycler: EmptyRecyclerView

  @BindView(R.id.empty_view)
  lateinit var emptyView: View

  @BindView(R.id.list_empty_title)
  lateinit var emptyViewTitle: TextView

  @BindView(R.id.list_empty_icon)
  lateinit var emptyViewIcon: ImageView

  @BindView(R.id.list_empty_subtitle)
  lateinit var emptyViewSubTitle: TextView

  @BindView(R.id.empty_view_progress_bar)
  lateinit var emptyViewProgress: ProgressBar

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
    ButterKnife.bind(this, view)
    emptyViewTitle.setText(R.string.albums_list_empty)
    syncButton = view.findViewById<Button>(R.id.list_empty_sync);
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
      showSortingDialog()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recycler.adapter = adapter
    recycler.emptyView = emptyView
    recycler.layoutManager = LinearLayoutManager(recycler.context)
    recycler.setHasFixedSize(true)
    adapter.setMenuItemSelectedListener(this)
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
    presenter.detach()
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

  override fun onDestroy() {
    Toothpick.closeScope(this)
    super.onDestroy()
  }

  private fun showSortingDialog() {
    MaterialAlertDialogBuilder(requireContext())
      .setItems(R.array.album_sorting__options) { dialog, which ->
        dialog.dismiss()
      }
      .setTitle(R.string.album_sorting__dialog_title)
      .setPositiveButton(R.string.album_sorting__positive_button) { dialog, which -> }
      .setNegativeButton(android.R.string.cancel) { dialog, which -> }
      .show()
  }
}
