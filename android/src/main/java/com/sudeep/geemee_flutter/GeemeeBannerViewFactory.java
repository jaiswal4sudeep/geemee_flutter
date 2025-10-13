package com.sudeep.geemee_flutter;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ai.geemee.GeeMee;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

/**
 * Robust GeemeeBannerViewFactory with:
 * - argument checks
 * - UI-thread usage for GeeMee SDK calls
 * - null-safe fallback view
 * - defensive try/catch logging
 */
public class GeemeeBannerViewFactory extends PlatformViewFactory {
    private static final String TAG = "GeemeeBannerViewFactory";
    private final Activity activity;

    public GeemeeBannerViewFactory(Activity activity) {
        super(StandardMessageCodec.INSTANCE);
        this.activity = activity;
    }

    @NonNull
    @Override
    public PlatformView create(@NonNull Context context, int viewId, Object args) {
        // default placeholder view (in case SDK fails / returns null)
        final View placeholder = new View(activity != null ? activity : context);

        String placementId = null;
        if (args instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) args;
            Object pid = params.get("placementId");
            if (pid instanceof String) {
                placementId = (String) pid;
            } else {
                Log.w(TAG, "placementId missing or not a string in args: " + args);
            }
        } else if (args != null) {
            Log.w(TAG, "Expected args Map but got: " + args.getClass().getName());
        }

        final String finalPlacementId = placementId;
        final View[] sdkViewHolder = new View[1];
        sdkViewHolder[0] = null;

        if (finalPlacementId != null) {
            // We must call SDK view creation on main thread. We'll run on UI thread and wait briefly.
            Runnable createRunnable = () -> {
                try {
                    // SDK method that returns a view; may throw or return null
                    sdkViewHolder[0] = GeeMee.showBanner(finalPlacementId);
                } catch (Exception e) {
                    Log.e(TAG, "GeeMee.showBanner threw an exception for placement " + finalPlacementId, e);
                }
            };

            if (Looper.myLooper() == Looper.getMainLooper()) {
                createRunnable.run();
            } else {
                if (activity != null) {
                    final CountDownLatch latch = new CountDownLatch(1);
                    activity.runOnUiThread(() -> {
                        try {
                            createRunnable.run();
                        } finally {
                            latch.countDown();
                        }
                    });
                    try {
                        // wait a short time; don't block too long on plugin thread
                        latch.await(200, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ie) {
                        Log.w(TAG, "Interrupted while waiting for banner creation", ie);
                        Thread.currentThread().interrupt();
                    }
                } else {
                    Log.w(TAG, "Activity is null and we're off the main thread - cannot safely create SDK banner view");
                }
            }
        } else {
            Log.w(TAG, "create: placementId is null - returning placeholder view");
        }

        final View bannerView = (sdkViewHolder[0] != null) ? sdkViewHolder[0] : placeholder;

        // Return a PlatformView that provides the banner and safely destroys it on dispose.
        return new PlatformView() {
            private boolean disposed = false;

            @Override
            public View getView() {
                return bannerView;
            }

            @Override
            public void dispose() {
                if (disposed) return;
                disposed = true;

                if (finalPlacementId == null) {
                    // nothing to destroy from SDK
                    return;
                }

                Runnable destroyRunnable = () -> {
                    try {
                        GeeMee.destroyBanner(finalPlacementId);
                    } catch (Exception e) {
                        Log.e(TAG, "GeeMee.destroyBanner threw for placement " + finalPlacementId, e);
                    }
                };

                if (Looper.myLooper() == Looper.getMainLooper()) {
                    destroyRunnable.run();
                } else {
                    if (activity != null) {
                        try {
                            activity.runOnUiThread(destroyRunnable);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to post destroyBanner to UI thread", e);
                        }
                    } else {
                        Log.w(TAG, "Activity is null when disposing banner - cannot call destroyBanner on UI thread");
                        // As a last resort, try calling directly (may be unsafe depending on SDK)
                        try {
                            destroyRunnable.run();
                        } catch (Exception e) {
                            Log.e(TAG, "Fallback destroyBanner failed", e);
                        }
                    }
                }
            }
        };
    }
}
