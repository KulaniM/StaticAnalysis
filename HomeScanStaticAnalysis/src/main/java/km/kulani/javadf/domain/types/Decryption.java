package km.kulani.javadf.domain.types;

public class Decryption extends Crypto {

	private static final String NAME = "dec";
	

	public Decryption(Crypto crypto, String key) {		
		this.setMode(crypto.getMode() + NAME);
		this.setType(getMode()+"(msg,"+key+")");
	}

	
}
