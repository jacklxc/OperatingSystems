package banker;

import java.util.ArrayList;

public class Banker {
	private int T,R,cycle;
	private int[] available, total;
	private int[][] allocation, initialClaim, need, released;
	private Boolean printOut=false;
	private ArrayList<Task> blockQueue = new ArrayList<Task>(); //A FIFO queue that contains blocked tasks.
	private ArrayList<Task> queue = new ArrayList<Task>(); //The regular queue
	private Task[] tasks;
	public Banker(int t, int r){
		T=t;// Total task number
		R=r;// total resource number
		cycle=0;
		available = new int[R]; //total available resource
		released = new int[T][R];//A buffer to store the resources released in this cycle
		allocation = new int[T][R];//Allocated resource
		initialClaim = new int[T][R]; // Initial claims of tasks, constants
		need = new int[T][R]; // initial claim - allocated
		total = new int[R]; //Total resources, constant
	}
	//used for initialization
	public void setAvailable(int i, int Num){
		available[i] = Num;
		total[i] = Num;
	}
	
	//Check if the request can be granted or not.
	public int request(int t, int i, int a){
		if(a + allocation[t][i] > initialClaim[t][i]){
			return -1;
		}
		else{
			if(isSafe(t,i,a)){
				return 1;
			}
			else{
				return 0;
			}
		}
	}
	//Check if the state is safe
	public Boolean isSafe(int t, int r, int a){
		int[] buff = new int[R];
		boolean[] finish = new boolean[T];
		for(int i=0;i<T;i++){
			if(tasks[i].terminated()||tasks[i].aborted()){
				finish[i] = true;
			}
			else{
				finish[i] = false;
			}
		}
		
		grant(t,r,a);// try to grant first
		
		for(int i=0;i<R;i++){
			buff[i] = available[i];
		}
		int i = 0;
		
		//look for processes that is not finished and the request exceeds buff
		do
		{
			boolean flag = true;// can finish normally
			//if need[i][j] <=buff[j]
			for(int j=0;j<R;j++)
			{
				if(need[i][j] > buff[j])
				{
					flag = false;
					break;
				}
			}
			//if finish[i]=false and Need[i,j]<=buff[j]
			if(finish[i]==false && flag)
			{
				for(int j=0;j<R;j++){
					buff[j] = buff[j] + allocation[i][j];
				}
				finish[i] = true;
				i = -1; //Traverse unfinished process
			}
		}while(++i<T);
		i = 0;
		Boolean status = false;
		while(finish[i]==true)
		{
			if(i == T-1){
				status = true; //is safe states, return true
				break;
			}
			i++;
		}
		
		//undo grant
		available[r]+=a;
		allocation[t][r]-=a;
		need[t][r] += a;
		
		return status; //is danger states, return false
	}
	
	//Grant the task
	public void grant(int t, int i, int a){
		available[i]-=a;
		allocation[t][i]+=a;
		need[t][i] -= a;
	}
	//Release resources from the task
	public void release(int t, int i, int a){
		released[t][i]+=a;
		allocation[t][i]-=a;
		if(allocation[t][i]<0){
			System.out.println("Released too much!");
		}
	}
	//Just for printing out more conveniently
	public int[] available_next_cycle(){
		int[] next_cycle = new int[R];
		for(int i=0; i<R; i++){
			next_cycle[i] = available[i];
			for(int t=0; t<T; t++){
				next_cycle[i] += released[t][i];
			}
		}
		return next_cycle;
	}
	//DEAL WITH BLOCK QUEUE 
	public void checkBlocked(){
		int request_status = 1; // 1 means request can be granted normally. 0 means the task should be blocked. -1 means the task should be aborted.
		if(printOut && blockQueue.size()>0){
			System.out.println("  Banker first checks blocked tasks:");
		}
		for(int a=0; a<blockQueue.size(); a++){
			Task task = blockQueue.get(a);
			int t = task.getID();
			if(task.hasNextActivity()){
				 if(task.getInstruction().equals("request")){
					request_status = request(t, task.getType(),task.getAmount());
					if(request_status == 1){
						grant(t,task.getType(),task.getAmount());
						task.unblock();
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
					else if(request_status == 0){//not granted
						if(printOut){
							System.out.println("      Task "+(t+1)+"'s request still cannot be granted (not safe).");
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
	//The main banker method
	public void run(Task[] TASKS){
		Boolean finished = false;
		tasks = TASKS;
		int request_status = 1; // 1 means request can be granted normally. 0 means the task should be blocked. -1 means the task should be aborted.
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
			//Regular banker queue
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
							initialClaim[t][task.getType()] = task.getAmount();
							need[t][task.getType()] = task.getAmount();
							if(initialClaim[t][task.getType()] <= total[task.getType()]){
								if(printOut){
									System.out.println("  Task "+(t+1)+" does initialization.");
								}
							}
							else{
								System.out.println("Banker aborts task "+(t+1)+"before run begins: ");
								System.out.println("	claim for resource " + (task.getType()+1) + "("+task.getAmount()+
										") exceeds number of units present ("+ total[task.getType()] + ")");
								task.abort();
							}
						}
						else if(task.getInstruction().equals("request")){
							if(!task.noDelay()){
								task.setDelay(task.getDelay());
							}
							if(task.delay()==0){
								request_status = request(t,task.getType(),task.getAmount());
								if(task.noDelay())
									task.setNoDelay(false);
								if(request_status == 1){
									grant(t,task.getType(),task.getAmount());
									if(printOut)
										System.out.println("  Task "+(t+1)+" completes its request (resource["+
												(task.getType()+1)+"]: requested = "+task.getAmount()+", remaining = "+ available[task.getType()]+")");
									}
								else if(request_status == 0){//not granted
									if(printOut){
										System.out.println("  Task "+(t+1)+"'s request cannot be granted (not safe).");
									}
									blockQueue.add(task);
									task.block();
								}
								else if(request_status == -1){
									task.abort();
									for(int ii=0; ii<R; ii++){
										release(t,ii,allocation[t][ii]);
									}
									System.out.println("During cycle "+cycle+"-"+(cycle+1)+" of Banker's algorithms");
									System.out.println("	Task "+(t+1)+ "'s request exceeds its claim; aborted; "
											+ available_next_cycle()[task.getType()] + " units available next cycle.");
								}
							}
							else if(task.delay()>0){
								//Only check if the task has to be aborted.
								if(task.getAmount() + allocation[t][task.getType()] > initialClaim[t][task.getType()]){
									task.abort();
									for(int ii=0; ii<R; ii++){
										release(t,ii,allocation[t][ii]);
									}
									System.out.println("During cycle "+cycle+"-"+(cycle+1)+" of Banker's algorithms");
									System.out.println("	Task "+(t+1)+ "'s request exceeds its claim; aborted; "
											+ available_next_cycle()[task.getType()] + " units available next cycle.");
								}
								else{
								task.compute();
								if(printOut)
									System.out.println("  Task "+(t+1)+ " computes. "+ task.getComputeTime()+"/"+task.delay()+" cycle(5)");
							
								}
							}
						}
						else if(task.getInstruction().equals("release")){
							if(task.noDelay()){
								task.setNoDelay(false);
							}
							else{
								task.setDelay(task.getDelay());
							}
							if(task.delay()==0){
								release(t,task.getType(),task.getAmount());
								if(printOut)
									System.out.println("  Task "+(t+1)+" completes its release (resource["+
							(task.getType()+1)+"]: released = "+task.getAmount()+", available next cycle = "+available_next_cycle()[task.getType()]+")");
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
						if(!(task.blocked()||task.terminated()||task.aborted())&&task.delay()==0){
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
			
			//Prepare for the next cycle
			for(int t=0; t<T; t++){
				for(int i=0;i<R;i++){
					available[i]+=released[t][i];
					need[t][i] += released[t][i];
					released[t][i]=0;
				}
			}
			cycle++;
		}
		finalPrint();
	}
	
	public void finalPrint(){
		//Print out outputs
		System.out.println();
		System.out.println("BANKER'S");
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
