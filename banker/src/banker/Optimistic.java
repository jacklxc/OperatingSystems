package banker;

import java.util.ArrayList;

public class Optimistic {
	private int T,R, cycle;
	private int[] available;
	private int[][] allocation;
	private int[] released; //A buffer contains the released resources only for this cycle
	private Boolean printOut=false, deadlock = false;
	private ArrayList<Task> blockQueue = new ArrayList<Task>(); //A FIFO queue that contains blocked tasks.
	private ArrayList<Task> queue = new ArrayList<Task>(); //The regular queue
	private Task[] tasks;
	public Optimistic(int t, int r){
		T=t;// Total task number
		R=r;// total resource number
		cycle = 0; 
		available = new int[R]; //Total available resource
		allocation = new int[T][R];//Allocated resource
		released = new int[R];//A buffer to store the resources released in this cycle
	}
	//used for initialization
	public void setAvailable(int i, int Num){
		available[i] = Num;
	}
	
	//Check if the request can be granted or not.
	public Boolean request(int i, int a){
		if(available[i]<a){
			return false;
		}
		else{
			return true;
		}
	}
	//Grant the task
	public void grant(int t, int i, int a){
		available[i]-=a;
		allocation[t][i]+=a;
	}
	//Release resources from the task
	public void release(int t, int i, int a){
		released[i]+=a;
		allocation[t][i]-=a;
		if(allocation[t][i]<0){
			System.out.println("Released too much!");
		}
	}
	
	
	public void deadlock(){
		//Check if deadlock occurs
		deadlock = true;
		Boolean[] deadlockChecker = new Boolean[T];
		for(int i=0; i<T; i++){//terminated or aborted?
			deadlockChecker[i] = false;
			if(tasks[i].aborted() || tasks[i].terminated() ||tasks[i].blocked()){
				deadlockChecker[i] = true;
			}
		}
		for(int i=0; i<T; i++){
			if(!deadlockChecker[i]){
				deadlock = false;
				break;
			}
		}
		if(deadlock){
			//Deal with deadlock
			for(int t=0; t<T; t++){
				Task task = tasks[t];
				//Abort tasks from the smallest number.
				if(!(task.aborted() || task.terminated())){
					//Check if the deadlock still remains
					for(int tt=0; tt<T; tt++){
						Task TASK = tasks[tt];
						if(!(TASK.aborted() || TASK.terminated())){
							if(TASK.getInstruction().equals("request")){
								//System.out.println("Resource "+TASK.getType()+" available"+available[TASK.getType()]+released[TASK.getType()]);
								//System.out.println("Task"+(tt+1)+"requests resource"+TASK.getType()+" "+TASK.getAmount());
								if(available[TASK.getType()]+released[TASK.getType()]>=TASK.getAmount()){
									deadlock = false;
									break;
								}
							}
							else{
								System.out.println("There is a task in blockQueue not requesting.");
							}
						}
					}
					if(deadlock&&blockQueue.size()>1){
						task.abort();
						for(int i=0; i<R; i++){
							release(t,i,allocation[t][i]);
						}
						blockQueue.remove(task);
						if(printOut){
							System.out.println("  Task "+(t+1)+" is aborted now and its resources are available next cycle.");
						}
					}
					if(!deadlock){
						break;
					}
				}
			}
		}
	}
	
	public void checkBlocked(){//DEAL WITH BLOCK QUEUE 
		if(printOut && blockQueue.size()>0){
			System.out.println("  First check blocked tasks:");
		}
		for(int a=0; a<blockQueue.size(); a++){
			Task task = blockQueue.get(a);
			int t = task.getID();
			if(task.hasNextActivity()){
				 if(task.getInstruction().equals("request")){
					if(request(task.getType(),task.getAmount())){
						grant(t,task.getType(),task.getAmount());
						task.setDelay(task.getDelay());
						task.unblock();
						if(task.delay()==0){
							if(printOut){
								System.out.println("      Task "+(t+1)+" completes its request (resource["+
										(task.getType()+1)+"]: requested = "+task.getAmount()+", remaining = "+ available[task.getType()]+")");
							}
							task.next(); //switch to the next activity.
							if(task.getInstruction().equals("terminate")){
								task.setDelay(task.getDelay());
								if(task.delay()==0){
									task.terminate();
									task.setFinishTime(cycle+1);
									if(task.getComputeTime() == 0 && printOut)
										System.out.println("  Task "+(t+1)+" finished at "+ (cycle+1));
									task.next();
								}
								else{
									task.compute();
									if(printOut)
										System.out.println("  Task "+(t+1)+ " computes. "+ task.getComputeTime()+"/"+task.delay()+" cycle(2)");
								}
							}
						}
						else if(task.delay()>0){
							task.compute();
							if(printOut)
								System.out.println("  Task "+(t+1)+ " computes. "+ task.getComputeTime()+"/"+task.delay()+" cycle(3)");
						}
					}
					else{//not granted
						if(printOut){
							System.out.println("      Task "+(t+1)+"'s request still cannot be granted.");
						}
						task.block();
					}
				}
				else{
					System.out.println("Unknown instruction in block queue: "+task.getInstruction());
				}
			}
		}
		for(int t=0; t<T; t++){//Remove tasks that were unblocked.
			if(!tasks[t].blocked() && blockQueue.contains(tasks[t])){
				blockQueue.remove(tasks[t]);
			}
		}
	}
	//The main method
	public void run(Task[] TASKS){
		Boolean finished = false;
		tasks = TASKS;
		while(!finished){
			if(printOut){
				System.out.println("During "+ cycle +"-"+(cycle+1));
			}
			
			//Update the regular queue
			queue.clear();
			for(int t=0;t<T;t++){
				if(!tasks[t].blocked()){
					queue.add(tasks[t]);
				}
			}
			//DEAL WITH BLOCK QUEUE FIRST
			checkBlocked();

			//Deal with the regular queue.
			for(int i=0; i<queue.size(); i++){
				Task task = queue.get(i);
				int t = task.getID();
				if(!(task.aborted()||task.terminated())){
					if(task.delay()>0){
						task.compute();
						if(task.getComputeTime()<=task.delay()){
							if(printOut){
								System.out.println("  Task "+(t+1)+ " computes. "+ task.getComputeTime()+"/"+task.delay()+" cycle(4)");
							}
						}
						else if(task.getComputeTime()>task.delay()){
							task.setDelay(0);
							task.clearComputeTime();
							task.setNoDelay(true);
						}
					}
					if(task.hasNextActivity()&&!task.terminated()&&task.delay()==0){
						if(task.getInstruction().equals("initiate")){
							if(printOut){
								System.out.println("  Task "+(t+1)+" does initialization.");
							}
						}
						else if(task.getInstruction().equals("request")){
							if(request(task.getType(),task.getAmount())){
								grant(t,task.getType(),task.getAmount());
								if(task.noDelay())
									task.setNoDelay(false);
								else
									task.setDelay(task.getDelay());
								if(task.delay()==0 && printOut){
									System.out.println("  Task "+(t+1)+" completes its request (resource["+
								(task.getType()+1)+"]: requested = "+task.getAmount()+", remaining = "+ available[task.getType()]+")");
								}
								else if(task.delay()>0){
									task.compute();
									if(printOut)
										System.out.println("  Task "+(t+1)+ " computes. "+ task.getComputeTime()+"/"+task.delay()+" cycle(5)");
								}
							}
							else{//not granted
								if(printOut){
									System.out.println("  Task "+(t+1)+"'s request cannot be granted.");
								}
								blockQueue.add(task);
								task.block();
							}
						}
						else if(task.getInstruction().equals("release")){
							if(task.noDelay())
								task.setNoDelay(false);
							else
								task.setDelay(task.getDelay());
							if(task.delay()==0){
								release(t,task.getType(),task.getAmount());
								if(printOut)
									System.out.println("  Task "+(t+1)+" completes its release (resource["+
							(task.getType()+1)+"]: released = "+task.getAmount()+", available next cycle = "+(released[task.getType()]+available[task.getType()])+")");
							}
							else if(task.delay()>0){
								task.compute();
								if(printOut)
									System.out.println("  Task "+(t+1)+ " computes. "+ task.getComputeTime()+"/"+task.delay()+" cycle(6)");
							}
						}
						else if(task.getInstruction().equals("terminate")){
							//terminate with noDelay.
							task.terminate();
							task.setFinishTime(cycle+1);
							if(printOut)
								System.out.println("  Task "+(t+1)+" finished at "+ (cycle+1));
							task.next();
						}
						//If the current activity is completed
						if(!(task.blocked()||task.terminated())&&task.delay()==0){
							task.next(); //switch to the next activity.
							if(task.getInstruction().equals("terminate")){
								task.setDelay(task.getDelay());
								if(task.delay()==0){
									task.terminate();
									task.setFinishTime(cycle+1);
									if(printOut)
										System.out.println("  Task "+(t+1)+" finished at "+ (cycle+1));
									task.next();
								}
								else{
									task.compute();
									if(printOut)
										System.out.println("  Task "+(t+1)+ " computes to get finished. "+ task.getComputeTime()+"/"+task.delay()+" cycle(1)");
								}
							}
						}
					}
				}
			}
			
			//Check if all of the tasks are finished
			finished = true;
			for(Task task: tasks){
				if(!(task.terminated() && task.getComputeTime()==0 || task.aborted())){
					finished = false;
					break;
				}
			}
			
			//Deal with deadlock
			deadlock();
			
			//Prepare for the next cycle
			for(int i=0;i<R;i++){
				available[i]+=released[i];
				released[i]=0;
			}
			cycle++;
		}
		finalPrint();
	}
	
	public void finalPrint(){
		//Print out outputs
		System.out.println("FIFO");
		int totalTime = 0;
		int totalWait = 0;
		for(int t=0; t<T; t++){
			Task task = tasks[t];
			if(task.aborted()){
				System.out.println("Task "+(t+1)+"		aborted");
			}
			else{
				totalTime+=task.getFinishTime();
				totalWait+=task.getWait();
				System.out.println("Task "+(t+1)+"		"+task.getFinishTime()+"	"
			+task.getWait()+"	"+ 100*task.getWait()/((double) task.getFinishTime())+"%");
			}
		}
		//Print out total
		System.out.println("Total 		"+totalTime+"	"
				+totalWait+"	"+ 100*totalWait/((double) totalTime)+"%");
	}
}
