package com.example.rgbledcontrol;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    SeekBar seekRED, seekGREEN, seekBLUE;
    TextView tvREDValue, tvGREENValue, tvBLUEValue;
    ImageView imgBluetooth;

    BluetoothAdapter bluetoothAdapter;
    int pairedDeviceCount=0;
    Set<BluetoothDevice> devices;
    BluetoothDevice remoteDevice;
    BluetoothSocket bluetoothSocket;
    OutputStream outputStream=null;
    InputStream inputStream=null;
    Thread workerThread=null;
    String strDelimiter="\n";
    char charDelimiter='\n';
    byte readBuffer[];
    int readBufferPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgBluetooth=findViewById(R.id.imgBluetooth);

        seekRED = findViewById(R.id.seekRED);
        seekRED.setOnSeekBarChangeListener(this);

        seekGREEN = findViewById(R.id.seekGREEN);
        seekGREEN.setOnSeekBarChangeListener(this);

        seekBLUE = findViewById(R.id.seekBLUE);
        seekBLUE.setOnSeekBarChangeListener(this);

        tvREDValue = findViewById(R.id.tvREDValue);
        tvGREENValue = findViewById(R.id.tvGREENValue);
        tvBLUEValue = findViewById(R.id.tvBLUEValue);

        checkBluetooth();


    }//onCreate ????????? ???~~

    //??????????????? ???????????? ?????? ?????? ??????
    void checkBluetooth() {
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null) {
            showToast("??????????????? ???????????? ?????? ???????????????.");
        }else {
            //????????? ??????????????? ???????????? ??????
            if(!bluetoothAdapter.isEnabled()) {
                Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 10);
            }else {
                selectDevice();
            }
        }
    }

    //???????????? ?????? ?????? ?????? ??? ??????
    void selectDevice() {
        devices=bluetoothAdapter.getBondedDevices();
        pairedDeviceCount=devices.size();
        if(pairedDeviceCount==0){
            showToast("???????????? ????????? ????????? ????????????.");
        }else {
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("???????????? ?????? ??????");
            List<String> listItems=new ArrayList<String>();
            for(BluetoothDevice device:devices) {
                listItems.add(device.getName());
            }
            listItems.add("??????");
            final CharSequence[] items=listItems.toArray(new CharSequence[listItems.size()]);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which==pairedDeviceCount){
                        showToast("????????? ??????????????????.");
                    }else {
                        connectToSelectedDevice(items[which].toString());
                    }
                }
            });
            builder.setCancelable(false);  //?????? ?????? ?????? ????????????
            AlertDialog dlg=builder.create();
            dlg.show();
        }
    }

    //????????? ???????????? ???????????? ??????
    void connectToSelectedDevice(String selectedDeviceName){
        remoteDevice=getDeviceFromBondedList(selectedDeviceName);
        UUID uuid=UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try{
            bluetoothSocket=remoteDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect(); //????????? ????????? ??????
            imgBluetooth.setImageResource(R.drawable.bluetooth_icon);
            outputStream=bluetoothSocket.getOutputStream();
            inputStream=bluetoothSocket.getInputStream();
        }catch (Exception e){
            showToast("?????? ????????? ?????? ????????????.");
            imgBluetooth.setImageResource(R.drawable.bluetooth_grayicon);
        }
    }



    //???????????? ???????????? ????????? ???????????? ??????
    BluetoothDevice getDeviceFromBondedList(String name) {
        BluetoothDevice selectedDevice=null;
        for(BluetoothDevice device: devices){
            if(name.equals(device.getName())){
                selectedDevice=device;
                break;
            }
        }
        return selectedDevice;
    }

    //????????? ??????
    void sendData(String msg){
        msg+=strDelimiter;
        try {
            outputStream.write(msg.getBytes());  // ??????????????? ????????? ??????
        }catch (Exception e) {
            showToast("????????? ?????? ????????? ????????? ??????????????????.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            workerThread.interrupt();
            inputStream.close();
            outputStream.close();
            bluetoothSocket.close();
        } catch (Exception e) {
            showToast("??? ?????? ??? ?????? ??????");
        }
    }

    void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    selectDevice();
                } else if (resultCode == RESULT_CANCELED) {
                    showToast("???????????? ???????????? ??????????????????.");
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        String message=""; // ??????????????? ?????? ?????? ???????????? ??????

        switch (seekBar.getId()) {

            case R.id.seekRED:
                tvREDValue.setText("RED : " + progress);
                message = "R"+String.valueOf(progress);
                break;

            case R.id.seekGREEN:
                tvGREENValue.setText("GREEN : " + progress);
                message = "G"+String.valueOf(progress);
                break;

            case R.id.seekBLUE:
                tvBLUEValue.setText("BLUE : " + progress);
                message = "B"+String.valueOf(progress);
                break;
        }

        if(outputStream != null) {
            sendData(message);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}