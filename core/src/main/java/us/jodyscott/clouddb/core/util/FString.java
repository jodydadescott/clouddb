package us.jodyscott.clouddb.core.util;

public class FString {

	private static final String MATCH_STRING = "{}";
	private static final String MATCH_REGEX = "\\{\\}";
	private static final String EMPTY_STRING = new String();

	public static String format(String message, Object... objects) {

		assert message != null;
		assert objects != null;

		int matchCount = getMatchCount(message);

		for (Object o : objects) {
			String objString = null;
			if (o == null) {
				objString = "(OBJECT IS NULL!)";
			} else {
				objString = String.valueOf(o);
			}
			message = message.replaceFirst(MATCH_REGEX, objString);
		}

		if (matchCount != objects.length) {
			String warning = String.format(
					" :: ASSERTION !!! The Number of %s is %s which differs from the number of objects which is %s",
					MATCH_STRING, matchCount, objects.length);
			message = message + warning;
		}

		return message;
	}

	private static int getMatchCount(String input) {

		int counter = 0;
		int sanity = 0;
		while (input.contains(MATCH_STRING)) {
			if (sanity++ > 1000) {
				return 1000;
			}
			counter++;
			input = input.replaceFirst(MATCH_REGEX, EMPTY_STRING);
		}
		return counter;
	}

}
