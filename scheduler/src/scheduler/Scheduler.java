package scheduler;

import java.io.*;
import java.util.*;

public class Scheduler {
	private static int time, running, cpuBurst, randomIndex = 0;
	private static boolean finish, verbose=false, showRandom=false;
	private static ArrayList<Process> processes;
	private static ArrayList<Integer> randomNums = new ArrayList<Integer>(); 
	private static ArrayList<Process> buffer = new ArrayList<Process>();
	private static String inputName;
	public static void main(String[] args) throws FileNotFoundException {
		
		if(args.length==2){
			verbose=true;
			inputName = args[1];
		}else 
			inputName = args[0];
		
		initializeRandom("random-numbers.txt");
		FCFS();
		RR();
		LCFS();
		HPRN();
		
	}
	
	private static int min(int a, int b){
		if(a>b){
			return b;
		}
		else{
			return a;
		}
	}
	
	private static int max(int a, int b){
		if(a>b){
			return a;
		}
		else{
			return b;
		}
	}
	
	private static void initializeRandom(String randFileName){
		try {
			BufferedReader br=new BufferedReader(new FileReader(randFileName));
			String line = null;
			while((line = br.readLine())!=null){
				randomNums.add(Integer.parseInt(line.trim()));
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static int randomOS(int U, boolean Verbose, boolean CPU){
		
		if(Verbose){
			if(CPU){
				System.out.println("Find burst when choosing ready process to run "+randomNums.get(randomIndex));
			}
			else{
				System.out.println("Find I/O burst when blocking a process "+randomNums.get(randomIndex));
			}
		}
		
		return 1 + randomNums.get(randomIndex++) % U;
	}
	
	private static void initialize(String inputName) throws FileNotFoundException{
		randomIndex = 0;
		Scanner scanner = new Scanner(new FileReader(inputName));
		String token = scanner.next();
		int count = Integer.parseInt(token);
		processes = new ArrayList<Process>();
		
		System.out.print("The original input was: " + count + " ");
		for(int i=0;i<count;i++){
			Integer parameter[] = new Integer[4];
			for(int j=0;j<4;j++){
				String raw = scanner.next();
				String checked = new String();
				if(raw.contains("(")){
					checked = raw.replace("(", "");
				}
				else if(raw.contains(")")){
					checked = raw.replace(")", "");
				}
				else{
					checked = raw;
				}
				parameter[j] = Integer.parseInt(checked);
			}
			//Initialize input processes
			Process process = new Process(parameter[0],parameter[1],parameter[2],parameter[3]);
			processes.add(process);
			System.out.print(" " + parameter[0] + " " + parameter[1] + " "+ parameter[2] + " "+ parameter[3]+ " ");
		}
		System.out.println();
		Collections.sort(processes);
		System.out.print("The (sorted) input is: " + count + " ");
		for(Process p:processes){
			System.out.print(" " + p.getA() + " " + p.getB() + " "+ p.getC() + " "+ p.getIO() + " ");
		}
		System.out.println();
		System.out.println();
		scanner.close();
	} 
	
	
	private static void FCFS() throws FileNotFoundException{
		initialize(inputName);
		time = 0;
		running = -1;
		finish = false;
		ArrayList<Process> queue= new ArrayList<Process>();
		float cpuUtility = 0;
		float IOutility = 0;
		int count = 0;
		int[] blocked = new int[processes.size()];
		
		for(int i=0; i<blocked.length; i++){
			blocked[i]=-1;
		}

		if(verbose){
			System.out.println("This detailed printout gives the state and remaining burst for each process");
		}
		while(!finish){
			if(verbose){
				System.out.print("Before cycle " + time + ": ");
				for(Process p:processes){
	 				if(p.terminated()){
	 					System.out.print("terminated 0	");
	 				}
	 				else if(p.notStarted()){
	 					System.out.print("unstarted 0	");
	 				}
	 				else if(p.ready()){
	 						System.out.print("  ready 0	");
	 				}
	 				else if(p.running()){
	 					System.out.print("running "+ cpuBurst+ "	");
	 				}
	 				else if(p.blocked()){
	 					System.out.print("blocked " + blocked[processes.indexOf(p)]+"	");
	 				}
	 				else{
	 					System.out.print("error");
	 				}
	 			}
				System.out.println();
			}
			
			//Decrement block time
			for(int i=0;i<blocked.length;i++){
				if(blocked[i]>0){
					blocked[i]--;
				}
			}
			
			//unblock
			for(int i=0;i<processes.size();i++){
				if(blocked[i] == 0){
					blocked[i]--;
					if(!processes.get(i).terminated()){
						processes.get(i).setReady();
						buffer.add(processes.get(i));
					}
				}
			}
			
			Collections.sort(buffer);
			for(Process p:buffer)
				queue.add(p);
			buffer.clear();
			
			//new process
			for(Process p:processes){
				if(p.getA()==time){
					p.setReady();
					buffer.add(p);
				}
			}
			
			Collections.sort(buffer);
			for(Process p:buffer)
				queue.add(p);
			buffer.clear();
			
			//If nothing is running
			if(running==-1){
				if(!queue.isEmpty()){
					queue.get(0).run();
					running=processes.indexOf(queue.get(0));
					cpuBurst= randomOS(processes.get(running).getB(),showRandom,true);
					queue.remove(0);
				}
			}
			//If there is change happening
			else if(running!=-1 && cpuBurst==1 ){
				if(processes.get(running).getC()==1){//Terminate the process
					if(processes.get(running).getFinish()==0){
						processes.get(running).setFinish(time);
					}
					processes.get(running).terminate();
					processes.get(running).decC();
				}
				else{ //Block the process
					processes.get(running).block();
					processes.get(running).decC();
					blocked[running]=randomOS(processes.get(running).getIO(),showRandom,false);
					processes.get(running).incIOT(blocked[running]);
				}
				if(!queue.isEmpty()){//Run a new process
					running=processes.indexOf(queue.get(0));
					processes.get(running).run();
					queue.remove(0);
					cpuBurst=randomOS(processes.get(running).getB(),showRandom,true);
				}
				else
					running=-1;
			}
			
			//If something is running
			else if(running!=-1 && cpuBurst>0){
				cpuBurst--;
				processes.get(running).decC();
			}
			
			
			count=0;
			for(Process p:processes){
				if(p.getC()==0){//Total CPU burst time achieved
					count++;
					if(processes.indexOf(p)==running){
						if(!queue.isEmpty()){
							running=processes.indexOf(queue.get(0));
							processes.get(running).run();
							queue.remove(0);
							cpuBurst=randomOS(processes.get(running).getB(),showRandom,true);
						}
					}
					
					if(p.getFinish()==0){
						p.setFinish(time);
					}
					p.terminate();
					queue.remove(p);///
				}
				if(count==processes.size()){
					finish=true;
				}
			}
			for(Process p:processes){
				if(p.ready())
					p.incWait();
			}
			
			if(running!=-1)
				cpuUtility++;
			
			for(int i=0; i<processes.size(); i++){
				if(blocked[i]>=0){
					IOutility++;
					break;
				}
			}
			time++;
		}
		double maxTime = time - 1;
		double wait = 0;
		double turnaround = 0;
		count = 0;
		System.out.println("The scheduling algorithm used was First Come First Served");
		System.out.println();
		for(Process p:processes){
 			wait +=p.getWait();
 			turnaround+=p.getFinish()-p.getA();
 			System.out.println("Process "+ count++ +":");
 			System.out.println("	(A,B,C,IO) = ("+p.getA()+ ","+p.getB()+","+ (p.getFinish()-p.getA()-p.getIOT()-p.getWait())+","+p.getIO()+")");
 			System.out.println("	Finishing time: "+ p.getFinish());
 			System.out.println("	Turnaround time: "+(p.getFinish()-p.getA()));
 			System.out.println("	I/O time: " + p.getIOT());
 			System.out.println("	Waiting time: " + p.getWait());
 			System.out.println();
 		}
 		System.out.println("Summary Data:");
 		System.out.println("	Finishing time: "+ maxTime);
 		System.out.println("	CPU Utilization: "+ (cpuUtility)/maxTime);
 		System.out.println("	I/O Utilization: "+ IOutility/maxTime);
 		System.out.println("	Throughput "+ (count/maxTime)*100 +" processes per hundread cycles");
 		System.out.println("	Average Turnaround time " + turnaround/count);
 		System.out.println("	Average Wait time "+ wait/count); 
 		System.out.println();
 		System.out.println("##############################################################################");
	}
	
	private static void RR() throws FileNotFoundException{
		initialize(inputName);
		time = 0;
		running = -1;
		finish = false;
		ArrayList<Process> queue= new ArrayList<Process>();
		float cpuUtility = 0;
		float IOutility = 0;
		int count = 0;
		int quantum = 2;
		int timeLeft = quantum;
		int[] blocked = new int[processes.size()];
		int[] cpuBursts = new int[processes.size()];
		int[] showRunning = new int[processes.size()];
		
		for(int i=0; i<blocked.length; i++){
			blocked[i]=-1;
			cpuBursts[i]=0;
			showRunning[i]=0;
		}

		if(verbose){
			System.out.println("This detailed printout gives the state and remaining burst for each process");
		}
		while(!finish){
			if(verbose){
				System.out.print("Before cycle " + time + ": ");
				for(Process p:processes){
	 				if(p.terminated()){
	 					System.out.print("terminated 0	");
	 				}
	 				else if(p.notStarted()){
	 					System.out.print("unstarted 0	");
	 				}
	 				else if(p.ready()){
	 						System.out.print("  ready 0	 ");
	 				}
	 				else if(p.running()){
	 					System.out.print("running "+ showRunning[processes.indexOf(p)]+ "	");
	 				}
	 				else if(p.blocked()){
	 					System.out.print("blocked " + blocked[processes.indexOf(p)]+"	");
	 				}
	 				else{
	 					System.out.print("error");
	 				}
	 			}
				System.out.println();
			}
			
			
			
			//unblock
			for(int i=0;i<processes.size();i++){
				if(blocked[i] == 0){
					blocked[i]--;
					if(!processes.get(i).terminated()){
						processes.get(i).setReady();
						buffer.add(processes.get(i));
					}
				}
			}
			
			//new process
			for(Process p:processes){
				if(p.getA()==time){
					p.setReady();
					buffer.add(p);
				}
			}

			
			//If nothing is running
			if(running==-1){
				if(!queue.isEmpty()){
					queue.get(0).run();
					running=processes.indexOf(queue.get(0));
					if(cpuBursts[running]==0){
						cpuBursts[running] = randomOS(processes.get(running).getB(),showRandom,true);
						showRunning[running] = min(quantum,cpuBursts[running]);
					}
					queue.remove(0);
					timeLeft = quantum;
				}
				else if(!buffer.isEmpty()){
					buffer.get(0).run();
					running=processes.indexOf(buffer.get(0));
					if(cpuBursts[running]==0){
						cpuBursts[running] = randomOS(processes.get(running).getB(),showRandom,true);
						showRunning[running] = min(quantum,cpuBursts[running]);
					}
					buffer.remove(0);
					timeLeft = quantum;
				}
				
			}
			//If there is change happening
			else if(running!=-1 && cpuBursts[running]==1 ){
				if(processes.get(running).getC()==1){//Terminate the process
					if(processes.get(running).getFinish()==0){
						processes.get(running).setFinish(time);
					}
					processes.get(running).terminate();
					cpuBursts[running]--;
					showRunning[running]--;
					processes.get(running).decC();
				}
				else{ //Block the process
					processes.get(running).block();
					processes.get(running).decC();
					cpuBursts[running]--;
					blocked[running]=randomOS(processes.get(running).getIO(),showRandom,false);
					processes.get(running).incIOT(blocked[running]);
				}
				if(!queue.isEmpty()){//Run a process from ready queue
					running=processes.indexOf(queue.get(0));
					processes.get(running).run();
					queue.remove(0);
					if(cpuBursts[running]==0){
						cpuBursts[running]=randomOS(processes.get(running).getB(),showRandom,true);
						showRunning[running] = min(quantum,cpuBursts[running]);
					}
					timeLeft = quantum;
				}
				else if(!buffer.isEmpty()){
					buffer.get(0).run();
					running=processes.indexOf(buffer.get(0));
					if(cpuBursts[running]==0){
						cpuBursts[running] = randomOS(processes.get(running).getB(),showRandom,true);
						showRunning[running] = min(quantum,cpuBursts[running]);
					}
					buffer.remove(0);
					timeLeft = quantum;
				}
				else
					running=-1;
			}
			//If something is running
			else if(running!=-1 && cpuBursts[running]>1){
				if(timeLeft == 1){//Preempt
					cpuBursts[running]--;
					showRunning[running]--;
					if(showRunning[running]==0)
						showRunning[running] = quantum;
					processes.get(running).decC();
					processes.get(running).setReady();
					buffer.add(processes.get(running));
					if(!queue.isEmpty()){
						running=processes.indexOf(queue.get(0));
						processes.get(running).run();
						queue.remove(0);
						if(cpuBursts[running]==0){
							cpuBursts[running]=randomOS(processes.get(running).getB(),showRandom,true);
							showRunning[running] = min(quantum,cpuBursts[running]);
						}
						timeLeft = quantum;
					}
					else if(!buffer.isEmpty()){
						buffer.get(0).run();
						running=processes.indexOf(buffer.get(0));
						if(cpuBursts[running]==0){
							cpuBursts[running] = randomOS(processes.get(running).getB(),showRandom,true);
							showRunning[running] = min(quantum,cpuBursts[running]);
						}
						buffer.remove(0);
						timeLeft = quantum;
					}
				}
				else{
					timeLeft--;
					cpuBursts[running]--;
					showRunning[running]--;
					if(showRunning[running]==0)
						showRunning[running] = quantum;
					processes.get(running).decC();
				}
			}			
			
			Collections.sort(buffer);
			for(Process p: buffer)
				queue.add(p);
			buffer.clear();
		
			//Decrement block time
			for(int i=0;i<blocked.length;i++){
				if(blocked[i]>0){
					blocked[i]--;
				}
			}
			count=0;
			for(Process p:processes){
				if(p.getC()==0){//Total CPU burst time achieved
					
					if(processes.indexOf(p)==running){
						if(!queue.isEmpty()){
							running=processes.indexOf(queue.get(0));
							processes.get(running).run();
							queue.remove(0);
							if(cpuBursts[running]==0){
								cpuBursts[running]=randomOS(processes.get(running).getB(),showRandom,true);
								showRunning[running] = min(quantum,cpuBursts[running]);
							}
							timeLeft = quantum;
						}
						else if(!buffer.isEmpty()){
							buffer.get(0).run();
							running=processes.indexOf(buffer.get(0));
							if(cpuBursts[running]==0){
								cpuBursts[running] = randomOS(processes.get(running).getB(),showRandom,true);
								showRunning[running] = min(quantum,cpuBursts[running]);
							}
							buffer.remove(0);
							timeLeft = quantum;
						}
					}
					count++;
					if(p.getFinish()==0){
						p.setFinish(time);
					}
					p.terminate();
					queue.remove(p);
				}
				if(count==processes.size()){
					finish=true;
				}
			}
			for(Process p:processes){
				if(p.ready())
					p.incWait();
			}
			
			if(running!=-1)
				cpuUtility++;
			for(int i=0; i<processes.size(); i++){
				if(blocked[i]>=0){
					IOutility++;
					break;
				}
			}
			time++;
		}
		double maxTime = -1;
		double wait = 0;
		double turnaround = 0;
		count = 0;
		System.out.println("The scheduling algorithm used was Round Robbin");
		System.out.println();
		for(Process p:processes){
			if(p.getFinish()>maxTime)
				maxTime = p.getFinish();
 			wait +=p.getWait();
 			turnaround+=p.getFinish()-p.getA();
 			System.out.println("Process "+ count++ +":");
 			System.out.println("	(A,B,C,IO) = ("+p.getA()+ ","+p.getB()+","+ (p.getFinish()-p.getA()-p.getIOT()-p.getWait())+","+p.getIO()+")");
 			System.out.println("	Finishing time: "+ p.getFinish());
 			System.out.println("	Turnaround time: "+(p.getFinish()-p.getA()));
 			System.out.println("	I/O time: " + p.getIOT());
 			System.out.println("	Waiting time: " + p.getWait());
 			System.out.println();
 		}
 		System.out.println("Summary Data:");
 		System.out.println("	Finishing time: "+ maxTime);
 		System.out.println("	CPU Utilization: "+ cpuUtility/maxTime);
 		System.out.println("	I/O Utilization: "+ IOutility/maxTime);
 		System.out.println("	Throughput "+ (count/maxTime)*100 +" processes per hundread cycles");
 		System.out.println("	Average Turnaround time " + turnaround/count);
 		System.out.println("	Average Wait time "+ wait/count); 
 		System.out.println();
 		System.out.println("##############################################################################");
	}
	
	
	private static void LCFS() throws FileNotFoundException{
		initialize(inputName);
		time = 0;
		running = -1;
		finish = false;
		ArrayList<Process> queue= new ArrayList<Process>();
		float cpuUtility = 0;
		float IOutility = 0;
		int count = 0;
		int[] blocked = new int[processes.size()];
		
		
		for(int i=0; i<blocked.length; i++){
			blocked[i]=-1;
		}

		if(verbose){
			System.out.println("This detailed printout gives the state and remaining burst for each process");
		}
		while(!finish){
			if(verbose){
				System.out.print("Before cycle " + time + ": ");
				for(Process p:processes){
	 				if(p.terminated()){
	 					System.out.print("terminated 0	");
	 				}
	 				else if(p.notStarted()){
	 					System.out.print("unstarted 0	");
	 				}
	 				else if(p.ready()){
	 						System.out.print("  ready 0	");
	 				}
	 				else if(p.running()){
	 					System.out.print("running "+ cpuBurst+ "	");
	 				}
	 				else if(p.blocked()){
	 					System.out.print("blocked " + blocked[processes.indexOf(p)]+"	");
	 				}
	 				else{
	 					System.out.print("error");
	 				}
	 			}
				System.out.println();
			}
			
			//Decrement block time
			for(int i=0;i<blocked.length;i++){
				if(blocked[i]>0){
					blocked[i]--;
				}
			}
			
			//new process
			for(Process p:processes){
				if(p.getA()==time){
					p.setReady();
					buffer.add(p);
				}
			}
			Collections.sort(buffer);
			Collections.reverse(buffer);
			
			for(Process p:buffer){
				queue.add(p);
			}
			buffer.clear();
			
			//unblock
			for(int i=0;i<processes.size();i++){
				if(blocked[i] == 0){
					blocked[i]--;
					if(!processes.get(i).terminated()){
						processes.get(i).setReady();
						buffer.add(processes.get(i));
					}
				}
			}
			Collections.sort(buffer);
			Collections.reverse(buffer);
			for(Process p:buffer){
				queue.add(p);
			}
			buffer.clear();
			
			//If nothing is running
			if(running==-1){
				if(!queue.isEmpty()){
					queue.get(queue.size()-1).run();
					running=processes.indexOf(queue.get(queue.size()-1));
					cpuBurst= randomOS(processes.get(running).getB(),showRandom,true);
					queue.remove(queue.size()-1);
				}
			}
			//If there is change happening
			else if(running!=-1 && cpuBurst==1 ){
				if(processes.get(running).getC()==1){//Terminate the process
					if(processes.get(running).getFinish()==0){
						processes.get(running).setFinish(time);
					}
					processes.get(running).terminate();
					processes.get(running).decC();
				}
				else{ //Block the process
					processes.get(running).block();
					processes.get(running).decC();
					blocked[running]=randomOS(processes.get(running).getIO(),showRandom,false);
					processes.get(running).incIOT(blocked[running]);
				}
				if(!queue.isEmpty()){//Run a new process
					running=processes.indexOf(queue.get(queue.size()-1));
					processes.get(running).run();
					queue.remove(queue.size()-1);
					cpuBurst=randomOS(processes.get(running).getB(),showRandom,true);
				}
				else
					running=-1;
			}
			
			//If something is running
			else if(running!=-1 && cpuBurst>0){
				cpuBurst--;
				processes.get(running).decC();
			}
			
			
			count=0;
			for(Process p:processes){
				if(p.getC()==0){//Total CPU burst time achieved
					count++;
					if(processes.indexOf(p)==running){
						if(!queue.isEmpty()){
							running=processes.indexOf(queue.get(queue.size()-1));
							processes.get(running).run();
							queue.remove(queue.size()-1);
							cpuBurst=randomOS(processes.get(running).getB(),showRandom,true);
						}
					}
					
					if(p.getFinish()==0){
						p.setFinish(time);
					}
					p.terminate();
					queue.remove(p);///
				}
				if(count==processes.size()){
					finish=true;
				}
			}
			for(Process p:processes){
				if(p.ready())
					p.incWait();
			}
			
			if(running!=-1)
				cpuUtility++;
			
			for(int i=0; i<processes.size(); i++){
				if(blocked[i]>=0){
					IOutility++;
					break;
				}
			}
			time++;
		}
		double maxTime = time - 1;
		double wait = 0;
		double turnaround = 0;
		count = 0;
		System.out.println("The scheduling algorithm used was Last Come First Served");
		System.out.println();
		for(Process p:processes){
 			wait +=p.getWait();
 			turnaround+=p.getFinish()-p.getA();
 			System.out.println("Process "+ count++ +":");
 			System.out.println("	(A,B,C,IO) = ("+p.getA()+ ","+p.getB()+","+ (p.getFinish()-p.getA()-p.getIOT()-p.getWait())+","+p.getIO()+")");
 			System.out.println("	Finishing time: "+ p.getFinish());
 			System.out.println("	Turnaround time: "+(p.getFinish()-p.getA()));
 			System.out.println("	I/O time: " + p.getIOT());
 			System.out.println("	Waiting time: " + p.getWait());
 			System.out.println();
 		}
 		System.out.println("Summary Data:");
 		System.out.println("	Finishing time: "+ maxTime);
 		System.out.println("	CPU Utilization: "+ (cpuUtility)/maxTime);
 		System.out.println("	I/O Utilization: "+ IOutility/maxTime);
 		System.out.println("	Throughput "+ (count/maxTime)*100 +" processes per hundread cycles");
 		System.out.println("	Average Turnaround time " + turnaround/count);
 		System.out.println("	Average Wait time "+ wait/count); 
 		System.out.println();
 		System.out.println("##############################################################################");
	}
	
	private static int HP(ArrayList<Process> queue1){
		int p = 0;
		for (int i=0; i<queue1.size(); i++){
			if(queue1.get(i).getR()>queue1.get(p).getR()){
				p = i;
			}
			else if(queue1.get(i).getR()==queue1.get(p).getR() && queue1.get(i).getA()< queue1.get(p).getA()){
				p = i;
			}
		}
		return p;
	}
	
	private static void HPRN() throws FileNotFoundException{
		initialize(inputName);
		time = 0;
		running = -1;
		finish = false;
		ArrayList<Process> queue= new ArrayList<Process>();
		float cpuUtility = 0;
		float IOutility = 0;
		int count = 0;
		int[] blocked = new int[processes.size()];
		
		for(int i=0; i<blocked.length; i++){
			blocked[i]=-1;
		}

		if(verbose){
			System.out.println("This detailed printout gives the state and remaining burst for each process");
		}
		while(!finish){
			if(verbose){
				System.out.print("Before cycle " + time + ": ");
				for(Process p:processes){
	 				if(p.terminated()){
	 					System.out.print("terminated 0	");
	 				}
	 				else if(p.notStarted()){
	 					System.out.print("unstarted 0	");
	 				}
	 				else if(p.ready()){
	 						System.out.print("  ready 0	");
	 				}
	 				else if(p.running()){
	 					System.out.print("running "+ cpuBurst+ "	");
	 				}
	 				else if(p.blocked()){
	 					System.out.print("blocked " + blocked[processes.indexOf(p)]+"	");
	 				}
	 				else{
	 					System.out.print("error");
	 				}
	 			}
				System.out.println();
			}
			
			//Decrement block time
			for(int i=0;i<blocked.length;i++){
				if(blocked[i]>0){
					blocked[i]--;
				}
			}
			
			//unblock
			for(int i=0;i<processes.size();i++){
				if(blocked[i] == 0){
					blocked[i]--;
					if(!processes.get(i).terminated()){
						processes.get(i).setReady();
						buffer.add(processes.get(i));
					}
				}
			}
			Collections.sort(buffer);
			for(Process p:buffer)
				queue.add(p);
			buffer.clear();
			
			//new process
			for(Process p:processes){
				if(p.getA()==time){
					p.setReady();
					buffer.add(p);
				}
			}
			Collections.sort(buffer);
			for(Process p:buffer)
				queue.add(p);
			buffer.clear();
			
			//If nothing is running
			if(running==-1){
				if(!queue.isEmpty()){
					int chosen = HP(queue);
					queue.get(chosen).run();
					running=processes.indexOf(queue.get(chosen));
					cpuBurst= randomOS(processes.get(running).getB(),showRandom,true);
					queue.remove(chosen);
				}
			}
			//If there is change happening
			else if(running!=-1 && cpuBurst==1 ){
				if(processes.get(running).getC()==1){//Terminate the process
					if(processes.get(running).getFinish()==0){
						processes.get(running).setFinish(time);
					}
					processes.get(running).terminate();
					processes.get(running).decC();
				}
				else{ //Block the process
					processes.get(running).block();
					processes.get(running).decC();
					blocked[running]=randomOS(processes.get(running).getIO(),showRandom,false);
					processes.get(running).incIOT(blocked[running]);
				}
				if(!queue.isEmpty()){//Run a new process
					int chosen = HP(queue);
					running=processes.indexOf(queue.get(chosen));
					processes.get(running).run();
					queue.remove(chosen);
					cpuBurst=randomOS(processes.get(running).getB(),showRandom,true);
				}
				else
					running=-1;
			}
			
			//If something is running
			else if(running!=-1 && cpuBurst>0){
				cpuBurst--;
				processes.get(running).decC();
			}
			
			
			count=0;
			for(Process p:processes){
				if(p.getC()==0){//Total CPU burst time achieved
					count++;
					if(processes.indexOf(p)==running){
						if(!queue.isEmpty()){
							int chosen = HP(queue);
							running=processes.indexOf(queue.get(chosen));
							processes.get(running).run();
							queue.remove(chosen);
							cpuBurst=randomOS(processes.get(running).getB(),showRandom,true);
						}
					}
					
					if(p.getFinish()==0){
						p.setFinish(time);
					}
					p.terminate();
					queue.remove(p);///
				}
				if(count==processes.size()){
					finish=true;
				}
			}
			for(Process p:processes){
				if(p.ready())
					p.incWait();
			}
			
			if(running!=-1)
				cpuUtility++;
			
			for(int i=0; i<processes.size(); i++){
				if(blocked[i]>=0){
					IOutility++;
					break;
				}
			}
			time++;
			//update r value
			for(Process p:processes){
				p.setR((time-p.getA())/(double) max(1,(p.getCC()-p.getC())));
			}
			
			
		}
		double maxTime = time-1;
		double wait = 0;
		double turnaround = 0;
		count = 0;
		System.out.println("The scheduling algorithm used was Highest Penalty Ratio Next");
		System.out.println();
		for(Process p:processes){
 			wait +=p.getWait();
 			turnaround+=p.getFinish()-p.getA();
 			System.out.println("Process "+ count++ +":");
 			System.out.println("	(A,B,C,IO) = ("+p.getA()+ ","+p.getB()+","+ (p.getFinish()-p.getA()-p.getIOT()-p.getWait())+","+p.getIO()+")");
 			System.out.println("	Finishing time: "+ p.getFinish());
 			System.out.println("	Turnaround time: "+(p.getFinish()-p.getA()));
 			System.out.println("	I/O time: " + p.getIOT());
 			System.out.println("	Waiting time: " + p.getWait());
 			System.out.println();
 		}
 		System.out.println("Summary Data:");
 		System.out.println("	Finishing time: "+ maxTime);
 		System.out.println("	CPU Utilization: "+ (cpuUtility)/maxTime);
 		System.out.println("	I/O Utilization: "+ IOutility/maxTime);
 		System.out.println("	Throughput "+ (count/maxTime)*100 +" processes per hundread cycles");
 		System.out.println("	Average Turnaround time " + turnaround/count);
 		System.out.println("	Average Wait time "+ wait/count); 
 		System.out.println();
 		System.out.println("##############################################################################");
	}
}
