package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import java.util.UUID;

/**
 * Stores the ID and the alternative schedule of a transaction.
 *
 * @author Rico Jasper
 */
public class Transaction {

	private final UUID id;

	private final ScheduleAlternative alternative;

	public Transaction(UUID id, ScheduleAlternative alternative) {
		this.id = id;
		this.alternative = alternative;
	}

	public UUID getId() {
		return id;
	}

	public ScheduleAlternative getAlternative() {
		return alternative;
	}

}
