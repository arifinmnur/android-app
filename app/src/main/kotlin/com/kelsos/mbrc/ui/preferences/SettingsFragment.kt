package com.kelsos.mbrc.ui.preferences

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.os.HandlerCompat
import androidx.navigation.findNavController
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kelsos.mbrc.BuildConfig
import com.kelsos.mbrc.R
import com.kelsos.mbrc.logging.FileLoggingTree
import com.kelsos.mbrc.platform.RemoteService
import com.kelsos.mbrc.ui.dialogs.webDialog
import com.kelsos.mbrc.utilities.RemoteUtils.getVersion
import timber.log.Timber

class SettingsFragment : PreferenceFragmentCompat() {

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    addPreferencesFromResource(R.xml.application_settings)

    val reduceOnIncoming = findPreference<ListPreference>(
      getString(R.string.settings_key_incoming_call_action)
    )
    val mOpenSource = findPreference<Preference>(getString(R.string.preferences_open_source))
    val mManager = findPreference<Preference>(
      resources.getString(R.string.preferences_key_connection_manager)
    )
    val mVersion = findPreference<Preference>(resources.getString(R.string.settings_version))
    val mBuild = findPreference<Preference>(resources.getString(R.string.pref_key_build_time))
    val mRevision = findPreference<Preference>(resources.getString(R.string.pref_key_revision))
    val debugLogging = findPreference<CheckBoxPreference>(
      resources.getString(R.string.settings_key_debug_logging)
    )

    debugLogging?.setOnPreferenceChangeListener { _, newValue ->
      if (newValue as Boolean) {
        Timber.plant(FileLoggingTree(requireContext().applicationContext))
      } else {
        val fileLoggingTree = Timber.forest().find { it is FileLoggingTree }
        fileLoggingTree?.let { Timber.uproot(it) }
      }

      true
    }

    mOpenSource?.setOnPreferenceClickListener {
      showOpenSourceLicenseDialog()
      false
    }

    reduceOnIncoming?.setOnPreferenceChangeListener { _, _ ->
      if (!hasPhonePermission()) {
        requestPhoneStatePermission()
      }
      true
    }

    mManager?.setOnPreferenceClickListener {
      requireView().findNavController().navigate(R.id.action_settingsFragment_to_connectionManagerFragment)
      false
    }

    try {
      val version = getVersion()
      mVersion?.summary = resources.getString(R.string.settings_version_number, version)
    } catch (e: PackageManager.NameNotFoundException) {
      Timber.d(e, "failed")
    }

    val mLicense = findPreference<Preference>(resources.getString(R.string.settings_key_license))
    mLicense?.setOnPreferenceClickListener {
      showLicenseDialog()
      false
    }

    mBuild?.summary = BuildConfig.BUILD_TIME
    mRevision?.summary = BuildConfig.GIT_SHA
  }

  private fun requestPhoneStatePermission() {
    requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_CODE)
  }

  private fun hasPhonePermission(): Boolean {
    return ActivityCompat.checkSelfPermission(
      requireActivity(),
      Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED
  }

  private fun showLicenseDialog() {
    webDialog(
      R.string.musicbee_remote_license_title,
      "file:///android_asset/license.html"
    )
  }

  private fun showOpenSourceLicenseDialog() {
    webDialog(
      R.string.open_source_licenses_title,
      "file:///android_asset/licenses.html"
    )
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    android.R.id.home -> {
      requireActivity().finish()
      true
    }
    else -> super.onOptionsItemSelected(item)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    if (
      requestCode == REQUEST_CODE &&
      grantResults.isNotEmpty() &&
      grantResults.first() == PackageManager.PERMISSION_GRANTED
    ) {
      requireActivity().run {
        Timber.v("Restarting service")
        stopService(Intent(this, RemoteService::class.java))
        val handler = Handler(Looper.getMainLooper())
        HandlerCompat.postDelayed(handler, {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(Intent(this, RemoteService::class.java))
          } else {
            requireActivity().startService(Intent(this, RemoteService::class.java))
          }
        }, null, 600)
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  companion object {
    private const val REQUEST_CODE = 15
  }
}