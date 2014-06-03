package scheduler.constraints;

public class VariableReadyError extends Error {

	private static final long serialVersionUID = 7056396513659022033L;

	public VariableReadyError() {
		super();
	}

	public VariableReadyError(String message, Throwable cause) {
		super(message, cause);
	}

	public VariableReadyError(String message) {
		super(message);
	}

	public VariableReadyError(Throwable cause) {
		super(cause);
	}

}
