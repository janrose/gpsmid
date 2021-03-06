// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /home/kai/workspace/CVS-rsync/GpsMid/src/de/ueller/midlet/gps/data/Planet.java,v $
// $RCSfile: Planet.java,v $
// $Revision: 1.1 $
// $Date: 2007-03-29 17:47:46 $
// $Author: james22 $
// 
// **********************************************************************

package de.ueller.midlet.gps.data;



/**
 * Planet datums and parameters. These values are taken from John
 * Snyder's <i>Map Projections --A Working Manual </i> You should add
 * datums as needed, consult the ellips.dat file.
 */
public class Planet {

    // Solar system id's. Add new ones as needed.
    final public static transient int Earth = 3;
    final public static transient int Mars = 4;

    // WGS84 / GRS80 datums
    final public static transient float wgs84_earthPolarRadiusMeters = 6356752.3142f;
    final public static transient double wgs84_earthPolarRadiusMeters_D = 6356752.3142;
    final public static transient float wgs84_earthEquatorialRadiusMeters = 6378137.0f;
    final public static transient double wgs84_earthEquatorialRadiusMeters_D = 6378137.0;
    final public static transient float wgs84_earthFlat = 1 - (wgs84_earthPolarRadiusMeters / wgs84_earthEquatorialRadiusMeters);// 1 -
                                                                                                                                 // (minor/major)
                                                                                                                                 // =
                                                                                                                                 // 1/298.257
    final public static transient float wgs84_earthEccen = (float) Math.sqrt(2
            * wgs84_earthFlat - (wgs84_earthFlat * wgs84_earthFlat));// sqrt(2*f
                                                                     // -
                                                                     // f^2)
                                                                     // =
                                                                     // 0.081819221f

    final public static transient float wgs84_earthEquatorialCircumferenceMeters = MoreMath.TWO_PI
            * wgs84_earthEquatorialRadiusMeters;
    final public static transient float wgs84_earthEquatorialCircumferenceKM = wgs84_earthEquatorialCircumferenceMeters / 1000f;
    final public static transient float wgs84_earthEquatorialCircumferenceMiles = wgs84_earthEquatorialCircumferenceKM * 0.62137119f;//HACK
                                                                                                                                     // use
                                                                                                                                     // UNIX
                                                                                                                                     // units?
    final public static transient float wgs84_earthEquatorialCircumferenceNMiles = 21600.0f; // 60.0f
                                                                                             // *
                                                                                             // 360.0f;
                                                                                             // //
                                                                                             // sixty
                                                                                             // nm
                                                                                             // per
                                                                                             // degree

    final public static transient double wgs84_earthEquatorialCircumferenceMeters_D = MoreMath.TWO_PI_D
            * wgs84_earthEquatorialRadiusMeters_D;
    final public static transient double wgs84_earthEquatorialCircumferenceKM_D = wgs84_earthEquatorialCircumferenceMeters_D / 1000;
    final public static transient double wgs84_earthEquatorialCircumferenceMiles_D = wgs84_earthEquatorialCircumferenceKM_D * 0.62137119;//HACK
                                                                                                                                         // use
                                                                                                                                         // UNIX
                                                                                                                                         // units?
    final public static transient double wgs84_earthEquatorialCircumferenceNMiles_D = 21600.0; // 60.0f
                                                                                               // *
                                                                                               // 360.0f;
                                                                                               // //
                                                                                               // sixty
                                                                                               // nm
                                                                                               // per
                                                                                               // degree

    //      wgs84_earthEquatorialCircumferenceKM*0.5389892f; // calculated,
    // same as line above.
    //      wgs84_earthEquatorialCircumferenceKM*0.5399568f;//HACK use UNIX
    // units? << This was wrong.

    // Mars
    final public static transient float marsEquatorialRadius = 3393400.0f;// meters
    final public static transient float marsEccen = 0.101929f;// eccentricity
                                                              // e
    final public static transient float marsFlat = 0.005208324f;// 1-(1-e^2)^1/2

    // International 1974
    final public static transient float international1974_earthPolarRadiusMeters = 6356911.946f;
    final public static transient float international1974_earthEquatorialRadiusMeters = 6378388f;
    final public static transient float international1974_earthFlat = 1 - (international1974_earthPolarRadiusMeters / international1974_earthEquatorialRadiusMeters);// 1 -
                                                                                                                                                                     // (minor/major)
                                                                                                                                                                     // =
                                                                                                                                                                     // 1/297

    // Extra scale constant for better viewing of maps (do not use
    // this to
    // calculate anything but points to be viewed!)
    public transient static int defaultPixelsPerMeter = 3272;// 3384:
                                                             // mattserver/Map.C,
                                                             // 3488:
                                                             // dcw

    // cannot construct
    private Planet() {}
}
