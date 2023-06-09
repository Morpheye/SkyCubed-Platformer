package skycubedPlatformer.util.appdata;

//Obfuscates save file so it is more annoying to directly edit
public class JsonObfuscator {
	static String obfuscate(String input, long key) {
		char[] output = new char[input.length()];
		
		for (int i=0; i<input.length(); i++) {
			short c = (short) input.charAt(i); //get unicode value
			output[i] = (char) shortValue(32767L - c + key);
			
		}
		
		return String.valueOf(output);
	}
	
	static String deobfuscate(String input, long key) {
		char[] output = new char[input.length()];
		
		for (int i=0; i<input.length(); i++) {
			short c = (short) input.charAt(i); //get unicode value
			output[i] = (char) shortValue(32767L - c + key);
			
		}
		
		return String.valueOf(output);
	}
	
	static short shortValue(long input) {
		if (input >= Short.MIN_VALUE && input <= Short.MAX_VALUE) return (short) input;
		else {
			long value = input;
			
			while (value > 32767L || input < -32768L) {
				if (value <= Short.MIN_VALUE) value += 65536L;
				else if (value >= Short.MAX_VALUE) value -= 65536L;
				
			}
			
			return (short) value;
		}
		
	}
}
