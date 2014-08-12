package ua.nr.calculator;

public class DivisionByZeroException extends ArithmeticException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DivisionByZeroException(){
		
	}
	
	public DivisionByZeroException(String message){
		super(message);
	}

}
