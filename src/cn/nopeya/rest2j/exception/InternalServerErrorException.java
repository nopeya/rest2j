package cn.nopeya.rest2j.exception;

public class InternalServerErrorException extends RuntimeException {
	public InternalServerErrorException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 7612827596817847616L;
}
