# zisky

ussd automation

## Code Example

`
  void callBalanceEnquiry() async {

    try {
      Map<String, String> map = Map();
      map["amount"] = "1";

      await Zisky.startAction("16", getResponse, extras: map);
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

`

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter, view our 
[online documentation](https://flutter.dev/docs), which offers tutorials, 
samples, guidance on mobile development, and a full API reference.
