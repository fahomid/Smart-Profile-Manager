package fahomid.com.smartprofilemanager.smartprofilemanager;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.midi.MidiDeviceService;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import static android.Manifest.permission.ACCESS_NOTIFICATION_POLICY;
import static android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS;

public class MainActivity extends AppCompatActivity {


    //flags and variables
    private ToggleButton onOffSwitch;
    private boolean reqPendingDS;
    private boolean reqPendingNP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setting activity view, variables, button and notification manager
        setContentView(R.layout.activity_main);
        onOffSwitch = findViewById(R.id.on_off_switch);
        reqPendingDS = false;
        reqPendingNP = false;

        //update toggle button
        checkAndUpdateStatus();

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked && !reqPendingDS && !reqPendingNP) {
                    startProfileService(false);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !reqPendingDS && !reqPendingNP) {
                    getPermissions();
                } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    startProfileService(isChecked);
                }
            }
        });
    }

    //check and update status
    private void checkAndUpdateStatus() {
        if(isMyServiceRunning(EventManager.class)) {
            onOffSwitch.setChecked(true);
            System.out.println("Process already running!");
        } else {
            onOffSwitch.setChecked(false);
            System.out.println("Process not running!");
        }
    }
    //check and get permissions
    private void getPermissions() {
        System.out.println("Inside: getPermissions()");
        boolean dsPermission = checkPermissionDeviceSetting();
        boolean npPermission = checkPermissionNotificationPolicy();
        if(onOffSwitch.isChecked() && dsPermission && npPermission) {
            startProfileService(true);
        } else if(!dsPermission && onOffSwitch.isChecked()){
            reqPendingDS = true;
            getDeviceSettingPermission();
        } else if(!npPermission && onOffSwitch.isChecked()) {
            reqPendingNP = true;
            getNotificationPolicyPermission();
        }
    }

    //check device setting permission
    private boolean checkPermissionDeviceSetting() {
        System.out.println("Inside: checkPermissionDeviceSetting()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(getApplicationContext())) {
            return true;
        } else {
            return false;
        }
    }

    //check notification policy permission
    private boolean checkPermissionNotificationPolicy() {
        System.out.println("Inside: checkPermissionNotificationPolicy()");
        NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && n.isNotificationPolicyAccessGranted()) {
            return true;
        } else {
            return false;
        }
    }

    //get permission device settings
    private void getDeviceSettingPermission() {
        System.out.println("Inside: getDeviceSettingPermission()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Permission Required!").setMessage("Please allow \"Device Settings\" permission. This app require your permission to work properly!\n\nGive permission now?").setPositiveButton("Yes", getDSPermission).setNegativeButton("No", getDSPermission).show();
        }
    }

    //get permission notification policy
    private void getNotificationPolicyPermission() {
        System.out.println("Inside: getNotificationPolicyPermission()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Permission Required!").setMessage("Please allow \"Do Not Disturbe Permission\" from next window. This app require your permission to work properly!\n\nGive permission now?").setPositiveButton("Yes", getNPPermission).setNegativeButton("No", getNPPermission).show();
        }
    }


    //handle device setting permission action
    DialogInterface.OnClickListener getDSPermission = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName())), 0);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    onOffSwitch.setChecked(false);
                    reqPendingDS = false;
                    Toast.makeText(getBaseContext(), "Permission denied! Service could not be started!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    //handle do not disturb policy permission action
    DialogInterface.OnClickListener getNPPermission = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 1);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    onOffSwitch.setChecked(false);
                    reqPendingNP = false;
                    Toast.makeText(getBaseContext(), "Permission denied! Service could no bet started!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



    //this handles start or stop service event
    private void startProfileService(boolean isChecked) {
        System.out.println("Inside: startProfileService()");
        if(isChecked) {
             Toast.makeText(getBaseContext(), "Starting Smart Profile Manager Service..!", Toast.LENGTH_SHORT).show();
             startService(new Intent(getBaseContext(), EventManager.class));
        } else {
             Toast.makeText(getBaseContext(), "Stopping Smart Profile Manager Service..!", Toast.LENGTH_SHORT).show();
             stopService(new Intent(getBaseContext(), EventManager.class));
        }
        new android.os.Handler().postDelayed(new Runnable() {
            public void run() {
                checkAndUpdateStatus();
            }
        },1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("Inside: onActivityResult()");
        //getPermissions();
        switch (requestCode) {
            case 0:
                if(!checkPermissionDeviceSetting()) {
                    Toast.makeText(getBaseContext(), "Permission denied! Service could no bet started!", Toast.LENGTH_SHORT).show();
                } else {
                    getPermissions();
                }
                onOffSwitch.setChecked(false);
                reqPendingDS = false;
                break;

            case 1:
                if(checkPermissionNotificationPolicy()) {
                    getPermissions();
                } else {
                    onOffSwitch.setChecked(false);
                    Toast.makeText(getBaseContext(), "Permission denied! Service could no bet started!", Toast.LENGTH_SHORT).show();
        }
                reqPendingNP = false;
                break;
        }
    }

    //this checks if the profile manager service is running already
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
