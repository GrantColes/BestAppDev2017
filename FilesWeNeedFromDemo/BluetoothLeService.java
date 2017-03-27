/////////////////////////////////////////////////////////////////////
//Description: This is the Low-Power Bluetooth Class taken from    //
//the Lattice Health Demo apk. This will be used for Bluetooth     //
//communication in our new app.                                    //
//                                                                 //
//Written by: Lattice, probably                                    //
//Contributed by: Grant                                            //
//Date: 3/27/2017                                                  //
/////////////////////////////////////////////////////////////////////

package com.example.HeartRate;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService
  extends Service
{
  public static final String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
  public static final String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
  public static final String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
  public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
  public static final UUID CCCD;
  public static final String DEVICE_DOES_NOT_SUPPORT_UART = "com.example.bluetooth.le.DEVICE_DOES_NOT_SUPPORT_UART";
  public static final UUID DIS_UUID;
  public static final String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
  public static final UUID FIRMWARE_REVISON_UUID;
  public static String HEART_RATE_MEASUREMENT;
  public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
  public static final UUID RX_SERVICE_UUID;
  private static final int STATE_CONNECTED = 2;
  private static final int STATE_CONNECTING = 1;
  private static final int STATE_DISCONNECTED = 0;
  private static final String TAG = BluetoothLeService.class.getSimpleName();
  public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
  public static final UUID TX_POWER_LEVEL_UUID;
  public static final UUID TX_POWER_UUID;
  public static final UUID UUID_HEART_RATE_MEASUREMENT;
  public static byte data_resp = 0;
  public static byte read_value = 0;
  private final IBinder mBinder = new LocalBinder();
  private BluetoothAdapter mBluetoothAdapter;
  private String mBluetoothDeviceAddress;
  private BluetoothGatt mBluetoothGatt;
  private BluetoothManager mBluetoothManager;
  private int mConnectionState = 0;
  private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
  {
    public void onCharacteristicChanged(BluetoothGatt paramAnonymousBluetoothGatt, BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic)
    {
      BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE", paramAnonymousBluetoothGattCharacteristic);
    }
    
    public void onCharacteristicRead(BluetoothGatt paramAnonymousBluetoothGatt, BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic, int paramAnonymousInt)
    {
      if (paramAnonymousInt == 0) {
        BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_DATA_AVAILABLE", paramAnonymousBluetoothGattCharacteristic);
      }
    }
    
    public void onConnectionStateChange(BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt1, int paramAnonymousInt2)
    {
      if (paramAnonymousInt2 == 2)
      {
        BluetoothLeService.this.mConnectionState = 2;
        BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_GATT_CONNECTED");
        Log.i(BluetoothLeService.TAG, "Connected to GATT server.");
        Log.i(BluetoothLeService.TAG, "Attempting to start service discovery:" + BluetoothLeService.this.mBluetoothGatt.discoverServices());
      }
      while (paramAnonymousInt2 != 0) {
        return;
      }
      BluetoothLeService.this.mConnectionState = 0;
      Log.i(BluetoothLeService.TAG, "Disconnected from GATT server.");
      BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_GATT_DISCONNECTED");
    }
    
    public void onServicesDiscovered(BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt)
    {
      if (paramAnonymousInt == 0)
      {
        BluetoothLeService.this.broadcastUpdate("com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED");
        return;
      }
      Log.w(BluetoothLeService.TAG, "onServicesDiscovered received: " + paramAnonymousInt);
    }
  };
  
  static
  {
    HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
    UUID_HEART_RATE_MEASUREMENT = UUID.fromString(HEART_RATE_MEASUREMENT);
    TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
  }
  
  private void broadcastUpdate(String paramString)
  {
    sendBroadcast(new Intent(paramString));
  }
  
  private void broadcastUpdate(String paramString, BluetoothGattCharacteristic paramBluetoothGattCharacteristic)
  {
    paramString = new Intent(paramString);
    if (TX_CHAR_UUID.equals(paramBluetoothGattCharacteristic.getUuid()))
    {
      paramBluetoothGattCharacteristic = paramBluetoothGattCharacteristic.getValue();
      if ((paramBluetoothGattCharacteristic != null) && (paramBluetoothGattCharacteristic.length > 0))
      {
        Log.e(TAG, "There are data coming in\n");
        localStringBuilder = new StringBuilder(paramBluetoothGattCharacteristic.length);
        j = paramBluetoothGattCharacteristic.length;
        i = 0;
        if (i < j) {
          break label131;
        }
        if ((paramBluetoothGattCharacteristic[0] == 64) && (DebugMode.debug_mode == 1))
        {
          data_resp = 1;
          read_value = paramBluetoothGattCharacteristic[1];
          Log.e(TAG, "the data resp here fffff");
        }
        Log.e(TAG, "Received byte from Julia\n");
        paramString.putExtra("com.example.bluetooth.le.EXTRA_DATA", localStringBuilder.toString());
      }
    }
    label131:
    byte b;
    do
    {
      sendBroadcast(paramString);
      return;
      b = paramBluetoothGattCharacteristic[i];
      localStringBuilder.append(String.format("%d|", new Object[] { Byte.valueOf(b) }));
      Log.e(TAG, String.format("%02x ", new Object[] { Byte.valueOf(b) }));
      i += 1;
      break;
      paramBluetoothGattCharacteristic = paramBluetoothGattCharacteristic.getValue();
    } while ((paramBluetoothGattCharacteristic == null) || (paramBluetoothGattCharacteristic.length <= 0));
    Log.e(TAG, "There are data coming in\n");
    StringBuilder localStringBuilder = new StringBuilder(paramBluetoothGattCharacteristic.length);
    int j = paramBluetoothGattCharacteristic.length;
    int i = 0;
    for (;;)
    {
      if (i >= j)
      {
        if ((paramBluetoothGattCharacteristic[0] == 64) && (DebugMode.debug_mode == 1))
        {
          data_resp = 1;
          read_value = paramBluetoothGattCharacteristic[1];
          Log.e(TAG, "the data resp here fffff");
        }
        Log.e(TAG, "Received byte from Julia\n");
        paramString.putExtra("com.example.bluetooth.le.EXTRA_DATA", localStringBuilder.toString());
        break;
      }
      b = paramBluetoothGattCharacteristic[i];
      localStringBuilder.append(String.format("%d|", new Object[] { Byte.valueOf(b) }));
      Log.e(TAG, String.format("%02x ", new Object[] { Byte.valueOf(b) }));
      i += 1;
    }
  }
  
  private void showMessage(String paramString)
  {
    Log.e(TAG, paramString);
  }
  
  public void close()
  {
    if (this.mBluetoothGatt == null) {
      return;
    }
    this.mBluetoothGatt.close();
    this.mBluetoothGatt = null;
  }
  
  public boolean connect(String paramString)
  {
    if ((this.mBluetoothAdapter == null) || (paramString == null))
    {
      Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
      return false;
    }
    if ((this.mBluetoothDeviceAddress != null) && (paramString.equals(this.mBluetoothDeviceAddress)) && (this.mBluetoothGatt != null))
    {
      Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
      if (this.mBluetoothGatt.connect())
      {
        this.mConnectionState = 1;
        return true;
      }
      return false;
    }
    BluetoothDevice localBluetoothDevice = this.mBluetoothAdapter.getRemoteDevice(paramString);
    if (localBluetoothDevice == null)
    {
      Log.w(TAG, "Device not found.  Unable to connect.");
      return false;
    }
    this.mBluetoothGatt = localBluetoothDevice.connectGatt(this, false, this.mGattCallback);
    Log.d(TAG, "Trying to create a new connection.");
    this.mBluetoothDeviceAddress = paramString;
    this.mConnectionState = 1;
    return true;
  }
  
  public void disconnect()
  {
    if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null))
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }
    this.mBluetoothGatt.disconnect();
  }
  
  public void enableTXNotification()
  {
    Object localObject = this.mBluetoothGatt.getService(RX_SERVICE_UUID);
    if (localObject == null)
    {
      showMessage("Rx service not found!");
      broadcastUpdate("com.example.bluetooth.le.DEVICE_DOES_NOT_SUPPORT_UART");
      return;
    }
    localObject = ((BluetoothGattService)localObject).getCharacteristic(TX_CHAR_UUID);
    if (localObject == null)
    {
      showMessage("Tx charateristic not found!");
      broadcastUpdate("com.example.bluetooth.le.DEVICE_DOES_NOT_SUPPORT_UART");
      return;
    }
    this.mBluetoothGatt.setCharacteristicNotification((BluetoothGattCharacteristic)localObject, true);
    localObject = ((BluetoothGattCharacteristic)localObject).getDescriptor(CCCD);
    ((BluetoothGattDescriptor)localObject).setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    this.mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor)localObject);
  }
  
  public List<BluetoothGattService> getSupportedGattServices()
  {
    if (this.mBluetoothGatt == null) {
      return null;
    }
    return this.mBluetoothGatt.getServices();
  }
  
  public boolean initialize()
  {
    if (this.mBluetoothManager == null)
    {
      this.mBluetoothManager = ((BluetoothManager)getSystemService("bluetooth"));
      if (this.mBluetoothManager == null)
      {
        Log.e(TAG, "Unable to initialize BluetoothManager.");
        return false;
      }
    }
    this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
    if (this.mBluetoothAdapter == null)
    {
      Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
      return false;
    }
    return true;
  }
  
  public IBinder onBind(Intent paramIntent)
  {
    return this.mBinder;
  }
  
  public boolean onUnbind(Intent paramIntent)
  {
    close();
    return super.onUnbind(paramIntent);
  }
  
  public void readCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic)
  {
    if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null))
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }
    this.mBluetoothGatt.readCharacteristic(paramBluetoothGattCharacteristic);
  }
  
  public void setCharacteristicNotification(BluetoothGattCharacteristic paramBluetoothGattCharacteristic, boolean paramBoolean)
  {
    if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null))
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }
    this.mBluetoothGatt.setCharacteristicNotification(paramBluetoothGattCharacteristic, paramBoolean);
    Log.e(TAG, "setcharacterisc here");
  }
  
  public void writeCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic)
  {
    if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null))
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }
    this.mBluetoothGatt.writeCharacteristic(paramBluetoothGattCharacteristic);
  }
  
  public void writeRXCharacteristic(byte[] paramArrayOfByte)
  {
    Object localObject = this.mBluetoothGatt.getService(RX_SERVICE_UUID);
    showMessage("mBluetoothGatt null" + this.mBluetoothGatt);
    if (localObject == null)
    {
      showMessage("Rx service not found!");
      broadcastUpdate("com.example.bluetooth.le.DEVICE_DOES_NOT_SUPPORT_UART");
      return;
    }
    localObject = ((BluetoothGattService)localObject).getCharacteristic(RX_CHAR_UUID);
    if (localObject == null)
    {
      showMessage("Rx charateristic not found!");
      broadcastUpdate("com.example.bluetooth.le.DEVICE_DOES_NOT_SUPPORT_UART");
      return;
    }
    ((BluetoothGattCharacteristic)localObject).setValue(paramArrayOfByte);
    boolean bool = this.mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic)localObject);
    Log.d(TAG, "write TXchar - status=" + bool);
  }
  
  public class LocalBinder
    extends Binder
  {
    public LocalBinder() {}
    
    BluetoothLeService getService()
    {
      return BluetoothLeService.this;
    }
  }
}
