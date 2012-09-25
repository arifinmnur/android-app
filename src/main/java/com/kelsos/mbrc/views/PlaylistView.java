package com.kelsos.mbrc.views;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockListActivity;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.kelsos.mbrc.data.MusicTrack;
import com.kelsos.mbrc.data.PlaylistArrayAdapter;
import com.kelsos.mbrc.events.UserActionEvent;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.controller.RunningActivityAccessor;
import com.kelsos.mbrc.enums.UserInputEventType;

import java.util.ArrayList;

public class PlaylistView extends RoboSherlockListActivity
{
	@Inject
	private RunningActivityAccessor accessor;
	@Inject
	private Bus bus;

    public void updateListData(ArrayList<MusicTrack> nowPlayingList) {
        PlaylistArrayAdapter adapter;
        adapter = new PlaylistArrayAdapter(this, R.layout.playlistview_item, nowPlayingList);
        setListAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.nowplayinglist);
		accessor.register(this);
		bus.post(new UserActionEvent(UserInputEventType.USERINPUT_EVENT_REQUEST_NOWPLAYING_LIST));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.string_value_now_playing);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String track = ((MusicTrack) getListView().getItemAtPosition(position)).getTitle();
		bus.post(new UserActionEvent(UserInputEventType.USERINPUT_EVENT_REQUEST_NOWPLAYING_PLAY_NOW,track));

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
}


