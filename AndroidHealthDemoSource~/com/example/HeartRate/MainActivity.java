package com.example.HeartRate;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.util.EncodingUtils;

public class MainActivity
  extends Activity
{
  // Variable Declaration
  // BLE and properties
  static int BLE_MAX_BYTES_PER_TRANSMISSION = 20;
  public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
  public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
  private static final String TAG = MainActivity.class.getSimpleName();

  //
  public static int data_head_en;
  public static int ir_end;
  public static String key_words;
  public static int len_cnt;
  private static BluetoothLeService mBluetoothLeService;
  private static ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
  private static BluetoothGattCharacteristic mNotifyCharacteristic;
  
  //
  public static int progress;
  public static int ram_addr;
  public static int rcv_data;
  public static int rd_addr;
  public static int rd_len;
  public static int rd_once;
  public static int rd_total;
  public static int rdata_state;
  public static int read_data_len = 0;

  //
  private final String LIST_UUID = "UUID";

  //
  TextView data_20s_value;
  TextView data_60s_value;

  //
  public int data_head = 0;
  int data_len = 0;
  int data_len_high = 0;
  int data_len_low = 0;
  public int data_type;
  public int data_value;

  //
  int divider = 0;
  int divider_high = 0;
  int divider_low = 0;

  //
  private int doubletap_counters = 0;
  private TextView doubletap_state;

  //
  public int fail_cnt = 0;

  //
  private int handheld_counters = 0;
  private TextView handheld_state;

  //
  public int heart_rate_data_cnt = 0;
  final int heart_rate_error = 100;
  final int heart_rate_range = 170;
  final int heart_rate_shold = 65536;
  public double heart_rate_value = 0.0D;

  //
  private boolean mConnected = false;
  private TextView mConnectionState;
  private String mDeviceAddress;
  private ExpandableListView mGattServicesList;

  // Method to connect to bluetooth LE to device
  private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
  {
    public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
    {
      paramAnonymousContext = paramAnonymousIntent.getAction();
      if ("com.example.bluetooth.le.ACTION_GATT_CONNECTED".equals(paramAnonymousContext))
      {
        MainActivity.this.mConnected = true;
        MainActivity.this.invalidateOptionsMenu();
      }
      do
      {
        try
        {
          Thread.sleep(500L);
          return;
        }
        catch (InterruptedException paramAnonymousContext)
        {
          paramAnonymousContext.printStackTrace();
          return;
        }
        if ("com.example.bluetooth.le.ACTION_GATT_DISCONNECTED".equals(paramAnonymousContext))
        {
          MainActivity.this.mConnected = false;
          MainActivity.this.invalidateOptionsMenu();
          return;
        }
        if ("com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED".equals(paramAnonymousContext))
        {
          MainActivity.mBluetoothLeService.enableTXNotification();
          Log.e(MainActivity.TAG, "gatt_services_discovered");
          return;
        }
      } while (!"com.example.bluetooth.le.ACTION_DATA_AVAILABLE".equals(paramAnonymousContext));
      Log.e(MainActivity.TAG, "data_available");
      MainActivity.this.dataprocess(paramAnonymousIntent.getStringExtra("com.example.bluetooth.le.EXTRA_DATA"));
    }
  };

  // Bluetooth Initalization
  private final ServiceConnection mServiceConnection = new ServiceConnection()
  {
    public void onServiceConnected(ComponentName paramAnonymousComponentName, IBinder paramAnonymousIBinder)
    {
      MainActivity.mBluetoothLeService = ((BluetoothLeService.LocalBinder)paramAnonymousIBinder).getService();
      if (!MainActivity.mBluetoothLeService.initialize())
      {
        Log.e(MainActivity.TAG, "Unable to initialize Bluetooth");
        MainActivity.this.finish();
      }
      if (MainActivity.mBluetoothLeService.connect(MainActivity.this.mDeviceAddress)) {
        Log.e(MainActivity.TAG, "BL initialization done");
      }
    }
    
    public void onServiceDisconnected(ComponentName paramAnonymousComponentName)
    {
      MainActivity.mBluetoothLeService = null;
    }
  };

  int pedo_cnt;
  int pedo_h;
  int pedo_intr_buf = 0;
  int pedo_l;

  //
  public int rd_out_num = 0;
  
  // Displays all the GATT devices available to connect with and displays their parameters
  private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener()
  {
    public boolean onChildClick(ExpandableListView paramAnonymousExpandableListView, View paramAnonymousView, int paramAnonymousInt1, int paramAnonymousInt2, long paramAnonymousLong)
    {
      if (MainActivity.mGattCharacteristics != null)
      {
        paramAnonymousExpandableListView = (BluetoothGattCharacteristic)((ArrayList)MainActivity.mGattCharacteristics.get(paramAnonymousInt1)).get(paramAnonymousInt2);
        paramAnonymousInt1 = paramAnonymousExpandableListView.getProperties();
        if ((paramAnonymousInt1 | 0x2) > 0)
        {
          if (MainActivity.mNotifyCharacteristic != null)
          {
            MainActivity.mBluetoothLeService.setCharacteristicNotification(MainActivity.mNotifyCharacteristic, false);
            MainActivity.mNotifyCharacteristic = null;
          }
          MainActivity.mBluetoothLeService.readCharacteristic(paramAnonymousExpandableListView);
        }
        if ((paramAnonymousInt1 | 0x10) > 0)
        {
          MainActivity.mNotifyCharacteristic = paramAnonymousExpandableListView;
          MainActivity.mBluetoothLeService.setCharacteristicNotification(paramAnonymousExpandableListView, true);
        }
        return true;
      }
      return false;
    }
  };

  //
  public byte setup_value = 0;
  private int shake_counters = 0;
  private TextView shake_state;
  String status = "IDLE";
  private int step_counters = 0;
  TextView temp_degree;
  public int temp_high = 0;
  public int temp_low = 0;
  public int test_start = 0;
  TextView textview_log;
  
  static
  {
    rdata_state = 20;
    key_words = "";
    rd_addr = 0;
    progress = 1;
    rd_once = 51;
    rd_total = 510;
    mGattCharacteristics = new ArrayList();
    ram_addr = 4096;
    rd_len = 0;
    rcv_data = 0;
    len_cnt = 0;
    ir_end = 0;
    data_head_en = 0;
  }
  
  public static void SendData(byte[] paramArrayOfByte, int paramInt)
  {
    byte[] arrayOfByte = new byte[paramInt];
    Log.e(TAG, "sendata flow \n");
    int i = 0;
    for (;;)
    {
      if (i >= paramInt)
      {
        mBluetoothLeService.writeRXCharacteristic(arrayOfByte);
        return;
      }
      arrayOfByte[i] = paramArrayOfByte[i];
      Log.e(TAG, String.format("%02x ", new Object[] { Byte.valueOf(arrayOfByte[i]) }));
      i += 1;
    }
  }
  
  private void clearUI()
  {
    this.mGattServicesList.setAdapter(null);
  }
  
  private void dataprocess(String paramString)
  {
    paramString = replaceBlank(paramString);
    int n = 0;
    int m = 0;
    int i2 = 0;
    int i1 = 0;
    int k = 0;
    int j = 0;
    String[] arrayOfString = paramString.split("[|]");
    int i;
    if ((paramString != "") && (this.test_start == 1))
    {
      Log.e(TAG, "process input here");
      i = 0;
      if (i < arrayOfString.length) {
        break label256;
      }
      switch (arrayOfString.length)
      {
      default: 
        Log.e(TAG, "case len default");
        i = 0;
        j = 0;
        label112:
        Log.e(TAG, "data_type" + String.format("%02x ", new Object[] { Integer.valueOf(i) }));
        Log.e(TAG, "data_value" + String.format("%02x ", new Object[] { Integer.valueOf(j) }));
        if ((i == 128) && (rdata_state != 20)) {
          this.pedo_intr_buf = 1;
        }
        if ((i == 128) && (rdata_state == 20)) {
          reg_rd(1, (byte)0, (byte)4);
        }
        break;
      }
    }
    label256:
    while (i != 64) {
      try
      {
        Thread.sleep(10L);
        rdata_state = 1;
        Log.e(TAG, "Intr process interrupt received\n");
        return;
        Log.e(TAG, "the data elements:" + arrayOfString[i]);
        i += 1;
        break;
        Log.e(TAG, "case len1");
        if ((Integer.parseInt(arrayOfString[0]) & 0xFF) == 128)
        {
          if (rdata_state == 20)
          {
            i = Integer.parseInt(arrayOfString[0]) & 0xFF;
            break label112;
          }
          this.pedo_intr_buf = 1;
          i = i2;
          break label112;
        }
        i = i2;
        if (this.data_head != 1) {
          break label112;
        }
        i = 64;
        j = Integer.parseInt(arrayOfString[0]) & 0xFF;
        this.data_head = 0;
        break label112;
        Log.e(TAG, "case len2");
        m = n;
        k = i1;
        if (rdata_state != 20)
        {
          m = n;
          k = i1;
          if ((Integer.parseInt(arrayOfString[0]) & 0xFF) == 64)
          {
            m = Integer.parseInt(arrayOfString[0]) & 0xFF;
            k = Integer.parseInt(arrayOfString[1]) & 0xFF;
          }
        }
        i = m;
        j = k;
        if (rdata_state == 20) {
          break label112;
        }
        i = m;
        j = k;
        if ((Integer.parseInt(arrayOfString[0]) & 0xFF) != 128) {
          break label112;
        }
        this.pedo_intr_buf = 1;
        i = m;
        j = k;
        if ((Integer.parseInt(arrayOfString[1]) & 0xFF) != 64) {
          break label112;
        }
        this.data_head = 1;
        i = 0;
        j = k;
        break label112;
        Log.e(TAG, "case len3");
        i = i2;
        if (rdata_state == 20) {
          break label112;
        }
        this.pedo_intr_buf = 1;
        i = m;
        j = k;
        if ((Integer.parseInt(arrayOfString[0]) & 0xFF) == 128)
        {
          i = Integer.parseInt(arrayOfString[1]) & 0xFF;
          j = Integer.parseInt(arrayOfString[2]) & 0xFF;
        }
        if ((Integer.parseInt(arrayOfString[2]) & 0xFF) != 128) {
          break label112;
        }
        i = Integer.parseInt(arrayOfString[0]) & 0xFF;
        j = Integer.parseInt(arrayOfString[1]) & 0xFF;
      }
      catch (InterruptedException paramString)
      {
        for (;;)
        {
          paramString.printStackTrace();
        }
      }
    }
    Log.e(TAG, "process data received");
    switch (rdata_state)
    {
    default: 
      Log.e(TAG, "case default");
      key_words = "";
      rdata_state = 20;
    }
    while ((this.pedo_intr_buf == 1) && (rdata_state == 20))
    {
      reg_rd(1, (byte)0, (byte)4);
      rdata_state = 1;
      this.pedo_intr_buf = 0;
      Log.e(TAG, "Buf process interrupt received\n");
      return;
      Log.e(TAG, "case 30");
      if ((j & 0x1) != 0)
      {
        reg_wr_byte((byte)1, (byte)19, (byte)0);
        reg_wr_byte((byte)1, (byte)20, (byte)0);
        reg_wr_byte((byte)1, (byte)21, (byte)0);
        reg_wr_byte((byte)1, (byte)17, (byte)0);
        reg_wr_byte((byte)1, (byte)17, (byte)-29);
        reg_wr_byte((byte)10, (byte)1, (byte)Byte.MIN_VALUE);
        reg_wr_byte((byte)10, (byte)1, (byte)1);
        reg_wr_byte((byte)0, (byte)6, (byte)111);
        reg_wr_byte((byte)1, (byte)1, (byte)3);
        reg_wr_byte((byte)8, (byte)1, (byte)1);
        rdata_state = 20;
      }
      else
      {
        reg_rd(1, (byte)1, (byte)2);
        rdata_state = 30;
        continue;
        Log.e(TAG, "case 1");
        if ((j & 0xFF) == 144)
        {
          reg_rd(1, (byte)10, (byte)2);
          rdata_state = 50;
        }
        else if ((j & 0xFF) == 16)
        {
          reg_rd(1, (byte)8, (byte)8);
          rdata_state = 10;
        }
        else if ((j & 0xFF) == 128)
        {
          reg_rd(1, (byte)10, (byte)2);
          rdata_state = 40;
          continue;
          Log.e(TAG, "case 10");
          this.data_20s_value.setText(String.format("%d", new Object[] { Integer.valueOf(j) }));
          reg_rd(1, (byte)8, (byte)9);
          rdata_state = 11;
          continue;
          Log.e(TAG, "case 10");
          this.data_60s_value.setText(String.format("%d", new Object[] { Integer.valueOf(j) }));
          rdata_state = 20;
          continue;
          Log.e(TAG, "case 40");
          this.temp_high = (j & 0xFF);
          reg_rd(1, (byte)10, (byte)3);
          rdata_state = 41;
          continue;
          Log.e(TAG, "case 41");
          this.temp_low = (j & 0xFF);
          double d2 = this.temp_high * 256 + (this.temp_low & 0xF0);
          double d1 = d2;
          if (d2 > Math.pow(2.0D, 15.0D) - 1.0D) {
            d1 = d2 - Math.pow(2.0D, 16.0D);
          }
          this.temp_degree.setText(String.format("%4.1f", new Object[] { Double.valueOf(0.0625D * d1 / 8.0D) }));
          rdata_state = 20;
          continue;
          Log.e(TAG, "case 50");
          this.temp_high = (j & 0xFF);
          reg_rd(1, (byte)10, (byte)3);
          rdata_state = 51;
          continue;
          Log.e(TAG, "case 51");
          this.temp_low = (j & 0xFF);
          d2 = this.temp_high * 256 + (this.temp_low & 0xF0);
          d1 = d2;
          if (d2 > Math.pow(2.0D, 15.0D) - 1.0D) {
            d1 = d2 - Math.pow(2.0D, 16.0D);
          }
          this.temp_degree.setText(String.format("%4.1f", new Object[] { Double.valueOf(0.0625D * d1 / 8.0D) }));
          reg_rd(1, (byte)8, (byte)8);
          rdata_state = 10;
        }
      }
    }
  }
  
  private void displayGattServices(List<BluetoothGattService> paramList)
  {
    if (paramList == null) {
      return;
    }
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    mGattCharacteristics = new ArrayList();
    paramList = paramList.iterator();
    label39:
    Object localObject2;
    Object localObject1;
    Object localObject3;
    if (paramList.hasNext())
    {
      localObject2 = (BluetoothGattService)paramList.next();
      localObject1 = new HashMap();
      ((HashMap)localObject1).put("UUID", ((BluetoothGattService)localObject2).getUuid().toString());
      localArrayList1.add(localObject1);
      localObject1 = new ArrayList();
      localObject3 = ((BluetoothGattService)localObject2).getCharacteristics();
      localObject2 = new ArrayList();
      localObject3 = ((List)localObject3).iterator();
    }
    for (;;)
    {
      if (!((Iterator)localObject3).hasNext())
      {
        mGattCharacteristics.add(localObject2);
        localArrayList2.add(localObject1);
        break label39;
        break;
      }
      BluetoothGattCharacteristic localBluetoothGattCharacteristic = (BluetoothGattCharacteristic)((Iterator)localObject3).next();
      ((ArrayList)localObject2).add(localBluetoothGattCharacteristic);
      HashMap localHashMap = new HashMap();
      localHashMap.put("UUID", localBluetoothGattCharacteristic.getUuid().toString());
      ((ArrayList)localObject1).add(localHashMap);
      Log.e(TAG, "Good news:sensor data in");
      int i = localBluetoothGattCharacteristic.getProperties();
      if ((i | 0x2) > 0)
      {
        if (mNotifyCharacteristic != null)
        {
          mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
          mNotifyCharacteristic = null;
        }
        mBluetoothLeService.readCharacteristic(localBluetoothGattCharacteristic);
      }
      if ((i | 0x10) > 0)
      {
        mNotifyCharacteristic = localBluetoothGattCharacteristic;
        mBluetoothLeService.setCharacteristicNotification(localBluetoothGattCharacteristic, true);
      }
    }
  }
  
  private static IntentFilter makeGattUpdateIntentFilter()
  {
    IntentFilter localIntentFilter = new IntentFilter();
    localIntentFilter.addAction("com.example.bluetooth.le.ACTION_GATT_CONNECTED");
    localIntentFilter.addAction("com.example.bluetooth.le.ACTION_GATT_DISCONNECTED");
    localIntentFilter.addAction("com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED");
    localIntentFilter.addAction("com.example.bluetooth.le.ACTION_DATA_AVAILABLE");
    return localIntentFilter;
  }
  
  static void mult_boot(int paramInt)
  {
    reg_wr_byte(, (byte)8, (byte)(paramInt & 0xFF));
    reg_wr_byte((byte)0, (byte)10, (byte)90);
    reg_wr_byte((byte)0, (byte)9, (byte)1);
  }
  
  public static void reg(int paramInt, byte paramByte1, byte paramByte2)
  {
    writedata(new byte[] { -1, -1, 6, paramByte1, paramByte2, (byte)((0xFF00 & paramInt) >> 8), (byte)(paramInt & 0xFF), 0 }, 8);
  }
  
  public static void reg_rd(int paramInt, byte paramByte1, byte paramByte2)
  {
    writedata(new byte[] { 1, paramByte1, paramByte2, (byte)((0xFF00 & paramInt) >> 8), (byte)(paramInt & 0xFF) }, 5);
  }
  
  public static void reg_rd_byte(byte paramByte1, byte paramByte2)
  {
    writedata(new byte[] { 1, paramByte1, paramByte2, 0, 1 }, 5);
  }
  
  public static void reg_rd_fixed_addr(int paramInt, byte paramByte1, byte paramByte2)
  {
    writedata(new byte[] { 3, paramByte1, paramByte2, (byte)((0xFF00 & paramInt) >> 8), (byte)(paramInt & 0xFF) }, 5);
  }
  
  public static void reg_wr(int paramInt, byte paramByte1, byte paramByte2, byte[] paramArrayOfByte)
  {
    byte[] arrayOfByte = new byte[paramInt + 5];
    arrayOfByte[0] = 0;
    arrayOfByte[1] = paramByte1;
    arrayOfByte[2] = paramByte2;
    arrayOfByte[3] = ((byte)((0xFF00 & paramInt) >> 8));
    arrayOfByte[4] = ((byte)(paramInt & 0xFF));
    int i = 0;
    for (;;)
    {
      if (i >= paramInt)
      {
        writedata(arrayOfByte, paramInt + 5);
        return;
      }
      arrayOfByte[(i + 5)] = paramArrayOfByte[i];
      i += 1;
    }
  }
  
  public static void reg_wr_burst(int paramInt, byte paramByte1, byte paramByte2, byte[] paramArrayOfByte)
  {
    byte[] arrayOfByte = new byte[paramInt + 7];
    arrayOfByte[0] = -1;
    arrayOfByte[1] = -1;
    arrayOfByte[2] = 5;
    arrayOfByte[3] = paramByte1;
    arrayOfByte[4] = paramByte2;
    arrayOfByte[5] = ((byte)((0xFF00 & paramInt) >> 8));
    arrayOfByte[6] = ((byte)(paramInt & 0xFF));
    int i = 0;
    for (;;)
    {
      if (i >= paramInt)
      {
        writedata(arrayOfByte, paramInt + 7);
        return;
      }
      arrayOfByte[(i + 7)] = paramArrayOfByte[i];
      i += 1;
    }
  }
  
  public static void reg_wr_byte(byte paramByte1, byte paramByte2, byte paramByte3)
  {
    writedata(new byte[] { 0, paramByte1, paramByte2, 0, 1, paramByte3 }, 6);
  }
  
  public static String replaceBlank(String paramString)
  {
    String str = "";
    if (paramString != null) {
      str = Pattern.compile("\\s*|\t|\r|\n|^\\s*|\\s*$").matcher(paramString).replaceAll("");
    }
    return str;
  }
  
  private void updateConnectionState(final int paramInt)
  {
    runOnUiThread(new Runnable()
    {
      public void run()
      {
        MainActivity.this.mConnectionState.setText(paramInt);
      }
    });
  }
  
  public static void writedata(byte[] paramArrayOfByte, int paramInt)
  {
    byte[] arrayOfByte = new byte[BLE_MAX_BYTES_PER_TRANSMISSION];
    int k = paramInt / BLE_MAX_BYTES_PER_TRANSMISSION;
    int i = 0;
    if (i >= k) {
      if (paramInt % BLE_MAX_BYTES_PER_TRANSMISSION != 0)
      {
        i = paramInt - BLE_MAX_BYTES_PER_TRANSMISSION * k;
        paramInt = 0;
      }
    }
    for (;;)
    {
      if (paramInt >= i)
      {
        SendData(arrayOfByte, i);
        return;
        int j = 0;
        for (;;)
        {
          if (j >= 20)
          {
            SendData(arrayOfByte, BLE_MAX_BYTES_PER_TRANSMISSION);
            i += 1;
            break;
          }
          arrayOfByte[j] = paramArrayOfByte[(BLE_MAX_BYTES_PER_TRANSMISSION * i + j)];
          j += 1;
        }
      }
      arrayOfByte[paramInt] = paramArrayOfByte[(BLE_MAX_BYTES_PER_TRANSMISSION * k + paramInt)];
      paramInt += 1;
    }
  }
  
  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    this.mDeviceAddress = getIntent().getStringExtra("DEVICE_ADDRESS");
    getActionBar().setDisplayHomeAsUpEnabled(true);
    Log.e(TAG, "start Main Activity");
    bindService(new Intent(this, BluetoothLeService.class), this.mServiceConnection, 1);
    setContentView(2130903042);
    paramBundle = (ToggleButton)findViewById(2131230731);
    Button localButton = (Button)findViewById(2131230735);
    this.data_20s_value = ((TextView)findViewById(2131230732));
    this.data_60s_value = ((TextView)findViewById(2131230733));
    this.temp_degree = ((TextView)findViewById(2131230734));
    rdata_state = 20;
    paramBundle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      public void onCheckedChanged(CompoundButton paramAnonymousCompoundButton, boolean paramAnonymousBoolean)
      {
        if (paramAnonymousBoolean)
        {
          Log.e(MainActivity.TAG, "Toggle button checked on pressed");
          MainActivity.reg_wr_byte((byte)1, (byte)1, (byte)Byte.MIN_VALUE);
          MainActivity.reg_wr_byte((byte)1, (byte)1, (byte)1);
          MainActivity.reg_rd(1, (byte)1, (byte)2);
          MainActivity.this.test_start = 1;
          MainActivity.rdata_state = 30;
          return;
        }
        MainActivity.this.data_20s_value.setText("");
        MainActivity.this.data_60s_value.setText("");
        MainActivity.this.temp_degree.setText("");
        MainActivity.this.test_start = 0;
        MainActivity.rdata_state = 0;
        MainActivity.this.pedo_intr_buf = 0;
        MainActivity.reg_rd(1, (byte)0, (byte)4);
        MainActivity.reg_wr_byte((byte)8, (byte)1, (byte)Byte.MIN_VALUE);
        MainActivity.reg_wr_byte((byte)10, (byte)1, (byte)Byte.MIN_VALUE);
        MainActivity.reg_wr_byte((byte)1, (byte)19, (byte)0);
        MainActivity.reg_wr_byte((byte)1, (byte)20, (byte)0);
        MainActivity.reg_wr_byte((byte)1, (byte)21, (byte)1);
        MainActivity.reg_wr_byte((byte)1, (byte)17, (byte)0);
        MainActivity.reg_wr_byte((byte)1, (byte)17, (byte)-29);
      }
    });
    localButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        MainActivity.this.startActivity(this.val$debug_intent);
        MainActivity.this.test_start = 2;
        MainActivity.key_words = "";
        MainActivity.this.rd_out_num = 0;
      }
    });
  }
  
  public boolean onCreateOptionsMenu(Menu paramMenu)
  {
    getMenuInflater().inflate(2131165184, paramMenu);
    if (this.mConnected)
    {
      paramMenu.findItem(2131230750).setVisible(false);
      paramMenu.findItem(2131230751).setVisible(true);
      return true;
    }
    paramMenu.findItem(2131230750).setVisible(true);
    paramMenu.findItem(2131230751).setVisible(false);
    return true;
  }
  
  protected void onDestroy()
  {
    super.onDestroy();
    mBluetoothLeService = null;
  }
  
  public boolean onOptionsItemSelected(MenuItem paramMenuItem)
  {
    switch (paramMenuItem.getItemId())
    {
    default: 
      return super.onOptionsItemSelected(paramMenuItem);
    case 2131230750: 
      mBluetoothLeService.connect(this.mDeviceAddress);
      return true;
    case 2131230751: 
      mBluetoothLeService.disconnect();
      return true;
    }
    onBackPressed();
    return true;
  }
  
  protected void onPause()
  {
    super.onPause();
    Log.e(TAG, "on-pause status");
  }
  
  protected void onResume()
  {
    super.onResume();
    registerReceiver(this.mGattUpdateReceiver, makeGattUpdateIntentFilter());
    if (mBluetoothLeService != null)
    {
      boolean bool = mBluetoothLeService.connect(this.mDeviceAddress);
      Log.d(TAG, "Connect request result=" + bool);
      Log.e(TAG, "Main Activity: onResume");
    }
    Log.e(TAG, "Outisde of if Main Activity: onResume");
  }
  
  // This is writing a fixed adress to the register. (jajajaja)
  public void reg_wr_fixed_addr(int paramInt, byte paramByte1, byte paramByte2, byte[] paramArrayOfByte)
  {
    byte[] arrayOfByte = new byte[paramInt + 5];
    arrayOfByte[0] = 2;
    arrayOfByte[1] = paramByte1;
    arrayOfByte[2] = paramByte2;
    arrayOfByte[3] = ((byte)((0xFF00 & paramInt) >> 8));
    arrayOfByte[4] = ((byte)(paramInt & 0xFF));
    int i = 0;
    for (;;)
    {
      if (i >= paramInt)
      {
        writedata(arrayOfByte, paramInt + 5);
        return;
      }
      arrayOfByte[(i + 5)] = paramArrayOfByte[i];
      i += 1;
    }
  }
  

  // Save data to a sd card, we dont have that, do we nee this method? Will it break if we take it out? Also ouputs to log.txt
  public void savedata(String paramString1, String paramString2)
  {
    FileService localFileService = new FileService();
    Log.e(TAG, "save file to disk\n");
    localFileService.saveToSDCard(paramString2, paramString1, 1);
  }


  // Accesses Storage device to save log I think, clay is confused by the False and True Null of Madness
  public static class FileService
  {
    private static final Boolean FALSE = null;
    private static final Boolean TRUE = null;
    
    public Boolean existfile(String paramString)
    {
      if (!new File(Environment.getExternalStorageDirectory(), paramString).exists()) {
        return TRUE;
      }
      return FALSE;
    }
    
    //Read a SD file, ja das guten
    public String readSDFile(String paramString)
      throws IOException
    {
      paramString = new FileInputStream(new File(Environment.getExternalStorageDirectory(), paramString));
      Object localObject = new byte[paramString.available()];
      paramString.read((byte[])localObject);
      localObject = EncodingUtils.getString((byte[])localObject, "UTF-8");
      paramString.close();
      return (String)localObject;
    }
    
    public void saveToRom(String paramString1, String paramString2) {}
    
    public void saveToSDCard(String paramString1, String paramString2, int paramInt)
    {
      Object localObject4 = null;
      Object localObject3 = null;
      Object localObject2 = localObject3;
      Object localObject1 = localObject4;
      for (;;)
      {
        try
        {
          paramString1 = new File(Environment.getExternalStorageDirectory(), paramString1);
          if (paramInt == 1)
          {
            localObject2 = localObject3;
            localObject1 = localObject4;
            paramString1 = new FileOutputStream(paramString1, false);
            localObject2 = paramString1;
            localObject1 = paramString1;
            paramString1.write(paramString2.getBytes());
          }
        }
        catch (Exception paramString1)
        {
          localObject1 = localObject2;
          paramString1.printStackTrace();
          try
          {
            ((FileOutputStream)localObject2).close();
            return;
          }
          catch (IOException paramString1)
          {
            paramString1.printStackTrace();
            return;
          }
        }
        finally
        {
          try
          {
            ((FileOutputStream)localObject1).close();
            throw paramString1;
          }
          catch (IOException paramString2)
          {
            paramString2.printStackTrace();
            continue;
          }
        }
        try
        {
          paramString1.close();
          return;
        }
        catch (IOException paramString1)
        {
          paramString1.printStackTrace();
        }
        localObject2 = localObject3;
        localObject1 = localObject4;
        paramString1 = new FileOutputStream(paramString1, true);
      }
    }
  }
}


/* Location:              C:\Users\Grant\Desktop\ConversionFolder\output2.jar!\com\example\HeartRate\MainActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */