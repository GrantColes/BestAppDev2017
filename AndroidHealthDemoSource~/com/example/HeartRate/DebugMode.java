package com.example.HeartRate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DebugMode
  extends Activity
{
  private static final String TAG = "VoiceDebug";
  static int command1_flag;
  static int command2_flag;
  static String debug_log = "";
  public static int debug_mode = 0;
  public static int fea_flag = 0;
  static int fea_len = 0;
  static int new1_flag;
  static int new2_flag;
  
  static
  {
    command1_flag = 0;
    command2_flag = 0;
    new1_flag = 0;
    new2_flag = 0;
  }
  
  private void Save2file(String paramString1, String paramString2)
  {
    Log.e("VoiceDebug", "save2file the data is " + paramString1);
    paramString1 = MainActivity.replaceBlank(paramString1).split(" ");
    int i = 0;
    for (;;)
    {
      if (i >= paramString1.length) {
        return;
      }
      i += 1;
    }
  }
  
  public void onCreate(final Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    Log.e("VoiceDebug", "enter Voice Debug activity");
    setContentView(2130903041);
    paramBundle = (EditText)findViewById(2131230721);
    final EditText localEditText1 = (EditText)findViewById(2131230722);
    final EditText localEditText2 = (EditText)findViewById(2131230723);
    final TextView localTextView = (TextView)findViewById(2131230728);
    Button localButton1 = (Button)findViewById(2131230724);
    Button localButton2 = (Button)findViewById(2131230725);
    Button localButton3 = (Button)findViewById(2131230726);
    Button localButton4 = (Button)findViewById(2131230720);
    new Intent(this, MainActivity.class);
    localButton4.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        Log.e("VoiceDebug", "button_finish pressed");
        DebugMode.debug_mode = 0;
        DebugMode.this.finish();
      }
    });
    localButton2.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        Log.e("VoiceDebug", "debug data input before");
        DebugMode.debug_mode = 1;
        byte b1 = (byte)(Integer.parseInt(paramBundle.getText().toString(), 16) & 0xFF);
        byte b2 = (byte)(Integer.parseInt(localEditText1.getText().toString(), 16) & 0xFF);
        byte b3 = (byte)(Integer.parseInt(localEditText2.getText().toString(), 16) & 0xFF);
        Log.e("VoiceDebug", "debug data input successfully");
        MainActivity.reg_wr_byte(b1, b2, b3);
        DebugMode.debug_log = DebugMode.debug_log + "write_reg " + String.format("%02x", new Object[] { Byte.valueOf(b1) }) + String.format("%02x ", new Object[] { Byte.valueOf(b2) }) + "value: " + String.format("%02x ", new Object[] { Byte.valueOf(b3) }) + "\n";
        localTextView.setText(DebugMode.debug_log);
      }
    });
    localButton1.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        BluetoothLeService.data_resp = 0;
        DebugMode.debug_mode = 1;
        byte b1 = (byte)(Integer.parseInt(paramBundle.getText().toString(), 16) & 0xFF);
        byte b2 = (byte)(Integer.parseInt(localEditText1.getText().toString(), 16) & 0xFF);
        int i = (byte)(Integer.parseInt(localEditText2.getText().toString(), 16) & 0xFF);
        MainActivity.reg_rd(1, b1, b2);
        while (BluetoothLeService.data_resp == 0) {}
        BluetoothLeService.data_resp = 0;
        DebugMode.debug_log = DebugMode.debug_log + "read_reg " + String.format("%02x", new Object[] { Byte.valueOf(b1) }) + String.format("%02x ", new Object[] { Byte.valueOf(b2) }) + "value: " + String.format("%02x   ", new Object[] { Byte.valueOf(BluetoothLeService.read_value) }) + String.format("%d ", new Object[] { Byte.valueOf(BluetoothLeService.read_value) }) + "\n";
        localTextView.setText(DebugMode.debug_log);
      }
    });
    localButton3.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        DebugMode.debug_log = "";
        localTextView.setText(DebugMode.debug_log);
      }
    });
  }
}


/* Location:              C:\Users\Grant\Desktop\ConversionFolder\output2.jar!\com\example\HeartRate\DebugMode.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */