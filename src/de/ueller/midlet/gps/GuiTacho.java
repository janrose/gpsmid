package de.ueller.midlet.gps;

/*
 * GpsMid - Copyright (c) 2008 Kai Krueger apmon at users dot sourceforge dot net 
 * See Copying
 */

import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;

import de.ueller.gps.data.Position;
import de.ueller.gps.tools.HelperRoutines;
import de.ueller.midlet.gps.GpsMid;
import de.ueller.midlet.graphics.LcdNumericFont;

public class GuiTacho extends Canvas implements CommandListener,
		GpsMidDisplayable, LocationUpdateListener {

	private final Command EXIT_CMD = new Command("Back", Command.BACK, 5);
	private final static Logger logger = Logger.getInstance(GuiTacho.class,
			Logger.DEBUG);
	private final Trace parent;
	private LcdNumericFont lcdFont;

	private Calendar cal = Calendar.getInstance();
	private StringBuffer timeString;
	private Date dur;

	private float alt_delta = 0.0f;
	private float odo = 0.0f;
	private long duration = 0;
	private float avg_spd = 0.0f;
	private float max_spd = 0.0f;

	private int kmhWidth = -1;
	private int msWidth = -1;
	private int mWidth = -1;
	private int kmWidth = -1;
	private int fHeight = -1;

	public GuiTacho(Trace parent) throws Exception {
		// #debug
		logger.info("init Tacho");

		this.parent = parent;
		addCommand(EXIT_CMD);
		setCommandListener(this);

		lcdFont = new LcdNumericFont();

		timeString = new StringBuffer();
		dur = new Date();
	}

	protected void paint(Graphics g) {
		//#debug debug
		logger.debug("Drawing Tacho screen");
		if (kmhWidth < 0) {
			/**
			 * Cache the values of the width of these Strings
			 */
			Font f = g.getFont();
			kmhWidth = f.stringWidth("km/h");
			kmWidth = f.stringWidth("km");
			msWidth = f.stringWidth("m/s");
			mWidth = f.stringWidth("m");
			fHeight = f.getHeight();
		}
		Position pos = parent.getCurrentPosition();
		odo = parent.gpx.currentTrkLength() / 1000.0f;
		avg_spd = parent.gpx.currentTrkAvgSpd() * 3.6f;
		max_spd = parent.gpx.maxTrkSpeed() * 3.6f;
		duration = parent.gpx.currentTrkDuration();
		
		int h = getHeight();
		int w = getWidth();
		
		g.setColor(0x00ffffff);
		g.fillRect(0, 0, w, h);

		g.setColor(0);
		int y = 0;
		
		cal.setTime(pos.date);
		timeString.setLength(0);
		timeString
				.append(
						HelperRoutines.formatInt2(cal
								.get(Calendar.DAY_OF_MONTH)))
				.append("/")
				.append(HelperRoutines.formatInt2(cal.get(Calendar.MONTH)))
				.append("/")
				.append(HelperRoutines.formatInt2(cal.get(Calendar.YEAR) % 100));
		g.drawString(timeString.toString(), 3, y, Graphics.TOP | Graphics.LEFT);
		
		g.drawString(parent.solution, (w >> 1) + 3, y, Graphics.TOP
				| Graphics.LEFT);
		
		timeString.setLength(0);
		y += fHeight;
		
		timeString.append(
				HelperRoutines.formatInt2(cal.get(Calendar.HOUR_OF_DAY)))
				.append(":").append(
						HelperRoutines.formatInt2(cal.get(Calendar.MINUTE)))
				.append(":").append(
						HelperRoutines.formatInt2(cal.get(Calendar.SECOND)));
		g.drawString(timeString.toString(), 3, y, Graphics.TOP | Graphics.LEFT);
		
		g.drawString("DOP: " + 0.0f, (w >> 1) + 3, y, Graphics.TOP
				| Graphics.LEFT);

		y += fHeight;
		g.drawLine(w >> 1, 0, w >> 1, y);
		g.drawLine(0, y, w, y);
		y += 64;
		lcdFont.setFontSize(48);
		
		g.drawString("km/h", w - 1, y - 3, Graphics.BOTTOM | Graphics.RIGHT);

		if (pos.speed > 10)
			lcdFont.drawInt(g, (int)(pos.speed * 3.6f), w - kmhWidth - 1, y - 5);
		else
			lcdFont.drawFloat(g, pos.speed * 3.6f, 1, w - kmhWidth - 1, y - 5);
		g.drawLine(0, y, w, y);
		
		lcdFont.setFontSize(18);
		g.drawLine(w >> 1, y, w >> 1, y + 32);
		y += 28;
		g.drawString("km", (w >> 1) - 1, y - 5, Graphics.BOTTOM
				| Graphics.RIGHT);
		if (odo > 10)
			lcdFont.drawFloat(g, odo, 1, (w >> 1) - kmWidth - 2, y);
		else
			lcdFont.drawFloat(g, odo, 2, (w >> 1) - kmWidth - 2, y);
		
		g.drawString("km/h", w - 1, y - 5, Graphics.BOTTOM | Graphics.RIGHT);
		if (avg_spd > 30)
			lcdFont.drawInt(g, (int)avg_spd, w - kmhWidth - 2, y);
		else
			lcdFont.drawFloat(g, avg_spd, 1, w - kmhWidth - 2, y);
		g.drawLine(0, y, w, y);
		g.drawLine(w >> 1, y, w >> 1, y + 32);
		y += 28;
		
		g
				.drawString("m", (w >> 1) - 1, y - 3, Graphics.BOTTOM
						| Graphics.RIGHT);
		lcdFont.drawInt(g, (int) pos.altitude, (w >> 1) - mWidth, y);
		
		g.drawString("m/s", w - 1, y - 3, Graphics.BOTTOM | Graphics.RIGHT);
		lcdFont.drawFloat(g, alt_delta, 1, w - msWidth, y);
		g.drawLine(0, y, w, y);
		y += fHeight;
		
		dur.setTime(duration);
		cal.setTime(dur);
		timeString.setLength(0);
		timeString.append(
				HelperRoutines.formatInt2(cal.get(Calendar.HOUR_OF_DAY)))
				.append(":").append(
						HelperRoutines.formatInt2(cal.get(Calendar.MINUTE)))
				.append(":").append(
						HelperRoutines.formatInt2(cal.get(Calendar.SECOND)));
		g.drawString(timeString.toString(), (w >> 1) - 1, y + 3,
				Graphics.BOTTOM | Graphics.RIGHT);
		g.drawString(max_spd + " km/h", w - 1, y + 3, Graphics.BOTTOM
				| Graphics.RIGHT);
	}

	public void show() {
		GpsMid.getInstance().show(this);
		synchronized (parent.locationUpdateListeners) {
			parent.locationUpdateListeners.addElement(this);
		}

	}

	public void commandAction(Command c, Displayable d) {
		if (c == EXIT_CMD) {
			synchronized (parent.locationUpdateListeners) {
				parent.locationUpdateListeners.removeElement(this);
			}
			parent.show();
		}
	}

	public void loctionUpdated() {
		repaint();
	}

}