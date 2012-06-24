package org.oobium.pipeline;

public interface AssetPipelineListener {

	public abstract void assetCreated(String[] urls);
	
	public abstract void assetDeleted(String[] urls);
	
}
