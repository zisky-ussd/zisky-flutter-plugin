package com.zisky.zisky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.zisky.sdk.model.USSDParameters;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static com.zisky.zisky.PermissionsUtil.isAccessiblityServicesEnable;
import static com.zisky.zisky.PermissionsUtil.openSettingsAccessibility;

/**
 * ZiskyPlugin
 */
public class ZiskyPlugin implements MethodCallHandler {
    /**
     * Plugin registration.
     */
    public static final String TAG = "ZiskyPlugin";

    private final Activity activity;

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "com.zisky.ussd.automation/zisky");
        channel.setMethodCallHandler(new ZiskyPlugin(registrar.activity()));
    }

    private ZiskyPlugin(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("getPlatformVersion")) {

            result.success("Android " + android.os.Build.VERSION.RELEASE);


        } else if (call.method.equals("callAction")) {

            proceedIfPermissionsAllowed(activity);
            final String actionId = call.argument("actionId");
            final HashMap<String, String> extras = call.argument("extras");
            Intent intent = new USSDParameters
                    .Builder(activity)
                    .process(actionId)
                    .build();
            if (extras != null && !extras.isEmpty()) {

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
            Log.d(TAG, "ACTION_ID " + actionId);
            activity.startActivity(intent);
            result.success("Android " + android.os.Build.VERSION.RELEASE);

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
}
