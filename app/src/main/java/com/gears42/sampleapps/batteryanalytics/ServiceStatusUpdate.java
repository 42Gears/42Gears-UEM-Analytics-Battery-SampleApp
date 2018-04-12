package com.gears42.sampleapps.batteryanalytics;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ServiceStatusUpdate extends Service implements Runnable {
    File battaryinfo;
    String data = "";
    final String path = Environment.getExternalStorageDirectory() + "/batteryinfo.csv";
    private String secretKey = "ENTER_YOUR_MDM_ANALYTICS_SECRET_KEY_HERE";
    Handler mHandler;
    Runnable mHandlerTask;
    String dname = "";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        battaryinfo = new File(Environment.getExternalStorageDirectory(), "batteryinfo.csv");
        if (!battaryinfo.exists()) {
            FileOutputStream fOut = null;
            try {
                battaryinfo.createNewFile();
                data = "Time,Device Name,Percentage,Health,Plugged status,Availability,Charging Status,Technology,Temperature,Voltage\n";
                fOut = new FileOutputStream(battaryinfo, true);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(data);
                myOutWriter.close();
                fOut.flush();
                fOut.close();
                data = "";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        getDeviceName();
        //  mHandler = new Handler();
        //mHandlerTask = this;
        return START_STICKY;
    }

    private void getDeviceName() {
        IntentFilter intentFilter = new IntentFilter("com.gears42.nixdevicename.COMMUNICATOR");

        final BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                dname = intent.getStringExtra("Device_Name");
                Log.d("Dname", dname);
                batteryInfo();
            }
        };

        registerReceiver(receiver, intentFilter);

        Intent intent = new Intent("com.nix.COMMUNICATOR");
        intent.putExtra("command", "get_device_name");
        sendBroadcast(intent);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("on destroy Service", "sec");
    }

    private void batteryInfo() {
        BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                boolean present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                String technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
                int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                double batterypercentage = ((double) level / scale) * 100;

                batterypercentage = Math.round((batterypercentage) * 100 / 100);
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                String time = String.valueOf(df.format(new Date()));
                data = "" + time + "," + dname +
                        "," + batterypercentage + "," + getBhealth(health) + "," + getBplugged(plugged) + "," + present + "," + getBstatus(status)
                        + "," + technology + "," + ((double) temperature / 10) + "," + voltage + "\n";
                // mHandlerTask.run();
                addDataInCSV(); // added here to update often- huge data
            }

        };
        this.registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    private void addDataInCSV() {
        try {
            FileOutputStream fOut = null;
            fOut = new FileOutputStream(battaryinfo, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.flush();
            fOut.close();
            ExportAnalytics(ServiceStatusUpdate.this.getApplicationContext());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ExportAnalytics(Context context) {

        try {
            Intent analyticsExportToNixIntent = new Intent();
            analyticsExportToNixIntent.setAction("com.gears42.nix.analytics");
            analyticsExportToNixIntent.putExtra("path", path);
            analyticsExportToNixIntent.putExtra("secret_key", secretKey);
            analyticsExportToNixIntent.putExtra("package_name", context.getApplicationInfo().packageName);
            context.sendBroadcast(analyticsExportToNixIntent);
        } catch (Exception e) {
        }
    }

    String getBhealth(int health) {
        switch (health) {
            case 1:
                return "Unknown";

            case 2:
                return "Good";

            case 3:
                return "Dead";

            case 4:
                return "Overheat";

            case 5:
                return "Overvoltage";

            case 6:
                return "Unspecified Failure";

            case 7:
                return "Cold";
        }

        return "";
    }

    String getBplugged(int plugged) {
        switch (plugged) {
            case 1:
                return "AC charger";

            case 2:
                return "USB Port";

            case 0:
                return "Unplugged";

            case 4:
                return "Wireless";
        }
        return "";
    }

    String getBstatus(int status) {
        switch (status) {
            case 1:
                return "Unknown";

            case 2:
                return "Charging";

            case 3:
                return "Discharging";

            case 4:
                return "Not Charging";

            case 5:
                return "Full";
        }
        return "";
    }

    @Override
    public void run() {
        Log.d("check", "sec" + dname);
        addDataInCSV();
        mHandler.postDelayed(mHandlerTask, 1000 * 60 * 5);
    }
}
