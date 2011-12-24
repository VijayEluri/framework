package org.oobium.app.server;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

class SslContextFactory {
	
	private static final Map<Server, SSLContext> contexts = new HashMap<Server, SSLContext>();
	
	static void addSslContext(Server server, ServerConfig config) {
		String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
		if(algorithm == null) {
			algorithm = "SunX509";
		}
		try {
			char[] password = config.option("keystore.password", "password").toCharArray();
			InputStream stream = new FileInputStream(config.option("keystore.location", "conf/keystore.jks"));

			KeyStore ks = KeyStore.getInstance(config.option("keystore.algorithm", "JKS"));
			ks.load(stream, password);
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			kmf.init(ks, password);
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
			tmf.init(ks);
			
			SSLContext context = SSLContext.getInstance(config.option("ssl.protocol", "TLS"));
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
			contexts.put(server, context);
		} catch(Exception e) {
			throw new Error("Failed to initialize the server SSLContext", e);
		}
	}

	static void removeSslContext(Server server) {
		contexts.remove(server);
	}
	
	static SSLContext getSslContext(Server server) {
		return contexts.get(server);
	}

}
