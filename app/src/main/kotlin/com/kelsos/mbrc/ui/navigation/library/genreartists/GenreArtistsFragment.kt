package com.kelsos.mbrc.ui.navigation.library.genreartists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.artists.ArtistEntity
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import com.kelsos.mbrc.ui.navigation.library.artists.ArtistEntryAdapter
import kotterknife.bindView
import org.koin.android.ext.android.inject

class GenreArtistsFragment : Fragment(), MenuItemSelectedListener<ArtistEntity> {

  private val recyclerView: RecyclerView by bindView(R.id.genre_artists__artist_list)
  private val emptyView: Group by bindView(R.id.genre_artists__empty_view)

  private val adapter: ArtistEntryAdapter by inject()
  private val actionHandler: PopupActionHandler by inject()
  private val presenter: GenreArtistsViewModel by inject()

  private lateinit var genre: String

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_genre_artists, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    adapter.setMenuItemSelectedListener(this)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    genre = GenreArtistsFragmentArgs.fromBundle(requireArguments()).genre

    val title = if (genre.isEmpty()) {
      getString(R.string.empty)
    } else {
      genre
    }
  }

  override fun onMenuItemSelected(@IdRes itemId: Int, item: ArtistEntity) {
    val action = actionHandler.artistSelected(itemId)
    if (action == LibraryPopup.PROFILE) {
      onItemClicked(item)
    }
  }

  override fun onItemClicked(item: ArtistEntity) {
    val directions = GenreArtistsFragmentDirections.actionGenreArtistsFragmentToArtistAlbumsFragment(
      artist = item.artist
    )
    findNavController(this).navigate(directions)
  }

  fun update(pagedList: PagedList<ArtistEntity>) {
    adapter.submitList(pagedList)
  }

  fun queue(success: Boolean, tracks: Int) {
    val message = if (success) {
      getString(R.string.queue_result__success, tracks)
    } else {
      getString(R.string.queue_result__failure)
    }
    Snackbar.make(recyclerView, R.string.queue_result__success, Snackbar.LENGTH_SHORT)
      .setText(message)
      .show()
  }
}