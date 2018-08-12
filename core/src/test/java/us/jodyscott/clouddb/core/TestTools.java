package us.jodyscott.clouddb.core;

import java.io.File;
import java.util.Date;

public class TestTools {

	public static void sleep(int seconds) {

		assert seconds > 0;
		assert seconds < 61;

		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
		}
	}

	public static boolean compareDates(Date a, Date b, int s) {
		// Compare to given dates are within given seconds of each other

		assert a != null;
		assert b != null;
		assert s >= 0;

		long normalized = Math.abs(a.getTime() - b.getTime());

		if (normalized > s * 1000)
			return false;
		else
			return true;

	}

	public static void rmMinusRF(String file) {
		assert file != null;
		rmMinusRF(new File(file));
	}

	public static void rmMinusRF(File file) {
		assert file != null;

		if (file.exists()) {
			if (file.isDirectory()) {
				for (File subFile : file.listFiles()) {
					rmMinusRF(subFile);
				}
			}
			file.delete();
		}
	}

}
