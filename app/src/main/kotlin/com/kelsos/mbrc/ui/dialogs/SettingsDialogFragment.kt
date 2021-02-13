package com.kelsos.mbrc.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kelsos.mbrc.R
import com.kelsos.mbrc.networking.connections.ConnectionSettingsEntity

class SettingsDialogFragment : DialogFragment() {

  private lateinit var hostEdit: EditText
  private lateinit var nameEdit: EditText
  private lateinit var portEdit: EditText

  private var mListener: SettingsSaveListener? = null
  private lateinit var settings: ConnectionSettingsEntity
  private var edit: Boolean = false

  private fun setConnectionSettings(settings: ConnectionSettingsEntity) {
    this.settings = settings
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    try {
      mListener = context as SettingsSaveListener?
    } catch (e: ClassCastException) {
      throw ClassCastException("$context must implement SettingsDialogListener")
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = MaterialAlertDialogBuilder(requireActivity())
      .setView(R.layout.ui_dialog_settings)
      .setTitle(if (edit) R.string.settings_dialog_edit else R.string.settings_dialog_add)
      .setPositiveButton(if (edit) R.string.settings_dialog_save else R.string.settings_dialog_add) { dialog, _ ->
        var shouldIClose = true
        val hostname = hostEdit.text.toString()
        val computerName = nameEdit.text.toString()

        if (hostname.isEmpty() || computerName.isEmpty()) {
          shouldIClose = false
        }

        val portText = portEdit.text.toString()

        val portNum = if (TextUtils.isEmpty(portText)) 0 else Integer.parseInt(portText)
        if (isValid(portNum) && shouldIClose) {
          settings.name = computerName
          settings.address = hostname
          settings.port = portNum
          mListener?.onSave(settings)
          dialog.dismiss()
        }
      }.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
      .show()


    hostEdit = dialog.findViewById(R.id.settings_dialog_host) ?: error("not found")
    nameEdit = dialog.findViewById(R.id.settings_dialog_name) ?: error("not found")
    portEdit = dialog.findViewById(R.id.settings_dialog_port) ?: error("not found")
    return dialog
  }

  override fun onStart() {
    super.onStart()
    nameEdit.setText(settings.name)
    hostEdit.setText(settings.address)

    if (settings.port > 0) {
      portEdit.setText(settings.port.toString())
    }
  }

  private fun isValid(port: Int): Boolean = if (port < MIN_PORT || port > MAX_PORT) {
    portEdit.error = getString(R.string.alert_invalid_port_number)
    false
  } else {
    true
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (!edit) {
      settings = ConnectionSettingsEntity()
    }
  }

  interface SettingsSaveListener {
    fun onSave(settings: ConnectionSettingsEntity)
  }

  companion object {

    private const val MAX_PORT = 65535
    private const val MIN_PORT = 1

    fun newInstance(settings: ConnectionSettingsEntity): SettingsDialogFragment {
      val fragment = SettingsDialogFragment()
      fragment.setConnectionSettings(settings)
      fragment.edit = true
      return fragment
    }
  }
}
