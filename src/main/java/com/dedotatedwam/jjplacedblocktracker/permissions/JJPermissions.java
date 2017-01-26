package com.dedotatedwam.jjplacedblocktracker.permissions;

import com.dedotatedwam.jjplacedblocktracker.JJPlacedBlockTracker;
import com.dedotatedwam.jjplacedblocktracker.Util;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class JJPermissions {

	@Inject
	Game game;

	public static void registerPD(String role, String permission, String description, PermissionService service) {

		Optional<Builder> optBuilder = service.newDescriptionBuilder(JJPlacedBlockTracker.class);
		if (optBuilder.isPresent()) {
			Builder builder = optBuilder.get();
			builder.id(permission)
					.description(Text.of(description))
					.assign(role, true)
					.register();
		}
	}

	public static int getPlacedBlocksPermissions (Player subject, String block_name) {
		if (subject.hasPermission("jjplacedblocktracker.whitelist.unlimited")) {
			return Integer.MAX_VALUE;
		}

		Optional<String> count = Util.getOptionFromSubject(subject, block_name);

		int result = 1;
		if (count.isPresent()) {
			try {
				result = Integer.parseInt(count.get());
			} catch (NumberFormatException e) {
				e.printStackTrace();		//TODO please handle this much nicer. :(
			}
		}
		return Math.max(result, 1);
	}
}