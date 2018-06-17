package com.kelsos.mbrc.ui.navigation.library.genres

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.genres.GenreEntity
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.ui.navigation.library.LibraryFragmentDirections
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import kotterknife.bindView
import toothpick.Toothpick
import javax.inject.Inject

class BrowseGenreFragment : Fragment(),
  BrowseGenreView,
  MenuItemSelectedListener<GenreEntity> {

  private val recycler: RecyclerView by bindView(R.id.library_browser__content)

  private val emptyView: Group by bindView(R.id.library_browser__empty_group)
  private val emptyViewTitle: TextView by bindView(R.id.library_browser__text_title)
  private val emptyViewProgress: ProgressBar by bindView(R.id.library_browser__loading_bar)

  @Inject
  lateinit var adapter: GenreEntryAdapter

  @Inject
  lateinit var actionHandler: PopupActionHandler

  @Inject
  lateinit var presenter: BrowseGenrePresenter

  private lateinit var syncButton: Button

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_browse, container, false)
  }

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

  override fun onCreate(savedInstanceState: Bundle?) {
    val scope = Toothpick.openScopes(requireActivity().application, requireActivity(), this)
    scope.installModules(BrowseGenreModule())
    super.onCreate(savedInstanceState)
    Toothpick.inject(this, scope)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.detach()
  }

  override fun update(pagedList: PagedList<GenreEntity>) {
    emptyView.isVisible = pagedList.isEmpty()
    adapter.submitList(pagedList)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    emptyViewTitle.setText(R.string.genres_list_empty)
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

  override fun onMenuItemSelected(itemId: Int, item: GenreEntity) {
    val action = actionHandler.genreSelected(itemId)
    if (action === LibraryPopup.PROFILE) {
      onItemClicked(item)
    } else {
      presenter.queue(action, item)
    }
  }

  override fun onItemClicked(item: GenreEntity) {
    val directions = LibraryFragmentDirections.actionLibraryFragmentToGenreArtistsActivity(
      item.genre
    )
    findNavController(this).navigate(directions)
  }

  override fun onDestroy() {
    Toothpick.closeScope(this)
    super.onDestroy()
  }

  override fun hideLoading() {
    emptyViewProgress.isVisible = false
  }
}