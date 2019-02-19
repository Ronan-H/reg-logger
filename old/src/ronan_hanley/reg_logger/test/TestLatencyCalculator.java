package ronan_hanley.reg_logger.test;

import java.util.Random;

import ronan_hanley.reg_logger.LatencyCalculator;

public class TestLatencyCalculator {

	public static void main(String[] args) {
		int bufferSize = 50;
		LatencyCalculator calc = new LatencyCalculator(bufferSize);
		Random rand = new Random(10);
		
		for (int i = 0; i < bufferSize; ++i) {
			int r = rand.nextInt(100);
			System.out.printf("Random number %d: %d%n", i, r);
			calc.addDelay(r);
		}
		System.out.println();
		
		int size = 10;
		System.out.printf("\nAverage of last %d: %d%n", size, calc.getAverageDelay(size));
	}

}
