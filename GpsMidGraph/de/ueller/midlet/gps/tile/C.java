package de.ueller.midlet.gps.tile;
/*
 * GpsMid - Copyright (c) 2007 Harald Mueller james22 at users dot sourceforge dot net 
 * See Copying
 */

public class C {
	public final static byte NODE_MASK_ROUTENODELINK=0x1;
	public final static byte NODE_MASK_TYPE=0x2;
	public final static byte NODE_MASK_NAME=0x4;
	public final static byte NODE_MASK_ROUTENODE=0x8;
	public final static byte NODE_MASK_NAMEHIGH=0x10;
	
	// node with name and no image
	public final static byte NODE_PLACE_CITY=1;
	public final static byte NODE_PLACE_TOWN=2;
	public final static byte NODE_PLACE_VILLAGE=3;
	public final static byte NODE_PLACE_HAMLET=4;
	public final static byte NODE_PLACE_SUBURB=5;
	/**
	 * minimum distances to set the is_in name to the next city
	 * to get the minimum distance use: <code>MAX_DIST_CITY[node.getType(null)]</code>
	 */
	public final static long[] MAX_DIST_CITY={0,8000,6000,3000,1000};
	
	// node with image and name place in the middle
	public final static byte NODE_AMENITY_PARKING=50;
	public static final byte NODE_AMENITY_SCHOOL = 51;
	public static final byte NODE_AMENITY_TELEPHONE = 52;
	public static final byte NODE_AMENITY_FUEL = 53;
	
	// node with image on node place the Image on the point
	public static final byte NODE_RAILWAY_STATION = 100;
	public static final byte NODE_AEROWAY_AERODROME = 101;
	public static final byte NODE_HIGHWAY_ROUNDABOUT = 102;
	
	
	public final static byte WAY_HIGHWAY_MOTORWAY=1;
	public final static byte WAY_HIGHWAY_MOTORWAY_LINK=2;
	public final static byte WAY_HIGHWAY_TRUNK=3;
	public final static byte WAY_HIGHWAY_PRIMARY=4;
	public final static byte WAY_HIGHWAY_SECONDARY=5;
	public final static byte WAY_HIGHWAY_MINOR=6;
	public final static byte WAY_HIGHWAY_RESIDENTIAL=7;
	public final static byte WAY_JUNCTION_ROUNDABOUT = 8;
	public final static byte WAY_HIGHWAY_TRACK=9;
	public final static byte WAY_HIGHWAY_UNCLASSIFIED=10;
	// ways after unclassified are not accessible by car
	public final static byte WAY_HIGHWAY_CYCLEWAY=11;
	public final static byte WAY_HIGHWAY_FOOTWAY=12;
	public final static byte WAY_RAILWAY_UNCLASSIFIED =13;
	public final static byte WAY_RAILWAY_RAIL = 14;
	public final static byte WAY_RAILWAY_SUBWAY = 15;
	public final static byte WAY_WATERWAY_RIVER=16;
	public final static byte WAY_HIGHWAY_STEPS=17;
	
	public final static byte AREA_AMENITY_UNCLASSIFIED=50;
	public final static byte AREA_AMENITY_PARKING=51;
	public final static byte AREA_AMENITY_PUBLIC_BUILDING=52;
	public final static byte AREA_NATURAL_WATER=60;
	public final static byte AREA_LEISURE_PARK=70;
	
	public final static byte AREA_LANDUSE_FARM=80;
	public final static byte AREA_LANDUSE_QUARRY=81;
	public final static byte AREA_LANDUSE_LANDFILL=82;
	public final static byte AREA_LANDUSE_BASIN=83;
	public final static byte AREA_LANDUSE_RESERVOIR=84;
	public final static byte AREA_LANDUSE_FOREST=85;
	public final static byte AREA_LANDUSE_ALLOTMENTS=86;
	public final static byte AREA_LANDUSE_RESIDENTIAL=87;
	public final static byte AREA_LANDUSE_RETAIL=88;
	public final static byte AREA_LANDUSE_COMMERCIAL=89;
	public final static byte AREA_LANDUSE_INDUSTRIAL=90;
	public final static byte AREA_LANDUSE_BROWNFIELD=91;
	public final static byte AREA_LANDUSE_GREENFIELD=92;
	public final static byte AREA_LANDUSE_CEMETERY=93;
	public final static byte AREA_LANDUSE_VILLAGE_GREEN=94;
	public final static byte AREA_LANDUSE_RECREATION_GROUND=95;
	
	public final static byte NAME_CITY=1;
	public final static byte NAME_SUBURB=2;
	public final static byte NAME_STREET=3;
	public final static byte NAME_AMENITY=4;

}