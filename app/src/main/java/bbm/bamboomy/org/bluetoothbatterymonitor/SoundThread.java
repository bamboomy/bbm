package bbm.bamboomy.org.bluetoothbatterymonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by Sander on 24-6-2017.
 */

public class SoundThread extends Thread implements MediaPlayer.OnErrorListener {

    private static MediaPlayer M_PLAYER;

    private final String NAME = "name";

    private MainActivity activity;

    public SoundThread(MainActivity mainActivity) {

        activity = mainActivity;
    }

    public void run() {

        while (true) {

            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = activity.getApplicationContext().registerReceiver(null, ifilter);

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int) (level * 100 / (float) scale);

            if (batteryPct >= 80) {

                try {
                    playSound();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }

            try {
                sleep(60000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

        while (M_PLAYER.isPlaying()) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        M_PLAYER.release();
    }

    private void playSound() throws IOException {

        AudioManager amanager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = amanager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        amanager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0);

        M_PLAYER = MediaPlayer.create(activity, R.raw.charge);
        M_PLAYER.setOnErrorListener(this);

        M_PLAYER.setAudioStreamType(AudioManager.STREAM_ALARM); // this is important.

        M_PLAYER.prepare();
        M_PLAYER.setVolume(1f, 1f);
        M_PLAYER.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

        Toast.makeText(activity, "music player failed", Toast.LENGTH_SHORT).show();
        if (M_PLAYER != null) {
            try {
                M_PLAYER.stop();
                M_PLAYER.release();
            } finally {
                M_PLAYER = null;
            }
        }

        return false;
    }
}