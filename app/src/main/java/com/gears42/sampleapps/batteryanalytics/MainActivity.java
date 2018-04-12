package com.gears42.sampleapps.batteryanalytics;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;


public class MainActivity extends Activity {

    private TextView batteryLevel;
    private TextView batteryStats;
    private EditText secretkey;
    private int currentBatteryLevel = -1;
    String imei;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        batteryStats = (TextView) findViewById(R.id.battery_stats);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
        startService(new Intent(MainActivity.this, ServiceStatusUpdate.class));
        this.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
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

            if ((currentBatteryLevel == -1) || (Math.abs(level - currentBatteryLevel) >= 1)) {
                currentBatteryLevel = level;

            }
            double batterypercentage = ((double) level / scale) * 100;
            batterypercentage = Math.round((batterypercentage) * 100 / 100);
            batteryStats.setText(
                    "IMEI: " + imei + "\n" +
                            "Time: " + Calendar.getInstance().getTime() + "\n" +
                            "Percentage: " + batterypercentage + "%\n" +
                            "Health: " + getBhealth(health) + "\n" +
                            "Plugged: " + getBplugged(plugged) + "\n" +
                            "Availability: " + present + "\n" +
                            "Charging Status: " + getBstatus(status) + "\n" +
                            "Technology: " + technology + "\n" +
                            "Temperature: " + ((double) temperature / 10) + " °C\n" +
                            "Voltage: " + voltage + "\n");

        }
    };

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
    protected void onResume() {
        super.onResume();
        this.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(this.batteryInfoReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("on destroy", "sec");
    }
}
