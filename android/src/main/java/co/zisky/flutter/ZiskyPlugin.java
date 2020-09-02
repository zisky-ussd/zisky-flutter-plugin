package co.zisky.flutter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import co.zisky.ussd.sdk.model.USSDParameters;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static co.zisky.ussd.sdk.Constants.USSD_MESSAGE;
import static co.zisky.ussd.sdk.Constants.USSD_STATUS;
import static co.zisky.flutter.JsonUtil.toJson;
import co.zisky.ussd.sdk.ZiSky;
/**
 * ZiskyPlugin
 */
public class ZiskyPlugin implements MethodCallHandler, EventChannel.StreamHandler, FlutterPlugin {
    /**
     * Plugin registration.
     */
    public static final String TAG = "ZiskyPlugin";

    private Context applicationContext;
    private MethodChannel methodChannel;
    private EventChannel eventChannel;
    private BinaryMessenger messenger;

    public static void registerWith(Registrar registrar) {
        ZiskyPlugin ziskyPlugin = new ZiskyPlugin();
        ziskyPlugin.onAttachedToEngine(registrar.context(), registrar.messenger());


    }

    private void onAttachedToEngine(Context applicationContext, BinaryMessenger messenger) {
        Log.d(TAG, "onAttachedToEngine");
        this.applicationContext = applicationContext;
        methodChannel = new MethodChannel(messenger, "co.zisky.ussd.automation/zisky");
        eventChannel = new EventChannel(messenger, "co.zisky.ussd.automation/zisky-stream");
        eventChannel.setStreamHandler(this);
        methodChannel.setMethodCallHandler(this);
        this.messenger = messenger;

    }

    private ZiskyPlugin() {
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("callAction")) {
            final String actionId = call.argument("actionId");
            final HashMap<String, String> extras = call.argument("extras");
            Log.d(TAG, "SUPPLIED ACTION_ID " + actionId);

            Intent intent = new USSDParameters
                    .Builder(applicationContext)
                    .processId(actionId)
                    .build();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (extras != null && !extras.isEmpty()) {
                Log.d(TAG, "SUPPLIED EXTRAS " + extras.toString());

                for (Map.Entry<String, String> entry : extras.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    try {
                        if (key == null) {
                            throw new RuntimeException("key is null");
                        }
                        if (value == null) {
                            throw new RuntimeException("value is null");
                        }

                        intent.putExtra(key, value);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Parameters", "Key value pair was not valid");
                    }


                }
            }
            applicationContext.startActivity(intent);
        }else if (call.method.equals("initialization")) {
            Log.d("INITIALIZATION", "initialization...... ");
            ZiSky.init(applicationContext);
        } else{
            result.notImplemented();
        }
    }


    private BroadcastReceiver messageReceiver;

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getApplicationContext(), messenger);
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding flutterPluginBinding) {
        Log.d(TAG, "messageReceiver deregistered");

        applicationContext = null;
        eventChannel.setStreamHandler(null);
        eventChannel = null;
    }


    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        messageReceiver = createBroadcastReceiver(eventSink);
        String packageName = applicationContext.getPackageName()+".TRANSACTION_CONFIRMATION";
        IntentFilter intentFilter = new IntentFilter(packageName);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        applicationContext.registerReceiver(messageReceiver, intentFilter);

    }

    @Override
    public void onCancel(Object o) {
        Log.d(TAG, "messageReceiver cancelled");

        if (messageReceiver != null) {
            applicationContext.unregisterReceiver(messageReceiver);
            messageReceiver = null;
        }

    }

    private BroadcastReceiver createBroadcastReceiver(final EventChannel.EventSink events) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                HashMap<String, String> parsed_variables =
                        (HashMap<String, String>) intent.getSerializableExtra("parsed_variables");

                if (intent.hasExtra(USSD_MESSAGE)
                        && intent.hasExtra(USSD_STATUS)

                ) {

                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("status", intent.getStringExtra(USSD_STATUS));
                    objectMap.put("message", intent.getStringExtra(USSD_MESSAGE));
                    objectMap.put("parsed_variables", parsed_variables);
                    events.success(toJson(objectMap));
                }


            }
        };

    }


}
