package matlab;

import java.io.File;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;

public class MatlabAccess {
	
	private static MatlabProxy proxyInstance;
	
	public static MatlabProxy getProxy() {
		if (proxyInstance == null) {

			MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
			.setUsePreviouslyControlledSession(true)
			.setMatlabStartingDirectory(new File("src/matlab"))
	//		.setHidden(true) // messes with starting directory
			.build();
			
			MatlabProxyFactory fact = new MatlabProxyFactory(options);
			
			try {
				proxyInstance = fact.getProxy();
			} catch (MatlabConnectionException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		return proxyInstance;
	}

}
