
public class LRUTable implements FrameTable{
	int frameNum;
	int frameTable[][]; //Each int[] contains page number, process number and last referenced time.

	public LRUTable(int frameNum){
		this.frameNum = frameNum;
		frameTable = new int[frameNum][4];
	}
	
	@Override
	public boolean pageFault(int pageNum, int processNum, int time) {
		for (int i = 0; i < frameNum; i++) {
			if ((frameTable[i][0] == pageNum) && (frameTable[i][1] == processNum)) {
				frameTable[i][2] = time;
				return false;
			}
		}
		return true;
	}

	@Override
	public void replace(Process[] processes, int pageNum, int processNum, int time) {
		int LRUTime = time;
		int evictedFrame = 0;
		
		for (int i = frameNum-1; i >= 0; i--) {
			//if there was an unused frame, use that frame element and end searching, 
			//searching begins from highest address
			if ((frameTable[i][0] == 0) && (frameTable[i][1] == 0)) {
				frameTable[i][0] = pageNum;//page number
				frameTable[i][1] = processNum;//process number
				frameTable[i][2] = time;//least recent time 
				frameTable[i][3] = time;//load at current time
				return;
			} 
			//find the least recently used frame
			else if (LRUTime > frameTable[i][2]) {
				evictedFrame = i;
				LRUTime = frameTable[i][2];
			}
		}
		//Evict the frame
		int evictedProcessNumber = frameTable[evictedFrame][1];
		Process evictedProcess = processes[evictedProcessNumber - 1];
		evictedProcess.addEvictCount();
		int loadTime = frameTable[evictedFrame][3];
		int residencyTime = time - loadTime;
		evictedProcess.addResidencyTime(residencyTime);	
		//New page
		frameTable[evictedFrame][0] = pageNum;
		frameTable[evictedFrame][1] = processNum;
		frameTable[evictedFrame][2] = time;
		frameTable[evictedFrame][3] = time;
	}
	
}
