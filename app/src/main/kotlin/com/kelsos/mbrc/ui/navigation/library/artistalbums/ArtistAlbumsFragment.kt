package com.kelsos.mbrc.ui.navigation.library.artistalbums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.albums.AlbumEntity
import com.kelsos.mbrc.content.nowplaying.queue.LibraryPopup
import com.kelsos.mbrc.ui.navigation.library.MenuItemSelectedListener
import com.kelsos.mbrc.ui.navigation.library.PopupActionHandler
import com.kelsos.mbrc.ui.navigation.library.albums.AlbumEntryAdapter
import kotterknife.bindView
import org.koin.android.ext.android.inject

class ArtistAlbumsFragment : Fragment(), MenuItemSelectedListener<AlbumEntity> {

  private val recyclerView: RecyclerView by bindView(R.id.artist_albums__album_list)
  private val emptyView: Group by bindView(R.id.artist_albums__empty_view)

  private val actionHandler: PopupActionHandler by inject()
  private val adapter: AlbumEntryAdapter by inject()
  private val viewModel: ArtistAlbumsViewModel by inject()

  private lateinit var artist: String

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_artist_albums, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    adapter.setMenuItemSelectedListener(this)

    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    recyclerView.adapter = adapter
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    artist = ArtistAlbumsFragmentArgs.fromBundle(requireArguments()).artist
    val title = if (artist.isEmpty()) {
      getString(R.string.empty)
    } else {
      artist
    }
  }

  override fun onMenuItemSelected(itemId: Int, item: AlbumEntity) {
    val action = actionHandler.albumSelected(itemId)
    if (action == LibraryPopup.PROFILE) {
      onItemClicked(item)
    } else {
    }
  }

  override fun onItemClicked(item: AlbumEntity) {
    val directions = ArtistAlbumsFragmentDirections.actionArtistAlbumsFragmentToAlbumTracksFragment(
        album = item.album,
        artist = item.artist
      )
    findNavController(this).navigate(directions)
  }

  fun update(albums: PagedList<AlbumEntity>) {
    adapter.submitList(albums)
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