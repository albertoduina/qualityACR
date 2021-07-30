package qualityACR;

import ij.Prefs;

public class ACRconfigurator {


	public static int readPreferences(String key, int default1) {
		int value = (int) (Prefs.getDouble(".prefer." + key, default1));
		return value;
	}

	public static double readPreferences(String key, double default1) {
		double value = (Prefs.getDouble(".prefer." + key, default1));
		return value;
	}

	public static String readPreferences(String key, String default1) {
		String value = (Prefs.getString(".prefer." + key, default1));
		return value;
	}

	public static boolean readPreferences(String key, boolean default1) {
		boolean value = (Prefs.getBoolean(".prefer." + key, default1));
		return value;
	}

	public static void writePreferences(String key, int value) {
		Prefs.set("prefer." + key, value);
		return;
	}

	public static void writePreferences(String key, double value) {
		Prefs.set("prefer." + key, value);
		return;
	}

	public static void writePreferences(String key, String value) {
		Prefs.set("prefer." + key, value);
		return;
	}

	public static void writePreferences(String key, boolean value) {
		Prefs.set("prefer." + key, value);
		return;
	}


}