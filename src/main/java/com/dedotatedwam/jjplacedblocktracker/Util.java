package com.dedotatedwam.jjplacedblocktracker;


import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class Util {

	private Util() {
	}

	public static Optional<String> getOptionFromSubject(Subject player, String... options) {
		for (String option : options) {
			String o = option.toLowerCase();

			// Option for context.
			Optional<String> os = player.getOption(player.getActiveContexts(), o);
			if (os.isPresent()) {
				return os;
			}

			// General option
			os = player.getOption(o);
			if (os.isPresent()) {
				return os;
			}
		}

		return Optional.empty();
	}

	// Parses the timeframe given in any time check to SQL to make sure the format is correct before decoding the
	// 		timeframe (e.g. in /getplacedblocksage <player> <timeframe> where <timeframe> must be valid for ease of implementation
	public static boolean isTimeframeCorrectFormat(String timeframe) {

		Character charCheck;                        // Used to provide checking methods
		timeframe = timeframe.toLowerCase();        // Lowercase it all to prevent unnecessary checks

		for (int i = 0; i < timeframe.length(); i++) {
			charCheck = timeframe.charAt(i);

			// For letter checks, this assumes the previous character was a number or a valid preceding character if necessary
			if (charCheck == 'm') {
				if (i == 0) {        // If we are at the beginning of the timeframe
					return false;    // Why did you place a m at the beginning you idiot???
				} else {
					if (i + 1 == timeframe.length()) {

					} else if (timeframe.charAt(i + 1) == 'o') {        // Month

					} else if (Character.isDigit(timeframe.charAt(i + 1))) {    // Minute followed by a number for the next time amount

					} else return false;    // Anything else is not a valid format, go away
				}
			} else if (charCheck == 'o') {
				if (i == 0) {        // If we are at the beginning of the timeframe
					return false;    // Why did you place an o at the beginning you idiot???
				} else {
					if (i + 1 == timeframe.length()) {

					} else if (Character.isDigit(timeframe.charAt(i + 1))) {        // Month followed by a number for the next time amount

					} else return false;    // Anything else is not a valid format, go away!
				}
			} else if (charCheck == 'w') {
				if (i == 0) {
					return false;    // Why did you place a w at the beginning you idiot???
				} else if (i == timeframe.length() || Character.isDigit(timeframe.charAt(i + 1))) {    // Week followed by a number for the next time amount

				} else return false;    // Anything else is not valid format, go away!
			} else if (charCheck == 'd') {
				if (i == 0) {
					return false;    // Why did you place the d at the beginning you idiot???
				} else {
					if (i + 1 == timeframe.length()) {

					} else if (Character.isDigit(timeframe.charAt(i + 1))) {    // Day followed by a number for the next time amount

					} else return false;    // Anything else is not valid format, go away!
				}
			} else if (charCheck == 'h') {
				if (i == 0) {
					return false;    // Why did you place an h at the beginning you idiot???
				} else {
					if (i + 1 == timeframe.length()) {

					} else if (Character.isDigit(timeframe.charAt(i + 1))) {    // Hour followed by a number for the next time amount

					} else return false;    // Anything else is not valid format, go away!
				}
			} else if (charCheck == 's') {
				if (i == 0) {
					return false;    // Why did you place an s at the beginning you idiot???
				} else {
					if (i+1 == timeframe.length()) {

					}
					else if (Character.isDigit(timeframe.charAt(i + 1))) {    // Second followed by a number for the next time amount

					} else return false;    // Anything else is not valid format, go away!
				}
			} else if (Character.isAlphabetic(charCheck))
				return false; // You weren't supposed to enter that character you dingus.
		}
		return true;
	}

	// Converts the timeframe to the current Unix time - the timeframe
	//	This value is then used to get which blocks were placed before that time
	//	 NOTE: This function assumes the timeframe was valid before decoding it!
	public static long convertTimeframe(String timeframe) {

		long timeTotal = 0;    // Time in seconds

		Character charCheck;                        // Used to provide checking methods
		timeframe = timeframe.toLowerCase();        // Lowercase it all to prevent unnecessary checks

		for (int i = 0; i < timeframe.length(); i++) {
			charCheck = timeframe.charAt(i);
			if (charCheck == 'm') {				// Minute
				if (i == timeframe.length() - 1)						// If at the end of the timeframe, then this m is implied to be minutes
					timeTotal = timeTotal + (getNumber(timeframe, i - 1) * 60);
				else if (Character.isDigit(timeframe.charAt(i+1))) {	// If the next character is a digit, then m is implied to be minutes
					timeTotal = timeTotal + (getNumber(timeframe, i - 1) * 60);
				}
			} else if (charCheck == 'o') {		// Month
				if (timeframe.charAt(i - 1) == 'm') {
					timeTotal = timeTotal + (getNumber(timeframe, i - 2) * 60 * 60 * 24 * 30);
				}
			} else if (charCheck == 'w') {		// Week
				timeTotal = timeTotal + (getNumber(timeframe, i - 1) * 60 * 60 * 24 * 7);
			} else if (charCheck == 'd') {		// Day
				timeTotal = timeTotal + (getNumber(timeframe, i - 1) * 60 * 60 * 24);
			} else if (charCheck == 'h') {		// Hour;
				timeTotal = timeTotal + (getNumber(timeframe, i - 1) * 60 * 60);
			} else if (charCheck == 's') {                                // Second
				timeTotal = timeTotal + getNumber(timeframe, i - 1);
			}
		}
		return (System.currentTimeMillis() / 1000 - timeTotal);        // Subtract the time elapsed since the block placement from the current time
	}

	// Used to get the number in between letters in a timeframe
	private static long getNumber(String timeframe, int i) {

		long timeCurr = 0;
		for (int j = i; j > -1; j--) {
			if (j == 0 || Character.isDigit(timeframe.charAt(j)))	// If j is at the beginning of timeframe or we hit a letter
				timeCurr = timeCurr + (Character.getNumericValue(timeframe.charAt(j))) * (long) Math.pow(10, (i - j));
			else
				break;
		}
		return timeCurr;
	}

	// Time in seconds --> String to be printed to console or user
	public static String timeFormatter(long time) {

		int month = 60*60*24*30;
		int week = 60*60*24*7;
		int day = 60*60*24;
		int hour = 60*60;
		int minute = 60;

		String output = "";

		if (time/month >= 1) {
			output = output + (int)time/month + " mo ";
			time = time - ((int)time/month) * month;
		}
		if (time/week >= 1) {
			output = output + (int)time/week + " w ";
			time = time - ((int)time/week) * week;
		}
		if (time/day >= 1) {
			output = output + (int)time/day + " d ";
			time = time - ((int)time/day) * day;
		}
		if (time/hour >= 1) {
			output = output + (int)time/hour + " h ";
			time = time - ((int)time/hour) * hour;
		}
		if (time/minute >= 1) {
			output = output + (int)time/minute + " min ";
			time = time - ((int)time/minute) * minute;
		}
		if (time >= 1) {
			output = output + (int)time + " s";
		}

		return output;
	}

	public static String getWorldNameFromUUID (UUID world_UUID) {

		Optional<World> world = Sponge.getServer().getWorld(world_UUID);

		if (world.isPresent()) {
			return world.get().getName();
		}

		return "null";
	}
}