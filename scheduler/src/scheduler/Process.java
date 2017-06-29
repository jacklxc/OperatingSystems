package scheduler;

public class Process implements Comparable<Process>{
	private int A,B,C,CC,IO,State;//-2 done, -1 not started, 0 ready, 1 running, 2 blocked
	private int Finish=0, IOT=0, Wait=0;
	private double R;
	public Process(int a, int b, int c, int io){
		A=a; 
		B=b; 
		C=c; 
		CC=c;//constant
		IO=io; 
		R=0;
		State=-1;
	}
	
	public void incIOT(int i){////
		this.IOT+=i;
	}
	public void incWait(){
		this.Wait++;
	}
	public int getFinish() {
		return Finish;
	}
	public void setFinish(int finish) {
		Finish = finish;
	}
	public int getIOT() {
		return IOT;
	}
	public void setIOT(int iOT) {
		IOT = iOT;
	}
	public int getWait() {
		return Wait;
	}
	public void setWait(int wait) {
		Wait = wait;
	}

	public int getA() {
		return A;
	}

	public void setA(int a) {
		A = a;
	}

	public int getB() {
		return B;
	}

	public void setB(int b) {
		B = b;
	}

	public int getC() {
		return C;
	}

	public void setC(int c) {
		C = c;
	}
	
	public void decC(){
		this.C--;
	}
	
	public int getCC(){
		return CC;
	}
	
	public int getIO() {
		return IO;
	}

	public void setIO(int iO) {
		IO = iO;
	}
	
	public void setR(double r){
		R=r;
	}

	public double getR(){
		return R;
	}
	
	public boolean running(){
		if(State == 1)
			return true;
		else
			return false;
	}
	
	public boolean blocked(){
		if(State == 2)
			return true;
		else
			return false;
	}
	
	public boolean ready(){
		if(State == 0)
			return true;
		else
			return false;
	}
	
	public boolean notStarted(){
		if(State == -1)
			return true;
		else
			return false;
	}
	
	public boolean terminated(){
		if(State == -2)
			return true;
		else
			return false;
	}

	public void run() {
		State = 1;
	}
	
	public void setReady() {
		State = 0;
	}
	public void block() {
		State = 2;
	}
	public void terminate() {
		State = -2;
	}
	@Override
	public int compareTo(Process p){//////
		if(p.A>this.A)return -1;
		else if(p.A<this.A)return 1;
		else return 0;
	}
}
