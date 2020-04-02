package com.zisky.zisky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.zisky.sdk.model.USSDParameters;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static com.zisky.sdk.Constants.USSD_MESSAGE;
import static com.zisky.sdk.Constants.USSD_STATUS;
import static com.zisky.zisky.PermissionsUtil.isAccessiblityServicesEnable;
import static com.zisky.zisky.PermissionsUtil.openSettingsAccessibility;

/**
 * ZiskyPlugin
 */
public class ZiskyPlugin implements MethodCallHandler, EventChannel.StreamHandler, FlutterPlugin {
    /**
     * Plugin registration.
     */
    public static final String TAG = "ZiskyPlugin";

    private final Activity activity;
    private Result resultLater;
    private Context applicationContext;
    private MethodChannel methodChannel;
    private EventChannel eventChannel;
    private BinaryMessenger messenger;

    public static void registerWith(Registrar registrar) {
        ZiskyPlugin ziskyPlugin = new ZiskyPlugin(registrar.activity());
        ziskyPlugin.onAttachedToEngine(registrar.context(), registrar.messenger());


    }

    private void onAttachedToEngine(Context applicationContext, BinaryMessenger messenger) {
        Log.d(TAG, "onAttachedToEngine");
        this.applicationContext = applicationContext;
        methodChannel = new MethodChannel(messenger, "com.zisky.ussd.automation/zisky");
        eventChannel = new EventChannel(messenger, "com.zisky.ussd.automation/zisky-stream");
        eventChannel.setStreamHandler(this);
        methodChannel.setMethodCallHandler(this);
        this.messenger = messenger;

    }

    private ZiskyPlugin(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("callAction")) {

            proceedIfPermissionsAllowed(activity);
            final String actionId = call.argument("actionId");
            final HashMap<String, String> extras = call.argument("extras");
            Log.d(TAG, "SUPPLIED ACTION_ID " + actionId);

            Intent intent = new USSDParameters
                    .Builder(applicationContext)
                    .process(actionId)
                    .build();
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
            resultLater = result;
        }
        {
            result.notImplemented();
        }
    }

    private void proceedIfPermissionsAllowed(Activity context) {
        boolean accessibilityEnabled = checkForAccessibilityService(context);
        if (!accessibilityEnabled) return;
        boolean overlayEnabled = checkForOverlayPermission(context);
        if (!overlayEnabled) return;

    }

    private boolean checkForAccessibilityService(Activity context) {
        boolean isEnabled = isAccessiblityServicesEnable(context);
        if (isEnabled) {
            return true;
        }
        openSettingsAccessibility(context);
        return false;
    }

    private boolean checkForOverlayPermission(Context context) {
        return verifyOverLay(context);
    }

    public static boolean verifyOverLay(Context context) {
        boolean m_android_doesnt_grant = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(context);
        if (m_android_doesnt_grant) {
            if (context instanceof Activity) {
                openSettingsOverlay((Activity) context);
            } else {
                Toast.makeText(context,
                        "Overlay permission have not grant permission.",
                        Toast.LENGTH_LONG).show();
            }
            return false;
        } else
            return true;
    }

    private static void openSettingsOverlay(final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle("USSD Overlay permission");
        ApplicationInfo applicationInfo = activity.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String name = applicationInfo.labelRes == 0 ?
                applicationInfo.nonLocalizedLabel.toString() : activity.getString(stringId);
        alertDialogBuilder
                .setMessage("You must allow for the app to appear '" + name + "' on top of other apps.");
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setNeutralButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        if (alertDialog != null) {
            alertDialog.show();
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

    public static final String INTENT_FILTER_PACKAGE = "zisky.com.test_automation.TRANSACTION_CONFIRMATION";

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        messageReceiver = createBroadcastReceiver(eventSink);
        IntentFilter intentFilter = new IntentFilter(INTENT_FILTER_PACKAGE);
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

                Log.d(TAG, "parsed_variables=" + parsed_variables != null ? parsed_variables.toString() : " " + " , status= " + intent.getStringExtra(USSD_STATUS));
                if (intent.hasExtra(USSD_MESSAGE)
                        && intent.hasExtra(USSD_STATUS)

                ) {

                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("status", intent.getStringExtra(USSD_STATUS));
                    objectMap.put("message", intent.getStringExtra(USSD_MESSAGE));
                    objectMap.put("parsed_variables", parsed_variables);
                    events.success(objectMap);
                }


            }
        };

    }


}
