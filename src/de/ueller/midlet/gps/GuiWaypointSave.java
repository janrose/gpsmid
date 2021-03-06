package de.ueller.midlet.gps;
/*
 * GpsMid - Copyright (c) 2008 Kai Krueger apmonkey at users dot sourceforge dot net 
 * See Copying
 */

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import de.ueller.gps.data.Configuration;
import de.ueller.midlet.gps.data.PositionMark;
import de.ueller.midlet.gps.data.Proj2DMoveUp;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;

public class GuiWaypointSave extends Form implements CommandListener {
	private TextField fldName;
	private ChoiceGroup cg;
	private static final Command saveCmd = new Command("Save", Command.OK, 1);
	private static final Command backCmd = new Command("Back", Command.BACK, 2);
	private Trace parent;
	private String name;
	private PositionMark waypt;
	
	protected static final Logger logger = Logger.getInstance(GuiWaypointSave.class,Logger.TRACE);

	public GuiWaypointSave(Trace tr) {
		super("Enter Waypoint name");
		this.parent = tr;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		fldName = new TextField("Name:", "", Configuration.MAX_WAYPOINTNAME_LENGTH, TextField.ANY);
		cg = new ChoiceGroup("Settings", Choice.MULTIPLE);
		cg.append("Recenter to GPS after saving",null);
		
		// Set up this Displayable to listen to command events
		setCommandListener(this);
		// add the commands
		addCommand(backCmd);
		addCommand(saveCmd);
		this.append(fldName);
		this.append(cg);
	}
	
	public void setData(PositionMark pos)
	{
		this.waypt = pos;
		this.name = "";
		fldName.setString(name);
	}

	public void commandAction(Command cmd, Displayable displayable) {
		if (cmd == saveCmd) {
			name = fldName.getString();
			logger.info("Saving waypoint with name: " + name);
			waypt.displayName = name;
			parent.gpx.addWayPt(waypt);

			// Recenter GPS after saving a Waypoint if this option is selected
			if (cg.isSelected(0)) {
				parent.gpsRecenter = true;
				parent.newDataReady();
			}
			parent.show();				
			return;
		}
		else if (cmd == backCmd) {
			parent.show();
	    	return;
		}
	}
	
	public void show() {
		if (parent.gpx.getNumberWaypoints() < 255) {
			GpsMid.getInstance().show(this);
		}
		else
		{
			// Show alert that no more waypoints can be saved 
			// (because the J2ME List class can not display more than
			// 255 items - at least on some phones).
			GpsMid.getInstance().alert("Error", "No more waypoints can be saved due to a platform restriction. Hint: Export the waypoints to a file, then delete them.", Alert.FOREVER);
		}
	}
}
