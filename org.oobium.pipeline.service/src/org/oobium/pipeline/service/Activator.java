package org.oobium.pipeline.service;

import org.oobium.pipeline.AssetPipeline;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	
	private AssetPipelineService service;
	
	public Activator() {
		service = new AssetPipelineService();
	}
	
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		context.registerService(AssetPipeline.class.getName(), service, null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		service = null;
	}

}
