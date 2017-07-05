package bbm.bamboomy.org.bluetoothbatterymonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import static bbm.bamboomy.org.bluetoothbatterymonitor.ServerThread.MY_UUID;

/**
 * Created by Sander on 24-6-2017.
 */

public class ClientThread extends Thread {

    private BluetoothSocket mmSocket;
    private BluetoothAdapter mBluetoothAdapter;
    private ImageView eye;
    private TextView percentage, time;
    private MainActivity activity;
    private boolean updateNow = true;
    private Date lastUpdate = null;
    private BluetoothDevice device;

    ClientThread(BluetoothDevice device, BluetoothAdapter defaultAdapter, ImageView eye, TextView percentage, MainActivity mainActivity, TextView time) {

        this.device = device;
        this.mBluetoothAdapter = defaultAdapter;
        this.eye = eye;
        this.percentage = percentage;
        this.activity = mainActivity;
        this.time = time;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.

        while (true) {

            for (int i = 0; i < 5 && !updateNow; i++) {

                adaptTime();

                for (int j = 0; j < 30 && !updateNow; j++) {

                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            query();

            updateNow = false;
        }

    }

    private void adaptTime() {

        if (lastUpdate != null) {

            Log.d("bbm", "going to adapt time...");

            activity.adaptTime(time, lastUpdate);
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {

        InputStream mmInStream = null;

        try {
            mmInStream = mmSocket.getInputStream();
        } catch (IOException e) {
            //Log.e(TAG, "Error occurred when creating input stream", e);
        }

        byte[] mmBuffer = new byte[1];

        try {
            // Read from the InputStream.
            mmInStream.read(mmBuffer);

            activity.adaptRow(eye, percentage, mmBuffer[0]);

            lastUpdate = Calendar.getInstance().getTime();

        } catch (IOException e) {
            //Log.d(TAG, "Input stream was disconnected", e);
        }
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            //Log.e(TAG, "Could not close the client socket", e);
        }
    }

    void query() {

        BluetoothSocket tmp = null;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            //Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;

        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                //Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        manageMyConnectedSocket(mmSocket);
    }

    void update() {

        updateNow = true;
    }
}