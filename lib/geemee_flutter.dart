import 'package:flutter/services.dart';

class GeemeeFlutter {
  static const MethodChannel _channel = MethodChannel('geemee_flutter');
  static const EventChannel _events = EventChannel('geemee_flutter_events');

  static Future<void> initSDK(String appKey) async {
    await _channel.invokeMethod("initSDK", {"appKey": appKey});
  }

  static Future<void> setUserId(String userId) async {
    await _channel.invokeMethod("setUserId", {"userId": userId});
  }

  static Future<String?> getUserId() async {
    return await _channel.invokeMethod("getUserId");
  }

  static Future<void> setDebugMode(bool debug) async {
    await _channel.invokeMethod("setDebugMode", {"debug": debug});
  }

  static Future<String?> getVersion() async {
    return await _channel.invokeMethod("getVersion");
  }

  static Future<bool> isOfferWallReady(String placementId) async {
    return await _channel.invokeMethod("isOfferWallReady", {
      "placementId": placementId,
    });
  }

  static Future<void> openOfferWall(String placementId) async {
    await _channel.invokeMethod("openOfferWall", {"placementId": placementId});
  }

  static Stream<Map<dynamic, dynamic>> get events {
    return _events.receiveBroadcastStream().map(
      (event) => Map<dynamic, dynamic>.from(event),
    );
  }
}
