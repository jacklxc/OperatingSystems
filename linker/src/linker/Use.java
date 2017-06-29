package linker;

public class Use {
	private String symbol;
	private int address;
	private boolean dummy;
	private boolean error = false;
	
	public Use(String symbol, int address, boolean dummy){
		set_symbol(symbol);
		set_address(address);
		set_dummy(dummy);
	}

	public void set_symbol(String symbol){
		this.symbol = symbol;
	}
	
	public String get_symbol(){
		return symbol;
	}
	
	public void set_address(int address) {
		this.address = address;
	}
	
	public int get_address(){
		return address;
	}
	
	public void set_dummy(boolean dummy){
		this.dummy = dummy;
	}
	
	public boolean is_dummy(){
		return dummy;
	}
	
	public boolean has_error(){
		return error;
	}
	
	public void set_error(){
		this.error = true;
	}
}
