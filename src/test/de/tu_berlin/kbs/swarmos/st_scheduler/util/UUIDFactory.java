package de.tu_berlin.kbs.swarmos.st_scheduler.util;

import java.util.UUID;

public final class UUIDFactory {
	
	public static UUID uuid(String name) {
		return UUID.nameUUIDFromBytes(name.getBytes());
	}

}
