package banker;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws FileNotFoundException{
		Scanner scanner = new Scanner(new FileReader(args[0]));
				
		//Total number of task T
		String firstline = scanner.nextLine();
		String firstLine[] = firstline.split("\\s+");
		int T = Integer.parseInt(firstLine[0]);

		//Total resource number R
		int R = Integer.parseInt(firstLine[1]);
		//allocate space for necessary data structure, initialize all matrix and class members
		Banker banker = new Banker(T,R);
		Optimistic optimistic = new Optimistic(T,R);
				
		//Resource Number of each type
		for(int i=0;i<firstLine.length-2;i++){
			banker.setAvailable(i,Integer.parseInt(firstLine[i+2]));
			optimistic.setAvailable(i,Integer.parseInt(firstLine[i+2]));
		}
				
		//initialize task arrays
		Task[] tasks = new Task[T];
		for(int i=0;i<tasks.length;i++){
			tasks[i] = new Task(i);
		}
		//read activity lines
		while(scanner.hasNext()){
			String activities = scanner.nextLine();
			//put into activities list
			if(!activities.contentEquals("")){
				String activity[] = activities.split("\\s+");
				int taskID = Integer.parseInt(activity[1]);
				tasks[taskID-1].addActivity(activity);
			}
		}
		optimistic.run(tasks);
		//reset tasks
		for(int i=0;i<tasks.length;i++){
			tasks[i].reset();
		}
		banker.run(tasks);
		//printTasks(tasks);
	}
	
	public static void printTasks(Task[] tasks){
		//Print out tasks
		for(Task task: tasks){
			task.printOut();
		}
	}
}
