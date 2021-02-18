package guo;

import gzf.gui.Vec_Guo;

/**
 * get the solar position of any time on any day of a year.
 * 
 * @author guoguo
 *
 */
public class SolarCalculator {
	public static final double TO_DEG = 180 / Math.PI;
	public static final double TO_RAD = Math.PI / 180;

	/**
	 * Axial tilt, also known as obliquity, is the angle between an object's
	 * rotational axis and its orbital axis (here is the earth).
	 */
	public static final double ANG_OBLIQUITY = 23.43333;

	/**
	 * The number of days in a year
	 */
	public static final int DAY_YEAR = 365;

	/**
	 * date of the summer solstice
	 */
	public static final int SUMMER_MONTH = 6;
	public static final int SUMMER_DAY = 22;
	/**
	 * 
	 */
	public static final int DAY_TO_START_SUMMER = dayToYearStart(SUMMER_MONTH, SUMMER_DAY);

	/**
	 * date of the winter solstice
	 */
	public static final int WINTER_MONTH = 12;
	public static final int WINTER_DAY = 22;
	public static final int DAY_TO_START_WINTER = dayToYearStart(WINTER_MONTH, WINTER_DAY);

	public static final Vec_Guo DIRECTION_EAST = new Vec_Guo(1, 0, 0);
	public static final Vec_Guo DIRECTION_NORTH = new Vec_Guo(0, 1, 0);

	/**
	 * 
	 * @param hour
	 * @param minute
	 */
	public static void checkTime(int hour, int minute) {
		if (hour < 0 || hour > 23)
			throw new IllegalArgumentException("Hour must between 0 and 23: " + hour);
		if (minute < 0 || minute > 59)
			throw new IllegalArgumentException("Minute must between 0 and 59: " + minute);
	}

	/**
	 * check if the input longitude is valid.
	 * 
	 * @param l
	 */
	public static void checkLongitude(double l) {
		if (l <= -90 || l >= 90)
			throw new IllegalArgumentException("Longitude must between -90(south) and 90(north): " + l);
	}

	/**
	 * check if the input data is valid.
	 * 
	 * @param month
	 * @param day
	 */
	public static void checkDate(int month, int day) {
		if (month < 1 || month > 12)
			throw new IllegalArgumentException("Month must between 1 and 12: " + month);
		if (day < 1 || day > 31)
			throw new IllegalArgumentException("Day must between 1 and 31: " + day);
		if (month == 2 && day > 28)
			throw new IllegalArgumentException("Day must less than 29 in February: " + day);
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			if (day > 30)
				throw new IllegalArgumentException("Day must less than 31 in Aprial, Jun, September, November: " + day);
		}
	}

	/**
	 * number of days to the start of a year (Jan. 1st)
	 * 
	 * @param month
	 * @param day
	 * @return
	 */
	public static int dayToYearStart(int month, int day) {
		checkDate(month, day);
		int day_sum = day;
		for (int i = 1; i < month; i++) {
			if (i == 2) {
				day_sum += 28;
			} else if (i == 4 || i == 6 || i == 9 || i == 11) {
				day_sum += 30;
			} else {
				day_sum += 31;
			}
		}
		return day_sum;
	}

	/**
	 * map hour between 0(00:00) and 1(23:59)
	 * 
	 * @param hour
	 * @param minute
	 * @return
	 */
	public static double getHourRatio(int hour, int minute) {
		checkTime(hour, minute);
		return (hour * 60.0 + minute) / (60.0 * 24.0);
	}

	/**
	 * map date between 0(after 22nd Jun) and 1(before 22nd Jun)
	 * 
	 * @param month
	 * @param day
	 * @return
	 */
	public static double getDayRatio(int month, int day) {
		int dayToStart = dayToYearStart(month, day);
		dayToStart -= DAY_TO_START_SUMMER;
		if (dayToStart < 0)
			dayToStart += DAY_YEAR;

		return (double) dayToStart / DAY_YEAR;
	}

	/**
	 * longitude(deg)
	 */
	private double longitude = 0;
	/**
	 * solar direct shoot position
	 */
	private double solarLongitude = 0;
	/**
	 * solar elevation angle in mid day
	 */
	private double solarElevAngMid = Math.PI / 2;

	private double dayRatio = 0.25;

	private Vec_Guo solarCircleBottom = new Vec_Guo(0, 0, -1);
	private Vec_Guo solarOutDirection = DIRECTION_EAST.dup();

	private Vec_Guo solarCircleNormal = DIRECTION_NORTH.dup().rev();
	private Vec_Guo d = new Vec_Guo();

	/**
	 * initiate a calculator
	 * 
	 * @param l
	 *            longitude of the place to be calculated, positive for northern
	 *            hemisphere and negative for southern hemisphere
	 */
	public SolarCalculator(double l) {
		setPosition(l);
	}

	/**
	 * set longitude of the place to be calculated.
	 * 
	 * @param l
	 *            longitude, positive for northern hemisphere and negative for
	 *            southern hemisphere
	 */
	public void setPosition(double l) {
		checkLongitude(l);
		longitude = l;

		double rotAng = longitude * TO_RAD;
		solarCircleBottom = Vec_Guo.zaxis.dup().rot(Vec_Guo.xaxis, rotAng).rev();
		solarCircleNormal = DIRECTION_NORTH.dup().rot(Vec_Guo.xaxis, rotAng).rev();
	}

	/**
	 * set date to be calculated
	 * 
	 * @param month
	 * @param day
	 */
	public void setDate(int month, int day) {
		setDate(getDayRatio(month, day));
		// dayRatio = getDayRatio(month, day);
		// solarLongitude = Math.cos(dayRatio * Math.PI * 2) * ANG_OBLIQUITY;
		//
		// // does not take part into the calculation process
		// solarElevAngMid = TO_RAD * (90 - Math.abs(longitude) +
		// solarLongitude);
		//
		// double angToNormal = TO_RAD * (90 + solarLongitude);
		// double dist = 1 / Math.tan(angToNormal);
		//
		// d = solarCircleNormal.dup().mul(dist);
	}

	/**
	 * set date to be calculated
	 * 
	 * @param ratio
	 *            between 0(after 22nd Jun) and 1(before 22nd Jun in next year)
	 */
	public void setDate(double ratio) {
		dayRatio = ratio;
		solarLongitude = Math.cos(dayRatio * Math.PI * 2) * ANG_OBLIQUITY;

		// does not take part into the calculation process
		solarElevAngMid = TO_RAD * (90 - Math.abs(longitude) + solarLongitude);

		double angToNormal = TO_RAD * (90 + solarLongitude);
		double dist = 1 / Math.tan(angToNormal);

		d = solarCircleNormal.dup().mul(dist);
	}

	/**
	 * get the solar elevation angle in the middle of the day
	 * 
	 * @return
	 */
	public double getSolarElevationAngleMid() {
		return solarElevAngMid;
	}

	/**
	 * return the solar position which is represented by a vector on a unit
	 * sphere at a given time and a given date.
	 * 
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @return
	 */
	public Vec_Guo getSolarPosition(int month, int day, int hour, int minute) {
		setDate(month, day);
		return getSolarPosition(hour, minute);
	}

	/**
	 * return the solar position, which is represented by a vector on a unit
	 * sphere, at a given time.
	 * 
	 * @param hour
	 *            from 0 to 23
	 * @param minute
	 *            from 0 to 59
	 * @return
	 */
	public Vec_Guo getSolarPosition(int hour, int minute) {
		double hourRatio = getHourRatio(hour, minute);
		return getSolarPosition(hourRatio);
	}

	/**
	 * return the solar position, which is represented by a vector on a unit
	 * sphere, at a given time.
	 * 
	 * @param hourRatio
	 *            between 0 and 1, indicates from 0:00 to 23:59
	 * @return
	 */
	public Vec_Guo getSolarPosition(double hourRatio) {
		double hourRatioAng = hourRatio * Math.PI * 2;
		double sin = Math.sin(hourRatioAng);
		double cos = Math.cos(hourRatioAng);

		// position on circle(time)
		Vec_Guo pos = solarCircleBottom.dup().mul(cos);
		pos.add(solarOutDirection.dup().mul(sin));

		// circle move alone the normal according to the date(consider the date)
		pos.add(d);

		// position on unit sphere
		return pos.unit();
	}
}
