
import java.util.Scanner;

public class Process {
	
	int processSize;
	int pageFaultCount;
	int evictCount;
	int residencyTime;
	int next; // next word

	public Process(int processSize, int processNum){
		this.processSize = processSize;
		this.pageFaultCount = 0;
		this.evictCount = 0;
		this.residencyTime = 0;
		this.next = (111 * processNum) % processSize;
	}

	
	//Get the next reference word
	public int getNext() {
		return next;
	}
	
	//Increment residency time
	public void addResidencyTime(int time) {
		residencyTime += time;
	}
		
	//Add page fault, which means, cannot find the page in the frame page time by one
	public void addFaultCount() {
		pageFaultCount++;
	}
	
	// Add eviction time
	public void addEvictCount() {
		evictCount++;
	}
	
	//Compute the next reference word based on A, B, and C
	public void nextReference(double A, double B, double C, Scanner random) {
		int randomNum = random.nextInt();
		double ratio = randomNum / (Integer.MAX_VALUE + 1d);
		if (ratio < A) {
			next = (next + 1) % processSize;
		} else if (ratio < A + B) {
			next = (next - 5 + processSize) % processSize;
		} else if (ratio < A + B + C) {
			next = (next + 4) % processSize;
		} else {
			next = random.nextInt() % processSize;
		}
	}
}
