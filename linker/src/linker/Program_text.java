package linker;
import java.util.ArrayList;

public class Program_text {
	private String operand;
	private int text;
	private boolean linked =false;
	private ArrayList<String> error_list;
	
	public Program_text(String operand, int text){
		set_operand(operand);
		set_text(text);
		error_list = new ArrayList<String>();
	}
	
	public void set_operand(String operand){
		this.operand = operand;
	}
	
	public String get_operand(){
		return operand;
	}
	
	public void set_text(int text){
		this.text = text;
	}
	
	public boolean is_linked(){
		return linked;
	}
	
	public void link(){
		this.linked = true;
	}
	
	public int opcode(){
		return text/1000;
	}
	
	public int address_field(){
		return text%1000;
	}
	
	public void set_address(int address){
		this.text = opcode()*1000 + address;
	}
	
	public int get_text(){
		return text;
	}
	
	public boolean has_error(){
		return error_list.size() > 0;
	}
	
	public void add_error(String error){
		error_list.add(error);
	}
	
	public void print_error(){
		for (String error: error_list){
			System.out.println(error);
		}
	}
}
