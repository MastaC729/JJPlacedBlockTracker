package com.dedotatedwam.jjplacedblocktracker.permissions;

import com.dedotatedwam.jjplacedblocktracker.JJPlacedBlockTracker;
import com.dedotatedwam.jjplacedblocktracker.Util;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class JJPermissions {

	private JJPermissions () {
	}

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

	public static int getPlacedBlocksPermissions (User subject, String block_name) {
		if (subject.hasPermission("jjplacedblocktracker.whitelist.unlimited")) {
			return Integer.MAX_VALUE;
		}

		// Checks to see if the
		Optional<String> count = Util.getOptionFromSubject(subject, "jjplacedblocktracker.whitelist." + block_name);

		int result = JJOptions.DEFAULT_PLACED_BLOCK_AMOUNT;		// Currently 1, will eventually be configurable
		if (count.isPresent()) {
			try {
				result = Integer.parseInt(count.get());
			} catch (NumberFormatException e) {
				e.printStackTrace();		//TODO please handle this much nicer. :(
			}
		}
		return Math.max(result, 1);
	}

	public static void setOptionPermissions() {
		final SubjectData globalSubjectData = JJPlacedBlockTracker.GLOBAL_SUBJECT.getTransientSubjectData();
		for (Map.Entry<String, Integer> optionEntry : JJPlacedBlockTracker.config.getOptions().entrySet()) {
			// Null because this plugin currently tracks global placement, rather than being on a per world basis
			// TODO make this plugin world contextual
			globalSubjectData.setOption(Collections.emptySet(), optionEntry.getKey(), optionEntry.getValue().toString());
		}
	}
}