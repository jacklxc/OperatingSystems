import java.util.ArrayList;

public class FIFOTable implements FrameTable{
	
	int frameNum;
	ArrayList<int[]> frameTable;

	public FIFOTable(int frameNum) {
		this.frameNum = frameNum;
		frameTable = new ArrayList<int[]>(); //The int[] contains page number, process number and the time referenced.
	}
	
	@Override
	public boolean pageFault(int pageNum, int processNum, int time) {
		for (int i = 0; i < frameTable.size(); i++) {
			int[] framePage = frameTable.get(i);
			if ((framePage[0] == pageNum) && (framePage[1] == processNum)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void replace(Process[] processes, int pageNum, int processNum, int time) {
		if (frameNum == frameTable.size()) {
			int[] evictedFrame = frameTable.get(0);
			int evictedProcessNum = evictedFrame[1];
			// get the evicted process
			Process evictedProcess = processes[evictedProcessNum - 1];
			evictedProcess.addEvictCount();
			// add total resident time for the evicted process
			int loadTime = evictedFrame[2];
			int residencyTime = time - loadTime;
			evictedProcess.addResidencyTime(residencyTime);
			//Evict the first page in the FIFO queue
			frameTable.remove(0);
		} 
		// add a new page to FIFO queue
		int[] newPage = {pageNum, processNum, time}; 
		frameTable.add(newPage);
	}
}
