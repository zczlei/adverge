package com.adverge.sdk.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adverge.sdk.R;
import com.adverge.sdk.adapter.PlatformConfigAdapter;
import com.adverge.sdk.model.Platform;
import com.adverge.sdk.network.AdServerClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PlatformConfigActivity extends AppCompatActivity {

    private RecyclerView platformList;
    private PlatformConfigAdapter adapter;
    private List<Platform> platforms = new ArrayList<>();
    private AdServerClient adServerClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_config);

        adServerClient = AdServerClient.getInstance(this);

        platformList = findViewById(R.id.platform_list);
        platformList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlatformConfigAdapter(platforms, new PlatformConfigAdapter.OnPlatformConfigListener() {
            @Override
            public void onSave(Platform platform) {
                savePlatform(platform);
            }

            @Override
            public void onEnable(Platform platform) {
                enablePlatform(platform);
            }

            @Override
            public void onDisable(Platform platform) {
                disablePlatform(platform);
            }
        });
        platformList.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add_platform);
        fabAdd.setOnClickListener(v -> showAddPlatformDialog());

        loadPlatforms();
    }

    private void loadPlatforms() {
        adServerClient.getPlatforms(new AdServerClient.PlatformCallback() {
            @Override
            public void onSuccess(List<Platform> platformList) {
                platforms.clear();
                platforms.addAll(platformList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PlatformConfigActivity.this, "加载平台配置失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePlatform(Platform platform) {
        adServerClient.savePlatform(platform, new AdServerClient.PlatformCallback() {
            @Override
            public void onSuccess(List<Platform> platformList) {
                loadPlatforms();
                Toast.makeText(PlatformConfigActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PlatformConfigActivity.this, "保存失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enablePlatform(Platform platform) {
        adServerClient.enablePlatform(platform.getName(), new AdServerClient.PlatformCallback() {
            @Override
            public void onSuccess(List<Platform> platformList) {
                loadPlatforms();
                Toast.makeText(PlatformConfigActivity.this, "启用成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PlatformConfigActivity.this, "启用失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disablePlatform(Platform platform) {
        adServerClient.disablePlatform(platform.getName(), new AdServerClient.PlatformCallback() {
            @Override
            public void onSuccess(List<Platform> platformList) {
                loadPlatforms();
                Toast.makeText(PlatformConfigActivity.this, "禁用成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PlatformConfigActivity.this, "禁用失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddPlatformDialog() {
        // TODO: 实现添加新平台的对话框
    }
} 