
package activity;

import com.alibaba.fastjson.JSON;
import com.washcar.bo.SimpleRespMqMsg;
import com.washcar.bo.SimpleSendMqMsg;
import com.washcar.bo.todev.DeviceInfoResp;
import com.washcar.bo.todev.EwmInfoResp;
import com.washcar.bo.todev.WashCarReq;
import com.washcar.bo.todev.WashcarResp;
import com.washcar.bo.towexin.ADInfoReq;
import com.washcar.bo.towexin.AdInfoResp;
import com.washcar.bo.towexin.DeviceErrorInfoReq;
import com.washcar.bo.towexin.DeviceInfoReq;
import com.washcar.bo.towexin.DeviceUsageInfoReq;
import com.washcar.bo.towexin.ErrorMsg;
import com.washcar.bo.towexin.QueryYuFuKaBalanceReq;
import com.washcar.bo.towexin.QueryYuFuKaBalanceResp;
import com.washcar.bo.towexin.SetDeviceFeeReq;
import com.washcar.bo.towexin.SetDeviceResp;
import com.washcar.bo.towexin.UpdateInfoResp;
import com.washcar.bo.towexin.WashCarWithYuFuKaReq;
import com.washcar.bo.towexin.WashCarWithYuFuKaResp;
import com.washcar.client.RemoteMessageArrivedListener;
import com.washcar.client.WLTService;
import com.washcar.client.WashCarMqttClient;
import com.washcar.constant.CmdType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <P>
 * 测试客户端发送HTTP请求,需要先启动HTTP服务器端 网点号 001121004609 001141006403 103707594809
 * </P>
 * 
 */
public class WashCarServiceTest implements RemoteMessageArrivedListener{
	private final Logger log = LoggerFactory.getLogger(WashCarServiceTest.class);
	private static WLTService service;
	private static String host = "tcp://139.196.32.176:62626";
	private final String DEVICE_ID = "D080216000001";
	private WashCarMqttClient mc;

	public void init(RemoteMessageArrivedListener listener) throws Exception {
		// android下使用
		service = new WLTService("zh_CN");
		service.setConnectionTimeoutSeconds(30);
		try {
			mc = new WashCarMqttClient(host, DEVICE_ID, "washcar", "washcar!@#321", listener);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void start() throws Exception{
		Thread.sleep(1000 * 60 * 660);
		mc.shutDown();
	}
	/**
	 * 获取 app 更新信息
	 * @throws Exception
	 */
	public void testGetUpdateInfo() throws Exception {
		SimpleSendMqMsg sendMsg = new SimpleSendMqMsg();
		sendMsg.setCmdType(CmdType.GET_UPDATE_INFO.getCode());
		sendMsg.setDeviceId(DEVICE_ID);
		sendMsg.setInvokeSN(System.currentTimeMillis() + "");
		String result = mc.sendMsgTOWexin(JSON.toJSONString(sendMsg).getBytes("utf-8"), sendMsg.getInvokeSN());
		UpdateInfoResp resp = JSON.parseObject(result, UpdateInfoResp.class);
		System.out.println("rslt is:" + resp);
		Thread.sleep(1000 * 60 * 60);
		mc.shutDown();
	}

	/**
	 * 获取广告信息
	 * 返回的verion 与 请求的version 不同，说明有广告更新
	 * @throws Exception
	 */

	public void testGetADInfo() throws Exception {
		ADInfoReq adInfoReq = new ADInfoReq();
		adInfoReq.setCmdType(CmdType.GET_AD_INFO.getCode());
		adInfoReq.setDeviceId(DEVICE_ID);
		adInfoReq.setInvokeSN(System.currentTimeMillis() + "");
		adInfoReq.setVersion("0.0.1");
		String result = mc.sendMsgTOWexin(JSON.toJSONString(adInfoReq).getBytes("utf-8"), adInfoReq.getInvokeSN());
		AdInfoResp resp = JSON.parseObject(result, AdInfoResp.class);
		System.out.println("rslt is:" + resp);
		Thread.sleep(1000 * 60 * 60);
		mc.shutDown();
	}

	/**
	 * 上报设备异常信息
	 * @throws Exception
	 */
	public void testSendDeviceErrorInfo() throws Exception {
		DeviceErrorInfoReq sendMsg = new DeviceErrorInfoReq();
		sendMsg.setCmdType(CmdType.SEND_DEVICE_ERROR_INFO.getCode());
		sendMsg.setDeviceId(DEVICE_ID);
		sendMsg.setInvokeSN(System.currentTimeMillis() + "");
		ErrorMsg error = new ErrorMsg();
		error.setErrorCode("001");
		error.setErrorMsg("device power off");
		error.setLevel(ErrorMsg.FATAL);
		sendMsg.getErrors().add(error);

		String result =
		mc.sendMsgTOWexin(JSON.toJSONString(sendMsg).getBytes("utf-8"),
		 sendMsg.getInvokeSN());
		SimpleRespMqMsg resp = JSON.parseObject(result, SimpleRespMqMsg.class);
		 System.out.println("rslt is:" + resp);
		Thread.sleep(1000 * 60 * 60);
		mc.shutDown();
	}
	public String receiveMsg(String msg) {
		SimpleSendMqMsg send = JSON.parseObject(msg, SimpleSendMqMsg.class);
		if (CmdType.WASH_CAR.getCode().equals(send.getCmdType())) {
			WashCarReq washCarReq = JSON.parseObject(msg, WashCarReq.class);
			log.info("recv lent charger cmd:" + washCarReq);
			WashcarResp resp = new WashcarResp();
			resp.setDeviceId(DEVICE_ID);
			resp.setInvokeSN(send.getInvokeSN());
			resp.setResult(SimpleSendMqMsg.SUCCESS);
			return JSON.toJSONString(resp);
		}

		if (CmdType.GET_DEVICE_INFO.getCode().equals(send.getCmdType())) {
			log.info("recv GET_DEVICE_INFO cmd:" + send);
			DeviceInfoResp resp = new DeviceInfoResp();
			resp.setDeviceId(DEVICE_ID);
			resp.setInvokeSN(send.getInvokeSN());
			resp.setResult(SimpleSendMqMsg.SUCCESS);
			List<String> canLentIds = new ArrayList<String>();
			canLentIds.add("C201603190001");

			return JSON.toJSONString(resp);
		}
		return "";
	}

	/**
	 * set device fee
	 * 设置机器消费金额
	 * @throws Exception
	 */
	public void setDeviceFee() throws Exception {
		SetDeviceFeeReq req = new SetDeviceFeeReq();
		req.setDeviceId(DEVICE_ID);
		req.setInvokeSN(System.currentTimeMillis() + "");
		req.setCmdType(CmdType.SET_DEVICE_FEE.getCode());
		req.setFee("15");
		String result = mc.sendMsgTOWexin(JSON.toJSONString(req).getBytes("utf-8"), req.getInvokeSN());
		SetDeviceResp rslt = JSON.parseObject(result, SetDeviceResp.class);
		if(rslt.getResult().equals(SetDeviceResp.SUCCESS)){
			//success
		}
		System.out.println("rslt is:" + rslt);
		// mc.shutDown();
	}

	/**
	 * 上报单次付款后的用水量，泡沫时间，吸尘时间。
	 * //@throws Exception
	 */
	public void sendDeviceUsageInfo() throws Exception {
		DeviceUsageInfoReq req = new DeviceUsageInfoReq();
		req.setDeviceId(DEVICE_ID);
		req.setInvokeSN(System.currentTimeMillis() + "");
		req.setCmdType(CmdType.SEND_DEVICE_USAGE.getCode());
		req.setVacuumingTime("12");//分钟?
		req.setPowerTime("30");//分钟?
		req.setWaterUsage("20");//用水量(升?)
		String result = mc.sendMsgTOWexin(JSON.toJSONString(req).getBytes("utf-8"), req.getInvokeSN());
		SimpleRespMqMsg rslt = JSON.parseObject(result, SimpleRespMqMsg.class);
		if(rslt.getResult().equals(SimpleRespMqMsg.SUCCESS)){
			//success
		}
		System.out.println("rslt is:" + rslt);
		// mc.shutDown();
	}
	
	/**
	 * 获取二维码信息
	 * 
	 * @throws Exception
	 */
	public  String  getEWMInfo() throws Exception {
		SimpleSendMqMsg req = new SimpleSendMqMsg();
		req.setDeviceId(DEVICE_ID);
		req.setInvokeSN(System.currentTimeMillis() + "");
		req.setCmdType(CmdType.GET_EWM.getCode());
		String result = mc.sendMsgTOWexin(JSON.toJSONString(req).getBytes("utf-8"), req.getInvokeSN());
		EwmInfoResp rslt = JSON.parseObject(result, EwmInfoResp.class);
		if(rslt.getResult().equals(SimpleRespMqMsg.SUCCESS)){
			return rslt.getDeviceEwmImage();
		}
		// mc.shutDown();
		return null;
	}

	/**
	 *上报洗车设备工作或空闲状态。
	 * 
	 * @throws Exception
	 */
	public void sendDeviceInfo() throws Exception {
		DeviceInfoReq req = new DeviceInfoReq();
		req.setDeviceId(DEVICE_ID);
		req.setInvokeSN(System.currentTimeMillis() + "");
		req.setCmdType(CmdType.SEND_DEVICE_INFO.getCode());
		req.setDeviceStatus(DeviceInfoReq.BUSY);
		String result = mc.sendMsgTOWexin(JSON.toJSONString(req).getBytes("utf-8"), req.getInvokeSN());
		SimpleRespMqMsg rslt = JSON.parseObject(result, SimpleRespMqMsg.class);
		if(rslt.getResult().equals(SimpleRespMqMsg.SUCCESS)){
			System.out.println("rslt is:" + rslt);
		}
		// mc.shutDown();
	}
	
	/**
	 *查询是否有余额
	 * 
	 * @throws Exception
	 */
	public void queryYufukaBalance() throws Exception {
		QueryYuFuKaBalanceReq req = new QueryYuFuKaBalanceReq();
		req.setDeviceId(DEVICE_ID);
		req.setInvokeSN(System.currentTimeMillis() + "");
		req.setCmdType(CmdType.SEND_DEVICE_INFO.getCode());
		req.setYuFuKaId("ID-160410000004");
		String result = mc.sendMsgTOWexin(JSON.toJSONString(req).getBytes("utf-8"), req.getInvokeSN());
		QueryYuFuKaBalanceResp rslt = JSON.parseObject(result, QueryYuFuKaBalanceResp.class);
		if(rslt.getResult().equals(SimpleRespMqMsg.SUCCESS)){
			System.out.println("rslt is:" + rslt);
		}		
		// mc.shutDown();
	}
	
	/**
	 *上报扣款金额，扣款成功，服务器下发扣款成功信息
	 * 
	 * @throws Exception
	 */
	public void washCarWithYufuKa() throws Exception {
		WashCarWithYuFuKaReq req = new WashCarWithYuFuKaReq();
		req.setDeviceId(DEVICE_ID);
		req.setInvokeSN(System.currentTimeMillis() + "");
		req.setCmdType(CmdType.SEND_DEVICE_INFO.getCode());
		req.setYuFuKaId("ID-160410000004");
		String result = mc.sendMsgTOWexin(JSON.toJSONString(req).getBytes("utf-8"), req.getInvokeSN());
		WashCarWithYuFuKaResp rslt = JSON.parseObject(result, WashCarWithYuFuKaResp.class);
		if(rslt.getResult().equals(SimpleRespMqMsg.SUCCESS)){
			System.out.println("rslt is:" + rslt);
		}		
		// mc.shutDown();
	}
}