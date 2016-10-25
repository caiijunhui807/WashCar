package usb;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Administrator on 2016/9/12.
 */
    public class UsbDeviceService extends Service {

    public static final String ACTION_RECEIVE_USB_DATA = "ReceiveUsbData";
    public static final String ACTION_SEND_USB_DATA = "SendUsbData";
    //管理Usb连接的客户端
    private UsbHostClient mUsbHostClient;
    private final IBinder mBinder = new LocalBinder();
    @Nullable


    private BroadcastReceiver mUsbBroadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(ACTION_SEND_USB_DATA)){
                //发送数据到usb设备
                byte[] data = intent.getByteArrayExtra("data");
                if(data != null){
                    mUsbHostClient.write(data);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter =new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_SEND_USB_DATA);
        registerReceiver(mUsbBroadcastReceiver, filter);
        mUsbHostClient = new UsbHostClient(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mUsbHostClient.releaseConnect();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mUsbBroadcastReceiver != null) unregisterReceiver(mUsbBroadcastReceiver);
        mUsbHostClient.releaseConnect();
    }
    public class LocalBinder extends Binder {
        public UsbDeviceService getService() {
            return UsbDeviceService.this;
        }
    }
}
