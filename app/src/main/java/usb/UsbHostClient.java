package usb;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import config.Constants;

/**
 * Created by Administrator on 2016/9/12.
 */
public class UsbHostClient {
    //设备已连接
    public final static int STATE_CONNECTED = 1;
    //设备已断开
    public final static int STATE_DISCONNECTED = 2;
    //设备连接中
    public final static int STATE_CONNECTING = 3;
    //设备断开中
    public final static int STATE_DISCONNECTING = 4;

    public final static int SEARCH_DEVICE_FINISH = 200;

    //baudRate,参考UsbPortLibs中的参数
    public final static int BYTES = 9600;
    //dataBits
    public final static int BYTE_BITS = 8;

    //Usb连接状态
    private int mConnectState = STATE_DISCONNECTED;

    //USB串行端口
    private UsbSerialPort mSerialPort;
    //USB输入输出流管理器
    private SerialInputOutputManager mSerialIoManager;

    private UsbInterface mInterface;
    private UsbManager mUsbManager;
    private UsbDeviceConnection mDeviceConnection;

    public List<UsbSerialPort> mSerialPorts = new ArrayList<UsbSerialPort>();

    //搜索设备代码
    private SearchDeviceRunnable mRunnable;
    private ExecutorService mExecutor = Executors
            .newSingleThreadExecutor();

    private Context mContext;


    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            //搜索设备任务完成后，连接设备
            if(msg.what == SEARCH_DEVICE_FINISH){
                connect(mContext);
            }
        };
    };

    public UsbHostClient(Context context){
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mContext = context;
        //执行搜索Usb设备
        performSearchDevice();
    }

    /**
     * 搜索USB连接设备，搜索到后进行连接
     */
    public void performSearchDevice() {
        if (mRunnable != null) {
            if (mRunnable.isRun()) {
                return;
            }
        }
        mRunnable = new SearchDeviceRunnable();
        new Thread(mRunnable).start();
    }

    /**
     * 连接一个Usb设备
     */
    public void connect(Context context) {
        if (mSerialPort != null) {
            releaseConnect();
            return;
        }
        //连接中
        setConnectState(STATE_CONNECTING);

        if (mSerialPorts.size() == 0) {
            setConnectState(STATE_DISCONNECTED);
            return;
        }

        for (int m = 0; m < mSerialPorts.size(); m++) {
            UsbSerialPort port = mSerialPorts.get(m);
            //int i = m + 1;
            UsbSerialDriver driver = port.getDriver();
            UsbDevice device = driver.getDevice();
            // 必须为指定的usb设备
            if (device.getVendorId() != Constants.PL2303VendorId
                    || device.getProductId() != Constants.PL2303ProductId){
                if(device.getVendorId() == Constants.CH340VendorId
                        || device.getProductId() == Constants.CH340ProductId)
                {
//					Toast.makeText(context,device.getVendorId()+">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",Toast.LENGTH_SHORT).show();
                }else {
                    continue;
                }
            }
            if(device.getVendorId() != Constants.CH340VendorId
                    || device.getProductId() != Constants.CH340ProductId)
            {
                if(device.getVendorId() == Constants.PL2303VendorId
                        || device.getProductId() == Constants.PL2303ProductId)
                {
//					Toast.makeText(context,device.getVendorId()+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<",Toast.LENGTH_SHORT).show();
                }else {
                    continue;
                }
            }

            mDeviceConnection = mUsbManager.openDevice(device);
            if (mDeviceConnection == null) {
                //setMessage("第" + i + "设备打开连接失败");
                continue;
            }
            //默认为第一个
            if (device.getInterfaceCount() > 0)
                mInterface = device.getInterface(0);

            try {
                port.open(mDeviceConnection);
                //设置连接参数
                port.setParameters(BYTES, BYTE_BITS, UsbSerialPort.STOPBITS_1,
                        UsbSerialPort.PARITY_NONE);
                //setMessage("第" + i + "设备打开连接成功");
               // FileUtils.writeLogToFile("设备打开连接成功", "".getBytes());
                Toast.makeText(context, "设备打开连接成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                //Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                //setMessage("第" + i + "设备USB端口打开失败");
              //  FileUtils.writeLogToFile("设备打开连接失败", "".getBytes());
                try {
                    if (mDeviceConnection != null) {
                        if (mInterface != null)
                            mDeviceConnection.releaseInterface(mInterface);
                        mDeviceConnection.close();
                    }
                    port.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                mInterface = null;
                mDeviceConnection = null;
                continue;
            }

            this.mSerialPort = port;
            setConnectState(STATE_CONNECTED);
            stopIOManager();
            initIOManager();

            // 发送连接成功
            break;
        }

        if (mSerialPort == null) {
            setConnectState(STATE_DISCONNECTED);
        }
    }

    /**
     * 释放usb连接
     */
    public void releaseConnect(){
        setConnectState(STATE_DISCONNECTING);
        stopIOManager();
        if (mSerialPort != null) {
            try {
                if (mDeviceConnection != null) {
                    if (mInterface != null)
                        mDeviceConnection.releaseInterface(mInterface);
                    mDeviceConnection.close();
                }
                if (mSerialPort != null)
                    mSerialPort.close();
            } catch (IOException e) {
                // Ignore
                e.printStackTrace();
            }
            mSerialPort = null;
            mInterface = null;
            mDeviceConnection = null;
        }

        setConnectState(STATE_DISCONNECTED);
    }


    /**
     * 初始化设备Io管理器
     */
    private void initIOManager() {
        if (mSerialPort != null) {
            mSerialIoManager = new SerialInputOutputManager(mSerialPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void stopIOManager() {
        if (mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    /**
     * 向MCU端写字节数据
     * @param data
     * @throws IOException
     */
    public void write(byte[] data){
        write(data, 1000);
    }

    public void write(byte[] data,int timeoutMillis){
        if (mSerialPort != null){
            //写入日志到文件
         //   FileUtils.writeLogToFile(" data send to usb is-->", data);
            try {
                mSerialPort.write(data, timeoutMillis);
            } catch (IOException e) {
        //        FileUtils.printExceptionToFile(e,"data send to usb failed");
            }
        }

    }

    public int getConnectState() {
        return mConnectState;
    }

    public void setConnectState(int mConnectState) {
        this.mConnectState = mConnectState;
    }

    /**
     * USB数据接收监听接口
     */
    private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {

        @Override
        public void onRunError(Exception e) {
            releaseConnect();
        }

        @Override
        public void onNewData(final byte[] data) {
            //FileUtils.writeLogToFile("",data);
            //发布搜索完成事件
            Intent intent = new Intent(UsbDeviceService.ACTION_RECEIVE_USB_DATA);
            intent.putExtra("data",data);
            mContext.sendBroadcast(intent);
        }

    };


    private class SearchDeviceRunnable implements Runnable {
        private boolean run = false;

        @Override
        public void run() {
            //开始搜索
            run = true;
            final List<UsbSerialDriver> drivers = UsbSerialProber
                    .getDefaultProber().findAllDrivers(mUsbManager);
            final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
            for (final UsbSerialDriver driver : drivers) {
                final List<UsbSerialPort> ports = driver.getPorts();
                result.addAll(ports);
            }

            mSerialPorts.clear();
            mSerialPorts.addAll(result);
            //搜索完成
            run = false;
            mHandler.sendEmptyMessage(SEARCH_DEVICE_FINISH);
        }

        public boolean isRun() {
            return run;
        }

    }

}
