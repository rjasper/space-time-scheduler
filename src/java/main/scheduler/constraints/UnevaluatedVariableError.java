package scheduler.constraints;

public class UnevaluatedVariableError extends Error {
	
	private static final long serialVersionUID = 8426777444057455171L;

	public UnevaluatedVariableError() {
		super();
	}

	public UnevaluatedVariableError(String message, Throwable cause) {
		super(message, cause);
	}

	public UnevaluatedVariableError(String message) {
		super(message);
	}

	public UnevaluatedVariableError(Throwable cause) {
		super(cause);
	}

}
