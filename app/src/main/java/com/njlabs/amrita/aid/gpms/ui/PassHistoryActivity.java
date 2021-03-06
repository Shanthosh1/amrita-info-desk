/*
 * MIT License
 *
 * Copyright (c) 2016 Niranjan Rajendran
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.njlabs.amrita.aid.gpms.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.njlabs.amrita.aid.BaseActivity;
import com.njlabs.amrita.aid.R;
import com.njlabs.amrita.aid.gpms.client.AbstractGpms;
import com.njlabs.amrita.aid.gpms.client.Gpms;
import com.njlabs.amrita.aid.gpms.models.HistoryEntry;
import com.njlabs.amrita.aid.gpms.responses.HistoryResponse;
import com.njlabs.amrita.aid.util.ExtendedSwipeRefreshLayout;

import java.util.List;

public class PassHistoryActivity extends BaseActivity {

    private ExtendedSwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private AbstractGpms gpms;

    @Override
    public void init(Bundle savedInstanceState) {
        setupLayout(R.layout.activity_gpms_list, Color.parseColor("#009688"));

        swipeRefreshLayout = (ExtendedSwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#009688"));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        final LinearLayoutManager layoutParams = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutParams);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        swipeRefreshLayout.setRefreshing(true);

        gpms = new Gpms(baseContext);

        loadData();
    }

    public void loadData() {
        gpms.getPassesHistory(new HistoryResponse() {
            @Override
            public void onSuccess(List<HistoryEntry> historyEntries) {
                recyclerView.setAdapter(new PassAdapter(historyEntries));
                swipeRefreshLayout.setRefreshing(false);
                if (historyEntries.size() == 0) {
                    findViewById(R.id.no_data_view).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.no_data_view).setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Snackbar.make(parentView, "Cannot establish reliable connection to the server. Try again.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public class PassAdapter extends RecyclerView.Adapter<PassAdapter.ViewHolder> {

        private List<HistoryEntry> historyEntries;

        public PassAdapter(List<HistoryEntry> historyEntries) {
            this.historyEntries = historyEntries;
        }

        @Override
        public PassAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pass_history_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            HistoryEntry historyEntry = historyEntries.get(position);
            holder.departure.setText(historyEntry.getDepartureTime());
            holder.arrival.setText(historyEntry.getArrivalTime());
            holder.actual_days.setText(historyEntry.getActualNumDays());
            holder.applied_days.setText(historyEntry.getNumDays());
            holder.occasion.setText(historyEntry.getOccasion());
        }

        @Override
        public int getItemCount() {
            return historyEntries.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView departure;
            public TextView applied_days;
            public TextView actual_days;
            public TextView occasion;
            public TextView arrival;

            public ViewHolder(View v) {
                super(v);
                departure = (TextView) v.findViewById(R.id.departure);
                arrival = (TextView) v.findViewById(R.id.arrival);
                applied_days = (TextView) v.findViewById(R.id.applied_days);
                actual_days = (TextView) v.findViewById(R.id.actual_days);
                occasion = (TextView) v.findViewById(R.id.occasion);
            }

        }
    }
}
