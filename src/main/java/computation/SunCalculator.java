package computation;

import processing.core.PGraphics;
import wblut.geom.*;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * sun calculator for any location, date or time
 *
 * @author Wu
 * @ref https://www.pveducation.org/pvcdrom/properties-of-sunlight/solar-time
 * @create 2021-02-8 8:58
 */

public class SunCalculator {
    public static final int groundRadius = 1000;
    public static final int[] Nanjing = new int[]{117, 24};
    public static final int[] winterSolstice = new int[]{12, 22};
    public static final int[] noon = new int[]{12, 0};

    private static final WB_GeometryFactory gf = new WB_GeometryFactory();

    /**
     * count days from the start of a year (Jan. 1st)
     *
     * @param
     * @return
     */
    private static int countDays(int month, int day) {
        checkDate(month, day);
        int dayCount = day;
        for (int i = 1; i < month; i++) {
            if (i == 2) {
                dayCount += 28;
            } else if (i == 4 || i == 6 || i == 9 || i == 11) {
                dayCount += 30;
            } else {
                dayCount += 31;
            }
        }
        return dayCount;
    }

    private static double hhmm2hours(int hour, int minute) {
        checkTime(hour, minute);
        return hour + minute / 60.;
    }

    private static int[] hours2hhmm(double hours) {
        if (Double.isNaN(hours))
            return null;
        else {
            int hour = (int) Math.floor(hours);
            int minute = (int) Math.round((hours - hour) * 60);
            return new int[]{hour, minute};
        }
    }

    private static String[] hours2hhmmStr(double hours) {
        if (Double.isNaN(hours))
            return null;
        else {
            String[] hhmm = new String[2];
            int hour = (int) Math.floor(hours);
            if (hour < 10)
                hhmm[0] = "0" + hour;
            else
                hhmm[0] = Integer.toString(hour);

            int minute = (int) Math.round((hours - hour) * 60);
            if (minute < 10)
                hhmm[1] = "0" + minute;
            else
                hhmm[1] = Integer.toString(minute);

            return hhmm;
        }

    }

    /**
     * check if the input location is valid
     *
     * @param
     * @return
     */
    private static void checkLonLat(double lon, double lat) {
        if (lon <= -180 || lon >= 180)
            throw new IllegalArgumentException(
                    "Longitude must be between -180(west) and 180(east): " + lon);
        if (lat <= -90 || lat >= 90)
            throw new IllegalArgumentException(
                    "Latitude must be between -90(south) and 90(north): " + lat);
    }

    /**
     * check if the input date is valid
     *
     * @param
     * @return
     */
    private static void checkDate(int month, int day) {
        if (month < 1 || month > 12)
            throw new IllegalArgumentException(
                    "Month must be between 1 and 12: " + month);
        if (day < 1 || day > 31)
            throw new IllegalArgumentException(
                    "Day must be between 1 and 31: " + day);
        if (month == 2 && day > 28)
            throw new IllegalArgumentException(
                    "Day must be less than 29 in February: " + day);
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            if (day > 30)
                throw new IllegalArgumentException(
                        "Day must be less than 31 in April, June, September, November: " + day);
        }
    }

    /**
     * check if input time is valid
     *
     * @param
     * @return
     */
    private static void checkTime(int hour, int minute) {
        if (hour == 24 && minute == 0)
            return;
        if (hour < 0 || hour > 23)
            throw new IllegalArgumentException(
                    "Hour must be between 0 and 23: " + hour);
        if (minute < 0 || minute > 59)
            throw new IllegalArgumentException(
                    "Minute must be between 00 and 59: " + minute);
    }

    private static void checkTime(double hours) {
        if (hours < 0 || hours > 24)
            throw new IllegalArgumentException(
                    "Hours must be between 0 and 24: " + hours);
    }

    private static double boundTrigonometry(double d) {
        return Math.max(Math.min(d, 1), -1);
    }

    /*=========================================================================================*/

    private double longitude, latitude, lonRad, latRad;
    private int month, day;
    private double localTime;
    private int[] location, date, time;

    private double delta, deltaRad;
    private int dayCounter;
    private double TC;

    /**
     * elevation in radians at specified time
     */
    private double alpha;
    /**
     * azimuth in radians at specified time
     */
    private double azimuth;

    private WB_Vector pos;
    private WB_Circle ground;
    private int pathDiv = 30;
    private WB_PolyLine path;
    private boolean polar;

    public SunCalculator() {
        setLocalPosition(Nanjing[0], Nanjing[1]);
        setDate(winterSolstice[0], winterSolstice[1]);
        setTime(noon[0], noon[1]);

        ground = gf.createCircleWithRadius(WB_Vector.ZERO(), groundRadius);
        polar = false;
    }

    public SunCalculator(double lon, double lat) {
        setLocalPosition(lon, lat);
        setDate(winterSolstice[0], winterSolstice[1]);
        setTime(noon[0], noon[1]);

        ground = gf.createCircleWithRadius(WB_Vector.ZERO(), groundRadius);
        polar = false;
    }

    public void setLocalPosition(double lon, double lat) {
        checkLonLat(lon, lat);
        longitude = lon;
        lonRad = Math.toRadians(longitude);
        latitude = lat;
        latRad = Math.toRadians(latitude);

        location = new int[]{(int) longitude, (int) latitude};
    }

    public void setDate(int month, int day) {
        checkDate(month, day);
        this.month = month;
        this.day = day;
        dayCounter = countDays(month, day);
        date=new int[]{this.month, this.day};

        delta = calDeclination();
        deltaRad = Math.toRadians(delta);
        TC = calTC();
    }

    public void setTime(double hours) {
        checkTime(hours);
        localTime = hours;
        time = hours2hhmm(hours);

        alpha = calElevation();
        azimuth = calAzimuth();
        pos = new WB_Vector(
                Math.cos(alpha) * Math.sin(azimuth),
                Math.cos(alpha) * Math.cos(azimuth),
                Math.sin(alpha))
                .mul(groundRadius);
    }

    public void setTime(int hour, int minute) {
        setTime(hhmm2hours(hour, minute));
    }

    public void setPathDiv(int div) {
        this.pathDiv = div;
    }

    public int[] getLocation() {
        return location;
    }

    public int[] getDate() {
        return date;
    }

    public int[] getTime() {
        return time;
    }

    public void printInfo() {
        String[] curTime = hours2hhmmStr(localTime);
        System.out.printf("(%.2f, %.2f) %d-%d %s:%s\n",
                longitude, latitude, month, day, curTime[0], curTime[1]);
        System.out.printf("Equation of Time\t%.2f minutes\n", calEoT());
        System.out.printf("Local Solar Time Meridian\t%.2f°\n", calLSTM());
        System.out.printf("Time Correction\t%.2f minutes\n", TC);
        System.out.printf("Declination\t%.2f°\n", delta);
        System.out.printf("Hour Angle\t%.2f°\n", calHRA());
        System.out.printf("Elevation\t%.2f°\n", Math.toDegrees(alpha));

        String[] LST = hours2hhmmStr(calLST());
        System.out.printf("Local Solar Time\t%s:%s\n", LST[0], LST[1]);

        System.out.printf("Azimuth\t%.2f°\n", Math.toDegrees(azimuth));

        double[] sunriseSunset = calSunriseSunset();
        String[] sunrise = hours2hhmmStr(sunriseSunset[0]);
        if (null != sunrise)
            System.out.printf("Sunrise\t%s:%s\n", sunrise[0], sunrise[1]);
        else
            System.out.printf("Sunrise\tNaN\n");
        String[] sunset = hours2hhmmStr(sunriseSunset[1]);
        if (null != sunset)
            System.out.printf("Sunset\t%s:%s\n", sunset[0], sunset[1]);
        else
            System.out.printf("Sunset\tNaN\n");
    }

    /**
     * Equation of Time (EoT): 用于校正地球轨道的偏心率和地轴倾斜
     * in minutes
     *
     * @param
     * @return
     */
    private double calEoT() {
        double bRad = Math.toRadians(calB());
        return 9.87 * Math.sin(2 * bRad) - 7.53 * Math.cos(bRad) - 1.5 * Math.sin(bRad);
    }

    /**
     * in degrees
     *
     * @param
     * @return
     */
    private double calB() {
        return 360. / 365 * (dayCounter - 81);
    }

    /**
     * Local Standard Time Meridian (LSTM)
     * in degrees
     * deltaGMT: difference of the Local Time (LT)
     * from Greenwich Mean Time (GMT) in hours
     *
     * @param
     * @return
     */
    private double calLSTM() {
        int deltaGMT = (int) Math.round(longitude / 15);
        return 15 * deltaGMT;
    }

    /**
     * Time Correction Factor (TC)
     * in minutes
     *
     * @param
     * @return
     */
    private double calTC() {
        double LSTM = calLSTM();
        return 4 * (longitude - LSTM) + calEoT();
    }

    /**
     * Local Solar Time (LST)
     * in hours
     *
     * @param
     * @return
     */
    private double calLST() {
        return localTime + TC / 60;
    }

    /**
     * Hour Angle (HRA)
     * in degrees
     * between -180° and 180°
     * noon - 0°
     * A.M. - negative
     * P.M. - positive
     *
     * @param
     * @return
     */
    private double calHRA() {
        double LST = calLST();
        return 15 * (LST - 12);
    }

    /**
     * declination [delta] - 偏角
     * in degrees
     *
     * @param
     * @return
     */
    private double calDeclination() {
        return 23.45 * Math.sin(Math.toRadians(calB()));
    }

    /**
     * Elevation [alpha] - 仰角
     * in radians
     * between 0 and PI/2
     *
     * @param
     * @return
     */
    private double calElevation() {
        double sine = Math.sin(deltaRad) * Math.sin(latRad) +
                Math.cos(deltaRad) * Math.cos(latRad) * Math.cos(
                        Math.toRadians(calHRA()));
        alpha = Math.asin(boundTrigonometry(sine));
        return alpha;
    }

    /**
     * max elevation - 最大仰角
     * in radians
     *
     * @param
     * @return
     */
    private double calMaxElevation() {
        return Math.PI / 2 + latRad - deltaRad;
    }

    /**
     * Azimuth - 方位角
     * in radians
     * north - 0
     * east - PI/2
     * south - PI
     * west - PI*3/2
     *
     * @param
     * @return
     */
    private double calAzimuth() {
        double cosine = (Math.sin(deltaRad) * Math.cos(latRad) -
                Math.cos(deltaRad) * Math.sin(latRad) * Math.cos(Math.toRadians(calHRA())))
                / Math.cos(alpha);
        azimuth = Math.acos(boundTrigonometry(cosine));
        if (calLST() > 12)
            azimuth = Math.PI * 2 - azimuth;

        return azimuth;
    }

    /**
     * sunrise time & sunset time
     *
     * @param
     * @return
     */
    private double[] calSunriseSunset() {
        double cosine = -Math.tan(latRad) * Math.tan(deltaRad);
        double sunrise = 12 - 1 / Math.toRadians(15) *
                Math.acos(boundTrigonometry(cosine)) - TC / 60;
        double sunset = 12 + 1 / Math.toRadians(15) *
                Math.acos(boundTrigonometry(cosine)) - TC / 60;

        if (sunrise < 0) {
            polar = true;
            sunrise = 0;
            sunset = hhmm2hours(23, 59);
        }

        if (sunrise == sunset) {
            polar = true;
            sunrise = 0;
            sunset = 0;
        }

        return new double[]{sunrise, sunset};
    }

    public void calSunPath() {
        double curTime = localTime;
        List<WB_Vector> pathPoints = new ArrayList<>();
        double[] sunriseSunset = calSunriseSunset();
        double step = /*(sunriseSunset[1] - sunriseSunset[0])*/24. / (pathDiv - 1);

        for (int i = 0; i < pathDiv; i++) {
            double tempTime = /*sunriseSunset[0] +*/ i * step;
            this.setTime(tempTime);
            pathPoints.add(pos);
        }

//        if (polar)
        pathPoints.add(pathPoints.get(0));
        path = gf.createPolyLine(pathPoints);
        setTime(curTime);
    }

    public void display(WB_Render render) {
        PGraphics app = render.getHome();

        app.pushStyle();
        app.fill(200);
        render.drawCircle(ground);

        app.noFill();
        app.stroke(255, 0, 0);
        app.strokeWeight(30);
        app.point(pos.xf(), pos.yf(), pos.zf());
        app.popStyle();
    }

    public void displayPath(WB_Render render) {
        PGraphics app = render.getHome();
        render.drawCircle(ground);

        app.pushStyle();
        app.stroke(0, 0, 255);
        app.strokeWeight(3);
        render.drawPolyLine(path);

//        for (int i = 0; i < path.getNumberOfPoints(); i++) {
//            WB_Coord v = path.getPoint(i);
//
//            app.fill(0);
//            app.textSize(50);
//            app.text(Integer.toString(i), v.xf(), v.yf(), v.zf());
//
//            app.stroke(150, 100, 0);
//            app.strokeWeight(10);
//            app.point(v.xf(), v.yf(), v.zf());
//        }

        app.popStyle();
    }

}
