package com.kelsos.mbrc.ui.connectionmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.utilities.nonNullObserver
import com.kelsos.mbrc.databinding.FragmentConnectionManagerBinding
import com.kelsos.mbrc.networking.connections.ConnectionSettingsEntity
import com.kelsos.mbrc.networking.discovery.DiscoveryStop
import com.kelsos.mbrc.ui.dialogs.SettingsDialogFragment
import org.koin.android.ext.android.inject

class ConnectionManagerFragment :
  Fragment(),
  SettingsDialogFragment.SettingsSaveListener,
  ConnectionAdapter.ConnectionChangeListener {

  private val connectionManagerViewModel: ConnectionManagerViewModel by inject()
  private lateinit var adapter: ConnectionAdapter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val binding: FragmentConnectionManagerBinding = DataBindingUtil.inflate(
      inflater,
      R.layout.fragment_connection_manager,
      container,
      false
    )

    val recyclerView = binding.connectionManagerConnections
    binding.connectionManagerAdd.setOnClickListener {
      SettingsDialogFragment.create(parentFragmentManager).show(this)
    }
    binding.connectionManagerScan.setOnClickListener {
      binding.connectionManagerProgress.isGone = false
      connectionManagerViewModel.startDiscovery()
    }

    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    adapter = ConnectionAdapter()
    adapter.setChangeListener(this)
    recyclerView.adapter = adapter
    connectionManagerViewModel.settings.nonNullObserver(viewLifecycleOwner) {
      adapter.submitList(it)
    }
    connectionManagerViewModel.default.observe(
      viewLifecycleOwner,
      {
        adapter.setDefault(it)
        if (it !== null) {
          adapter.setSelectionId(it.id)
        }
      }
    )
    connectionManagerViewModel.discoveryStatus.nonNullObserver(viewLifecycleOwner) {
      it.contentIfNotHandled?.let { status ->
        binding.connectionManagerProgress.isGone = true
        val message: String = when (status) {
          DiscoveryStop.NoWifi -> getString(R.string.con_man_no_wifi)
          DiscoveryStop.NotFound -> getString(R.string.con_man_not_found)
          DiscoveryStop.Complete -> {
            getString(R.string.con_man_success)
          }
        }
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
      }
    }
    return binding.root
  }

  override fun onSave(settings: ConnectionSettingsEntity) {
    connectionManagerViewModel.save(settings)
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
}
