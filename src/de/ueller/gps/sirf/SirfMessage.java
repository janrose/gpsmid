/**
 * SirfDecoder
 * 
 * takes an InputStream and interpret layer 3 and layer 4. Than make
 * callbacks to the receiver witch ahas to implement SirfMsgReceiver 
 *
 * @version $Revision: 1.11 $$ ($Name:  $)
 * @autor Harald Mueller james22 at users dot sourceforge dot net
 * Copyright (C) 2007 Harald Mueller
 */
package de.ueller.gps.sirf;

import java.util.Date;


import de.ueller.gps.data.Position;
import de.ueller.gps.data.Satelit;
import de.ueller.midlet.gps.LocationMsgReceiver;
import de.ueller.midlet.gps.Logger;

public class SirfMessage {

	public byte[]			readBuffer	= new byte[1023];


	Position				pold;

	public int				length		= 0;
	Date date=new Date();

	private final LocationMsgReceiver	receiver;
	//#debug
	private final static Logger logger = Logger.getInstance(SirfInput.class,Logger.DEBUG);
	
	public SirfMessage(LocationMsgReceiver receiver) {
		this.receiver = receiver;}


	public SirfMessage(byte[] readBuffer,LocationMsgReceiver receiver) {
		this.receiver=receiver;
		this.readBuffer = readBuffer;
		length = readBuffer.length;
	}

	public int getByte(int i) {
		int buf = readBuffer[i];
		if (buf < 0) {
			buf = (buf + 256);
		}
		return buf;
	}

	public int getMessageId() {
		return readBuffer[0];
	}

//	public short get2Byte(int i) {
//		return (short) ((readBuffer[i] << 8) | readBuffer[i+1]);
//		return (short) (getByte(i) <<8 | getByte(i + 1));
//	}

//	public int get2ByteUnsigned(int i) {
////		return (getByte(i) * 256 + getByte(i + 1));
//		return((getByte(i) << 8) | getByte(i+1));
//	}
	public int get2ByteUnsigned(int i) {
		return ((((readBuffer[i]+256)&0xff) << 8) 
				| ((readBuffer[i+1]+256)&0xff));
	}
	public short get2Byte(int i) {
		return (short)((((readBuffer[i]+256)&0xff) << 8) 
				| ((readBuffer[i+1]+256)&0xff));
	}

//	public int get4Byte(int i) {
//		return (getByte(i) * 16777216 + getByte(i + 1) * 65536 + getByte(i + 2) * 256 + getByte(i + 3));
//		return ((getByte(i) << 24) | (getByte(i + 1) << 16) | (getByte(i + 2) << 8) | getByte(i + 3));
//	}
	public int get4Byte(int i) {
		
		return ((((readBuffer[i]+256)&0xff) << 24) 
				| (((readBuffer[i+1]+256)&0xff) << 16) 
				| (((readBuffer[i+2]+256)&0xff) << 8) 
				| ((readBuffer[i+3]+256)&0xff));
	}


	public String decodeMsg(SirfMessage smsg) {
		int type = smsg.getMessageId();
		//#debug debug
		logger.debug("Sirf:" + type);
		switch (type) {
			case 2:
				return decodeMeasureNavigation();
			case 4:
				return decodeMeasuredTrackerDataOut();
//			case 6:
//				return decodeSoftwareVersion();
//			case 9:
//				return decodeThroughput();
//			case 11:
//				return decodeCommandAck();
//			case 12:
//				return decodeCommandNack();
			case 41:
				return decodeGeodeticNavigationData();
		}
//		return message("msg " + type + " not implemented");
		 return null;
	}

	private String decodeMeasuredTrackerDataOut() {
		int anz=getByte(7);
		Satelit s[]=new Satelit[anz];
		for (int l = 0; l < anz; l++){
			s[l]=decode1sMeasuredTrackerDataOut(8+l*15);			
		}
//		pcs.firePropertyChange("SatelitData",null,s);
		receiver.receiveStatelit(s);
		return null;

	}

	private Satelit decode1sMeasuredTrackerDataOut(int i) {
		Satelit s=new Satelit();
		s.id=getByte(i++);
		s.azimut=getByte(i++)*1.5f;
		s.elev=getByte(i++)/2f;
		s.state=get2ByteUnsigned(i);
		i+=2;
		// take only the first of the signal to noise ratio
		s.snr = getByte(i++);
		return s;
	}

	private String decodeThroughput() {
		return null;
		// return "Throughput SegStatMax:"+(1d*get2ByteUnsigned(1)/186d)+" last ms: "+(1d*get2ByteUnsigned(7)/186d);
	}

	private String decodeSoftwareVersion() {
		return message(new String(readBuffer));
	}

	private String decodeCommandNack() {

		return message("Not OK for Message " + getByte(1));
	}

	private String decodeCommandAck() {
		return message("OK for Message " + getByte(1));
	}

	private String decodeGeodeticNavigationData() {
		short valid = get2Byte(1);
		// short type = get2Byte(3);
		// short eWeekNr = get2Byte(5);
		// int two = get4Byte(7);
		short year = get2Byte(11);
		short month = (short) getByte(13);
		short day = (short) getByte(14);
		short hour = (short) getByte(15);
		short min = (short) getByte(16);
		double second = 1d * get2ByteUnsigned(17) / 1000d;
		// int satIdList = get4Byte(19);
		double lat = getDeg(23);
		double lon = getDeg(27);
		// double altEl = getMeter(31);
		double altMSL = getMeter(35);
		// short mapDatum = getByte(39);
		double sog = 0.01d * get2ByteUnsigned(40);
		double course = 0.01d * get2ByteUnsigned(42);
//		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//		cal.set(Calendar.YEAR, year);
//		cal.set(Calendar.MONTH, month - 1);
//		cal.set(Calendar.DAY_OF_MONTH, day);
//		cal.set(Calendar.HOUR_OF_DAY, hour);
//		cal.set(Calendar.MINUTE, min);
//		cal.set(Calendar.SECOND, (int) second);

		Position p = new Position((float) lat, (float) lon, (float) altMSL, (float) sog, (float) course, valid, date/*cal.getTime()*/);
		receiver.receivePosItion(p);
		pold = p;
		return null;
//		return message("" + day + "." + month + "." + year + " " + hour + ":" + min + ":" + second + " Lat=" + lat + " lon=" + lon + " h=" + altMSL + " sog=" + sog);
	}

	private double getMeter(int i) {
		return (0.01d * get4Byte(i));
	}

	private double getDeg(int i) {
//		return (get4Byte(i) * 1.0d / 10000000d);
		return (get4Byte(i) * .0000001d );
	}

	private String decodeMeasureNavigation() {
		short byte1 = (short) getByte(20);
		boolean dgps=false;
		String msg="UK";
		if ((byte1 & 0x80) > 0){
			dgps=true;
		}
		byte1=(short) (byte1 & 0x7);
		switch (byte1){
			case 0: msg="No"; break;
			case 1: msg="1S"; break;
			case 2: msg="2S"; break;
			case 3: msg="2D"; break;
			case 4: msg="3D"; break;
			case 5: msg="2DP"; break;
			case 6: msg="3DP"; break;
			case 7: msg="DR"; break;

		}
		String ret= ((dgps)?"DGPS " : "" )+ msg;
		receiver.receiveSolution(ret);
//		receiver.receiveSatInUse(getByte(28));
		return null;
	}

	private String message(String msg) {
		receiver.receiveMessage(msg);
		return msg;
	}
}
