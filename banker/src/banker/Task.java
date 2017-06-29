package banker;

import java.util.ArrayList;

public class Task {
	private ArrayList<String[]> activities = new ArrayList<String[]>();
	private int id, activityIndex, blockTime, finishTime, delay, cpuTime; 
	private Boolean aborted = false, terminated = false, blocked =false, noDelay = false;//If just finished computing, ignore the delay
	
	public Task(int i){
		id = i;
		activityIndex = 0; //The index of activity
		blockTime = 0; //Blocked (wait) time
		finishTime = 0;
		delay = 0; //delay time of the current activity
		cpuTime = 0;// The time already computed
	}
	
	public int getID(){
		return id;
	}
	
	public void addActivity(String[] activity){
		activities.add(activity);
	}
	//reset everything to go back to initialized state.
	public void reset(){
		activityIndex = 0;
		blockTime = 0; 
		finishTime = 0;
		delay = 0;
		aborted=false;
		terminated=false;
		blocked=false;
		cpuTime = 0;
		noDelay = false;
	}
	
	public Boolean hasNextActivity(){
		if(activityIndex == activities.size()){
			return false;
		}
		else{
			return true;
		}
	}
	//No delay means after computing is finished, the task can skip some steps to execute
	public void setNoDelay(Boolean NODELAY){
		noDelay = NODELAY;
	}
	
	public Boolean noDelay(){
		return noDelay;
	}
	//Switch to the next activity
	public void next() {
		activityIndex++;
	}
	//
	public String getInstruction(){
		return activities.get(activityIndex)[0];
	}
	
	public int getDelay(){
		String DELAY = activities.get(activityIndex)[2];
		return Integer.parseInt(DELAY);
	}
	//get resource type of the current activity
	public int getType(){
		String type = activities.get(activityIndex)[3];
		return Integer.parseInt(type)-1;
	}
	//get the amount of resource of the current activity
	public int getAmount(){
		String amount = activities.get(activityIndex)[4];
		return Integer.parseInt(amount);
	}
	//block the task
	public void block(){
		blocked = true;
		blockTime++;
	}
	//get wait time
	public int getWait(){
		return blockTime;
	}
	
	public void unblock(){
		blocked = false;
	}
	
	public Boolean blocked(){
		return blocked;
	}
	
	public void compute(){
		cpuTime++;
	}
	//update the delay time
	public void setDelay(int DELAY){
		delay=DELAY;
	}
	
	public void terminate(){
		terminated = true;
	}
	
	public Boolean terminated(){
		if(terminated){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void setFinishTime(int time){
		finishTime = time;
	}
	
	public int getFinishTime(){
		return finishTime;
	}
	
	public int finishTime(){
		return finishTime;
	}
	
	public void abort(){
		aborted = true;
	}
	
	public void clearComputeTime(){
		cpuTime = 0;
	}
	
	public int getComputeTime(){
		return cpuTime;
	}
	
	public int delay(){
		return delay;
	}
	
	public Boolean aborted(){
		return aborted;
	}
	
	public void printOut(){
		for(String[] activity: activities){
			for(String token: activity){
				System.out.print(token+" ");
			}
			System.out.println();
		}
	}
}
