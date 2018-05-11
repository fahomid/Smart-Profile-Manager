package fahomid.com.smartprofilemanager.smartprofilemanager;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class EventManager extends Service implements SensorEventListener {


    //flags and variables
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private Sensor mAccelerometer;
    private static final int SENSOR_SENSITIVITY = 4;
    private Profile profile;
    final private String HOME = "Home";
    final private String POCKET = "Pocket";
    final private String SILENT = "Silent";
    private boolean proximityStatus;
    private long lastTime = 0;
    private float lastX, lastY, lastZ;
    private static final int THRESHOLD = 600;
    private boolean deviceShaking;
    private String lastProfile;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        proximityStatus = true;
        deviceShaking = false;
        lastProfile = HOME;
        profile = new Profile(getBaseContext());
        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mProximity, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, mSensorManager.SENSOR_DELAY_NORMAL);
        Toast.makeText(getBaseContext(), "Smart Profile Manager service started in background!", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //check and assign sensor values to flags
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTime) > 100) {
                long diffTime = (currentTime - lastTime);
                lastTime = currentTime;
                float speed = Math.abs(x + y + z - lastX - lastY - lastZ)/ diffTime * 10000;
                if (speed > THRESHOLD) {
                    deviceShaking = true;
                }
                lastX = x;
                lastY = y;
                lastZ = z;
            }
        } else if(event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                proximityStatus = false;
            } else {
                proximityStatus = true;
            }
        }

        //comparing flag values to decide what to do
        if(proximityStatus && lastZ > -1 && lastProfile != HOME) {
            profile.setProfileMode(HOME);
            lastProfile = HOME;
            System.out.println("Phone in Home mode!");
        } else if(proximityStatus == false && lastY < 0 && lastZ < 8.5 && lastZ > -8.5 && lastProfile != POCKET) {
            profile.setProfileMode(POCKET);
            lastProfile = POCKET;
            System.out.println("Phone in Pocket mode!");
        } else if(proximityStatus == false && lastZ < -8.5 && lastProfile != SILENT) {
            profile.setProfileMode(SILENT);
            lastProfile = SILENT;
            System.out.println("Phone in Silent mode!");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onTaskRemoved(Intent intent) {
        startService(new Intent(getBaseContext(), EventManager.class));
        System.out.println("Task removed from!");
        super.onTaskRemoved(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        Toast.makeText(getBaseContext(), "Smart Profile Manager stopped successfully!", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
