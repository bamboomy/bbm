/*
 * Copyright (c) 2016 Sander Theetaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package bbm.bamboomy.org.bluetoothbatterymonitor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlueToothDialog extends DialogFragment {

    private MainActivity activity;

    private String name = "";
    private String[] names;
    private List<String> devicesList;

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        getNames();

        final String[] names = devicesList.toArray(new String[devicesList.size()]);

        builder.setTitle(R.string.choose_device)
                .setItems(names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        /*
                        if (which == 5) {
                            activity.createNewNameDialog();
                        } else if (which == 6) {
                            showCustomNameDialog();
                        } else {
                            name = names[which];

                            activity.setName(name);
                        }
                        */
                    }
                });
        return builder.create();
    }
/*
    private void showCustomNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("How should we name you?");

        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                name = input.getText().toString();

                if (!isAlphaNumerical(name)) {
                    showNameRetryDialog();
                } else {
                    activity.setName(name);
                }
            }
        });

        builder.show();
    }

    private void showNameRetryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("No special characters allowed in name...");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.createNewNameDialog();
            }
        });

        builder.show();
    }
*/

    public void getNames() {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        devicesList = new ArrayList<>();

        for(BluetoothDevice bt : pairedDevices) {
            devicesList.add(bt.getName());
        }
   }
}