package com.eserve.android.adapter;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eserve.android.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.jetbrains.annotations.NotNull;

public class MapMarkerInfoAdapter implements GoogleMap.InfoWindowAdapter {


    private final View customView;
    boolean notFirstTime;


    public MapMarkerInfoAdapter(Context mContext) {
        customView = LayoutInflater.from(mContext).inflate(R.layout.layout_marker_label, null);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View getInfoWindow(@NonNull @NotNull Marker marker) {
        renderWindowInfo(marker);
        return customView;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View getInfoContents(@NonNull @NotNull Marker marker) {
        renderWindowInfo(marker);
        return customView;
    }


    private void renderWindowInfo(Marker marker) {
        String title = marker.getTitle();
        String snippet = marker.getSnippet();

        TextView markerTitle = customView.findViewById(R.id.title);
        TextView markerSnippet = customView.findViewById(R.id.snippet);

        if (!TextUtils.isEmpty(title)) {
            markerTitle.setText(title);
        }
        if (!TextUtils.isEmpty(snippet)) {
            markerSnippet.setText(snippet);
        }

    }


}
