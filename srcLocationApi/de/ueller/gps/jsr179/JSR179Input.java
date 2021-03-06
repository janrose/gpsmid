/*
 * GpsMid - Copyright (c) 2007 Harald Mueller james22 at users dot sourceforge dot net
 *        - Copyright (c) 2008 Kai Krueger apmonkey at users dot sourceforge dot net
 *        - Copyright (c) 2008 sk750 at users dot sourceforge dot net
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
 * See Copying
 */
package de.ueller.gps.jsr179;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import de.ueller.gps.data.Position;
import de.ueller.gps.nmea.NmeaMessage;
import de.ueller.gps.tools.StringTokenizer;
import de.ueller.midlet.gps.LocationMsgProducer;
import de.ueller.midlet.gps.LocationMsgReceiver;
import de.ueller.midlet.gps.LocationMsgReceiverList;
import de.ueller.midlet.gps.Logger;

public class JSR179Input implements LocationListener, LocationMsgProducer {
	private final static Logger logger = Logger.getInstance(JSR179Input.class,
			Logger.TRACE);

	/** location provider */
	private LocationProvider locationProvider = null;
	private LocationMsgReceiverList receiver;
	private NmeaMessage smsg;
	Date date = new Date();
	Position pos = new Position(0f, 0f, 0f, 0f, 0f, 0, date);

	private OutputStream rawDataLogger;

	
	public JSR179Input(){
		this.receiver = new LocationMsgReceiverList();
	}


	public boolean init(LocationMsgReceiver receiver) {
		logger.info("start JSR179 LocationProvider");
		this.receiver.addReceiver(receiver);
		createLocationProvider();
		// We may be able to get some additional information such as the number
		// of satellites form the NMEA string
		smsg = new NmeaMessage(receiver);
		return true;
	}

	/**
	 * Initializes LocationProvider uses default criteria
	 * 
	 * @throws Exception
	 */
	void createLocationProvider() {
		logger.trace("enter createLocationProvider()");
		if (locationProvider == null) {
			// try out different locationprovider criteria combinations, the
			// ones with maximum features first
			for (int i = 0; i <= 2; i++) {
				try {
					Criteria criteria = new Criteria();
					switch (i) {
					case 0:
						criteria.setAltitudeRequired(true); // also require the
															// criteria below
					case 1:
						criteria.setSpeedAndCourseRequired(true);
						break;
					}
					locationProvider = LocationProvider.getInstance(criteria);
					logger.info(locationProvider.toString());
					break; // we are using this criteria
				} catch (LocationException le) {
					logger.info("LocationProvider criteria not fitting: " + i);
					locationProvider = null;
				}
			}
			if (locationProvider != null) {
				updateSolution(locationProvider.getState());
			} else {
				receiver.locationDecoderEnd("no JSR179 Provider");
				//#debug
				logger.error("Cannot create LocationProvider for criteria.");
			}
		}
		//#debug
		logger.trace("exit createLocationProvider()");
	}

	public void locationUpdated(LocationProvider provider, Location location) {
		logger.trace("enter locationUpdated(provider,location)");
		Coordinates coordinates = location.getQualifiedCoordinates();
		pos.latitude = (float) coordinates.getLatitude();
		pos.longitude = (float) coordinates.getLongitude();
		pos.altitude = (float) coordinates.getAltitude();
		pos.course = location.getCourse();
		pos.speed = location.getSpeed();
		pos.date.setTime(location.getTimestamp());
		receiver.receivePosItion(pos);
		String nmeaString = location
				.getExtraInfo("application/X-jsr179-location-nmea");
		// String nmeaString =
		// "$GPGSA,A,3,32,23,31,11,20,,,,,,,,2.8,2.6,1.0*3D\r\n$GPGSV,3,1,10,20,71,071,38,23,60,168,41,17,42,251,25,11,36,148,37*73\r\n$GPGSV,3,2,10,04,29,300,16,13,26,197,,31,21,054,47,32,10,074,38*70\r\n$GPGSV,3,3,10,12,04,339,17,05,01,353,15*75\r\n"
		// ;
		logger.info("Using extra NMEA info in JSR179: " + nmeaString);
		if (nmeaString != null) {
			if (rawDataLogger != null) {
				try {
					rawDataLogger.write(nmeaString.getBytes());
					rawDataLogger.flush();
				} catch (IOException ioe) {
					logger.exception("Could not write raw GPS log", ioe);
				}
			}
			Vector messages = StringTokenizer.getVector(nmeaString, "\n");
			if (messages != null) {
				for (int i = 0; i < messages.size(); i++) {
					String nmeaMessage = (String) messages.elementAt(i);
					if (nmeaMessage.startsWith("$")) {
						// Cut off $GP from the start
						nmeaMessage = nmeaMessage.substring(3);
					}
					int starIdx = nmeaMessage.indexOf("*");
					if (starIdx > 0) {
						// remove the checksum
						nmeaMessage = nmeaMessage.substring(0, starIdx);
					}
					if ((nmeaMessage != null) && (nmeaMessage.length() > 5))
						smsg.decodeMessage(nmeaMessage);
				}
			}
		}

		// logger.trace("exit locationUpdated(provider,location)");
	}

	public void providerStateChanged(LocationProvider arg0, int state) {
		logger.trace("enter providerStateChanged(locationProvider," + state
				+ "");
		updateSolution(state);
	}

	public void close() {
		//#debug
		logger.trace("enter close()");
		// if (locationProvider != null){
		// locationProvider.setLocationListener(null, -1, -1, -1);
		// }
		if (locationProvider != null) {
			// locationProvider.reset();
			try {
				locationProvider.setLocationListener(null, 0, 0, 0);
			} catch (Exception e) {
			}
		}
		locationProvider = null;
		receiver.locationDecoderEnd();
		//#debug
		logger.trace("exit close()");
	}

	private void updateSolution(int state) {
		if (state == LocationProvider.AVAILABLE) {
			locationProvider.setLocationListener(this, 1, -1, -1);
			if (receiver != null)
				receiver.receiveSolution("On");
		}
		if (state == LocationProvider.OUT_OF_SERVICE) {
			locationProvider.setLocationListener(this, 0, -1, -1);
			if (receiver != null) {
				receiver.receiveSolution("Off");
				receiver.receiveMessage("provider stopped");
			}
		}
		if (state == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			locationProvider.setLocationListener(this, 0, -1, -1);
			if (receiver != null)
				receiver.receiveSolution("0");
		}
	}

	public void disableRawLogging() {
		if (rawDataLogger != null) {
			try {
				rawDataLogger.close();
			} catch (IOException e) {
				logger.exception("Couldn't close raw gps logger", e);
			}
			rawDataLogger = null;
		}
	}

	public void enableRawLogging(OutputStream os) {
		rawDataLogger = os;
	}

	public void addLocationMsgReceiver(LocationMsgReceiver rec) {
		receiver.addReceiver(rec);
	}

	public boolean removeLocationMsgReceiver(LocationMsgReceiver rec) {
		return receiver.removeReceiver(rec);
	}

}
