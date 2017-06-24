package bbm.bamboomy.org.bluetoothbatterymonitor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView add;
    private BlueToothDialog bluetoothDialog;
    private TableLayout container;

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
    }

    void addDevice(Device newDevice){

        container.addView(buildRowFromDevice(newDevice));

    }

    private View buildRowFromDevice(Device device) {

        TableRow result = new TableRow(this);

        TextView textView = new TextView(this);
        textView.setText(device.getName());

        result.addView(textView);

        ImageView imageview = new ImageView(this);
        int id = getResources().getIdentifier("bbm.bamboomy.org.bluetoothbatterymonitor:drawable/remove", null, null);
        imageview.setImageResource(id);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
        imageview.setLayoutParams(layoutParams);

        result.addView(imageview);
        layoutParams = new LinearLayout.LayoutParams(1000, 100);
        result.setLayoutParams(layoutParams);

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
}
