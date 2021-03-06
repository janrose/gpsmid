package de.ueller.midlet.gps;
/*
 * GpsMid - Copyright (c) 2008 sk750 at users dot sourceforge dot net 
 * See Copying
 */

import javax.microedition.lcdui.*;

import de.ueller.gps.data.Configuration;
import de.ueller.gpsMid.mapData.SingleTile;
import de.ueller.midlet.gps.importexport.ObexExportSession;
import de.ueller.midlet.gps.tile.C;
import de.ueller.midlet.gps.tile.POIdescription;
import de.ueller.midlet.gps.tile.WayDescription;

public class GuiOverviewElements extends Form implements CommandListener, ItemStateListener {
	private ChoiceGroup ovElGroupCG;
	private ChoiceGroup ovElNameRequirementCG;
	private ChoiceGroup ovElHideOtherCG;

	private TextField fldNamePart; 
	
	private Image areaPict = null;
	
	private ChoiceGroup ovElSelectionCG;

	private static boolean[] showOther = new boolean[3];
	private static byte[] nameRequirement = new byte[3];
	private static byte ovElGroupNr = 0;
	
	// commands
	private static final Command CMD_OK = new Command("Ok", Command.OK, 1);
	private static final Command CMD_OFF = new Command("Off", Command.ITEM, 2);
	
	// other
	private Trace parent;
	
	public int frmMaxEl = 0;
	public boolean variableGroupsAdded = false;
	public boolean namePartFieldAdded = false;
	public boolean hideOtherGroupAdded = false;
	
	public GuiOverviewElements(Trace tr) {
		super("Overview/Filter Map");
		this.parent = tr;
		try {
			ovElGroupCG = new ChoiceGroup("Element Type: ", ChoiceGroup.EXCLUSIVE);
			ovElGroupCG.append("POIs", null);
			ovElGroupCG.append("Areas", null);
			ovElGroupCG.append("Ways", null);
			ovElGroupCG.setSelectedIndex(ovElGroupNr, true);
			append(ovElGroupCG);
			setItemStateListener(this);
			
			variableGroupsAdded = false;
			itemStateChanged(ovElGroupCG);

			addCommand(CMD_OK);
			addCommand(CMD_OFF);
			
			// Set up this Displayable to listen to command events
			setCommandListener(this);
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void applyElGroupElementStates() {
		byte count = 0;
		byte nonOverviewMode = C.OM_SHOWNORMAL;
		byte overviewMode = C.OM_OVERVIEW;
		byte nameReq = C.OM_NAME_ALL;
		
		nameRequirement[ovElGroupNr] = (byte) ovElNameRequirementCG.getSelectedIndex();
		switch (nameRequirement[ovElGroupNr]) {
			case 1:
				nameReq = C.OM_NO_NAME;				
				break;
			case 2:				
				nameReq = C.OM_WITH_NAME;				
				break;
			case 3:				
				nameReq = C.OM_WITH_NAMEPART;				
				break;
		}
		if (namePartFieldAdded) {
			parent.pc.c.set0Poi1Area2WayNamePart(ovElGroupNr, fldNamePart.getString());
		}
		if (hideOtherGroupAdded) {
			showOther[ovElGroupNr] = ovElHideOtherCG.isSelected(0);
		}
		// default to put all elements in "silent" overview mode
		// if there's a name requirement but no overview element at all selected
		if (nameReq != C.OM_NAME_ALL) {
			nonOverviewMode = C.OM_OVERVIEW2;
		}
		if ( !showOther[ovElGroupNr] || nameReq != C.OM_NAME_ALL) {
			// only hide non-overview elements if at least one overview element is selected
			// This is implemented so because otherwise simple switching between POIs, areas and ways would
			// cause to disappear all the elements in the list unintentionally
			for(byte i = 0; i < ovElSelectionCG.size(); i++ ) {
				if (ovElSelectionCG.isSelected(i)) {
					nonOverviewMode = C.OM_HIDE;
					break;
				}
			}
		}
		nonOverviewMode |= nameReq;
		overviewMode |= nameReq;
		
		switch (ovElGroupNr) {
			case 0:
				// save overview mode state to node description
				for (byte i = 1; i < parent.pc.c.getMaxType(); i++) {				
					if (parent.pc.c.isNodeHideable(i)) {
						parent.pc.c.setNodeOverviewMode(i, ovElSelectionCG.isSelected(count)?overviewMode:nonOverviewMode);
						count++;
					}
				}
				break;
			case 1:
				// save overview mode state to 'area' description
				for (byte i = 1; i < parent.pc.c.getMaxWayType(); i++) {				
					WayDescription w = parent.pc.c.getWayDescription(i);
					if (w.isArea && parent.pc.c.isWayHideable(i) ) {
						parent.pc.c.setWayOverviewMode(i, ovElSelectionCG.isSelected(count)?overviewMode:nonOverviewMode);
						count++;
					}
				}
				break;
			case 2:
				// save overview mode state to way description
				for (byte i = 1; i < parent.pc.c.getMaxWayType(); i++) {				
					WayDescription w = parent.pc.c.getWayDescription(i);
					if (!w.isArea && parent.pc.c.isWayHideable(i) ) {
						parent.pc.c.setWayOverviewMode(i, ovElSelectionCG.isSelected(count)?overviewMode:nonOverviewMode);
						count++;
					}
				}
				break;
		}
		
	}
	
	
	public void commandAction(Command c, Displayable d) {
		if (c == CMD_OK) {				
			ovElGroupNr = (byte) ovElGroupCG.getSelectedIndex();
			applyElGroupElementStates();
			parent.show();
			return;
		}
		if (c == CMD_OFF) {			
			parent.pc.c.clearAllNodesOverviewMode();
			parent.pc.c.clearAllWaysOverviewMode();
			parent.show();
			return;
		}
	}
	
	public void itemStateChanged(Item item) {
		if (item == ovElGroupCG) {			
			// only delete variable Choice groups if they were added
			if (variableGroupsAdded) {
				applyElGroupElementStates();
				delete(frmMaxEl); // element list				
				frmMaxEl--;
				delete(1); // nameReq
				frmMaxEl--;
			}

			byte count=0;

			// Warning: do not move this up - it must be after applyElGroupElementStates()
			ovElGroupNr = (byte) ovElGroupCG.getSelectedIndex();
			String ovElGroupName = ovElGroupCG.getString(ovElGroupNr); 

			// set NameRequirement state in form
			ovElNameRequirementCG = new ChoiceGroup("Name Check", ChoiceGroup.EXCLUSIVE);
			ovElNameRequirementCG.append("off", null);
			ovElNameRequirementCG.append("only unnamed " + ovElGroupName, null);
			ovElNameRequirementCG.append("only named " + ovElGroupName, null);
			ovElNameRequirementCG.append(ovElGroupName + " containing...", null);
			ovElNameRequirementCG.setSelectedIndex(nameRequirement[ovElGroupNr], true); 

			// set None-Overview state in form
			ovElHideOtherCG = new ChoiceGroup("Non-Overview " + ovElGroupName, ChoiceGroup.EXCLUSIVE);
			ovElHideOtherCG.append("Show normally", null);
			ovElHideOtherCG.append("Filter out", null);			
			if (showOther[ovElGroupNr]) {
				ovElHideOtherCG.setSelectedIndex(0, true);
			} else {
				ovElHideOtherCG.setSelectedIndex(1, true);				
			}

			ovElSelectionCG = new ChoiceGroup("Overview " + ovElGroupName, ChoiceGroup.MULTIPLE);
			switch (ovElGroupNr) {
				case 0:
					// set POI overview states in form				
					for (byte i = 1; i < parent.pc.c.getMaxType(); i++) {				
						if (parent.pc.c.isNodeHideable(i)) {
							ovElSelectionCG.append(parent.pc.c.getNodeTypeDesc(i), parent.pc.c.getNodeSearchImage(i));
							ovElSelectionCG.setSelectedIndex(count, ((parent.pc.c.getNodeOverviewMode(i) & C.OM_MODE_MASK) == C.OM_OVERVIEW) );
							count++;
						}
					}
					break;
				case 1:
					// set Area overview states in form
					for (byte i = 1; i < parent.pc.c.getMaxWayType(); i++) {				
						WayDescription w = parent.pc.c.getWayDescription(i);
						if (w.isArea && parent.pc.c.isWayHideable(i) ) {
							ovElSelectionCG.append(w.description, areaImage(w.lineColor));
							ovElSelectionCG.setSelectedIndex(count, ((parent.pc.c.getWayOverviewMode(i) & C.OM_MODE_MASK) == C.OM_OVERVIEW) );
							count++;
						}
					}
					break;
				case 2:
					// set Way overview  states in form
					for (byte i = 1; i < parent.pc.c.getMaxWayType(); i++) {				
						WayDescription w = parent.pc.c.getWayDescription(i);
						if (!w.isArea && parent.pc.c.isWayHideable(i) ) {
							ovElSelectionCG.append(w.description, wayImage(w));
							ovElSelectionCG.setSelectedIndex(count, ((parent.pc.c.getWayOverviewMode(i) & C.OM_MODE_MASK) == C.OM_OVERVIEW) );
							count++;
						}
					}
					break;
			}
			append(ovElSelectionCG);
			frmMaxEl++;
			insert(1, ovElNameRequirementCG);
			frmMaxEl++;
			variableGroupsAdded = true;
			ovElGroupNr = (byte) ovElGroupCG.getSelectedIndex();
		}
		ovElGroupNr = (byte) ovElGroupCG.getSelectedIndex();
		String ovElGroupName = ovElGroupCG.getString(ovElGroupNr); 
		if (item == ovElNameRequirementCG || item == ovElGroupCG) {
			if (hideOtherGroupAdded) {
				frmMaxEl--;
				delete(frmMaxEl);
				hideOtherGroupAdded = false;
			}
			if ((byte) ovElNameRequirementCG.getSelectedIndex() == 0) {
				insert(frmMaxEl, ovElHideOtherCG);
				frmMaxEl++;
				hideOtherGroupAdded = true;
			}
			if (namePartFieldAdded) {
				delete(2);
				frmMaxEl--;
				namePartFieldAdded = false;
			}
			if ((byte) ovElNameRequirementCG.getSelectedIndex() == 3) { 
				fldNamePart = new TextField("...this name part:", parent.pc.c.get0Poi1Area2WayNamePart(ovElGroupNr), 20, TextField.ANY);
				insert(2, fldNamePart);
				frmMaxEl++;
				namePartFieldAdded = true;
			}
		}
	}
	
	private static Image areaImage(int color)
    {        
        Image img = Image.createImage(16,16);
        Graphics g = img.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 16, 16);
        return img;        
    }

	private Image wayImage(WayDescription w)
    {        
        Image img = Image.createImage(16,16);
        Graphics g = img.getGraphics();
        g.setColor(parent.pc.c.BACKGROUND_COLOR);
        g.fillRect(0, 0, 16, 16);
        g.setColor(w.lineColor);
        if (w.wayWidth == 1 || !Configuration.getCfgBitState(Configuration.CFGBIT_STREETRENDERMODE)) {
        	g.setStrokeStyle(w.lineStyle);
        	g.drawLine(0, 8, 15, 8);
        } else {
        	g.fillRect(0, (16-w.wayWidth)/2, 16, w.wayWidth);
        	g.setColor(w.boardedColor);
        	g.drawLine(0, (16-w.wayWidth)/2-1, 15, (16-w.wayWidth)/2-1);
        	g.drawLine(0, (16-w.wayWidth)/2 + w.wayWidth, 15, (16-w.wayWidth)/2 + w.wayWidth);
        }
        return img;
    }

	public void show() {
		GpsMid.getInstance().show(this);
	}

}
