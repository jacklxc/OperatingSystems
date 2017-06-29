package linker;

import java.util.*;

public class Linker {
	
	private static SymbolList symbol_list;
	private static Hashtable<String,Integer> symbol_table;
	private static UseList use_list;
	private static ArrayList<ArrayList<Program_text>> text_list;
	private static ArrayList<String> warning_list;
	
	public static void main(String[] args){
		symbol_list = new SymbolList();
		symbol_table = new Hashtable<String,Integer>();
		use_list = new UseList();
		text_list = new ArrayList<ArrayList<Program_text>>();
		warning_list = new ArrayList<String>();
		
		try{
			first_pass();
			check_symbol();
			make_symbol_table();
			check_use_defined();
			second_pass();
			output();
		}
		catch(Exception e){
			System.out.println("There is an error in main\n");
			System.err.println(e.getMessage());
		}		
	}
	

	public static void first_pass(){
		Scanner input = new Scanner(System.in);
		try{
			int indicator_count = 0;
			int address_offset = 0;
			int module = 0;
			int which_list = 0;
			while(input.hasNext()){
				int indicator = Integer.parseInt(input.next());
				module = indicator_count / 3;
				which_list = indicator_count % 3;
				if(which_list == 0){ // Now we are in definition list
					String symbol_name;
					int symbol_address;
					
					for (int i = 0; i < indicator; i++){
						symbol_name = input.next();
						symbol_address = Integer.parseInt(input.next());
						Symbol symbol = new Symbol(symbol_name,symbol_address,address_offset,false);
						//Need to check if symbol already exist.
						symbol_list.add(symbol, module);
					}
					if (indicator == 0){//set a dummy symbol.
						symbol_name = "dummy";
						symbol_address = 0;
						Symbol symbol = new Symbol(symbol_name,symbol_address,address_offset,true);
						symbol_list.add(symbol, module);
					}
				}
				else if(which_list == 1){// Now we are in use list
					String use_name;
					int use_address;
					
					for (int i = 0; i < indicator; i++){
						use_name = input.next();
						use_address = Integer.parseInt(input.next());
						Use use = new Use(use_name, use_address, false);
						use_list.add(use,module);
					}
					if (indicator == 0) {//set a dummy use.
						use_name = "dummy";
						use_address = -1;
						Use use = new Use(use_name,use_address,true);
						use_list.add(use,module);
					}
				}
				else{// Now we are in program text list
					for (int i = 0; i<indicator; i++){
						String operand = input.next();
						int text = Integer.parseInt(input.next());
						Program_text program_text = new Program_text(operand, text);
						if(i==0){
							text_list.add(new ArrayList<Program_text>());
						}
						text_list.get(indicator_count/3).add(program_text);
						address_offset++;
					}
				}
				indicator_count++;
			}
			symbol_list.linearize();
			use_list.linearize();
		}
		catch (Exception e){
			System.err.println("There is an error in the first pass.");
			System.err.println(e.getMessage());
		} 
		finally{
			input.close();
		}
	}
	
	public static void check_symbol(){
		for (int i = 0; i < symbol_list.size()-1; i++){ 
			for (int j = i+1; j<symbol_list.size(); j++){
				//Check if there is multiply defined symbol
				if (!symbol_list.get_linear(i).is_dummy()&&!symbol_list.get_linear(j).is_dummy()&&
						symbol_list.get_linear(i).get_name().equals(symbol_list.get_linear(j).get_name())){
					symbol_list.get_linear(j).set_dummy(true);
					symbol_list.get_linear(i).add_error("Error: This variable is multiply defined; first value used.");
				}
			}
		}
		for (int i = 0; i < symbol_list.module_size(); i++){
			if (symbol_list.get_linear(i).get_address() >= text_list.get(symbol_list.get_module(i)).size() && !symbol_list.get_linear(i).is_dummy()){
				//Address appearing in definition exceeds the size of the module
				symbol_list.get_linear(i).add_error("Error: The value of "+symbol_list.get_linear(i).get_name()+ " is outside module "
						+ Integer.toString(symbol_list.get_module(i)) + "; zero (relative) used.");
				symbol_list.get_linear(i).set_address(0);
			}
		}
	}
	
	public static void make_symbol_table(){
		//Obtain the symbol table
		for (int i = 0; i<symbol_list.size(); i++){
			if (!symbol_list.get_linear(i).is_dummy()){
				symbol_table.put(symbol_list.get_linear(i).get_name(), symbol_list.get_linear(i).absolute_address());
			}
		}
	}
	
	public static void check_use_defined(){
		boolean defined;
		for (int i = 0; i<use_list.size(); i++){
			defined = false;
			for (int j = 0; j<symbol_list.size(); j++){
				if (!use_list.get_linear(i).is_dummy() && !symbol_list.get_linear(j).is_dummy()&&
						use_list.get_linear(i).get_symbol().equals(symbol_list.get_linear(j).get_name())){
					symbol_list.get_linear(j).set_used(true);
					defined = true;
					break;
				}
			}
			if (!defined && !use_list.get_linear(i).is_dummy()){//There is a symbol not defined but used in use list
				use_list.get_linear(i).set_error();
				// This is really tricky, if a symbol is not defined but used, the original address specified by the use
				//must be free from the error of E type address not on use chain. 
				text_list.get(i).get(use_list.get_linear(i).get_address()).link();
				text_list.get(i).get(use_list.get_linear(i).get_address()).add_error(
						"Error: "+use_list.get_linear(i).get_symbol() +" is not defined; zero used.");
				use_list.get_linear(i).set_address(0);
			}
		}
		
		for (int i = 0; i < symbol_list.size(); i++){
			if (!symbol_list.get_linear(i).is_used() && !symbol_list.get_linear(i).is_dummy()){
				warning_list.add("Warning: "+symbol_list.get_linear(i).get_name()+" was defined in module "+
						Integer.toString(symbol_list.get_module(i)) + " but never used.");
			}
		}
	}
	
	public static void second_pass(){
		String operand;
		int text;
		try{
		//Deal with External address based on use list.
			for (int m = 0; m < use_list.size(); m++){
				if (!use_list.get_linear(m).is_dummy()){
					ArrayList<Program_text> module = text_list.get(use_list.get_module(m));
					int next = use_list.get_linear(m).get_address();
					while(next!=777){
						if (next >= module.size()){
							//The address in the use list exceeds the size of the module.
							text_list.get(use_list.get_module(m)).get(use_list.get_position(m)).add_error(
									"Error: Pointer in use chain exceeds module size; chain terminated.");
							break;
						}
						Program_text program_text = module.get(next);
						program_text.link();
						
						//The operand is not E but still in use list.
						if (!program_text.get_operand().equals("E")){
							program_text.add_error("Error: " + program_text.get_operand() + 
									" type address on use chain; treated as E type.");
							program_text.set_operand("E");
						}
						next = program_text.address_field();
						if (use_list.get_linear(m).has_error()){// For each program text used by symbol that was not defined, prompt error.
							program_text.add_error("Error: "+use_list.get_linear(m).get_symbol() +" is not defined; zero used.");
						}
						try{
							program_text.set_address(symbol_table.get(use_list.get_linear(m).get_symbol())); 
						}
						catch (NullPointerException e){
							program_text.set_address(0);
						}
					}
				}
			}
			
			//Deal with Relative address and also check if there is External address not linked.
			for (int i = 0; i< text_list.size(); i++){
				for(int j = 0; j<text_list.get(i).size(); j++){
					Program_text program_text = text_list.get(i).get(j);
					text = program_text.address_field();
					operand = program_text.get_operand();
					if(operand.equals("R")){
						text += symbol_list.get(i,0).get_offset();
						program_text.set_address(text);
					}
					//Check if there is External address not linked.
					else if (operand.equals("E") && !program_text.is_linked()){
						program_text.add_error("Error: E type address not on use chain; treated as I type.");
						program_text.set_operand("I");
					}
				}
			}
		}
		
		catch (Exception e){
			System.err.println("There is an error in the second pass.");
			System.err.println(e.getMessage());
		}
		
	}
	
	public static void output(){
		System.out.println("Symbol Table");	
		for (int i = 0; i < symbol_list.size(); i++){
			Symbol symbol = symbol_list.get_linear(i);
			if (!symbol.is_dummy()){
				if (symbol.has_error()){
					System.out.print(symbol.get_name() + "=" + Integer.toString(symbol.absolute_address()) + "\t");
					symbol.print_error();
				}
				else{
					System.out.println(symbol.get_name() + "=" + Integer.toString(symbol.absolute_address()));
				}
			}
		}
		System.out.println();
		System.out.println("Memory Map");
		int index=0;
		for (int i = 0; i < text_list.size(); i++){
			ArrayList<Program_text> module = text_list.get(i);
			for (int j = 0; j < module.size(); j++){
				Program_text program_text = module.get(j);
				if (program_text.has_error()){
					System.out.print(Integer.toString(index) + " " + Integer.toString(program_text.get_text()) +"\t");
					program_text.print_error();
				}
				else{
					System.out.println(Integer.toString(index) + " " + Integer.toString(program_text.get_text()));
				}
				index++;
			}
		}
		//Print warnings
		System.out.println();
		for (String error: warning_list){
			System.out.println(error);
		}
	}
}
