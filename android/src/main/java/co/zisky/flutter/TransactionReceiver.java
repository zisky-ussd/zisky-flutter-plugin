package co.zisky.flutter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;


public class TransactionReceiver extends BroadcastReceiver {

    public static final String TAG = "TransactionReceiver";

    public TransactionReceiver() {

    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onReceive(Context context, Intent intent) {


        Log.e(TAG,"BROADCAST DATA RECEIVED");
        HashMap<String, String> parsed_variables =
                (HashMap<String, String>) intent.getSerializableExtra("parsed_variables");

        Log.e(TAG, parsed_variables +"");


    }

}