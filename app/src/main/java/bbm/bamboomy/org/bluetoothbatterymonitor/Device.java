package bbm.bamboomy.org.bluetoothbatterymonitor;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

/**
 * Created by Sander on 24-6-2017.
 */

public class Device {

    private String name;

    public Device(String name){

        this.name = name;
    }

    String getName(){
        return name;
    }
}
