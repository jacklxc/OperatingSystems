
public interface FrameTable {
	
	//Check if page fault occurs
	boolean pageFault(int pageNum, int processNum, int time);
	//If page fault occurs, then replace
	void replace(Process[] processes, int pageNum, int processNum, int time);
}
