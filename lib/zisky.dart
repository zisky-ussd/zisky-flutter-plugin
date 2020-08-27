import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';

class Zisky {
  static const MethodChannel methodChannel =
  const MethodChannel('co.zisky.ussd.automation/zisky');

  static const stream =
  const EventChannel('co.zisky.ussd.automation/zisky-stream');

  static StreamSubscription _broadcastReceiverSubscription;

  static Future<String> startAction(String actionId, Function result,
      {Map<String, String> extras}) async {
    try {
      var completer = new Completer<Object>();
      _broadcastReceiverSubscription =
          stream.receiveBroadcastStream().listen(result);

      if (extras == null) {
        extras = new Map();
      }

      await methodChannel.invokeMethod('callAction',
          <String, dynamic>{'actionId': actionId, "extras": extras});
      _broadcastReceiverSubscription.onData(completer.complete);

      return completer.future;
    } on PlatformException catch (e) {
      print("ZISKYEXCEPTION $e");
    }
    return null;
  }


}
