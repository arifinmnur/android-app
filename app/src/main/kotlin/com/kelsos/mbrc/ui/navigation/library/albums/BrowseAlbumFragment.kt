package com.kelsos.mbrc.ui.navigation.library.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.ui.dialogs.SortingDialog
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import kotterknife.bindView
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieActivityModule
import javax.inject.Inject

class BrowseAlbumFragment : Fragment(),
  BrowseAlbumView,
  MenuItemSelectedListener<AlbumEntity> {

  private val recycler: RecyclerView by bindView(R.id.library_browser__content)

  private val emptyView: Group by bindView(R.id.library_browser__empty_group)
  private val emptyViewTitle: TextView by bindView(R.id.library_browser__text_title)
  private val emptyViewProgress: ProgressBar by bindView(R.id.library_browser__loading_bar)

  @Inject
  lateinit var adapter: AlbumEntryAdapter

  @Inject
  lateinit var actionHandler: PopupActionHandler

  @Inject
  lateinit var presenter: BrowseAlbumPresenter

  private lateinit var syncButton: Button

  override fun search(term: String) {
    syncButton.isGone = term.isNotEmpty()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(R.layout.fragment_browse, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    val scope = Toothpick.openScopes(requireActivity().application, this)
    scope.installModules(SmoothieActivityModule(activity), BrowseAlbumModule())
    super.onCreate(savedInstanceState)
    Toothpick.inject(this, scope)
    setHasOptionsMenu(true)
  }

  override fun onStart() {
    super.onStart()
    presenter.attach(this)
  }

  override fun onStop() {
    super.onStop()
    presenter.detach()
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
    syncButton = view.findViewById(R.id.list_empty_sync)
    syncButton.setOnClickListener {
      presenter.sync()
    }
    recycler.adapter = adapter
    recycler.layoutManager = LinearLayoutManager(recycler.context)
    recycler.setHasFixedSize(true)
    adapter.setMenuItemSelectedListener(this)
    presenter.attach(this)
    presenter.load()
  }

  override fun onMenuItemSelected(@IdRes itemId: Int, item: AlbumEntity) {
    val action = actionHandler.albumSelected(itemId, item, requireActivity())
    if (action != LibraryPopup.PROFILE) {
      presenter.queue(action, item)
    }
  }

  override fun onItemClicked(item: AlbumEntity) {
    actionHandler.albumSelected(item, requireActivity())
  }

  override fun update(pagedList: PagedList<AlbumEntity>) {
    emptyView.isVisible = pagedList.isEmpty()
    adapter.submitList(pagedList)
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

  override fun hideLoading() {
    emptyViewProgress.isVisible = false
  }

  override fun onDestroy() {
    Toothpick.closeScope(this)
    super.onDestroy()
  }
}