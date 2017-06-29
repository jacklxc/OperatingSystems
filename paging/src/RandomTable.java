import java.util.Scanner;

public class RandomTable implements FrameTable{
	
	int frameNum;
	Scanner random;
	int[][] frameTable; //Each int[] contains page number, process number and the time referenced.

	public RandomTable(int frameNum, Scanner random){
		this.frameNum = frameNum;
		this.random = random;
		frameTable = new int[frameNum][3];
	}
	
	@Override
	public boolean pageFault(int pageNum, int processNum, int time) {
		for (int i = 0; i < frameNum; i++) {
			if ((frameTable[i][0] == pageNum) && (frameTable[i][1] == processNum)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void replace(Process[] processes, int pageNum, int processNum, int time) {
		//If there is an unsed frame, search from the highest number of frame
		for (int i = (frameNum - 1); i >= 0; i--) {
			if ((frameTable[i][0] == 0) && (frameTable[i][1] == 0)) {
				frameTable[i][0] = pageNum;
				frameTable[i][1] = processNum;
				frameTable[i][2] = time;
				return;
			}
		}	
		//Find a random page and evict.
		int randomNum = random.nextInt();
		int evictedFrame = randomNum % frameNum;
		int evictedProcessNum = frameTable[evictedFrame][1];
		//get the evicted process
		Process evictedProcess = processes[evictedProcessNum - 1];
		evictedProcess.addEvictCount();
		int loadTime = frameTable[evictedFrame][2];
		int residencyTime = time - loadTime;
		evictedProcess.addResidencyTime(residencyTime);//has been residence for such a long time	
		//frame refreshed with new information
		frameTable[evictedFrame][0] = pageNum;
		frameTable[evictedFrame][1] = processNum;
		frameTable[evictedFrame][2] = time;
	}
}
