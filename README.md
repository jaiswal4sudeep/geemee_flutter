## **Overview**
`geemee_flutter` lets you:
- Initialize GeeMee SDK
- Set/Get a custom user ID
- Enable debug mode
- Check if OfferWall is ready
- Open the OfferWall
- Listen to SDK events in Flutter

---

## **Installation**
**`pubspec.yaml`**
```yaml
dependencies:
  geemee_flutter: ^0.0.1
```
Run:
```bash
flutter pub get
```

## **Usage**

**Import**
```dart
import 'package:geemee_flutter/geemee_flutter.dart';
```

**Initialize**
```dart
await GeemeeFlutter.initSDK("YOUR_APP_KEY");
```

**Set/Get User ID**
```dart
await GeemeeFlutter.setUserId("user123");
String? uid = await GeemeeFlutter.getUserId();
print("Current User ID: $uid");
```

**Enable Debug Mode**
```dart
await GeemeeFlutter.setDebugMode(true);
```

**Get SDK Version**
```dart
String? version = await GeemeeFlutter.getVersion();
print("GeeMee SDK Version: $version");
```

**Check OfferWall Status**
```dart
bool ready = await GeemeeFlutter.isOfferWallReady("YOUR_PLACEMENT_ID");
print("OfferWall ready: $ready");
```

**Open OfferWall**
```dart
await GeemeeFlutter.openOfferWall("YOUR_PLACEMENT_ID");
```

---

## **Listen to SDK Events**
```dart
GeemeeFlutter.events.listen((event) {
  print("Event Received: ${event['type']}, Data: ${event['data']}");
});
```

---