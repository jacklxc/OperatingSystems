import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
	static int M; //Machine size
	static int P; //page size
	static int S; //size of a process
	static int J; //The job mix
	static int N; //Number of references
	static String R; //Replace algorithm
	static Scanner randomNumbers;
	static int q = 3;
	
	static FrameTable frameTable;
	static Process[] processes;
	
	public static void main(String[] args) throws FileNotFoundException {
		
		M = Integer.parseInt(args[0]);
		P = Integer.parseInt(args[1]);
		S = Integer.parseInt(args[2]);
		J = Integer.parseInt(args[3]);
		N = Integer.parseInt(args[4]);
		R = args[5];
		randomNumbers = new Scanner(new FileReader("random-numbers.txt"));
		int frameNum = M / P;
		if(R.equals("random")){
			frameTable = new RandomTable(frameNum,randomNumbers);
		}
		
		else if(R.equals("fifo")){
			frameTable = new FIFOTable(frameNum);
		}
		
		else if(R.equals("lru")){
			frameTable = new LRUTable(frameNum);
		}
		
		else{
			System.out.println("There is error in input!");
		}
		
		run();
		print();
	}
	//Run in two cases according to job mix J.
	public static void run(){
		if (J == 1) {
			int processNumber = 1;
			processes = new Process[1];
			processes[0] = new Process(S, processNumber);
			oneProcess();
		} 
		
		else if(J > 1 ){
			processes = new Process[4];
			for(int i=0;i<4;i++){
				processes[i] = new Process(S,i+1);
			}
			fourProcesses();
		}
		
		else {
			System.out.println("Job mix error");
		}
	}
	
	public static void oneProcess() {
		for (int runTime = 1; runTime <= N; runTime++) {
			int pageNumber = processes[0].getNext() / P;			
			//If page fault occurs then replace
			if (frameTable.pageFault(pageNumber, 1, runTime)) {
				frameTable.replace(processes, pageNumber, 1, runTime);
				processes[0].addFaultCount();
			}
			processes[0].nextReference(1, 0, 0, randomNumbers);
		}
	}
	
	//J=2 or J=3 or J=4
	public static void fourProcesses(){
		int totalCycle = N / q;
		double A[] = new double[4];
		double B[] = new double[4];
		double C[] = new double[4];

		//Initiate A, B and C
		if(J == 2){
			for(int i=0;i<4;i++){
				A[i] = 1;
				B[i] = 0;
				C[i] = 0;
			}
		}
		else if(J == 3){		
			for(int i=0;i<4;i++){
				A[i] = 0;
				B[i] = 0;
				C[i] = 0;
			}	
		}
		else if(J == 4){
			A[0] = 0.75;
			B[0] = 0.25;  
			C[0] = 0;
			A[1] = 0.75;
			B[1] = 0;       
			C[1] = 0.25;
			A[2] = 0.75;
			B[2] = 0.125;
			C[2] = 0.125;
			A[3] = 0.5;  
			B[3] = 0.125;
			C[3] = 0.125;
		}

		//run all processes, each process begins referencing by its A, B and C
		for (int i = 0; i <= totalCycle; i++) {
			for(int j=0;j<4;j++){
				reference(j+1,A[j],B[j],C[j],i,totalCycle);
			}
		}
	}
	
	public static void reference(int processNumber, double A, double B, double C, int cycle, int totalCycle){
		int needToReference;//how many times will produce a reference word in one quantum
		if(cycle!=totalCycle){
			needToReference = q;
		}
		else{
			needToReference = N % q;
		}
		for (int ref = 0; ref < needToReference; ref++) {
			int time = q * cycle * 4 + ref + 1 + (processNumber - 1) * needToReference;
			int pageNumber = processes[processNumber - 1].getNext() / P;
			// if page fault occurs
			if (frameTable.pageFault(pageNumber, processNumber, time)) {
				frameTable.replace(processes, pageNumber, processNumber, time);
				processes[processNumber - 1].addFaultCount();
			}
			// referencing the next word.
			processes[processNumber - 1].nextReference(A, B, C, randomNumbers);
		}
	}
	
	public static void print() {
		int totalFaultTimes = 0;
		int totalResidencyTimes = 0;
		int totalEvictTimes = 0;
		//Echo input data
		System.out.println("The machine size is " + M +".");
		System.out.println("The page size is " + P +".");
		System.out.println("The process size is " + S +".");
		System.out.println("The job mix number is " + J +".");
		System.out.println("The number of references per process is " + N +".");
		System.out.println("The replacement algorithm is " + R +".");
		System.out.println("The level of debugging output is 0");
		System.out.println();
		
		//print the number of page faults and the average residency time.
		for (int i = 0; i < processes.length; i++) {
			int faultTime = processes[i].pageFaultCount;
			int residencyTime = processes[i].residencyTime;
			int evictTime = processes[i].evictCount;
			if (evictTime == 0) {
				System.out.println("Process " + (i + 1) + " had " + faultTime + " faults.\n\tWith no evictions, the average residence is undefined.");
			} 
			else {
				double averageResidency = (double) residencyTime / evictTime;
				System.out.println("Process " + (i + 1) + " had " + faultTime + " faults and " + averageResidency + " average residency.");
			}
			totalFaultTimes += faultTime;
			totalResidencyTimes += residencyTime;
			totalEvictTimes += evictTime;
		}
		
		//the total number of faults and the overall average residency time
		if (totalEvictTimes == 0) {
			System.out.println("\nThe total number of faults is " + totalFaultTimes + ".\n\tWith no evictions, the overall average residency is undifined.");
		} 
		else {
			double totalAverageResidency = (double)totalResidencyTimes / totalEvictTimes;
			System.out.println("\nThe total number of faults is "+ totalFaultTimes+ " and the overall average residency is " + totalAverageResidency + ".");
		}		
	}
}
