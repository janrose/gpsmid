package de.ueller.midlet.gps;
/*
 * GpsMid - Copyright (c) 2008 sk750 at users dot sourceforge dot net 
 * See Copying
 */

import javax.microedition.lcdui.*;

import de.ueller.gps.data.Configuration;

public class GuiMapFeatures extends Form implements CommandListener {
	private final Form	menuMapFeatures = new Form("Map Features");
	// Groups
	private ChoiceGroup elemsGroup;
	private	String [] elems = new String[5];
	private boolean[] selElems = new boolean[5];

	private ChoiceGroup altInfosGroup;
	private	String [] altInfos = new String[2];
	private	boolean[] selAltInfos = new boolean[2];

	private ChoiceGroup modesGroup;
	private	String [] modes = new String[3];
	private	boolean[] selModes = new boolean[3];

	private Gauge gaugeDetailBoost; 

	// commands
	private static final Command CMD_APPLY = new Command("Apply", Command.BACK, 1);
	private static final Command CMD_SAVE = new Command("Save", Command.ITEM, 2);
	private static final Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, 3);
	//private static final Command CMD_SETSTARTUPPOS = new Command("Set map startup position", Command.ITEM, 3);
	
	// other
	private Trace parent;
	private static Configuration config;
	
	public GuiMapFeatures(Trace tr) {
		super("Map features");
		config=GpsMid.getInstance().getConfig();
		this.parent = tr;
		try {
			// set choice texts and convert bits from render flag into selection states
			elems[0] = "POI labels"; 			selElems[0]=config.getCfgBitState(config.CFGBIT_POITEXTS);
			elems[1] = "POIs";					selElems[1]=config.getCfgBitState(config.CFGBIT_POIS);
			elems[2] = "Area labels"; 			selElems[2]=config.getCfgBitState(config.CFGBIT_AREATEXTS);
			elems[3] = "Way labels"; 			selElems[3]=config.getCfgBitState(config.CFGBIT_WAYTEXTS);
			elems[4] = "Waypoint labels"; 		selElems[4]=config.getCfgBitState(config.CFGBIT_WPTTEXTS);
			elemsGroup = new ChoiceGroup("Elements", Choice.MULTIPLE, elems ,null);
			elemsGroup.setSelectedFlags(selElems);
			append(elemsGroup);
			
			altInfos[0] = "Lat/lon"; 			selAltInfos[0]=config.getCfgBitState(config.CFGBIT_SHOWLATLON);
			altInfos[1] = "Type information"; 	selAltInfos[1]=config.getCfgBitState(config.CFGBIT_SHOWWAYPOITYPE);
			altInfosGroup = new ChoiceGroup("Alternative Info", Choice.MULTIPLE, altInfos ,null);
			altInfosGroup.setSelectedFlags(selAltInfos);
			append(altInfosGroup);
			
			modes[0] = "Full Screen"; 			selModes[0]=config.getCfgBitState(config.CFGBIT_FULLSCREEN);
			modes[1] = "Render as streets"; 	selModes[1]=config.getCfgBitState(config.CFGBIT_STREETRENDERMODE);
			modes[2] = "Routing help"; 	selModes[2]=config.getCfgBitState(config.CFGBIT_ROUTING_HELP);
			modesGroup = new ChoiceGroup("Mode", Choice.MULTIPLE, modes ,null);
			modesGroup.setSelectedFlags(selModes);			
			append(modesGroup);

			gaugeDetailBoost = new Gauge("Increase Detail of lower Zoom Levels", true, 3, 0);
			append(gaugeDetailBoost);
			gaugeDetailBoost.setValue(config.getDetailBoost());
			
			addCommand(CMD_APPLY);
			addCommand(CMD_SAVE);
			addCommand(CMD_CANCEL);
			//addCommand(CMD_SETSTARTUPPOS);


			
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

		if (c == CMD_APPLY || c == CMD_SAVE) {			
			// determine if changes should be written to recordstore
			boolean setAsDefault=(c==CMD_SAVE);
			
			// convert boolean array with selection states for renderOpts
			// to one flag with corresponding bits set
	        elemsGroup.getSelectedFlags(selElems);
			config.setCfgBitState(config.CFGBIT_POITEXTS, selElems[0], setAsDefault);
			config.setCfgBitState(config.CFGBIT_POIS, selElems[1], setAsDefault);
			config.setCfgBitState(config.CFGBIT_AREATEXTS, selElems[2], setAsDefault);
			config.setCfgBitState(config.CFGBIT_WAYTEXTS, selElems[3], setAsDefault);
			config.setCfgBitState(config.CFGBIT_WPTTEXTS, selElems[4], setAsDefault);

			altInfosGroup.getSelectedFlags(selAltInfos);
			config.setCfgBitState(config.CFGBIT_SHOWLATLON, selAltInfos[0], setAsDefault);
			config.setCfgBitState(config.CFGBIT_SHOWWAYPOITYPE, selAltInfos[1], setAsDefault);

			modesGroup.getSelectedFlags(selModes);
			config.setCfgBitState(config.CFGBIT_FULLSCREEN, selModes[0], setAsDefault);
			config.setCfgBitState(config.CFGBIT_STREETRENDERMODE, selModes[1], setAsDefault);
			config.setCfgBitState(config.CFGBIT_ROUTING_HELP, selModes[2], setAsDefault);

			config.setDetailBoost(gaugeDetailBoost.getValue(), setAsDefault); 

			parent.show();
			return;
		}
	}
	
	public void show() {
		GpsMid.getInstance().show(this);
	}

}