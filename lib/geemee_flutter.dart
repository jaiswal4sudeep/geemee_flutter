import 'package:flutter/services.dart';

/// ================= BANNER SIZE ENUM =================
enum GeemeeBannerSize {
  banner, // 320x50
  mediumRectangle, // 300x250
  leaderboard, // 728x90
}

extension GeemeeBannerSizeExt on GeemeeBannerSize {
  String get value {
    switch (this) {
      case GeemeeBannerSize.banner:
        return "BANNER";
      case GeemeeBannerSize.mediumRectangle:
        return "MEDIUM_RECTANGLE";
      case GeemeeBannerSize.leaderboard:
        return "LEADERBOARD";
    }
  }
}

/// ================= MAIN PLUGIN CLASS =================
class GeemeeFlutter {
  static const MethodChannel _channel = MethodChannel('geemee_flutter');
  static const EventChannel _events = EventChannel('geemee_flutter_events');

  /// ================= SDK =================
  /// Initializes the GeeMee SDK with the provided app key.
  /// This method must be called before using any other features of the SDK.
  static Future<void> initSDK({required String appKey}) async {
    await _channel.invokeMethod("initSDK", {"appKey": appKey});
  }

  /// Sets the user ID for the SDK.
  /// This is optional but recommended for tracking user-specific data.
  /// The user ID can be used to identify the user across sessions.
  /// It should be unique for each user.
  static Future<void> setUserId({required String userId}) async {
    await _channel.invokeMethod("setUserId", {"userId": userId});
  }

  /// Retrieves the current user ID set in the SDK.
  static Future<String?> getUserId() async {
    return await _channel.invokeMethod("getUserId");
  }

  /// Enables or disables debug mode for the SDK.
  static Future<void> setDebugMode({required bool debug}) async {
    await _channel.invokeMethod("setDebugMode", {"debug": debug});
  }

  /// Retrieves the current version of the SDK.
  static Future<String?> getVersion() async {
    return await _channel.invokeMethod("getVersion");
  }

  /// ================= OFFER WALL =================
  /// Checks if the Offer Wall is ready to be shown.
  /// Returns true if the Offer Wall is ready, false otherwise.
  static Future<bool> isOfferWallReady({required String placementId}) async {
    return await _channel.invokeMethod("isOfferWallReady", {
      "placementId": placementId,
    });
  }

  /// Opens the Offer Wall for the user.
  /// The Offer Wall allows users to complete offers in exchange for rewards.
  /// This method should be called only if `isOfferWallReady` returns true.
  /// The `placementId` is used to identify the specific Offer Wall configuration.
  static Future<void> openOfferWall({required String placementId}) async {
    await _channel.invokeMethod("openOfferWall", {"placementId": placementId});
  }

  /// ================= BANNER =================
  /// Loads a banner ad with the specified placement ID and ad size.
  /// The `placementId` is used to identify the specific banner ad configuration.
  /// The `adSize` specifies the size of the banner ad to be loaded.
  /// Supported sizes are defined in the `GeemeeBannerSize` enum.
  static Future<void> loadBanner({
    required String placementId,
    required GeemeeBannerSize adSize,
  }) async {
    await _channel.invokeMethod("loadBanner", {
      "placementId": placementId,
      "adSize": adSize.value,
    });
  }

  /// Shows the banner ad for the specified placement ID.
  /// This method should be called after `loadBanner` to display the ad.
  /// If the banner is already shown, it will not reload the ad.
  /// The `placementId` is used to identify the specific banner ad configuration.
  static Future<void> showBanner({required String placementId}) async {
    await _channel.invokeMethod("showBanner", {"placementId": placementId});
  }

  /// Hides the banner ad for the specified placement ID.
  /// This method will remove the banner from the view.
  /// It does not destroy the banner; it can be shown again later.
  static Future<bool> isBannerReady({required String placementId}) async {
    return await _channel.invokeMethod("isBannerReady", {
      "placementId": placementId,
    });
  }

  /// Destroys the banner ad for the specified placement ID.
  static Future<void> destroyBanner({required String placementId}) async {
    await _channel.invokeMethod("destroyBanner", {"placementId": placementId});
  }

  /// ================= INTERSTITIAL =================
  /// Checks if an interstitial ad is ready to be shown.
  /// Returns true if the interstitial ad is ready, false otherwise.
  static Future<bool> isInterstitialReady({required String placementId}) async {
    return await _channel.invokeMethod("isInterstitialReady", {
      "placementId": placementId,
    });
  }

  /// Shows an interstitial ad for the specified placement ID.
  /// This method should be called only if `isInterstitialReady` returns true.
  /// The interstitial ad is a full-screen ad that covers the entire screen.
  static Future<void> showInterstitial({required String placementId}) async {
    await _channel.invokeMethod("showInterstitial", {
      "placementId": placementId,
    });
  }

  /// ================= PLAYMEE =================
  /// Checks if the PlayMee User Center is ready to be opened.
  /// Returns true if the User Center is ready, false otherwise.
  static Future<bool> isUserCenterReady({required String placementId}) async {
    return await _channel.invokeMethod("isUserCenterReady", {
      "placementId": placementId,
    });
  }

  /// Opens the PlayMee User Center for the specified placement ID.
  /// The User Center allows users to view their rewards, offers, and account details.
  /// This method should be called only if `isUserCenterReady` returns true.
  static Future<void> openUserCenter({required String placementId}) async {
    await _channel.invokeMethod("openUserCenter", {"placementId": placementId});
  }

  /// ================= EVENTS =================
  /// Listens to events from the GeeMee SDK.
  /// The events are sent as a stream of maps containing event data.
  /// Each event map contains the event type and any additional data related to the event.
  static Stream<Map<dynamic, dynamic>> get events {
    return _events.receiveBroadcastStream().map(
      (event) => Map<dynamic, dynamic>.from(event),
    );
  }
}
