package de.ueller.midlet.gps;
/*
 * GpsMid - Copyright (c) 2008 sk750 at users dot sourceforge dot net 
 * See Copying
 */

import javax.microedition.lcdui.*;

import de.ueller.gps.data.Configuration;
import de.ueller.midlet.gps.tile.C;
import de.ueller.midlet.gps.tile.SoundDescription;

public class GuiSetupSound extends Form implements CommandListener {
	private final Form	menuSetupSound = new Form("Sounds");
	// Groups
	private ChoiceGroup sndGpsGroup;
	private	String [] sndGps = new String[2];
	private boolean[] selSndGps = new boolean[2];

	private ChoiceGroup sndRoutingGroup=null;
	private	String [] sndRouting = new String[1];
	private	boolean[] selSndRouting = new boolean[1];

	// commands
	private static final Command CMD_SAVE = new Command("Ok", Command.ITEM, 2);
	private static final Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, 3);
	
	// other
	private GuiDiscover parent;
	private static Configuration config;
	
	public GuiSetupSound(GuiDiscover parent) {
		super("Sounds");
		config=GpsMid.getInstance().getConfig();
		this.parent = parent;
		try {
			// set choice texts and convert bits from config flag into selection states
			sndGps[0] = "Connect"; 			selSndGps[0]=config.getCfgBitState(config.CFGBIT_SND_CONNECT);
			sndGps[1] = "Disconnect";		selSndGps[1]=config.getCfgBitState(config.CFGBIT_SND_DISCONNECT);
			sndGpsGroup = new ChoiceGroup("GPS", Choice.MULTIPLE, sndGps ,null);
			sndGpsGroup.setSelectedFlags(selSndGps);
			append(sndGpsGroup);
			
			// only add Routing Sound Group if we have Sound Description
			// at least for LEFT
			String soundFile=null;
			SoundDescription sDes=C.getSoundDescription("LEFT");
			if (sDes!=null) {
				soundFile=sDes.soundFile;
			}
			if(soundFile!=null) {			
				sndRouting[0] = "Routing Instructions"; 			selSndRouting[0]=config.getCfgBitState(config.CFGBIT_SND_ROUTINGINSTRUCTIONS);
				sndRoutingGroup = new ChoiceGroup("Routing", Choice.MULTIPLE, sndRouting ,null);
				sndRoutingGroup.setSelectedFlags(selSndRouting);
				append(sndRoutingGroup);
			}
			
			addCommand(CMD_SAVE);
			addCommand(CMD_CANCEL);

			// Set up this Displayable to listen to command events
			setCommandListener(this);
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	public void commandAction(Command c, Displayable d) {

		if (c == CMD_CANCEL) {			
			parent.show();
			return;
		}

		if (c == CMD_SAVE) {			
			// convert boolean array with selection states for config bits
			// to one flag with corresponding bits set
	        sndGpsGroup.getSelectedFlags(selSndGps);
			config.setCfgBitState(config.CFGBIT_SND_CONNECT, selSndGps[0], true);
			config.setCfgBitState(config.CFGBIT_SND_DISCONNECT, selSndGps[1], true);

			if (sndRoutingGroup!=null) {
				sndRoutingGroup.getSelectedFlags(selSndRouting);
				config.setCfgBitState(config.CFGBIT_SND_ROUTINGINSTRUCTIONS, selSndRouting[0], true);
			}

			parent.show();
			return;
		}
	}
	
	public void show() {
		GpsMid.getInstance().show(this);
	}

}