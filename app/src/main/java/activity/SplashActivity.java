package activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Iterator;

import caijh.jinmaigao.com.washcar.R;
import activity.MainActivity;
import config.Constants;

/**
 * Created by Administrator on 2016/9/18.
 */
public class SplashActivity extends Activity {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);


        initConfig();

        checkUsbPermission();
    }


    private void initConfig() {

        //打开wifi
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mUsbReceiver);
            unregisterReceiver(mUsbGrantReceiver);
        } catch (Exception e) {
        }
    }


    private void startMainActivity() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                // usb设备拔掉

            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                // usb设备接入
                checkUsbPermission();
            }
        }
    };

    //在调用getUsb()时，申请USB设备授权时激发
    private BroadcastReceiver mUsbGrantReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 得到授权
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            UsbDeviceConnection connection = mUsbManager
                                    .openDevice(device);
                            if (connection == null) {
                                // usb出现问题

                            } else {
                                connection.close();
                                connection = null;
                                startMainActivity();
                            }
                        }
                    } else {
                        // 获取权限失败
                        checkUsbPermission();
                        return;
                    }
                }
            }
        }
    };

    /**
     * 检查是否有USB设备的连接权限，有则启动主界面，无继续搜索USB设备
     */
    public void checkUsbPermission() {
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice tmpDevice = deviceIterator.next();
            if (tmpDevice.getVendorId() == Constants.PL2303VendorId
                    && tmpDevice.getProductId() == Constants.PL2303ProductId) {
                UsbDeviceConnection connection = null;
                if (mUsbManager.hasPermission(tmpDevice)) {
                    connection = mUsbManager.openDevice(tmpDevice);
                }

                if (connection == null) {
                    // 授权
                    PendingIntent mPermissionIntent = PendingIntent
                            .getBroadcast(this, 0, new Intent(
                                    ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(
                            ACTION_USB_PERMISSION);
                    // 注册 USB 授权通知器，如果得到授权，而有消息,从而打开设备
                    registerReceiver(mUsbGrantReceiver, filter);
                    // 申请授权,等到 mUsbReceiver 成功则创建 driver
                    mUsbManager.requestPermission(tmpDevice, mPermissionIntent);
                } else {
                    // 成功获取usb管理权限
                    startMainActivity();
                }
            } else if (tmpDevice.getVendorId() == Constants.CH340VendorId
                    && tmpDevice.getProductId() == Constants.CH340ProductId) {
                UsbDeviceConnection connection = null;
                if (mUsbManager.hasPermission(tmpDevice)) {
                    connection = mUsbManager.openDevice(tmpDevice);
                }

                if (connection == null) {
                    // 授权
                    PendingIntent mPermissionIntent = PendingIntent
                            .getBroadcast(this, 0, new Intent(
                                    ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(
                            ACTION_USB_PERMISSION);
                    // 注册 USB 授权通知器，如果得到授权，而有消息,从而打开设备
                    registerReceiver(mUsbGrantReceiver, filter);
                    // 申请授权,等到 mUsbReceiver 成功则创建 driver
                    mUsbManager.requestPermission(tmpDevice, mPermissionIntent);
                } else {
                    // 成功获取usb管理权限
                    startMainActivity();
                }
            }
        }
    }

}

