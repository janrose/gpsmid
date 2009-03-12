package de.ueller.gpsMid.mapData;
/*
 * GpsMid - Copyright (c) 2008 Kai Krueger apmonkey at users dot sourceforge dot net 
 * See Copying
 */

import javax.microedition.lcdui.Graphics;

import de.ueller.gps.tools.HelperRoutines;
import de.ueller.midlet.gps.Logger;
import de.ueller.midlet.gps.data.MoreMath;
import de.ueller.midlet.gps.tile.PaintContext;


public class GpxDisplayTile extends GpxTile {	
		
	public GpxDisplayTile() {
		super();
	}

	private void paintLocal(PaintContext pc) {
		/**
		 * Painting Tracklogs
		 */			
		for (int i = 0; i < noTrkPts; i++) {
			if (pc.getP().isPlotable(trkPtLat[i], trkPtLon[i])) {
				pc.getP().forward(trkPtLat[i], trkPtLon[i], pc.lineP2);
				pc.g.drawImage(pc.images.IMG_MARK_DISP, pc.lineP2.x, pc.lineP2.y, Graphics.HCENTER | Graphics.VCENTER);					
			}
		}
	}
	
}
