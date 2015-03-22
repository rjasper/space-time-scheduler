package de.tu_berlin.mailbox.rjasper.util;

public final class Throwables {

	public static boolean thrownBy(Throwable throwable, Class<?> clazz) {
		String throwerClassName = throwable.getStackTrace()[0].getClassName();
		String clazzName = clazz.getName();

		return throwerClassName.equals(clazzName);
	}

}
