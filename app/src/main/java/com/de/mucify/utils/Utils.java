package com.de.mucify.utils;

public class Utils {
    public static String millisecondsToReadableString(int progress) {
        long millis = progress % 1000;
        long second = (progress / 1000) % 60;
        long minute = (progress / (1000 * 60)) % 60;
        long hour = (progress / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
    }
}
