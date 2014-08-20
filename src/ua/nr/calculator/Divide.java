package ua.nr.calculator;

public class Divide implements Strategy{

	@Override
	public double execute(double a, double b) {
		if (b==0){
			throw new DivisionByZeroException();
		}
		return a/b;
	}

}
