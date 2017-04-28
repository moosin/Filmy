package tech.salroid.filmy.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tech.salroid.filmy.BuildConfig;
import tech.salroid.filmy.R;
import tech.salroid.filmy.activities.MovieDetailsActivity;
import tech.salroid.filmy.custom_adapter.WatchlistAdapter;
import tech.salroid.filmy.customs.BreathingProgress;
import tech.salroid.filmy.data_classes.WatchlistData;
import tech.salroid.filmy.network_stuff.TmdbVolleySingleton;
import tech.salroid.filmy.parser.WatchListMovieParseWork;
import tech.salroid.filmy.tmdb_account.UnMarkingWatchList;

/*
 * Filmy Application for Android
 * Copyright (c) 2016 Sajal Gupta (http://github.com/salroid).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class WatchList extends Fragment implements WatchlistAdapter.ClickListener, WatchlistAdapter.LongClickListener, UnMarkingWatchList.UnmarkedListener {

    WatchlistAdapter watchlistAdapter;

    @BindView(R.id.breathingProgress)
    BreathingProgress breathingProgress;
    @BindView(R.id.my_watchlist_recycler)
    RecyclerView my_watchlist_movies_recycler;
    @BindView(R.id.fav_image)
    ImageView dataImageView;
    @BindView(R.id.wl_display_text)
    TextView wlTextView;
    @BindView(R.id.emptyContainer)
    LinearLayout emptyContainer;


    private TmdbVolleySingleton tmdbVolleySingleton = TmdbVolleySingleton.getInstance();
    private RequestQueue tmdbrequestQueue = tmdbVolleySingleton.getRequestQueue();

    private String api_key = BuildConfig.API_KEY;
    private String account_id;

    private ProgressDialog progressDialog;
    private List<WatchlistData> list;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_watch_list, container, false);
        ButterKnife.bind(this, view);

        showProgress();

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);

        if (tabletSize) {

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

                StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(6,
                        StaggeredGridLayoutManager.VERTICAL);
                my_watchlist_movies_recycler.setLayoutManager(gridLayoutManager);
            } else {
                StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(8,
                        StaggeredGridLayoutManager.VERTICAL);
                my_watchlist_movies_recycler.setLayoutManager(gridLayoutManager);
            }

        } else {

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

                StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3,
                        StaggeredGridLayoutManager.VERTICAL);
                my_watchlist_movies_recycler.setLayoutManager(gridLayoutManager);
            } else {
                StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(5,
                        StaggeredGridLayoutManager.VERTICAL);
                my_watchlist_movies_recycler.setLayoutManager(gridLayoutManager);
            }

        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getfavourites();
    }


    private void getfavourites() {


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, null, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            int total_results = response.getInt("total_results");

                            if (total_results > 0)
                                parseoutput(response.toString());
                            else {
                                hideProgress();
                                emptyContainer.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e("webi", "Volley Errorbelow: " + error.getCause());
                hideProgress();
                emptyContainer.setVisibility(View.VISIBLE);
                wlTextView.setText("Failed to get your list.");
            }
        });

        tmdbrequestQueue.add(jsonObjectRequest);

    }

    private void parseoutput(String s) {

        WatchListMovieParseWork pw = new WatchListMovieParseWork(getActivity(), s);
        list = pw.parse_watchlist();
        watchlistAdapter = new WatchlistAdapter(getActivity(), list);
        if (list.size() == 0)
            wlTextView.setVisibility(View.VISIBLE);
        my_watchlist_movies_recycler.setAdapter(watchlistAdapter);
        watchlistAdapter.setClickListener(this);
        watchlistAdapter.setLongClickListener(this);

        hideProgress();

    }


    @Override
    public void itemClicked(WatchlistData watchlistData, int position) {

        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
        intent.putExtra("network_applicable", true);
        intent.putExtra("title", watchlistData.getFav_title());
        intent.putExtra("id", watchlistData.getFav_id());
        intent.putExtra("activity", false);

        startActivity(intent);

    }

    public void showProgress() {


        if (breathingProgress != null && my_watchlist_movies_recycler != null) {

            breathingProgress.setVisibility(View.VISIBLE);
            my_watchlist_movies_recycler.setVisibility(View.INVISIBLE);

        }
    }


    public void hideProgress() {

        if (breathingProgress != null && my_watchlist_movies_recycler != null) {

            breathingProgress.setVisibility(View.INVISIBLE);
            my_watchlist_movies_recycler.setVisibility(View.VISIBLE);

        }


    }


    @Override
    public void itemLongClicked(final WatchlistData watchlistData, final int position) {


        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        arrayAdapter.add("Remove");
        adb.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                UnMarkingWatchList unMarkingWatchList = new UnMarkingWatchList();
                unMarkingWatchList.setUnmarkedListener(WatchList.this);

                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Watchlist");
                progressDialog.setMessage("Removing..");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();

                unMarkingWatchList.removeFromWatchList(getActivity(), watchlistData.getFav_id(), position);
            }
        });

        adb.show();

    }

    @Override
    public void unmarked(int position) {

        if (progressDialog != null)
            progressDialog.dismiss();
        if (watchlistAdapter != null && list != null) {
            list.remove(position);
            watchlistAdapter.notifyItemRemoved(position);

            if (watchlistAdapter.getItemCount() == 0)
                emptyContainer.setVisibility(View.VISIBLE);

        }
    }
}
