package bbm.bamboomy.org.bluetoothbatterymonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView add;
    private BlueToothDialog bluetoothDialog;
    private TableLayout container;

    private final int REQUEST_ENABLE_BT = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        /*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

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
    }

    private void listen() {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        (new ServerThread(mBluetoothAdapter, this)).start();
    }

    void addDevice(BluetoothDevice newDevice) {

        container.addView(buildRowFromDevice(newDevice));

    }

    private View buildRowFromDevice(final BluetoothDevice device) {

        final TableRow result = new TableRow(this);

        TextView textView = new TextView(this);
        textView.setText(device.getName());

        result.addView(textView);

        final ImageView eye = new ImageView(this);
        int id = getResources().getIdentifier("bbm.bamboomy.org.bluetoothbatterymonitor:drawable/eye", null, null);
        eye.setImageResource(id);

        final TextView percentage = new TextView(this);
        percentage.setText("");

        result.addView(percentage);

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(500, 100);
        eye.setLayoutParams(layoutParams);

        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                (new ClientThread(device, BluetoothAdapter.getDefaultAdapter(), eye, percentage, MainActivity.this)).start();
            }
        });

        result.addView(eye);

        ImageView imageview = new ImageView(this);
        id = getResources().getIdentifier("bbm.bamboomy.org.bluetoothbatterymonitor:drawable/remove", null, null);
        imageview.setImageResource(id);

        imageview.setLayoutParams(layoutParams);

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

    void connect() {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
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
            }
        });
    }
}
