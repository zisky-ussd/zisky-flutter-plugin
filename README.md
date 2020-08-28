# Zisky

Zisky is a flutter plugin that automates android ussd sessions.

## import
```bash
import 'package:zisky/zisky.dart';
```

## Usage

```java
  void callBalanceEnquiry() async {

    try {

      await Zisky.startAction("action_id", getResponse);
    } on PlatformException catch (e) {
      print(e);
    }
  }

  String getResponse(response) {
    print("RESULT FINAL= $response");
    if (response != null) {
      ActionResponse responseObj =
          ActionResponse.fromJson(jsonDecode(response));

      if (responseObj.parsed_variables.containsKey("balance")) {
        setState(() {
          result = "\$" + responseObj.parsed_variables["balance"];
        });
      }
    }

    return response;
  }

```



```java
  void sendMoney() async {

    try {
      Map<String, String> map = Map();
      map["amount"] = "1";
      map["destination"] = "263774......";
      await Zisky.startAction("action_id", getResponse, extras: map);
    } on PlatformException catch (e) {
      print(e);
    }
  }



```

### Github Example

[zisky-flutter-ussd-automation](https://github.com/zisky-ussd/zisky-flutter-ussd-automation)

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android.

For help getting started with Flutter, view our 
[online documentation](https://flutter.dev/docs), which offers tutorials, 
samples, guidance on mobile development, and a full API reference.
