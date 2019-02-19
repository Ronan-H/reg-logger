package ronan_hanley.reg_logger;

import java.util.LinkedList;
import java.util.List;

public class LatencyCalculator {
	private List<Long> delays;
	private int bufferSize;
	
	public LatencyCalculator(int bufferSize) {
		this.bufferSize = bufferSize;
		
		delays = new LinkedList<Long>();
	}
	
	public void addDelay(long delay) {
		delays.add(delay);
		
		while (delays.size() > bufferSize) {
			delays.remove(0);
		}
	}
	
	public long getAverageDelay(int numFrames) {
		if (delays.size() == 0) {
			return 40000000;
		}
		
		long total = 0;
		long avg;
		
		int startFrame = Math.max(delays.size() - numFrames, 0);
		int numUsed = delays.size() - startFrame;
		
		//System.out.printf("Start frame: %d%n", startFrame);
		//System.out.printf("Num frames to use: %d%n", numUsed);
		
		for (int i = startFrame; i < delays.size(); ++i) {
			total += delays.get(i);
		}
		
		//System.out.println("Total: " + total);
		
		avg = (long)Math.round((double)total / numUsed);

		return avg;
	}
	
	public long getLastDelay() {
		return delays.get(delays.size() - 1);
	}
	
	public void reset() {
		delays.clear();
	}
	
}
