package linker;
import java.util.ArrayList;

public class UseList {
	private ArrayList<ArrayList<Use>> list;
	private ArrayList<int[]> index_list;
	
	public UseList(){
		list = new ArrayList<ArrayList<Use>>();
		index_list = new ArrayList<int[]>();
	}
	
	public void add(Use use, int i){
		if (list.size() == i){
			list.add(new ArrayList<Use>());
		}
		list.get(i).add(use);
	}
	
	public Use get(int i, int j){
		return list.get(i).get(j);
	}
	
	public void linearize(){
		for (int i=0; i < list.size(); i++){
			for (int j=0; j < list.get(i).size(); j++){
				int[] pair = new int[2];
				pair[0] = i;
				pair[1] = j;
				index_list.add(pair);
			}
		}
	}
	
	public Use get_linear(int i){
		return list.get(index_list.get(i)[0]).get(index_list.get(i)[1]);
	}
	
	public int get_module(int i){
		return index_list.get(i)[0];
	}
	
	public int get_position(int i){
		return index_list.get(i)[1];
	}
	
	public int module_size(){
		return list.size();
	}
	
	public int size(){
		return index_list.size();
	}
}

