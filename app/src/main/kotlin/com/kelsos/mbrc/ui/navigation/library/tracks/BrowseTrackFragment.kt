package com.kelsos.mbrc.ui.navigation.library.tracks

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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.tracks.TrackEntity
import com.kelsos.mbrc.extensions.gone
import com.kelsos.mbrc.extensions.hide
import com.kelsos.mbrc.extensions.show
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import com.kelsos.mbrc.ui.navigation.library.tracks.TrackEntryAdapter.MenuItemSelectedListener
import kotterknife.bindView
import toothpick.Toothpick
import javax.inject.Inject

class BrowseTrackFragment : Fragment(),
  BrowseTrackView,
  MenuItemSelectedListener {

  private val recycler: RecyclerView by bindView(R.id.library_browser__content)

  private val emptyView: Group by bindView(R.id.library_browser__empty_group)
  private val emptyViewTitle: TextView by bindView(R.id.library_browser__text_title)
  private val emptyViewProgress: ProgressBar by bindView(R.id.library_browser__loading_bar)

  @Inject
  lateinit var adapter: TrackEntryAdapter

  @Inject
  lateinit var actionHandler: PopupActionHandler

  @Inject
  lateinit var presenter: BrowseTrackPresenter

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
    val scope = Toothpick.openScopes(requireActivity().application, this)
    scope.installModules(BrowseTrackModule())
    super.onCreate(savedInstanceState)
    Toothpick.inject(this, scope)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.detach()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    emptyViewTitle.setText(R.string.tracks_list_empty)
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

  override fun update(list: List<TrackEntity>) {
    if (list.isEmpty()) {
      emptyView.show()
    } else {
      emptyView.hide()
    }
    adapter.setList(list)
  }

  override fun onMenuItemSelected(@IdRes itemId: Int, track: TrackEntity) {
    presenter.queue(track, actionHandler.trackSelected(itemId))
  }

  override fun onItemClicked(track: TrackEntity) {
    presenter.queue(track)
  }

  override fun hideLoading() {
    emptyViewProgress.gone()
  }
}
