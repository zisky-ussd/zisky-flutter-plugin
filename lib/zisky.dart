import 'dart:async';

import 'package:flutter/services.dart';

class Zisky {
  static const MethodChannel _channel =
      const MethodChannel('zisky');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
