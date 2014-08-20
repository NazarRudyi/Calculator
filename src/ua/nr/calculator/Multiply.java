package ua.nr.calculator;

public class Multiply implements Strategy{

	@Override
	public double execute(double a, double b) {
		return a*b;
	}

}
