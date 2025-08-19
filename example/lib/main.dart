import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:geemee_flutter/geemee_flutter.dart';

const String appKey = "APP_KEY";
const String offerwallPlacementId = "PLACEMENT_ID";
const String interstitialAdPlacementId = "PLACEMENT_ID";
const String bannerAdPlacementId = 'PLACEMENT_ID';
const String playMeePlacementId = 'PLACEMENT_ID';
const String userId = "USER_ID";

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _bannerLoaded = false;

  @override
  void initState() {
    super.initState();
    GeemeeFlutter.events.listen((event) {
      log("GeeMee Event: $event");
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('GeeMee SDK Test')),
        body: Stack(
          children: [
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  ElevatedButton(
                    onPressed: () async {
                      await GeemeeFlutter.initSDK(appKey: appKey);
                      log("SDK Initialized");
                    },
                    child: const Text('Init SDK'),
                  ),
                  const SizedBox(height: 10),

                  ElevatedButton(
                    onPressed: () async {
                      await GeemeeFlutter.setUserId(userId: userId);
                      log("User ID set to $userId");
                    },
                    child: const Text('Set User ID'),
                  ),
                  const SizedBox(height: 20),

                  /// OFFERWALL
                  ElevatedButton(
                    onPressed: () async {
                      final ready = await GeemeeFlutter.isOfferWallReady(
                        placementId: offerwallPlacementId,
                      );
                      log("OfferWall Ready: $ready");
                    },
                    child: const Text('Is OfferWall Ready?'),
                  ),
                  ElevatedButton(
                    onPressed: () async {
                      await GeemeeFlutter.openOfferWall(
                        placementId: offerwallPlacementId,
                      );
                      log("OfferWall Opened");
                    },
                    child: const Text('Open OfferWall'),
                  ),
                  const Divider(),

                  /// BANNER (updated)
                  ElevatedButton(
                    onPressed: () async {
                      await GeemeeFlutter.loadBanner(
                        placementId: bannerAdPlacementId,
                        adSize: GeemeeBannerSize.banner,
                      );

                      log("Banner Loaded");
                    },
                    child: const Text('Load Banner'),
                  ),
                  ElevatedButton(
                    onPressed: () async {
                      final ready = await GeemeeFlutter.isBannerReady(
                        placementId: bannerAdPlacementId,
                      );
                      setState(() => _bannerLoaded = ready);
                      log("Banner Ready: $ready");
                    },
                    child: const Text('Is Banner Ready?'),
                  ),
                  ElevatedButton(
                    onPressed: () async {
                      await GeemeeFlutter.destroyBanner(
                        placementId: bannerAdPlacementId,
                      );
                      setState(() {
                        _bannerLoaded = false;
                      });
                      log("Banner Destroyed");
                    },
                    child: const Text('Destroy Banner'),
                  ),

                  const Divider(),

                  /// INTERSTITIAL
                  ElevatedButton(
                    onPressed: () async {
                      final ready = await GeemeeFlutter.isInterstitialReady(
                        placementId: interstitialAdPlacementId,
                      );
                      log("Interstitial Ready: $ready");
                    },
                    child: const Text('Is Interstitial Ready?'),
                  ),
                  ElevatedButton(
                    onPressed: () async {
                      await GeemeeFlutter.showInterstitial(
                        placementId: interstitialAdPlacementId,
                      );
                      log("Interstitial Shown");
                    },
                    child: const Text('Show Interstitial'),
                  ),
                  const Divider(),

                  /// PLAYMEE
                  ElevatedButton(
                    onPressed: () async {
                      final ready = await GeemeeFlutter.isUserCenterReady(
                        placementId: playMeePlacementId,
                      );
                      log("UserCenter Ready: $ready");
                    },
                    child: const Text('Is PlayMee Ready?'),
                  ),
                  ElevatedButton(
                    onPressed: () async {
                      await GeemeeFlutter.openUserCenter(
                        placementId: playMeePlacementId,
                      );
                      log("UserCenter Opened");
                    },
                    child: const Text('Open PlayMee'),
                  ),
                ],
              ),
            ),
            if (_bannerLoaded) ...[
              Positioned(
                right: 20,
                bottom: 20,
                child: SizedBox(
                  width: 120,
                  height: 120,
                  child: Center(
                    child: GeemeeBanner(placementId: bannerAdPlacementId),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
