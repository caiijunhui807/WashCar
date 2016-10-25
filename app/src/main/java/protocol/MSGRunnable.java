package protocol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;



import java.util.concurrent.ConcurrentLinkedQueue;

import utils.ByteUtils;
import utils.MyUtils;

public class MSGRunnable implements Runnable {
    private ConcurrentLinkedQueue<Byte> messages;
    private byte[] order = new byte[0];
    private Handler handler;


    public MSGRunnable(ConcurrentLinkedQueue<Byte> messages, Handler handler) {
        this.messages = messages;
        this.handler = handler;
    }

    @Override
    public void run() {
        synchronized (messages) {

            while (messages.isEmpty() == false) {

                // 轮询消息messages
                byte item = messages.poll();
                byte[] newData = {item};
                if (item == (byte) 0xaa) {
                    order = ByteUtils.uniteBytes(order, newData);
                    if(order[0]==(byte)0xee && order[1]==0x0a && order.length==13){
                        if(MyUtils.chekSum(order) ==order[11] && order[12]== (byte) 0xaa){
                            //校验成功

                            Bundle bundle=new Bundle();
                            bundle.putByte("key",order[7]);
                            bundle.putByte("error1",order[4]);
                            bundle.putByte("error2",order[5]);
                            bundle.putByte("error3",order[6]);
                            Message msg=handler.obtainMessage();
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    }
                    order = new byte[0];
                    return;
                }
                order = ByteUtils.uniteBytes(order, newData);
            }

        }

    }

}
