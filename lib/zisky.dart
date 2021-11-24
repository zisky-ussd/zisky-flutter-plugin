import 'dart:async';

import 'package:flutter/services.dart';

class Zisky {
  static const MethodChannel methodChannel =
  const MethodChannel('co.zisky.ussd.automation/zisky');

  static const stream =
  const EventChannel('co.zisky.ussd.automation/zisky-stream');

  static StreamSubscription? _broadcastReceiverSubscription;

  static onData(Function? onData){

  }
  static Future<dynamic> startAction(String actionId,Function result,
      {Map<String, String>? extras}) async {
    try {
      Completer completer = new Completer<String>();
      _broadcastReceiverSubscription =
          stream.receiveBroadcastStream().listen((data){
            print(data);
            result(data);
          });

      if (extras == null) {
        extras = new Map();
      }

      await methodChannel.invokeMethod('callAction',
          <String, dynamic>{'actionId': actionId, "extras": extras});
      _broadcastReceiverSubscription?.onData(completer.complete);

      return completer.future;
    } on PlatformException catch (e) {
      print("ZISKYEXCEPTION $e");
    }
    return null;
  }

  static Future<void> init() async {
    try {
      await methodChannel.invokeMethod('initialization');
    } on PlatformException catch (e) {
      print("ZISKY_INITIALIZATION_EXCEPTION $e");
    }
  }
}
