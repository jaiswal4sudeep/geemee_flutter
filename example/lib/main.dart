import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:geemee_flutter/geemee_flutter.dart';

const String appKey = "YOUR_APP_KEY";
const String placementId = "YOUR_PLACEMENT_ID";
const String userId = "test_user_123";

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
    GeemeeFlutter.events.listen((event) => log("GeeMee Event: $event"));
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('GeeMee SDK Test')),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                ElevatedButton(
                  onPressed: () async {
                    await GeemeeFlutter.initSDK(appKey);
                    log("SDK Initialized");
                  },
                  child: const Text('Init SDK'),
                ),
                const SizedBox(height: 10),
                ElevatedButton(
                  onPressed: () async {
                    await GeemeeFlutter.setUserId(userId);
                    log("User ID set to $userId");
                  },
                  child: const Text('Set User ID'),
                ),
                const SizedBox(height: 10),
                ElevatedButton(
                  onPressed: () async {
                    final bool ready = await GeemeeFlutter.isOfferWallReady(
                      placementId,
                    );
                    log("OfferWall Ready: $ready");
                  },
                  child: const Text('Is OfferWall Ready?'),
                ),
                const SizedBox(height: 10),
                ElevatedButton(
                  onPressed: () async {
                    await GeemeeFlutter.openOfferWall(placementId);
                    log("OfferWall Opened");
                  },
                  child: const Text('Open OfferWall'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
