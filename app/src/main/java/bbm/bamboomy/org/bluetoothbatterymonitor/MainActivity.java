package bbm.bamboomy.org.bluetoothbatterymonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnErrorListener {

    private TextView add;
    private BlueToothDialog bluetoothDialog;
    private TableLayout container;

    static boolean PLAY_HERE = false;

    private final int REQUEST_ENABLE_BT = 7;

    private DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private ServerThread myServerThread;

    private SoundThread soundThread;

    private MediaPlayer M_PLAYER_BACKGROUND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri
                        .fromParts("mailto", "bamboomy@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bluetooth Battery Monitor");
                startActivity(Intent
                        .createChooser(emailIntent, "Send feedback..."));
            }
        });

        add = (TextView) findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bluetoothDialog = new BlueToothDialog();

                bluetoothDialog.setActivity(MainActivity.this);

                bluetoothDialog.show(getSupportFragmentManager(), "test");
            }
        });

        container = (TableLayout) findViewById(R.id.container);

        listen();

        soundThread = new SoundThread(this);

        new Thread(soundThread).start();

        if (PLAY_HERE) {

            M_PLAYER_BACKGROUND = MediaPlayer.create(this, R.raw.here);
            M_PLAYER_BACKGROUND.setOnErrorListener(this);
            M_PLAYER_BACKGROUND.setLooping(false);
            M_PLAYER_BACKGROUND.setVolume(100, 100);

            M_PLAYER_BACKGROUND.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("Devices", MODE_PRIVATE);

        /*
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("deviceNames");

        editor.commit();
        */

        if (prefs.getBoolean("inited", false)) {
            retrieveDevices(prefs);
        } else {
            initDevices(prefs);
        }
    }

    private void retrieveDevices(SharedPreferences prefs) {

        Log.d("bbm", "retrieving...");

        Set<String> pairedDevicesNames = prefs.getStringSet("deviceNames", new HashSet<String>());

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (String name : pairedDevicesNames) {
            for (BluetoothDevice device : pairedDevices) {

                if (device.getName().equals(name)) {
                    addDeviceWithoutPersist(device);
                }
            }
        }
    }

    private void initDevices(SharedPreferences prefs) {

        Log.d("bbm", "initing...");

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice bt : pairedDevices) {
            addDeviceWithoutPersist(bt);
        }

        SharedPreferences.Editor editor = prefs.edit();

        Set<String> pairedDevicesNames = new HashSet<>();

        for (BluetoothDevice bt : pairedDevices) {
            pairedDevicesNames.add(bt.getName());
        }

        editor.putStringSet("deviceNames", pairedDevicesNames);

        editor.putBoolean("inited", true);
        editor.commit();
    }

    private void listen() {

        myServerThread = new ServerThread(this);

        myServerThread.start();
    }

    void addDevice(BluetoothDevice newDevice) {

        SharedPreferences prefs = getSharedPreferences("Devices", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> pairedDevicesNames = prefs.getStringSet("deviceNames", new HashSet<String>());

        pairedDevicesNames.add(newDevice.getName());

        editor.clear();
        editor.putBoolean("inited", true);
        editor.putStringSet("deviceNames", pairedDevicesNames);

        editor.commit();

        addDeviceWithoutPersist(newDevice);
    }

    void addDeviceWithoutPersist(BluetoothDevice newDevice) {

        container.addView(buildRowFromDevice(newDevice));
    }

    private View buildRowFromDevice(final BluetoothDevice device) {

        final TableRow result = new TableRow(this);

        TextView textView = new TextView(this);
        textView.setText(device.getName());

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(300, 100);
        textView.setLayoutParams(layoutParams);

        result.addView(textView);

        final ImageView eye = new ImageView(this);
        int id = getResources().getIdentifier("bbm.bamboomy.org.bluetoothbatterymonitor:drawable/eye", null, null);
        eye.setImageResource(id);

        layoutParams = new TableRow.LayoutParams(200, 100);
        eye.setLayoutParams(layoutParams);

        result.addView(eye);

        final TextView percentage = new TextView(this);
        percentage.setText("");
        percentage.setLayoutParams(layoutParams);
        percentage.setVisibility(View.GONE);

        result.addView(percentage);

        final TextView time = new TextView(this);
        time.setText("");
        time.setLayoutParams(layoutParams);

        final ClientThread clientThread = new ClientThread(device, BluetoothAdapter.getDefaultAdapter(),
                eye, percentage, MainActivity.this, time);

        clientThread.start();

        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clientThread.update();
            }
        });

        result.addView(time);

        ImageView imageview = new ImageView(this);
        id = getResources().getIdentifier("bbm.bamboomy.org.bluetoothbatterymonitor:drawable/remove", null, null);
        imageview.setImageResource(id);

        imageview.setLayoutParams(layoutParams);

        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.removeView(result);

                SharedPreferences prefs = getSharedPreferences("Devices", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                Set<String> pairedDevicesNames = prefs.getStringSet("deviceNames", new HashSet<String>());

                String exailedOne = null;

                Log.d("bbm", "size before:" + pairedDevicesNames.size());

                for (String name : pairedDevicesNames) {

                    if (name.equals(device.getName())) {
                        exailedOne = name;

                        Log.d("bbm", "going to remove: " + exailedOne);
                    }
                }

                pairedDevicesNames.remove(exailedOne);

                Log.d("bbm", "size after:" + pairedDevicesNames.size());

                editor.clear();
                editor.putBoolean("inited", true);
                editor.putStringSet("deviceNames", pairedDevicesNames);

                editor.commit();
            }
        });

        result.addView(imageview);

        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {

        }
    }

    public void adaptRow(final ImageView eye, final TextView percentage, final int numBytes) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                eye.setVisibility(View.GONE);
                percentage.setText(numBytes + " %");
                percentage.setVisibility(View.VISIBLE);
            }
        });
    }

    public void adaptTime(final TextView time, final Date lastUpdate) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Date now = Calendar.getInstance().getTime();

                long diff = getDateDiff(lastUpdate, now, TimeUnit.MINUTES);

                if (diff < 5) {

                    if (diff == 1) {

                        time.setText(diff + " minute");

                    } else {
                        time.setText(diff + " minutes");
                    }
                } else {
                    time.setText(df.format(lastUpdate));
                }
            }
        });
    }

    /**
     * Get a diff between two dates
     *
     * @param date1    the oldest date
     * @param date2    the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        myServerThread.cancel();

        if (M_PLAYER_BACKGROUND != null) {

            M_PLAYER_BACKGROUND.release();
            M_PLAYER_BACKGROUND = null;
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }
}
