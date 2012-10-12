package com.kelsos.mbrc.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockPreferenceActivity;
import com.google.inject.Inject;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.controller.RunningActivityAccessor;
import com.kelsos.mbrc.enums.UserInputEventType;
import com.kelsos.mbrc.events.UserActionEvent;
import com.squareup.otto.Bus;

import static android.app.AlertDialog.Builder;

public class AppPreferenceView extends RoboSherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

	@Inject
	Bus bus;
	@Inject
	RunningActivityAccessor accessor;

	private EditTextPreference hostEditTextPreference;
    private EditTextPreference portEditTextPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		accessor.register(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.main_menu_title_settings);
        addPreferencesFromResource(R.xml.application_settings);
        hostEditTextPreference = (EditTextPreference) getPreferenceScreen().findPreference(getApplicationContext().getString(R.string.settings_key_hostname));
        portEditTextPreference = (EditTextPreference) getPreferenceScreen().findPreference(getApplicationContext().getString(R.string.settings_key_port));

		final Context pContext = this;
		portEditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object o)
			{
				int portNumber = Integer.parseInt(o.toString());
				if(portNumber<1||portNumber>65535){
					final Builder alert = new Builder(pContext);
					alert.setTitle(R.string.alert_invalid_range);
					alert.setMessage(R.string.alert_invalid_port_number);
					alert.setPositiveButton(android.R.string.ok, null);
					alert.show();
					return false;
				}
				else {
					return true;
				}
			}
		});
    }

    @Override
    protected void onResume() {
        super.onResume();
        hostEditTextPreference.setSummary(PreferenceManager.getDefaultSharedPreferences(this).getString(getApplicationContext().getString(R.string.settings_key_hostname), null));
        portEditTextPreference.setSummary(PreferenceManager.getDefaultSharedPreferences(this).getString(getApplicationContext().getString(R.string.settings_key_port), null));
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getApplicationContext().getString(R.string.settings_key_hostname))) {
            hostEditTextPreference.setSummary(sharedPreferences.getString(getApplicationContext().getString(R.string.settings_key_hostname), null));
        } else if (key.equals(getApplicationContext().getString(R.string.settings_key_port))) {
            portEditTextPreference.setSummary(sharedPreferences.getString(getApplicationContext().getString(R.string.settings_key_port), null));
        }
		//TODO: Add check.
		bus.post(new UserActionEvent(UserInputEventType.USERINPUT_EVENT_SETTINGS_CHANGED));
    }

	@Override
	public void onDestroy()
	{
		accessor.unRegister(this);
		super.onDestroy();
	}
}