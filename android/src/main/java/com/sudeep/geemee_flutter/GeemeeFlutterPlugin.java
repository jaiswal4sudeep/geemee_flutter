package com.sudeep.geemee_flutter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ai.geemee.AdSize;
import ai.geemee.GeeMee;
import ai.geemee.GeeMeeCallback;
import ai.geemee.GError;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.platform.PlatformViewRegistry;

/** GeemeeFlutterPlugin */
public class GeemeeFlutterPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
  private static final String TAG = "GeemeeFlutterPlugin";

  private MethodChannel channel;
  private EventChannel eventChannel;
  private EventChannel.EventSink eventSink;
  private EventChannel.StreamHandler streamHandler;
  private Context context;
  private Activity activity;
  // cached registry so we can register platform views when activity attaches
  private PlatformViewRegistry platformViewRegistry;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    context = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "geemee_flutter");
    channel.setMethodCallHandler(this);

    eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "geemee_flutter_events");

    // keep a reference so we can unregister cleanly later
    streamHandler = new EventChannel.StreamHandler() {
      @Override
      public void onListen(Object arguments, EventChannel.EventSink events) {
        eventSink = events;
      }

      @Override
      public void onCancel(Object arguments) {
        eventSink = null;
      }
    };
    eventChannel.setStreamHandler(streamHandler);

    // Cache the platform view registry now (only FlutterPluginBinding exposes it)
    try {
      platformViewRegistry = flutterPluginBinding.getPlatformViewRegistry();
    } catch (Exception e) {
      Log.w(TAG, "Failed to get PlatformViewRegistry from FlutterPluginBinding", e);
      platformViewRegistry = null;
    }

    // Do NOT register the view factory here if activity is null - we'll do that in onAttachedToActivity.
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    try {
      String method = call.method;
      switch (method) {
        /** ================= SDK INIT ================= */
        case "initSDK": {
          String appKey = call.argument("appKey");
          if (appKey == null) {
            result.error("INVALID_ARGUMENT", "appKey is required", null);
            return;
          }
          try {
            GeeMee.setCallback(new GeeMeeCallback() {
              @Override
              public void onInitSuccess() { safeSendEvent("onInitSuccess", null); }
              @Override
              public void onInitFailed(GError error) { safeSendEvent("onInitFailed", errorToMap(error)); }

              /** Banner Callbacks */
              @Override
              public void onBannerReady(String placementId) { safeSendEvent("onBannerReady", placementId); }
              @Override
              public void onBannerLoadFailed(String placementId, GError gError) {
                Log.d(TAG, "Banner Load Failed: " + (gError != null ? gError.toString() : "null"));
                safeSendEvent("onBannerLoadFailed", errorToMap(gError));
              }
              @Override
              public void onBannerShowFailed(String placementId, GError gError) {
                Log.d(TAG, "Banner Show Failed: " + (gError != null ? gError.toString() : "null"));
                safeSendEvent("onBannerShowFailed", errorToMap(gError));
              }

              @Override
              public void onBannerClick(String placementId) { safeSendEvent("onBannerClick", placementId); }

              /** Interstitial Callbacks */
              @Override
              public void onInterstitialOpen(String placementId) { safeSendEvent("onInterstitialOpen", placementId); }
              @Override
              public void onInterstitialOpenFailed(String placementId, GError gError) { safeSendEvent("onInterstitialOpenFailed", errorToMap(gError)); }
              @Override
              public void onInterstitialClose(String placementId) { safeSendEvent("onInterstitialClose", placementId); }

              /** OfferWall Callbacks */
              @Override
              public void onOfferWallOpen(String placement) { safeSendEvent("onOfferWallOpen", placement); }
              @Override
              public void onOfferWallOpenFailed(String placement, GError error) { safeSendEvent("onOfferWallOpenFailed", errorToMap(error)); }
              @Override
              public void onOfferWallClose(String placement) { safeSendEvent("onOfferWallClose", placement); }

              /** PlayMee Callbacks */
              @Override
              public void onUserCenterOpen(String placementId) { safeSendEvent("onUserCenterOpen", placementId); }
              @Override
              public void onUserCenterOpenFailed(String placementId, GError gError) { safeSendEvent("onUserCenterOpenFailed", errorToMap(gError)); }
              @Override
              public void onUserCenterClose(String placementId) { safeSendEvent("onUserCenterClose", placementId); }

              @Override
              public void onUserInteraction(String placementId, String data) {
                safeSendEvent("onUserInteraction", placementId + ":" + data);
              }
            });
            GeeMee.initSDK(appKey);
            result.success(null);
          } catch (Exception e) {
            Log.e(TAG, "initSDK error", e);
            result.error("INIT_ERROR", e.getMessage(), null);
          }
          break;
        }

        /** ================= USER SETTINGS ================= */
        case "setUserId": {
          String userId = call.argument("userId");
          if (userId == null) {
            result.error("INVALID_ARGUMENT", "userId is required", null);
            return;
          }
          try {
            GeeMee.setUserId(userId);
            result.success(null);
          } catch (Exception e) {
            Log.e(TAG, "setUserId error", e);
            result.error("SET_USER_ERROR", e.getMessage(), null);
          }
          break;
        }

        case "getUserId": {
          try {
            String uid = GeeMee.getUserId();
            result.success(uid);
          } catch (Exception e) {
            Log.e(TAG, "getUserId error", e);
            result.error("GET_USER_ERROR", e.getMessage(), null);
          }
          break;
        }

        case "setDebugMode": {
          Boolean debug = call.argument("debug");
          try {
            GeeMee.debug(debug != null && debug);
            result.success(null);
          } catch (Exception e) {
            Log.e(TAG, "setDebugMode error", e);
            result.error("DEBUG_ERROR", e.getMessage(), null);
          }
          break;
        }

        case "getVersion": {
          try {
            result.success(GeeMee.getVersion());
          } catch (Exception e) {
            Log.e(TAG, "getVersion error", e);
            result.error("VERSION_ERROR", e.getMessage(), null);
          }
          break;
        }

        /** ================= OFFER WALL ================= */
        case "isOfferWallReady": {
          String placement = call.argument("placementId");
          if (placement == null) {
            result.error("INVALID_ARGUMENT", "placementId is required", null);
            return;
          }
          try {
            result.success(GeeMee.isOfferWallReady(placement));
          } catch (Exception e) {
            Log.e(TAG, "isOfferWallReady error", e);
            result.error("OFFERWALL_READY_ERROR", e.getMessage(), null);
          }
          break;
        }

        case "openOfferWall": {
          String placement = call.argument("placementId");
          if (placement == null) {
            result.error("INVALID_ARGUMENT", "placementId is required", null);
            return;
          }
          try {
            GeeMee.openOfferWall(placement);
            result.success(null);
          } catch (Exception e) {
            Log.e(TAG, "openOfferWall error", e);
            result.error("OFFERWALL_OPEN_ERROR", e.getMessage(), null);
          }
          break;
        }

        /** ================= BANNER ================= */
        case "loadBanner": {
          String placementBanner = call.argument("placementId");
          String size = call.argument("adSize");
          if (placementBanner == null || size == null) {
            result.error("INVALID_ARGUMENT", "placementId and adSize are required", null);
            return;
          }
          try {
            AdSize adSize = AdSize.BANNER;
            if ("MEDIUM_RECTANGLE".equals(size)) adSize = AdSize.MEDIUM_RECTANGLE;
            else if ("LEADERBOARD".equals(size)) adSize = AdSize.LEADERBOARD;
            GeeMee.loadBanner(placementBanner, adSize);
            result.success(null);
          } catch (Exception e) {
            Log.e(TAG, "loadBanner error", e);
            result.error("LOAD_BANNER_ERROR", e.getMessage(), null);
          }
          break;
        }

        case "isBannerReady": {
          String placement = call.argument("placementId");
          if (placement == null) {
            result.error("INVALID_ARGUMENT", "placementId is required", null);
            return;
          }
          try {
            result.success(GeeMee.isBannerReady(placement));
          } catch (Exception e) {
            Log.e(TAG, "isBannerReady error", e);
            result.error("BANNER_READY_ERROR", e.getMessage(), null);
          }
          break;
        }

        case "destroyBanner": {
          String placement = call.argument("placementId");
          if (placement == null) {
            result.error("INVALID_ARGUMENT", "placementId is required", null);
            return;
          }
          try {
            GeeMee.destroyBanner(placement);
            result.success(null);
          } catch (Exception e) {
            Log.e(TAG, "destroyBanner error", e);
            result.error("DESTROY_BANNER_ERROR", e.getMessage(), null);
          }
          break;
        }

        /** ================= INTERSTITIAL ================= */
        case "isInterstitialReady": {
          String placement = call.argument("placementId");
          if (placement == null) {
            result.error("INVALID_ARGUMENT", "placementId is required", null);
            return;
          }
          try {
            result.success(GeeMee.isInterstitialReady(placement));
          } catch (Exception e) {
            Log.e(TAG, "isInterstitialReady error", e);
            result.error("INTERSTITIAL_READY_ERROR", e.getMessage(), null);
          }
          break;
        }

        case "showInterstitial": {
          String placement = call.argument("placementId");
          if (placement == null) {
            result.error("INVALID_ARGUMENT", "placementId is required", null);
            return;
          }
          try {
            GeeMee.showInterstitial(placement);
            result.success(null);
          } catch (Exception e) {
            Log.e(TAG, "showInterstitial error", e);
            result.error("SHOW_INTERSTITIAL_ERROR", e.getMessage(), null);
          }
          break;
        }

        /** ================= PLAYMEE ================= */
        case "isUserCenterReady": {
          String placement = call.argument("placementId");
          if (placement == null) {
            result.error("INVALID_ARGUMENT", "placementId is required", null);
            return;
          }
          try {
            result.success(GeeMee.isUserCenterReady(placement));
          } catch (Exception e) {
            Log.e(TAG, "isUserCenterReady error", e);
            result.error("USERCENTER_READY_ERROR", e.getMessage(), null);
          }
          break;
        }

        case "openUserCenter": {
          String placement = call.argument("placementId");
          if (placement == null) {
            result.error("INVALID_ARGUMENT", "placementId is required", null);
            return;
          }
          try {
            GeeMee.openUserCenter(placement);
            result.success(null);
          } catch (Exception e) {
            Log.e(TAG, "openUserCenter error", e);
            result.error("OPEN_USERCENTER_ERROR", e.getMessage(), null);
          }
          break;
        }

        default:
          result.notImplemented();
      }
    } catch (Exception e) {
      Log.e(TAG, "onMethodCall unexpected error", e);
      result.error("UNEXPECTED_ERROR", e.getMessage(), null);
    }
  }

  /** Safe wrapper around sending events to Flutter - avoids crashes if sink is null or sink throws */
  private void safeSendEvent(String eventName, Object data) {
    try {
      if (eventSink != null) {
        Map<String, Object> map = new HashMap<>();
        map.put("event", eventName);
        if (data != null) {
          map.put("data", data);
        }
        eventSink.success(map);
      } else {
        Log.w(TAG, "EventSink is null - dropping event: " + eventName);
      }
    } catch (Exception e) {
      // log and swallow - do not crash the SDK callback thread
      Log.e(TAG, "Failed to send event to Flutter: " + eventName, e);
    }
  }

  /** convert GError to simple map (null safe) to send to dart side */
  private Map<String, Object> errorToMap(GError error) {
    Map<String, Object> m = new HashMap<>();
    if (error == null) return m;
    try {
      // GError API may differ across SDK versions. Use toString() as a safe fallback
      m.put("error", String.valueOf(error));
    } catch (Exception e) {
      Log.e(TAG, "errorToMap failed", e);
      m.put("error", "unknown_error");
    }
    return m;
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    try {
      if (channel != null) {
        channel.setMethodCallHandler(null);
        channel = null;
      }
      if (eventChannel != null) {
        // unregister our stream handler
        eventChannel.setStreamHandler(null);
        eventChannel = null;
      }
      // clear sink and stream handler references
      eventSink = null;
      streamHandler = null;
      // clear cached registry
      platformViewRegistry = null;
    } catch (Exception e) {
      Log.e(TAG, "onDetachedFromEngine error", e);
    }
  }

  /** ====== ActivityAware Methods ====== */
  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    try {
      // register the banner view factory now that activity is available
      if (activity != null && platformViewRegistry != null) {
        platformViewRegistry.registerViewFactory(
                "geemee_banner_view", new GeemeeBannerViewFactory(activity));
      } else {
        Log.w(TAG, "Activity or PlatformViewRegistry is null onAttachedToActivity - cannot register banner view");
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to register GeemeeBannerViewFactory", e);
    }
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    try {
      if (activity != null && platformViewRegistry != null) {
        platformViewRegistry.registerViewFactory(
                "geemee_banner_view", new GeemeeBannerViewFactory(activity));
      } else {
        Log.w(TAG, "Activity or PlatformViewRegistry is null onReattachedToActivityForConfigChanges");
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to re-register GeemeeBannerViewFactory", e);
    }
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }
}
