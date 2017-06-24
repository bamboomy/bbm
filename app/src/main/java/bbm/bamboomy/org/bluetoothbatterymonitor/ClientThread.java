package bbm.bamboomy.org.bluetoothbatterymonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import static bbm.bamboomy.org.bluetoothbatterymonitor.ServerThread.MY_UUID;

/**
 * Created by Sander on 24-6-2017.
 */

public class ClientThread extends Thread {

    private final BluetoothSocket mmSocket;
    private BluetoothAdapter mBluetoothAdapter;
    private ImageView eye;
    private TextView percentage;
    private MainActivity activity;

    ClientThread(BluetoothDevice device, BluetoothAdapter defaultAdapter, ImageView eye, TextView percentage, MainActivity mainActivity) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            //Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;

        this.mBluetoothAdapter = defaultAdapter;
        this.eye = eye;
        this.percentage = percentage;
        this.activity = mainActivity;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.

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

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {

        InputStream mmInStream = null;

        try {
            mmInStream = mmSocket.getInputStream();
        } catch (IOException e) {
            //Log.e(TAG, "Error occurred when creating input stream", e);
        }

        byte[] mmBuffer = new byte[1];
        int numBytes = 0; // bytes returned from read()

        try {
            // Read from the InputStream.
            numBytes = mmInStream.read(mmBuffer);

            activity.adaptRow(eye, percentage, numBytes);

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
}