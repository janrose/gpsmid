package de.ueller.midlet.gps;
/*
 * GpsMid - Copyright (c) 2007 Harald Mueller james22 at users dot sourceforge dot net 
 * See Copying
 */
import de.ueller.gps.data.Position;
import de.ueller.gps.data.Satelit;


public interface LocationMsgReceiver {
	public static final byte SIRF_FAIL_NO_START_SIGN1=0;
	public static final byte SIRF_FAIL_NO_START_SIGN2=1;
	public static final byte SIRF_FAIL_MSG_TO_LONG=2;
	public static final byte SIRF_FAIL_MSG_INTERUPTED=3;
	public static final byte SIRF_FAIL_MSG_CHECKSUM_ERROR=4;
	public static final byte SIRF_FAIL_NO_END_SIGN1=5;
	public static final byte SIRF_FAIL_NO_END_SIGN2=6;
	public static final byte SIRF_FAIL_COUNT=7;
	
	public void receivePosItion(Position pos);
	public void receiveStatelit(Satelit[] sat);
	public void receiveMessage(String s);
	public void locationDecoderEnd();
	public void receiveStatistics(int[] statRecord,byte qualtity);
	public void receiveSolution(String s);
	public void locationDecoderEnd(String msg);
}