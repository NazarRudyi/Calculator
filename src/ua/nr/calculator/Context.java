package ua.nr.calculator;

public class Context {
	
	private static Strategy sStrategy;
	
	public Context() {		
	}
	
	
	public void setStrategy(Strategy strategy){
		sStrategy = strategy;
	}
	
	
	public static double execute(double a, double b){
		return sStrategy.execute(a, b);
	}

}
