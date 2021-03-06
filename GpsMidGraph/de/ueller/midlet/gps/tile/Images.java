package de.ueller.midlet.gps.tile;

import java.io.IOException;

import javax.microedition.lcdui.Image;

/**
 * holds all the references to images and icons used in the programm
 */
public class Images {
	/** used for currently recording gpx-tracks */
	public final Image IMG_MARK = Image.createImage("/mark.png");
	/** used for gpx-tracks loaded from the database */
	public final Image IMG_MARK_DISP = Image.createImage("/mark_display.png");
	/*public final Image IMG_PARKING=Image.createImage("/parking.png");
	public final Image IMG_FUEL=Image.createImage("/fuel.png");
	public final Image IMG_SCHOOL=Image.createImage("/school.png");
	public final Image IMG_TELEPHONE=Image.createImage("/telephone.png");
	public final Image IMG_RAILSTATION=Image.createImage("/railstation.png");
	public final Image IMG_AERODROME=Image.createImage("/aerodrome.png");*/
	/** icon for the routing target point */
	public final Image IMG_TARGET=Image.createImage("/target.png");
	public final Image IMG_STRAIGHTON=Image.createImage("/straighton.png");
	public final Image IMG_LEFT=Image.createImage("/left.png");
	public final Image IMG_RIGHT=Image.createImage("/right.png");
	public final Image IMG_HALFLEFT=Image.createImage("/halfleft.png");
	public final Image IMG_HALFRIGHT=Image.createImage("/halfright.png");
	public final Image IMG_HARDLEFT=Image.createImage("/hardleft.png");
	public final Image IMG_HARDRIGHT=Image.createImage("/hardright.png");
	public final Image IMG_MOTORWAYENTER=Image.createImage("/motorway.png");
	public final Image IMG_MOTORWAYLEAVE=Image.createImage("/motorway_exit.png");
	public final Image IMG_TUNNEL_INTO=Image.createImage("/tunnel.png");
	public final Image IMG_TUNNEL_OUT_OF=Image.createImage("/tunnel_end.png");
	public Images() throws IOException{
		
	}
}
