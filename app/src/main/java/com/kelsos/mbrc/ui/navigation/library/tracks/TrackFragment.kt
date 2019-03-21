package com.kelsos.mbrc.ui.navigation.library.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.tracks.Track
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import com.kelsos.mbrc.utilities.nonNullObserver
import kotterknife.bindView
import org.koin.android.ext.android.inject

class TrackFragment : Fragment(), MenuItemSelectedListener<Track> {

  private val recycler: RecyclerView by bindView(R.id.library_browser__content)

  private val emptyView: Group by bindView(R.id.library_browser__empty_group)
  private val emptyViewTitle: TextView by bindView(R.id.library_browser__text_title)

  private val adapter: TrackAdapter by inject()
  private val actionHandler: PopupActionHandler by inject()
  private val viewModel: TrackViewModel by inject()

  private lateinit var syncButton: Button

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_browse, container, false)
  }

  fun search(term: String) {
    syncButton.isGone = term.isNotEmpty()
  }

  fun queue(success: Boolean, tracks: Int) {
    val message = if (success) {
      getString(R.string.queue_result__success, tracks)
    } else {
      getString(R.string.queue_result__failure)
    }
    Snackbar.make(recycler, R.string.queue_result__success, Snackbar.LENGTH_SHORT)
      .setText(message)
      .show()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    emptyViewTitle.setText(R.string.tracks_list_empty)
    syncButton = view.findViewById(R.id.list_empty_sync)
    syncButton.setOnClickListener {
    }
    recycler.adapter = adapter
    recycler.layoutManager = LinearLayoutManager(recycler.context)
    recycler.setHasFixedSize(true)
    adapter.setMenuItemSelectedListener(this)
    viewModel.tracks.nonNullObserver(this) { list ->
      emptyView.isVisible = list.isEmpty()
      adapter.submitList(list)
    }
  }

  override fun onMenuItemSelected(@IdRes itemId: Int, item: Track) {
    val action = actionHandler.trackSelected(itemId)
    if (action == LibraryPopup.PROFILE) {
      onItemClicked(item)
    }
  }

  override fun onItemClicked(item: Track) {
  }
}