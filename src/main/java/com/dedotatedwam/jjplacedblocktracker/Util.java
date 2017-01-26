package com.dedotatedwam.jjplacedblocktracker;


import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;

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

	// TODO Remove this if never used
	public Optional<User> getUser(UUID uuid) {
		Optional<Player> onlinePlayer = Sponge.getServer().getPlayer(uuid);

		// The compiler is saying this won't work, so oh well :S
		//if (onlinePlayer.isPresent()) {
		//	return onlinePlayer;
		//}

		Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);

		return userStorage.get().get(uuid);
	}
}

