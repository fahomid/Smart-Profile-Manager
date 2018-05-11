package fahomid.com.smartprofilemanager.smartprofilemanager;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;

public class Profile {

    //flags and variables
    private AudioManager manager;
    private Context baseContext;

    Profile(Context context) {
        baseContext = context;
        manager = (AudioManager) baseContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setProfileMode(String mode) {
        switch (mode) {
            case "Home":
                manager.setStreamVolume(AudioManager.STREAM_RING, manager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                Settings.System.putInt(baseContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0);
                break;

            case "Pocket":
                manager.setStreamVolume(AudioManager.STREAM_RING, (int)Math.ceil(manager.getStreamMaxVolume(AudioManager.STREAM_RING) / 2),AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                Settings.System.putInt(baseContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 1);
                break;

            case "Silent":
                manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                Settings.System.putInt(baseContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 1);
                break;

            default:
                break;
        }
    }
}
