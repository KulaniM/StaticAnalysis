package km.kulani.javadf.domain.types;

public class Encryption extends Crypto {

	private static final String NAME = "enc";

	public Encryption(Crypto crypto, String key) {				
		setMode(crypto.getMode() + NAME);
		setType(getMode()+"(msg,"+key+")");		
	}



}
