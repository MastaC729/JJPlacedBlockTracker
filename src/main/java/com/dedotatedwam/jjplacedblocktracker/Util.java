package com.dedotatedwam.jjplacedblocktracker;


import org.spongepowered.api.service.permission.Subject;

import java.util.Optional;

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
	public static boolean isTimeframeCorrectFormat (String timeframe) {

		Character charCheck;						// Used to provide checking methods
		timeframe = timeframe.toLowerCase();		// Lowercase it all to prevent unnecessary checks

		for(int i=0; i < timeframe.length(); i++) {
			charCheck = timeframe.charAt(i);

			// For letter checks, this assumes the previous character was a number or a valid preceding character if necessary
			switch (charCheck) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
				case 9:		// Is the character a digit?
					break;	// Keep checking the timeframe
				case 'm':
					if (i == 0) {		// If we are at the beginning of the timeframe
						return false;	// Why did you place a m at the beginning you idiot???
					}
					else if (timeframe.charAt(i+1) == 'o') {		// Month
						break;
					}
					else if (Character.isDigit(timeframe.charAt(i+1))) {	// Minute followed by a number for the next time amount
						break;
					}
					else return false;	// Anything else is not a valid format, go away!
				case 'o':
					if (i == 0) {		// If we are at the beginning of the timeframe
						return false;	// Why did you place an o at the beginning you idiot???
					}
					else if (timeframe.charAt(i-1) == 'm') {		// Month followed by a number for the next time amount
						break;
					}
					else return false;	// Anything else is not a valid format, go away!
				case 'w':
					if (i == 0) {
						return false;    // Why did you place a w at the beginning you idiot???
					}
					else if (Character.isDigit(timeframe.charAt(i+1))) {	// Week followed by a number for the next time amount
							break;
					}
					else return false;	// Anything else is not valid format, go away!
				case 'd':
					if (i == 0) {
						return false;    // Why did you place the d at the beginning you idiot???
					}
					else if (Character.isDigit(timeframe.charAt(i+1))) {	// Day followed by a number for the next time amount
						break;
					}
					else return false;	// Anything else is not valid format, go away!
				case 'h':
					if (i == 0) {
						return false;    // Why did you place an h at the beginning you idiot???
					}
					else if (Character.isDigit(timeframe.charAt(i+1))) {	// Hour followed by a number for the next time amount
						break;
					}
					else return false;	// Anything else is not valid format, go away!
				case 's':
					if (i == 0) {
						return false;    // Why did you place an s at the beginning you idiot???
					}
					else if (Character.isDigit(timeframe.charAt(i+1))) {	// Second followed by a number for the next time amount
						break;
					}
					else return false;	// Anything else is not valid format, go away!
			}
		}
		return true;
	}

	// Converts the timeframe to the current Unix time - the timeframe
	//	This value is then used to get which blocks were placed before that time
	//	 NOTE: This function assumes the timeframe was valid before decoding it!
	public static long convertTimeframe (String timeframe) {

		int timeTotal = 0;	// Time in seconds

		Character charCheck;						// Used to provide checking methods
		timeframe = timeframe.toLowerCase();		// Lowercase it all to prevent unnecessary checks

		for(int i=0; i < timeframe.length(); i++) {
			charCheck = timeframe.charAt(i);

			switch (charCheck) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
				case 9:		// Is the character a digit?
					break;	// Go past them, numbers are handled when it sees a letter
				case 'm':
					if (timeframe.charAt(i+1) == 'o') {		// Month
						break;
					}
					else if (Character.isDigit(timeframe.charAt(i+1))) {	// Minute
						timeTotal = timeTotal + (getNumber(timeframe, i) * 60);
					}
				case 'o':
					if (timeframe.charAt(i-1) == 'm') {		// Month
						timeTotal = timeTotal + (getNumber(timeframe, i) * 60 * 60 * 24 * 30);
					}
				case 'w':
					if (Character.isDigit(timeframe.charAt(i+1))) {	// Week
						timeTotal = timeTotal + (getNumber(timeframe, i) * 60 * 60 * 24 * 7);
					}
				case 'd':
					if (Character.isDigit(timeframe.charAt(i+1))) {	// Day
						timeTotal = timeTotal + (getNumber(timeframe, i) * 60 * 60 * 24);
					}
				case 'h':
					if (Character.isDigit(timeframe.charAt(i+1))) {	// Hour;
						timeTotal = timeTotal + (getNumber(timeframe, i) * 60 * 60);
					}
				case 's':
					if (Character.isDigit(timeframe.charAt(i+1))) {	// Second
						timeTotal = timeTotal + getNumber(timeframe, i);
					}
			}
		}

		return (System.currentTimeMillis()/1000 - timeTotal);		// Subtract the time elapsed since the block placement from the current time
	}

	private static int getNumber (String timeframe, int i) {

		int timeCurr = 0;
		for (int j = i; (j > -1) || Character.isDigit(timeframe.charAt(j)); j--) {
			timeCurr = timeCurr + ((int) timeframe.charAt(j)) * (int)Math.pow(10, (i-j));
		}
		return timeCurr;
	}
}

