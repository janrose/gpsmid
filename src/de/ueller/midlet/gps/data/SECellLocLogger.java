/*
 * GpsMid - Copyright (c) 2009 Kai Krueger apmonkey at users dot sourceforge dot net 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * See COPYING
 */
package de.ueller.midlet.gps.data;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

//#if polish.api.fileconnection
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
//#endif

import de.ueller.gps.data.Configuration;
import de.ueller.gps.data.Position;
import de.ueller.gps.data.Satelit;
import de.ueller.gps.tools.HelperRoutines;
import de.ueller.midlet.gps.LocationMsgReceiver;
import de.ueller.midlet.gps.Logger;

/**
 * 
 * The SECellLocLogger is a LocationMsgReceiver that listens to location updates
 * and if the location has changed significantly saves a location and cell id to
 * a text file. This can then be used to upload to openCellId.org to help fill
 * their database with cell tower locations.
 * 
 * This currently only works for Sony Ericsson JP-7.3 and later phones.
 * 
 */
public class SECellLocLogger implements LocationMsgReceiver {

	private static final Logger logger = Logger.getInstance(
			SECellLocLogger.class, Logger.TRACE);

	private Position prevPos;

	private Writer wr;

	private boolean valid;

	public boolean init() {
		//#if polish.api.fileconnection
		try {
			//#debug info
			logger
					.info("Attempting to enable cell-id logging for OpenCellId.org");

			prevPos = null;
			valid = false;
			String url = Configuration.getGpsRawLoggerUrl();
			url += "cellIDLog" + HelperRoutines.formatSimpleDateNow() + ".txt";

			String cellidS = System.getProperty("com.sonyericsson.net.cellid");
			String mccS = System.getProperty("com.sonyericsson.net.cmcc");
			String mncS = System.getProperty("com.sonyericsson.net.cmnc");
			String lacS = System.getProperty("com.sonyericsson.net.lac");

			if (((cellidS != null) && (mccS != null) && (mncS != null))) {
				Connection logCon = Connector.open(url);
				if (logCon instanceof FileConnection) {
					FileConnection fileCon = (FileConnection) logCon;
					if (!fileCon.exists()) {
						fileCon.create();
					}
					wr = new OutputStreamWriter(fileCon.openOutputStream());
					wr.write("lat,lon,mcc,mnc,lac,cellid,\n");
				} else {
					logger
							.info("Trying to perform cell-id logging on anything else than filesystem is currently not supported");
					return false;
				}
				//#debug info
				logger.info("Enabling cell-id logging");
				return true;
			} else {
				//#debug info
				logger
						.info("Cell-id properties were empty, this is only supported on newer Sony Ericsson phones");
			}
		} catch (Exception e) {
			logger.silentexception(
					"Logging of cell-ids is not supported on this phone", e);
		}
		//#endif
		//#debug info
		logger.info("NOT enabling cell-id logging");
		return false;
	}

	public void locationDecoderEnd() {
		locationDecoderEnd("Closing");
	}

	public void locationDecoderEnd(String msg) {
		logger.info("Closing Cell-id logger with msg: " + msg);
		try {
			if (wr != null) {
				wr.flush();
				wr.close();
				wr = null;
			}
		} catch (IOException ioe) {
			logger.exception("Failed to close cell-id logger", ioe);
		}
	}

	public void receiveMessage(String s) {
		// Nothing to do
	}

	public void receivePosItion(Position pos) {
		//#if polish.api.fileconnection
		int cellid;
		short mcc;
		byte mnc;
		short lac;

		//#debug trace
		logger.trace("Received position update: " + pos);

		if (!valid) {
			//#debug debug
			logger.debug("Currently no valid fix, so skipping CellID logging");
			return;
		}

		if (prevPos != null) {
			float dist = ProjMath.getDistance(pos.latitude
					* MoreMath.FAC_DECTORAD, pos.longitude
					* MoreMath.FAC_DECTORAD, prevPos.latitude
					* MoreMath.FAC_DECTORAD, prevPos.longitude
					* MoreMath.FAC_DECTORAD);
			//#debug debug
			logger.debug("Distance from previously saved pos: " + dist);
			if (dist < 25.0f) {
				return;
			}
		}
		/**
		 * We need to clone the prevPos, as the pos object gets reused for each
		 * new position
		 */
		prevPos = new Position(pos);

		try {

			String cellidS = System.getProperty("com.sonyericsson.net.cellid");
			String mccS = System.getProperty("com.sonyericsson.net.cmcc");
			String mncS = System.getProperty("com.sonyericsson.net.cmnc");
			String lacS = System.getProperty("com.sonyericsson.net.lac");

			if (cellidS != null) {
				try {
					cellid = Integer.parseInt(cellidS, 16);
					mcc = (short) Integer.parseInt(mccS);
					mnc = (byte) Integer.parseInt(mncS);
					lac = (short) Integer.parseInt(lacS, 16);
					//#debug debug
					logger.debug("Cellid: " + cellid + "  mcc: " + mcc
							+ "  mnc: " + mnc + "  lac: " + lac + " -> "
							+ pos.latitude + "," + pos.longitude);
					wr.write(pos.latitude + "," + pos.longitude + "," + mcc
							+ "," + mnc + "," + lac + "," + cellid + "\n");
					wr.flush();
				} catch (NumberFormatException nfe) {
					logger
							.silentexception(
									"Invalid number format, so could not convert cell-id data",
									nfe);
				}
			}
		} catch (Exception e) {
			logger.silentexception("Failed to retrieve Cell-id for logging", e);
		}
		//#endif
	}

	public void receiveSolution(String s) {
		if ((s.equalsIgnoreCase("off")) && (s.equalsIgnoreCase("NoFix"))
				&& (s.equalsIgnoreCase("cell")) && (s.equalsIgnoreCase("0S"))) {
			valid = false;
		} else {
			valid = true;
		}
	}

	public void receiveStatelit(Satelit[] sat) {
		// Nothing to do
	}

	public void receiveStatistics(int[] statRecord, byte qualtity) {
		// Nothing to do
	}
}
