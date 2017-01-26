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
}

