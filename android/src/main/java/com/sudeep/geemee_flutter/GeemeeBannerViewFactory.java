package com.sudeep.geemee_flutter;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Map;

import ai.geemee.GeeMee;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class GeemeeBannerViewFactory extends PlatformViewFactory {
    private final Activity activity;

    public GeemeeBannerViewFactory(Activity activity) {
        super(StandardMessageCodec.INSTANCE);
        this.activity = activity;
    }

    @NonNull
    @Override
    public PlatformView create(@NonNull Context context, int viewId, Object args) {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) args;

        String placementId = (String) params.get("placementId");

        // Get banner view from SDK
        View bannerView = GeeMee.showBanner(placementId);

        return new PlatformView() {
            @Override
            public View getView() {
                return bannerView;
            }

            @Override
            public void dispose() {
                // Optional: destroy banner when widget is disposed
                GeeMee.destroyBanner(placementId);
            }
        };
    }
}
