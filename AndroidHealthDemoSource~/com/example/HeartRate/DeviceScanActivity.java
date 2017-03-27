package com.example.HeartRate;

import android.app.ActionBar;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class DeviceScanActivity
  extends ListActivity
{
  private static final int REQUEST_ENABLE_BT = 1;
  private static final long SCAN_PERIOD = 1000L;
  private static final String TAG = "FirstDemo";
  String deviceName_temp;
  private BluetoothAdapter mBluetoothAdapter;
  private Handler mHandler;
  private LeDeviceListAdapter mLeDeviceListAdapter;
  private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
  {
    public void onLeScan(final BluetoothDevice paramAnonymousBluetoothDevice, int paramAnonymousInt, byte[] paramAnonymousArrayOfByte)
    {
      DeviceScanActivity.this.runOnUiThread(new Runnable()
      {
        public void run()
        {
          DeviceScanActivity.this.mLeDeviceListAdapter.addDevice(paramAnonymousBluetoothDevice);
          DeviceScanActivity.this.mLeDeviceListAdapter.notifyDataSetChanged();
        }
      });
    }
  };
  private boolean mScanning;
  
  private void scanLeDevice(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      this.mHandler.postDelayed(new Runnable()
      {
        public void run()
        {
          DeviceScanActivity.this.mScanning = false;
          DeviceScanActivity.this.mBluetoothAdapter.stopLeScan(DeviceScanActivity.this.mLeScanCallback);
          DeviceScanActivity.this.invalidateOptionsMenu();
        }
      }, 1000L);
      this.mScanning = true;
      Log.e("FirstDemo", "scan progress started\n");
      this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
    }
    for (;;)
    {
      invalidateOptionsMenu();
      return;
      this.mScanning = false;
      this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
    }
  }
  
  protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
  {
    if ((paramInt1 == 1) && (paramInt2 == 0))
    {
      finish();
      return;
    }
    super.onActivityResult(paramInt1, paramInt2, paramIntent);
  }
  
  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    getActionBar().setTitle(2131034119);
    this.mHandler = new Handler();
    if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth"))
    {
      Toast.makeText(this, 2131034118, 0).show();
      finish();
    }
    this.mBluetoothAdapter = ((BluetoothManager)getSystemService("bluetooth")).getAdapter();
    if (this.mBluetoothAdapter == null)
    {
      Toast.makeText(this, 2131034120, 0).show();
      finish();
    }
  }
  
  public boolean onCreateOptionsMenu(Menu paramMenu)
  {
    getMenuInflater().inflate(2131165185, paramMenu);
    if (!this.mScanning)
    {
      paramMenu.findItem(2131230753).setVisible(false);
      paramMenu.findItem(2131230752).setVisible(true);
      paramMenu.findItem(2131230749).setActionView(null);
      return true;
    }
    paramMenu.findItem(2131230753).setVisible(true);
    paramMenu.findItem(2131230752).setVisible(false);
    paramMenu.findItem(2131230749).setActionView(2130903040);
    return true;
  }
  
  protected void onListItemClick(ListView paramListView, View paramView, int paramInt, long paramLong)
  {
    paramListView = this.mLeDeviceListAdapter.getDevice(paramInt);
    if (paramListView == null) {
      return;
    }
    paramView = new Intent(this, MainActivity.class);
    paramView.putExtra("DEVICE_NAME", paramListView.getName());
    paramView.putExtra("DEVICE_ADDRESS", paramListView.getAddress());
    if (this.mScanning)
    {
      this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
      this.mScanning = false;
    }
    startActivity(paramView);
  }
  
  public boolean onOptionsItemSelected(MenuItem paramMenuItem)
  {
    switch (paramMenuItem.getItemId())
    {
    default: 
      return true;
    case 2131230752: 
      this.mLeDeviceListAdapter.clear();
      scanLeDevice(true);
      return true;
    }
    scanLeDevice(false);
    return true;
  }
  
  protected void onPause()
  {
    super.onPause();
    scanLeDevice(false);
    this.mLeDeviceListAdapter.clear();
  }
  
  protected void onResume()
  {
    super.onResume();
    if ((!this.mBluetoothAdapter.isEnabled()) && (!this.mBluetoothAdapter.isEnabled())) {
      startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
    }
    this.mLeDeviceListAdapter = new LeDeviceListAdapter();
    setListAdapter(this.mLeDeviceListAdapter);
    scanLeDevice(true);
  }
  
  private class LeDeviceListAdapter
    extends BaseAdapter
  {
    private LayoutInflater mInflator = DeviceScanActivity.this.getLayoutInflater();
    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList();
    
    public LeDeviceListAdapter() {}
    
    public void addDevice(BluetoothDevice paramBluetoothDevice)
    {
      if (!this.mLeDevices.contains(paramBluetoothDevice)) {
        this.mLeDevices.add(paramBluetoothDevice);
      }
    }
    
    public void clear()
    {
      this.mLeDevices.clear();
    }
    
    public int getCount()
    {
      return this.mLeDevices.size();
    }
    
    public BluetoothDevice getDevice(int paramInt)
    {
      return (BluetoothDevice)this.mLeDevices.get(paramInt);
    }
    
    public Object getItem(int paramInt)
    {
      return this.mLeDevices.get(paramInt);
    }
    
    public long getItemId(int paramInt)
    {
      return paramInt;
    }
    
    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
    {
      BluetoothDevice localBluetoothDevice;
      if (paramView == null)
      {
        paramView = this.mInflator.inflate(2130903045, null);
        paramViewGroup = new DeviceScanActivity.ViewHolder();
        paramViewGroup.deviceAddress = ((TextView)paramView.findViewById(2131230748));
        paramViewGroup.deviceName = ((TextView)paramView.findViewById(2131230747));
        paramView.setTag(paramViewGroup);
        localBluetoothDevice = (BluetoothDevice)this.mLeDevices.get(paramInt);
        String str = localBluetoothDevice.getName();
        if ((str == null) || (str.length() <= 0)) {
          break label121;
        }
        paramViewGroup.deviceName.setText(str);
      }
      for (;;)
      {
        paramViewGroup.deviceAddress.setText(localBluetoothDevice.getAddress());
        return paramView;
        paramViewGroup = (DeviceScanActivity.ViewHolder)paramView.getTag();
        break;
        label121:
        paramViewGroup.deviceName.setText(2131034121);
      }
    }
  }
  
  static class ViewHolder
  {
    TextView deviceAddress;
    TextView deviceName;
  }
}


/* Location:              C:\Users\Grant\Desktop\ConversionFolder\output2.jar!\com\example\HeartRate\DeviceScanActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */