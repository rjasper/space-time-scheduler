package util;

import java.util.UUID;

public final class UUIDFactory {
	
	public static UUID uuid(String name) {
		return UUID.nameUUIDFromBytes(name.getBytes());
	}

}
