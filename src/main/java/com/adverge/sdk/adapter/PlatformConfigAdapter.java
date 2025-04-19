package com.adverge.sdk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adverge.sdk.R;
import com.adverge.sdk.model.Platform;

import java.util.List;

public class PlatformConfigAdapter extends RecyclerView.Adapter<PlatformConfigAdapter.ViewHolder> {

    private List<Platform> platforms;
    private OnPlatformConfigListener listener;

    public interface OnPlatformConfigListener {
        void onSave(Platform platform);
        void onEnable(Platform platform);
        void onDisable(Platform platform);
    }

    public PlatformConfigAdapter(List<Platform> platforms, OnPlatformConfigListener listener) {
        this.platforms = platforms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_platform_config, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Platform platform = platforms.get(position);
        holder.bind(platform);
    }

    @Override
    public int getItemCount() {
        return platforms.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView platformName;
        Switch platformEnabled;
        EditText appId;
        EditText appKey;
        EditText placementId;
        EditText bidFloor;
        Button saveButton;

        ViewHolder(View itemView) {
            super(itemView);
            platformName = itemView.findViewById(R.id.platform_name);
            platformEnabled = itemView.findViewById(R.id.platform_enabled);
            appId = itemView.findViewById(R.id.platform_app_id);
            appKey = itemView.findViewById(R.id.platform_app_key);
            placementId = itemView.findViewById(R.id.platform_placement_id);
            bidFloor = itemView.findViewById(R.id.platform_bid_floor);
            saveButton = itemView.findViewById(R.id.btn_save);
        }

        void bind(Platform platform) {
            platformName.setText(platform.getName());
            platformEnabled.setChecked(platform.isEnabled());
            appId.setText(platform.getAppId());
            appKey.setText(platform.getAppKey());
            placementId.setText(platform.getPlacementId());
            bidFloor.setText(String.valueOf(platform.getBidFloor()));

            platformEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    listener.onEnable(platform);
                } else {
                    listener.onDisable(platform);
                }
            });

            saveButton.setOnClickListener(v -> {
                platform.setAppId(appId.getText().toString());
                platform.setAppKey(appKey.getText().toString());
                platform.setPlacementId(placementId.getText().toString());
                try {
                    platform.setBidFloor(Double.parseDouble(bidFloor.getText().toString()));
                } catch (NumberFormatException e) {
                    platform.setBidFloor(0.0);
                }
                listener.onSave(platform);
            });
        }
    }
} 