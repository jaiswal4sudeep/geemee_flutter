package com.sudeep.geemee_flutter;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ai.geemee.GeeMee;
import ai.geemee.GeeMeeCallback;
import ai.geemee.GError;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** GeemeeFlutterPlugin */
public class GeemeeFlutterPlugin implements FlutterPlugin, MethodCallHandler {
  private MethodChannel channel;
  private EventChannel eventChannel;
  private EventChannel.EventSink eventSink;
  private Context context;

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
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "initSDK":
        String appKey = call.argument("appKey");
        GeeMee.setCallback(new GeeMeeCallback() {
          @Override
          public void onInitSuccess() {
            sendEvent("onInitSuccess", null);
          }

          @Override
          public void onInitFailed(GError error) {
            sendEvent("onInitFailed", error );
          }

          @Override
          public void onBannerReady(String s) {}

          @Override
          public void onBannerLoadFailed(String s, GError gError) {}

          @Override
          public void onBannerShowFailed(String s, GError gError) {}

          @Override
          public void onBannerClick(String s) {}

          @Override
          public void onInterstitialOpen(String s) {}

          @Override
          public void onInterstitialOpenFailed(String s, GError gError) {}

          @Override
          public void onInterstitialClose(String s) {}

          @Override
          public void onOfferWallOpen(String placement) {
            sendEvent("onOfferWallOpen", placement);
          }

          @Override
          public void onOfferWallOpenFailed(String placement, GError error) {
            sendEvent("onOfferWallOpenFailed", error);
          }

          @Override
          public void onOfferWallClose(String placement) {
            sendEvent("onOfferWallClose", placement);
          }

          @Override
          public void onUserCenterOpen(String s) {}

          @Override
          public void onUserCenterOpenFailed(String s, GError gError) {}

          @Override
          public void onUserCenterClose(String s) {}

          @Override
          public void onUserInteraction(String s, String s1) {}

        });
        GeeMee.initSDK(appKey);
        result.success(null);
        break;

      case "setUserId":
        String userId = call.argument("userId");
        GeeMee.setUserId(userId);
        result.success(null);
        break;

      case "getUserId":
        String id = GeeMee.getUserId();
        result.success(id);
        break;

      case "setDebugMode":
        Boolean debug = call.argument("debug");
        GeeMee.debug(debug != null && debug);
        result.success(null);
        break;

      case "getVersion":
        String version = GeeMee.getVersion();
        result.success(version);
        break;

      case "isOfferWallReady":
        String pidCheck = call.argument("placementId");
        boolean ready = GeeMee.isOfferWallReady(pidCheck);
        result.success(ready);
        break;

      case "openOfferWall":
        String pidOpen = call.argument("placementId");
        GeeMee.openOfferWall(pidOpen);
        result.success(null);
        break;

      default:
        result.notImplemented();
    }
  }

  private void sendEvent(String eventName, Object data) {
    if (eventSink != null) {
      Map<String, Object> map = new HashMap<>();
      map.put("event", eventName);
      map.put("data", data);
      eventSink.success(map);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    eventChannel.setStreamHandler(null);
  }
}