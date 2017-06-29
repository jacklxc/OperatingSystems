package linker;
import java.util.ArrayList;

public class Symbol {
	private String name;
	private int address;
	private int offset;
	private boolean used = false;
	private boolean dummy;
	private ArrayList<String> error_list;
	
	public Symbol(String name, int address, int offset, boolean dummy){
		set_name(name);
		set_address(address);
		set_offset(offset);
		set_dummy(dummy);
		error_list = new ArrayList<String>();
	}
	
	public void set_name(String name){
		this.name = name;
	}
	
	public String get_name(){
		return name;
	}
	
	public void set_address(int address){
		this.address = address;
	}
	
	public int get_address(){
		return address;
	}
	
	public void set_offset(int offset){
		this.offset = offset;
	}
	
	public int get_offset(){
		return offset;
	}
	
	public void set_used(boolean used){
		this.used = used;
	}
	
	public boolean is_used(){
		return used;
	}
	
	public void set_dummy(boolean dummy){
		this.dummy = dummy;
	}
	
	public boolean is_dummy(){
		return dummy;
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
	
	public int absolute_address(){
		return this.address + this.offset;
	}
}
