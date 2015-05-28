package com.sap.sapjam.samples;

public class Commands {
	public String _odata_call_type;
	public String _odata_call;
	public String _description;
	public String _command;
	
	public Commands(String description, String odata_call_type, String odata_call, String command, String... jamParameters) {
		this._description = description;
		this._odata_call_type = odata_call_type;
		this._odata_call = odata_call;
		this._command = command;
		
		if (jamParameters != null){
			processJamParameters(jamParameters);
		}
	}
	
	private void processJamParameters(String... jamParameters){
		String[] currentKeyValuePair;
		String currentKey;
		String currentValue;
		outerloop:
		for (String s:jamParameters){
			if (s == null){
				break outerloop;
			}
			else if (s != null){
				currentKeyValuePair = s.split(":", 2);
				currentKey = currentKeyValuePair[0];
				currentValue = currentKeyValuePair[1];
				
				if (currentKey.equalsIgnoreCase("groupId")){
					if (currentValue.equalsIgnoreCase("null")){
						this._command += "&Id=ENTER_YOUR_ID_HERE";
					}
					break outerloop;
				}
			}
		}
	}
	
	/*private void processJamParameters(String... jamParameters){
		if (jamParameters != null){
			outerloop:
			for (String s:jamParameters){
				if (s == null){
					this._command += "&Id=ENTER_YOUR_ID_HERE";
					break outerloop;
				}
				else if (s.equalsIgnoreCase("none")){
					break outerloop;
				}
			}
		}
	}*/
	
	
	
}
