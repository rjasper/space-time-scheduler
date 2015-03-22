package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

public class CollisionException extends Exception {

	private static final long serialVersionUID = 2727252132184723119L;

	public CollisionException() {
		super();
	}

	public CollisionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CollisionException(String message) {
		super(message);
	}

	public CollisionException(Throwable cause) {
		super(cause);
	}

}
