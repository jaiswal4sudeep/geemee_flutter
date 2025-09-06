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

/** GeemeeFlutterPlugin */
public class GeemeeFlutterPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
  private MethodChannel channel;
  private EventChannel eventChannel;
  private EventChannel.EventSink eventSink;
  private Context context;
  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    context = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "geemee_flutter");
    channel.setMethodCallHandler(this);

    eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "geemee_flutter_events");
    eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
      @Override
      public void onListen(Object arguments, EventChannel.EventSink events) {
        eventSink = events;
      }

      @Override
      public void onCancel(Object arguments) {
        eventSink = null;
      }
    });

    flutterPluginBinding.getPlatformViewRegistry().registerViewFactory(
      "geemee_banner_view", new GeemeeBannerViewFactory(activity));
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      /** ================= SDK INIT ================= */
      case "initSDK":
        String appKey = call.argument("appKey");
        GeeMee.setCallback(new GeeMeeCallback() {
          @Override
          public void onInitSuccess() { sendEvent("onInitSuccess", null); }
          @Override
          public void onInitFailed(GError error) { sendEvent("onInitFailed", error); }

          /** Banner Callbacks */
          @Override
          public void onBannerReady(String placementId) { sendEvent("onBannerReady", placementId); }
          @Override
          public void onBannerLoadFailed(String placementId, GError gError) {
            Log.d("Banner Load Failed", gError.toString());
            sendEvent("onBannerLoadFailed", gError);
          }
          @Override
          public void onBannerShowFailed(String placementId, GError gError) {
            Log.d("Banner Show Failed", gError.toString());
            sendEvent("onBannerShowFailed", gError);
          }
          
          @Override
          public void onBannerClick(String placementId) { sendEvent("onBannerClick", placementId); }

          /** Interstitial Callbacks */
          @Override
          public void onInterstitialOpen(String placementId) { sendEvent("onInterstitialOpen", placementId); }
          @Override
          public void onInterstitialOpenFailed(String placementId, GError gError) { sendEvent("onInterstitialOpenFailed", gError); }
          @Override
          public void onInterstitialClose(String placementId) { sendEvent("onInterstitialClose", placementId); }

          /** OfferWall Callbacks */
          @Override
          public void onOfferWallOpen(String placement) { sendEvent("onOfferWallOpen", placement); }
          @Override
          public void onOfferWallOpenFailed(String placement, GError error) { sendEvent("onOfferWallOpenFailed", error); }
          @Override
          public void onOfferWallClose(String placement) { sendEvent("onOfferWallClose", placement); }

          /** PlayMee Callbacks */
          @Override
          public void onUserCenterOpen(String placementId) { sendEvent("onUserCenterOpen", placementId); }
          @Override
          public void onUserCenterOpenFailed(String placementId, GError gError) { sendEvent("onUserCenterOpenFailed", gError); }
          @Override
          public void onUserCenterClose(String placementId) { sendEvent("onUserCenterClose", placementId); }

          @Override
          public void onUserInteraction(String placementId, String data) {
            sendEvent("onUserInteraction", placementId + ":" + data);
          }
        });
        GeeMee.initSDK(appKey);
        result.success(null);
        break;

      /** ================= USER SETTINGS ================= */
      case "setUserId":
        GeeMee.setUserId(call.argument("userId"));
        result.success(null);
        break;

      case "getUserId":
        result.success(GeeMee.getUserId());
        break;

      case "setDebugMode":
        Boolean debug = call.argument("debug");
        GeeMee.debug(debug != null && debug);
        result.success(null);
        break;

      case "getVersion":
        result.success(GeeMee.getVersion());
        break;

      /** ================= OFFER WALL ================= */
      case "isOfferWallReady":
        result.success(GeeMee.isOfferWallReady(call.argument("placementId")));
        break;

      case "openOfferWall":
        GeeMee.openOfferWall(call.argument("placementId"));
        result.success(null);
        break;

      /** ================= BANNER ================= */
      case "loadBanner":
        String placementBanner = call.argument("placementId");
        String size = call.argument("adSize");
        AdSize adSize = AdSize.BANNER;
        if ("MEDIUM_RECTANGLE".equals(size)) adSize = AdSize.MEDIUM_RECTANGLE;
        else if ("LEADERBOARD".equals(size)) adSize = AdSize.LEADERBOARD;
        GeeMee.loadBanner(placementBanner, adSize);
        result.success(null);
        break;

      case "isBannerReady":
        result.success(GeeMee.isBannerReady(call.argument("placementId")));
        break;

      case "destroyBanner":
        GeeMee.destroyBanner(call.argument("placementId"));
        result.success(null);
        break;

      /** ================= INTERSTITIAL ================= */
      case "isInterstitialReady":
        result.success(GeeMee.isInterstitialReady(call.argument("placementId")));
        break;

      case "showInterstitial":
        GeeMee.showInterstitial(call.argument("placementId"));
        result.success(null);
        break;

      /** ================= PLAYMEE ================= */
      case "isUserCenterReady":
        result.success(GeeMee.isUserCenterReady(call.argument("placementId")));
        break;

      case "openUserCenter":
        GeeMee.openUserCenter(call.argument("placementId"));
        result.success(null);
        break;

      default:
        result.notImplemented();
    }
  }

  /** Send events to Flutter */
  private void sendEvent(String eventName, Object data) {
    if (eventSink != null) {
      Map<String, Object> map = new HashMap<>();
      map.put("event", eventName);
      if(data != null){
        map.put("data", data);
      }
      eventSink.success(map);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    eventChannel.setStreamHandler(null);
  }

  /** ====== ActivityAware Methods ====== */
  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }
}
