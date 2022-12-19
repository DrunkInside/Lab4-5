package bsu.rfe.java.group10.lab4.Slavinsky.varC;

public class Borders {
	public double minX;
	public double maxX;
	public double minY;
	public double maxY;
	
	public Borders(Borders b) {
		minX = b.minX;
		maxX = b.maxX;
		minY = b.minY;
		maxY = b.maxY;
	}

	public Borders() {
		minX = 0;
		maxX = 0;
		minY = 0;
		maxY = 0;
	}
}
