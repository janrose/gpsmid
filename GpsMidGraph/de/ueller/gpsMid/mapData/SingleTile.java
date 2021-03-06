/*
 * GpsMid - Copyright (c) 2007 Harald Mueller james22 at users dot sourceforge dot net 
 * 			Copyright (c) 2008 Kai Krueger apm at users dot sourceforge dot net
 * See Copying
 */
package de.ueller.gpsMid.mapData;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import de.ueller.gps.data.Configuration;
import de.ueller.gps.data.SearchResult;
import de.ueller.midlet.gps.Logger;
import de.ueller.midlet.gps.Trace;
import de.ueller.midlet.gps.data.ProjMath;
import de.ueller.midlet.gps.data.Way;
import de.ueller.midlet.gps.tile.C;
import de.ueller.midlet.gps.tile.POIdescription;
import de.ueller.midlet.gps.tile.PaintContext;
import de.ueller.midlet.gps.tile.QueueableTile;

public class SingleTile extends Tile implements QueueableTile {

	public static final byte STATE_NOTLOAD = 0;

	public static final byte STATE_LOADSTARTED = 1;

	public static final byte STATE_LOADREADY = 2;

	private static final byte STATE_CLEANUP = 3;

	/**
	 * fpm is the fixed point multiplier used to convert
	 * latitude / logitude from radians to fixpoint representation
	 * 
	 * With this multiplier, one should get a resolution
	 * of 1m at the equator.
	 * 
	 * 6378159.81 = circumference of the earth in meters / 2 pi. 
	 * 
	 * This constant has to be in synchrony with the value in Osm2GpsMid
	 */	
	public static final float fpm = 6378159.81f;
    public static final float fpminv = 1/fpm; //Saves a floatingpoint devision

    
	// Node[] nodes;
	public short[] nodeLat;

	public short[] nodeLon;

	public int[] nameIdx;

	public byte[] type;

	Way[][] ways;

	byte state = 0;
	
	boolean abortPainting = false;

	private static Font poiFont;
	
	 private final static Logger logger= Logger.getInstance(SingleTile.class,Logger.DEBUG);

	public final byte zl;
	
	SingleTile(DataInputStream dis, int deep, byte zl) throws IOException {
//		 logger.debug("load " + deep + ":ST Nr=" + fileId);
		this.zl = zl;
		minLat = dis.readFloat();
		minLon = dis.readFloat();
		maxLat = dis.readFloat();
		maxLon = dis.readFloat();
		fileId = (short) dis.readInt();
	
//		 logger.debug("ready " + deep + ":ST Nr=" + fileId);
	}

	private boolean isDataReady() {
		if (state == STATE_NOTLOAD) {
			// logger.debug("singleTile start load " + fileId );
			state = STATE_LOADSTARTED;						
			Trace.getInstance().getDataReader().add(this,this);
			return false;
		}
		if (state == STATE_LOADSTARTED) {
			// logger.debug("singleTile wait for load " + fileId);
			// drawBounds(pc, 255, 255, 55);
			return false;
		}
		if (state == STATE_CLEANUP) {
			// logger.debug("singleTile wait for Cleanup " + fileId);			
			return false;
		}
		return true;

	}

	
	public void walk(PaintContext pc,int opt) {
		walk(pc, opt, Tile.LAYER_ALL);
	}
	
	public void paint(PaintContext pc, byte layer) {
		walk(pc, Tile.OPT_PAINT, layer);
	}
	
	private synchronized void walk(PaintContext pc,int opt, byte layer) {

		boolean renderArea = ((layer & Tile.LAYER_AREA) != 0);
		boolean renderAll = ((layer & Tile.LAYER_ALL) != 0);;
		boolean renderHighlight = ((layer & Tile.LAYER_HIGHLIGHT) != 0);
		byte relLayer = (byte)(((int)layer) & ~(Tile.LAYER_AREA | Tile.LAYER_HIGHLIGHT));
		
		
		if (pc.getP() == null) {
			logger.error("We don't have a projection to walk Tile");
			return;
		}
		
		if (contain(pc)) {
			if (!isDataReady()) {
				if ((opt & Tile.OPT_WAIT_FOR_LOAD) == 0){
					/**
					 * We don't have the data yet. No need to wait, we 
					 * will just render it the next time if the data is
					 * available then.
					 */
					//#debug debug
					logger.debug("Walk don't wait for TileData");
					return;
				} else {
					synchronized (this) {
						try {
							wait(1000);
							//#debug debug
							logger.debug("Walk Wait for TileData");
						} catch (InterruptedException e) {
						}
					}
				}
			}
			
			
			/**
			 * Calculate pc coordinates in terms of relative single tile coordinates
			 */
			
			float pcLDlatF = ((pc.getP().getMinLat() - this.centerLat) * SingleTile.fpm);
			float pcLDlonF = ((pc.getP().getMinLon() - this.centerLon) * SingleTile.fpm);
			float pcRUlatF = ((pc.getP().getMaxLat() - this.centerLat) * SingleTile.fpm);
			float pcRUlonF = ((pc.getP().getMaxLon() - this.centerLon) * SingleTile.fpm);
			short pcLDlat;
			short pcLDlon;
			short pcRUlat;
			short pcRUlon;
			
			if (pcLDlatF > Short.MAX_VALUE || pcLDlatF < Short.MIN_VALUE) {
				if (pcLDlatF > Short.MAX_VALUE) 
					pcLDlat = Short.MAX_VALUE;
				else
					pcLDlat = Short.MIN_VALUE;
			} else {
				pcLDlat = (short)pcLDlatF;
			}
			
			if (pcRUlatF > Short.MAX_VALUE || pcRUlatF < Short.MIN_VALUE) {
				if (pcRUlatF > Short.MAX_VALUE) 
					pcRUlat = Short.MAX_VALUE;
				else
					pcRUlat = Short.MIN_VALUE;
			} else {
				pcRUlat = (short)pcRUlatF;
			}
			if (pcLDlonF > Short.MAX_VALUE || pcLDlonF < Short.MIN_VALUE) {
				if (pcLDlonF > Short.MAX_VALUE) 
					pcLDlon = Short.MAX_VALUE;
				else
					pcLDlon = Short.MIN_VALUE;
			} else {
				pcLDlon = (short)pcLDlonF;
			}
			if (pcRUlonF > Short.MAX_VALUE || pcRUlonF < Short.MIN_VALUE) {
				if (pcRUlonF > Short.MAX_VALUE) 
					pcRUlon = Short.MAX_VALUE;
				else
					pcRUlon = Short.MIN_VALUE;
			} else {
				pcRUlon = (short)pcRUlonF;
			}

			lastUse = 0;
			if (layer != Tile.LAYER_NODE) {
				if (ways == null) {
					return;
				}

				for (int l = 0; l < ways.length; l++) {
					if (((relLayer != l) && !renderAll) || (ways[l] == null)) {
						continue;
					}

					/**
					 * Render all ways in the appropriate layer
					 */
					for (int i = 0; i < ways[l].length; i++) {
						if (abortPainting)
							return;
						Way w = ways[l][i];
						if (w == null) continue;
						//Determine if the way is an area or not. 
						if (w.isArea() != renderArea)
							continue;

						// logger.debug("test Bounds of way");
						if (!w.isOnScreen(pcLDlat, pcLDlon, pcRUlat, pcRUlon)) continue; 
						/**
						 * In addition to rendering we also check for which way
						 * corresponds to the target set in the paintcontext identified
						 * by the name of the way and the coordinates of a node on the way
						 */
						if (pc.target != null ) {
							//							logger.debug("search target nameIdx" );
							if (pc.target.entity == null && pc.target.nameIdx == w.nameIdx) {
								// 								logger.debug("search target way");
								/**
								 * The name of the way and the target matches, now we
								 * check if the coordinates match.
								 * 
								 * We have to be careful here, to not get into trouble
								 * with the 32bit float to 16bit short conversion.
								 * To prevent rounding issues, test for approximate
								 * equality
								 */
								short targetLat = (short)((pc.target.lat - centerLat)*fpm);
								short targetLon = (short)((pc.target.lon - centerLon)*fpm);
								for (int i1 = 0; i1 < w.path.length; i1++) {
									short s = w.path[i1];
									if ((Math.abs(nodeLat[s] - targetLat) < 2) && 
											(Math.abs(nodeLon[s] - targetLon) < 2)) {
										//										logger.debug("found Target way");
										pc.target.setEntity(w, w.getNodesLatLon(this, true), w.getNodesLatLon(this, false));
									}
								}
							}
						}
						if ((opt & Tile.OPT_PAINT) != 0){
							w.setColor(pc);
							if (!w.isArea()) {
								if (renderHighlight) {									
									if (pc.highlightedPathOnTop) {
										w.paintHighlightPath(pc, this, (byte) 0);
									}
								} else {
									w.paintAsPath(pc, this, relLayer);
									if (!pc.highlightedPathOnTop) {
										w.paintHighlightPath(pc, this, (byte) 0);
									}
								}
								
							} else {
								w.paintAsArea(pc, this);
							}
						} else if ((opt & Tile.OPT_CONNECTIONS2WAY) != 0) {
							if (!w.isArea()) {
								w.connections2WayMatch(pc, this);
							}
						} else if ((opt & Tile.OPT_FIND_CURRENT) != 0) {
							if (!w.isArea()) {
								w.processPath(pc, this, Tile.OPT_FIND_CURRENT, (byte) 0);
							}
						}
					}
				}
			} else {
				/**
				 * Drawing nodes
				 */
				if ((opt & Tile.OPT_PAINT) != 0){
					for (short i = 0; i < type.length; i++) {
						if (abortPainting)
							return;
						if (type[i] == 0) {
							break;
						}
						if (!isNodeOnScreen(i, pcLDlat, pcLDlon, pcRUlat, pcRUlon))
							continue;
						paintNode(pc, i);
					}
				}
			}
		}
	}

	public boolean cleanup(int level) {
		if (state != STATE_NOTLOAD ) {
			// logger.info("test tile unused fid:" + fileId + "c:"+lastUse);
			if (lastUse > level) {
				abortPainting = true;
				synchronized(this) {
				// nodes = null;
				state = STATE_CLEANUP;
				nameIdx = null;
				nodeLat = null;
				nodeLon = null;
				type = null;
				ways = null;
				state = STATE_NOTLOAD;
				}
				abortPainting = false;
				// logger.info("discard content for tile " + fileId);
				return true;
			}
		}
		return false;
	}

	public void dataReady() {
		lastUse = -1;
		state = STATE_LOADREADY;
	}

	public void paintNode(PaintContext pc, int i) {
		Image img = null;
		byte t=type[i];
		boolean hideable = C.isNodeHideable(t);
		
		byte om = C.getNodeOverviewMode(t);
		switch (om & C.OM_MODE_MASK) {
			case C.OM_SHOWNORMAL: 
				// if not in Overview Mode check for scale
				if (pc.scale > C.getNodeMaxScale(t)) {
					return;
				}
				// disabling POIs does not disable PLACE TEXTs (city, suburb, etc.) 
				if (! (t >= C.MIN_PLACETYPE && t <= C.MAX_PLACETYPE)) {
					if (hideable & !Configuration.getCfgBitState(Configuration.CFGBIT_POIS)) {
						return;
					}
				}
				break;
			case C.OM_HIDE: 				
				if (hideable) {
					return;
				}
				break;
		}
		switch (om & C.OM_NAME_MASK) {
			case C.OM_WITH_NAMEPART: 
				if (nameIdx[i] == -1) return;
				String name = pc.trace.getName(nameIdx[i]);
				if (name == null) return;
				if (name.toUpperCase().indexOf(C.get0Poi1Area2WayNamePart((byte) 0).toUpperCase()) == -1) return;
				break;
			case C.OM_WITH_NAME: 
				if (nameIdx[i] == -1) return;
				break;
			case C.OM_NO_NAME: 
				if (nameIdx[i] != -1) return;
				break;
		}
	
		pc.g.setColor(C.getNodeTextColor(t));
		img = C.getNodeImage(t);
		// logger.debug("calc pos "+pc);
		
		pc.getP().forward(nodeLat[i], nodeLon[i], pc.swapLineP, this);
		
		if (img != null ) {
			// logger.debug("draw img " + img);
			if (nameIdx[i] == -1 || C.isNodeImageCentered(t) || pc.scale > C.getNodeMaxTextScale(t)) {
				pc.g.drawImage(img, pc.swapLineP.x, pc.swapLineP.y,
						Graphics.VCENTER | Graphics.HCENTER);
			} else {
				pc.g.drawImage(img, pc.swapLineP.x, pc.swapLineP.y,
						Graphics.BOTTOM | Graphics.HCENTER);
			}
		}
		if (pc.scale > C.getNodeMaxTextScale(t)) {
			return;
		}
		
		// PLACE TEXTS (from city to suburb)
		if (t >= C.MIN_PLACETYPE && t <= C.MAX_PLACETYPE) {
			if (!Configuration.getCfgBitState(Configuration.CFGBIT_PLACETEXTS)) {
				return;
			}
		// OTHER POI texts
		} else {
			if (hideable && !Configuration.getCfgBitState(Configuration.CFGBIT_POITEXTS) ) {
				return;
			}
		}

		
		// logger.debug("draw txt " + );
		String name;
		if (Configuration.getCfgBitState(Configuration.CFGBIT_SHOWWAYPOITYPE)) {
			name = pc.c.getNodeTypeDesc(t);
		}
		else {
			name = pc.trace.getName(nameIdx[i]);
		}
		if (name != null) {			
			Font originalFont = pc.g.getFont();
			if (poiFont==null) {
				if (Configuration.getCfgBitState(Configuration.CFGBIT_POI_LABELS_LARGER)) {
					poiFont = originalFont;
				} else {
					poiFont=Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
				}
			}
			pc.g.setFont(poiFont);

			if (img == null) {
				pc.g.drawString(name, pc.swapLineP.x, pc.swapLineP.y,
						Graphics.BASELINE | Graphics.HCENTER);
			} else {
				if (C.isNodeImageCentered(t)){
					pc.g.drawString(name, pc.swapLineP.x, pc.swapLineP.y+8,
							Graphics.TOP | Graphics.HCENTER);						
				} else {
					pc.g.drawString(name, pc.swapLineP.x, pc.swapLineP.y,
							Graphics.TOP | Graphics.HCENTER);
				}
			}
			pc.g.setFont(originalFont);
		}
		
	}

	public String toString() {
		return "ST" + zl + "-" + fileId+ ":" + lastUse;
	}

/*   private float[] getFloatNodes(short[] nodes, float offset) {
	    float [] res = new float[nodes.length];
	    for (int i = 0; i < nodes.length; i++) {
		res[i] = nodes[i]*fpminv + offset;
	    }
	    return res;
	}
*/
   /**
    * Returns a Vector of SearchResult containing POIs of
    * type searchType close to lat/lon. The list is ordered
    * by distance with the closest one first.  
    */
   public Vector getNearestPoi(byte searchType, float lat, float lon, float maxDist) {	   
	   Vector resList = new Vector();
	   
	   if (!isDataReady()) {		   
		   synchronized(this) {
			   try {
				   /**
				    * Wait for the tile to be loaded in order to process it
				    * We should be notified once the data is loaded, but
				    * have a timeout of 500ms
				    */
				   wait(500);
			   } catch (InterruptedException e) {
				   /**
				    * Nothing to do in this case, as we need to deal
				    * with the case that nothing has been returned anyway
				    */
			   }			   
		   }
	   }
	   /**
	    * Try again and see if it has been loaded by now
	    * If not, then give up and skip this tile in order
	    * not to slow down surch too much
	    */
	   if (!isDataReady()) {		   
		   return new Vector();
	   }
	   
	   for (int i = 0; i < type.length; i++) {
		   if (type[i] == searchType) {
			   SearchResult sr = new SearchResult();
			   sr.lat = nodeLat[i]*fpminv + centerLat;
			   sr.lon = nodeLon[i]*fpminv + centerLon;
			   sr.nameIdx = nameIdx[i];
			   sr.type = (byte)(-1 * searchType); //It is a node. They have the top bit set to distinguish them from ways in search results
			   sr.dist = ProjMath.getDistance(sr.lat, sr.lon, lat, lon);
			   if (sr.dist < maxDist) {
				   resList.addElement(sr);				   
			   }
		   }
	   }
	   /**
	    * Perform a bubble sort on the distances of the search
	    * This is stupidly inefficient, but easy to code.
	    * Also we expect there only to be very few entries in
	    * the list, so shouldn't harm too much. 
	    */
	   boolean isSorted = false;
	   while(!isSorted) {
		   isSorted = true;
		   for (int i = 0; i < resList.size() - 1; i++) {
			   SearchResult a = (SearchResult) resList.elementAt(i);
			   SearchResult b = (SearchResult) resList.elementAt(i + 1);
			   if (a.dist > b.dist) {
				   resList.setElementAt(a, i + 1);
				   resList.setElementAt(b, i);
				   isSorted = false;
			   }
		   }
	   }
	   return resList;
   }
   
   private boolean isNodeOnScreen(int nodeId, short pcLDlat, short pcLDlon, short pcRUlat, short pcRUlon) {
	   short testLat=nodeLat[nodeId]; 
	   if (testLat < pcLDlat) {
		   return false;
	   }
	   if (testLat > pcRUlat) {
		   return false;
	   }
	   short testLon=nodeLon[nodeId]; 
	   if (testLon < pcLDlon) {
		   return false;
	   }
	   if (testLon > pcRUlon) {
		   return false;
	   }
	   return true;
   }
   
   public static void newPOIFont() {
	   poiFont = null;
   }   	
}
