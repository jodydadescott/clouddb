package us.jodyscott.clouddb.core.util;

import java.util.Calendar;
import java.util.Date;

public class TestTools {

	public static boolean sameDay(Date a, Date b) {

		// Return true if Date is

		Calendar calendarA = Calendar.getInstance();
		calendarA.setTime(a);

		Calendar calendarB = Calendar.getInstance();
		calendarB.setTime(b);

		return calendarA.get(Calendar.DAY_OF_YEAR) == calendarB.get(Calendar.DAY_OF_YEAR);
	}

}
