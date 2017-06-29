package linker;
import java.util.ArrayList;

public class SymbolList {
	private ArrayList<ArrayList<Symbol>> list;
	private ArrayList<int[]> index_list;
	
	public SymbolList(){
		list = new ArrayList<ArrayList<Symbol>>();
		index_list = new ArrayList<int[]>();
	}
	
	public void add(Symbol symbol, int i){
		if (list.size() == i){
			list.add(new ArrayList<Symbol>());
		}
		list.get(i).add(symbol);
	}
	
	public Symbol get(int i, int j){
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
	
	public Symbol get_linear(int i){
		return list.get(index_list.get(i)[0]).get(index_list.get(i)[1]);
	}
	
	public int get_module(int i){ // get linear index and return module index
		return index_list.get(i)[0];
	}
	
	public int module_size(){
		return list.size();
	}
	
	public int size(){
		return index_list.size();
	}
}

