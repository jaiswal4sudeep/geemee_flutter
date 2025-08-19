# GeeMee Flutter SDK

## Overview
The GeeMee Flutter SDK provides a simple and efficient way to integrate the GeeMee advertising platform into your Flutter applications. It supports various ad formats including banners, interstitials, and offer walls, as well as user center functionalities.

## Features
- **Banner Ads**: Load, show, and manage banner ads with different sizes.
- **Interstitial Ads**: Load and display full-screen interstitial ads.
- **Offer Wall**: Integrate an offer wall for users to complete offers in exchange for rewards.
- **PlayMee User Center**: Access user rewards, offers, and account details.  
- **Event Handling**: Listen to SDK events through a stream.

## Installation
To use the GeeMee Flutter SDK, add the following dependency to your `pubspec.yaml` file:
```yaml
dependencies:
  geemee_flutter: ^0.0.2
```

## Usage
### Initialization
Before using any features of the SDK, you need to initialize it with your app key:
```dart
import 'package:geemee_flutter/geemee_flutter.dart';
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await GeemeeFlutter.initSDK(appKey: "your_app_key");
  runApp(MyApp());
}
```

### Setting User ID
You can set a user ID to track user-specific data:
```dart
await GeemeeFlutter.setUserId(userId: "unique_user_id");
```

### Banner Ads
To load and show a banner ad:
```dart
await GeemeeFlutter.loadBanner(
  placementId: "your_placement_id",
  adSize: GeemeeBannerSize.banner,
);
GeemeeBanner(
  placementId: "your_placement_id",
),
```

### Interstitial Ads
To check if an interstitial ad is ready and show it:
```dart
if (await GeemeeFlutter.isInterstitialReady(placementId: "your_placement_id")) {
  await GeemeeFlutter.showInterstitial(placementId: "your_placement_id");
}
```

### Offer Wall
To open the offer wall:
```dart
if (await GeemeeFlutter.isOfferWallReady(placementId: "your_placement_id")) {
  await GeemeeFlutter.openOfferWall(placementId: "your_placement_id");
}
```

### PlayMee User Center
To open the PlayMee User Center:
```dart
if (await GeemeeFlutter.isUserCenterReady(placementId: "your_placement_id")) {
  await GeemeeFlutter.openUserCenter(placementId: "your_placement_id");
} 
```

### Listening to Events
You can listen to SDK events using the `events` stream:
```dart
GeemeeFlutter.events.listen((event) {
  print("Received event: $event");
});
```

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
