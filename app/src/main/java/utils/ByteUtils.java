/*     */
package utils;
/*     */

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/*     */
/*     */
/*     */
/*     */

/*     */
/*     */ public class ByteUtils
/*     */ {
    public static String binary(byte[] bytes, int radix) {
        return new BigInteger(1, bytes).toString(radix);// 这里的1代表正数
    }

    /*     */
/*     */
    public static byte[] chatOrders(String c)
/*     */ {
/*  77 */
        byte[] m = c.getBytes();
/*  78 */
        if (m.length % 2 == 0) {
/*  79 */
            byte[] bytes = new byte[m.length / 2];
/*  80 */
            int i = 0;
            for (int j = 0; i < m.length; ++j) {
/*  81 */
                bytes[j] = uniteByte(m[i], m[(i + 1)]);
/*     */ 
/*  80 */
                i += 2;
/*     */
            }
/*     */ 
/*  83 */
            return bytes;
/*     */
        }
/*  85 */
        return null;
/*     */
    }

    /*     */
/*     */
    public static byte uniteByte(byte src0, byte src1)
/*     */ {
/*  90 */
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
/*  91 */
        _b0 = (byte) (_b0 << 4);
/*  92 */
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
/*  93 */
        byte ret = (byte) (_b0 ^ _b1);
/*  94 */
        return ret;
/*     */
    }
/*     */
///*     */   public static byte[] uniteBytes(byte[][] data)
///*     */   {
///* 104 */     int length = 0;
///* 105 */     for (byte[] msg : data) {
///* 106 */       length += msg.length;
///*     */     }
///* 108 */     byte[] newData = new byte[length];
///* 109 */     int i = 0;
///* 110 */     for (byte[] msg : data) {
///* 111 */       System.arraycopy(msg, 0, newData, i, msg.length);
///* 112 */       i += msg.length;
///*     */     }
///*     */
///* 115 */     return newData;
///*     */   }


    /**
     * 合并多个字节数组
     *
     * @param data
     * @return
     */
    public static byte[] uniteBytes(byte[]... data) {
        int length = 0;
        for (byte[] msg : data) {
            length += msg.length;
        }
        byte[] newData = new byte[length];
        int i = 0;
        for (byte[] msg : data) {
            System.arraycopy(msg, 0, newData, i, msg.length);
            i += msg.length;
        }

        return newData;
    }

    /*     */
/*     */
    public static byte[] intToByte(int number)
/*     */ {
/* 125 */
        byte[] abyte = new byte[4];
/*     */ 
/* 127 */
        abyte[3] = (byte) (0xFF & number);
/*     */ 
/* 129 */
        abyte[2] = (byte) ((0xFF00 & number) >> 8);
/* 130 */
        abyte[1] = (byte) ((0xFF0000 & number) >> 16);
/* 131 */
        abyte[0] = (byte) ((0xFF000000 & number) >> 24);
/* 132 */
        return abyte;
/*     */
    }

    /*     */
/*     */
    public static int byteArrayToInt(byte[] bytes)
/*     */ {
/* 142 */
        int value = 0;
/*     */ 
/* 144 */
        for (int i = 0; i < 4; ++i) {
/* 145 */
            int shift = (3 - i) * 8;
/* 146 */
            value += ((bytes[i] & 0xFF) << shift);
/*     */
        }
/* 148 */
        return value;
/*     */
    }

    /*     */
/*     */
    public static String formatOct(int value)
/*     */ {
/* 155 */
        if (value < 10) {
/* 156 */
            return "0" + value;
/*     */
        }
/*     */ 
/* 159 */
        return Integer.toString(value);
/*     */
    }

    /*     */
/*     */
    public static byte[] decodeByte(byte src)
/*     */ {
/* 170 */
        byte[] des = new byte[2];
/* 171 */
        des[0] = (byte) (src & 0xF);
/* 172 */
        des[1] = (byte) ((src & 0xF0) >> 4);
/* 173 */
        return des;
/*     */
    }

    /*     */
/*     */
    public static String decodeByteToHexString(byte src)
/*     */ {
/* 181 */
        byte[] des = new byte[2];
/* 182 */
        des[1] = (byte) (src & 0xF);
/* 183 */
        des[0] = (byte) ((src & 0xF0) >> 4);
/* 184 */
        return Integer.toHexString(des[0]) + Integer.toHexString(des[1]);
/*     */
    }
    public static String hexString2binaryString(String hexString)
    {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++)
        {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(hexString
                    .substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    /*     */
/*     */
    public static String decodeBytesToHexString(byte[] data)
/*     */ {
/* 194 */
        String result = new String();
/* 195 */
        for (byte dd : data) {
/* 196 */
            result = result.concat(decodeByteToHexString(dd));
/*     */
        }
/* 198 */
        return result;
/*     */
    }

    /*     */
/*     */
    public static String getbyteMsg(byte[] data)
/*     */ {
/* 208 */
        String msg = new String();
/* 209 */
        for (byte by : data) {
/* 210 */
            byte[] db = decodeByte(by);
/* 211 */
            msg.concat(getMsg(db[0], db[1]));
/*     */
        }
/* 213 */
        return msg;
/*     */
    }

    /*     */
/*     */
    public static String getMsg(byte one, byte two) {
/* 217 */
        byte[] data = {one, two};
/* 218 */
        String msg = new String(data, 0, data.length);
/* 219 */
        return msg;
/*     */
    }

    /*     */
/*     */
    public static byte uniteNumber(byte src0, byte src1)
/*     */ {
/* 224 */
        byte _b0 = src0;
/* 225 */
        _b0 = (byte) (_b0 << 4);
/* 226 */
        byte _b1 = src1;
/* 227 */
        byte ret = (byte) (_b0 ^ _b1);
/* 228 */
        return ret;
/*     */
    }

    /*     */
/*     */
    public static String getLocalIpAddress()
/*     */     throws SocketException
/*     */ {
/*     */
        Enumeration enumIpAddr;
/* 238 */
        for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
/* 239 */
            NetworkInterface intf = (NetworkInterface) en.nextElement();
/* 240 */
            for (enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
/* 241 */
                InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
/* 242 */
                if ((!(inetAddress.isLoopbackAddress())) && (inetAddress instanceof Inet4Address)) {
/* 243 */
                    return inetAddress.getHostAddress().toString();
/*     */
                }
/*     */
            }
/*     */
        }
/* 247 */
        return null;
/*     */
    }

    /*     */
/*     */
    public static String byteToBit(byte b)
/*     */ {
/* 254 */
        return "" + (byte) (b >> 7 & 0x1) + (byte) (b >> 6 & 0x1) + (byte) (b >> 5 & 0x1) + (byte) (b >> 4 & 0x1) + (byte) (b >> 3 & 0x1) + (byte) (b >> 2 & 0x1) + (byte) (b >> 1 & 0x1) + (byte) (b >> 0 & 0x1);
/*     */
    }

    /*     */
/*     */
    public static String intoIp(int i)
/*     */ {
/* 260 */
        return (i & 0xFF) + "." + (i >> 8 & 0xFF) + "." + (i >> 16 & 0xFF) + "." + (i >> 24 & 0xFF);
/*     */
    }

    /*     */
/*     */
    public static byte BitToByte(String byteStr)
/*     */ {
/*     */
        int re;
/* 268 */
        if (null == byteStr) {
/* 269 */
            return 0;
/*     */
        }
/* 271 */
        int len = byteStr.length();
/* 272 */
        if ((len != 4) && (len != 8)) {
/* 273 */
            return 0;
/*     */
        }
/* 275 */
        if (len == 8) {
/* 276 */
            if (byteStr.charAt(0) == '0')
/* 277 */ re = Integer.parseInt(byteStr, 2);
/*     */
            else
/* 279 */         re = Integer.parseInt(byteStr, 2) - 256;
/*     */
        }
/*     */
        else {
/* 282 */
            re = Integer.parseInt(byteStr, 2);
/*     */
        }
/* 284 */
        return (byte) re;
/*     */
    }

    /*     */
/*     */
    public static void printBytes(byte[] bytes)
/*     */ {
/* 291 */
        if ((null != bytes) && (bytes.length > 0)) {
/* 292 */
            for (int i = 0; i < bytes.length; ++i) {
/* 293 */
                if (i == bytes.length - 1)
/* 294 */ System.out.print(decodeByteToHexString(bytes[i]));
/*     */
                else {
/* 296 */
                    System.out.print(decodeByteToHexString(bytes[i]) + "-");
/*     */
                }
/*     */ 
/*     */
            }
/*     */ 
/* 306 */
            System.out.println("");
/*     */
        }
/*     */
    }
/*     */
}