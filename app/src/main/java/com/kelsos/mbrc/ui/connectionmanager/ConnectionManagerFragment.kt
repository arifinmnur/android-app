package com.kelsos.mbrc.ui.connectionmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.ProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.networking.connections.ConnectionSettingsEntity
import com.kelsos.mbrc.networking.discovery.DiscoveryStop
import com.kelsos.mbrc.ui.dialogs.SettingsDialogFragment
import com.kelsos.mbrc.utilities.nonNullObserver
import kotterknife.bindView
import org.koin.android.ext.android.inject

class ConnectionManagerFragment : Fragment(),
  SettingsDialogFragment.SettingsSaveListener,
  ConnectionAdapter.ConnectionChangeListener {

  private val connectionManagerViewModel: ConnectionManagerViewModel by inject()

  private val recyclerView: RecyclerView by bindView(R.id.connection_manager__connections)

  private lateinit var adapter: ConnectionAdapter

  private val addButton: Button by bindView(R.id.connection_manager__add)
  private val scanButton: Button by bindView(R.id.connection_manager__scan)

  private fun onAddButtonClick() {
    SettingsDialogFragment.create(parentFragmentManager).show(this)
  }

  private fun onScanButtonClick() {
    view?.findViewById<ProgressIndicator>(R.id.connection_manager__progress)?.isGone = false
    connectionManagerViewModel.startDiscovery()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_connection_manager, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    addButton.setOnClickListener { onAddButtonClick() }
    scanButton.setOnClickListener { onScanButtonClick() }

    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    adapter = ConnectionAdapter()
    adapter.setChangeListener(this)
    recyclerView.adapter = adapter
    connectionManagerViewModel.settings.nonNullObserver(this) {
      adapter.submitList(it)
    }
    connectionManagerViewModel.default.observe(this, Observer {
      adapter.setDefault(it)
    })
    connectionManagerViewModel.discoveryStatus.nonNullObserver(this) {
      it.getContentIfNotHandled()?.let { status ->
        onDiscoveryStopped(status)
      }
    }
  }

  override fun onSave(settings: ConnectionSettingsEntity) {
    connectionManagerViewModel.save(settings)
  }

  fun onDiscoveryStopped(status: Int) {
    view?.findViewById<ProgressIndicator>(R.id.connection_manager__progress)?.isGone = true

    val message: String = when (status) {
      DiscoveryStop.NO_WIFI -> getString(R.string.con_man_no_wifi)
      DiscoveryStop.NOT_FOUND -> getString(R.string.con_man_not_found)
      DiscoveryStop.COMPLETE -> {
        getString(R.string.con_man_success)
      }
      else -> getString(R.string.unknown_reason)
    }

    Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show()
  }

  override fun onDelete(settings: ConnectionSettingsEntity) {
    connectionManagerViewModel.delete(settings)
  }

  override fun onEdit(settings: ConnectionSettingsEntity) {
    val settingsDialog = SettingsDialogFragment.newInstance(settings, parentFragmentManager)
    settingsDialog.show(this)
  }

  override fun onDefault(settings: ConnectionSettingsEntity) {
    connectionManagerViewModel.setDefault(settings)
  }

  fun updateDefault(defaultId: Long) {
    adapter.setSelectionId(defaultId)
  }
}