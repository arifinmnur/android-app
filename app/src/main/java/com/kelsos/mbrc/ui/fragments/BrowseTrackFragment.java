package com.kelsos.mbrc.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.adapters.TrackEntryAdapter;
import com.kelsos.mbrc.data.library.Track;
import com.kelsos.mbrc.events.bus.RxBus;
import com.kelsos.mbrc.events.ui.NotifyUser;
import com.kelsos.mbrc.helper.PopupActionHandler;
import com.kelsos.mbrc.services.BrowseSync;
import com.kelsos.mbrc.ui.widgets.EmptyRecyclerView;
import javax.inject.Inject;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import toothpick.Scope;
import toothpick.Toothpick;

public class BrowseTrackFragment extends Fragment
    implements TrackEntryAdapter.MenuItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

  @BindView(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
  @BindView(R.id.search_recycler_view) EmptyRecyclerView recycler;
  @BindView(R.id.empty_view) LinearLayout emptyView;

  @Inject RxBus bus;
  @Inject TrackEntryAdapter adapter;
  @Inject PopupActionHandler actionHandler;
  @Inject BrowseSync sync;

  private Subscription subscription;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_library_search, container, false);
    ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    Scope scope = Toothpick.openScopes(getActivity().getApplication(), getActivity(), this);
    super.onCreate(savedInstanceState);
    Toothpick.inject(this, scope);
  }

  @Override
  public void onStart() {
    super.onStart();
    adapter.init(null);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    swipeLayout.setOnRefreshListener(this);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
    recycler.setHasFixedSize(true);
    recycler.setLayoutManager(layoutManager);
    adapter.setMenuItemSelectedListener(this);
    recycler.setAdapter(adapter);
    recycler.setEmptyView(emptyView);
  }

  @Override
  public void onMenuItemSelected(MenuItem menuItem, Track entry) {
    actionHandler.trackSelected(menuItem, entry);
  }

  @Override
  public void onItemClicked(Track track) {
    actionHandler.trackSelected(track);
  }

  @Override
  public void onRefresh() {
    if (!swipeLayout.isRefreshing()) {
      swipeLayout.setRefreshing(true);
    }

    if (subscription != null && !subscription.isUnsubscribed()) {
      return;
    }

    subscription = sync.syncTracks(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnTerminate(() -> swipeLayout.setRefreshing(false))
        .subscribe(() -> adapter.refresh(), t -> {
          bus.post(new NotifyUser(R.string.refresh_failed));
          Timber.v(t, "failed");
        });
  }
}
