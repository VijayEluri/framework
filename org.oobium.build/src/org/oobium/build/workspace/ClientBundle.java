package org.oobium.build.workspace;

import java.io.File;
import java.util.jar.Manifest;

public class ClientBundle extends Bundle {

	ClientBundle(Type type, File file, Manifest manifest) {
		super(type, file, manifest);
		if(activator != null && activator.exists()) {
			throw new IllegalStateException("Activator exists - Client bundle cannot depend on OSGi");
		}
	}

}
