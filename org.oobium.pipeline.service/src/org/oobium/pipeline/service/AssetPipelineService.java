package org.oobium.pipeline.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.app.http.MimeType;
import org.oobium.pipeline.AssetPipeline;
import org.oobium.pipeline.PipelinedAsset;
import org.oobium.utils.StringUtils;

public class AssetPipelineService implements AssetPipeline {

	private Map<String, PipelinedAsset> assetsByLocation;
	private Map<String, PipelinedAsset> assetsByKey;
	
	public AssetPipelineService() {
		assetsByKey = new HashMap<String, PipelinedAsset>();
		assetsByLocation = new HashMap<String, PipelinedAsset>();
	}
	
	void addAsset(Asset asset) {
		synchronized(assetsByLocation) {
			assetsByLocation.put(asset.getLocation(), asset);
		}
	}
	
	@Override
	public PipelinedAsset createAsset(MimeType type, List<String> assets) {
		synchronized(assets) {
			AssetCreator creator = new AssetCreator(this, type, assets);
			assetsByKey.put(getKey(type, assets), creator.getAsset());
			new Thread(creator).start();
			return creator.getAsset();
		}
	}
	
	@Override
	public PipelinedAsset deleteAsset(MimeType type, List<String> assets) {
		return null; // TODO
	}

	@Override
	public PipelinedAsset getAsset(MimeType type, List<String> assets) {
		String key = getKey(type, assets);
		PipelinedAsset asset = assetsByKey.get(key);
		if(asset == null) {
			synchronized(assets) {
				asset = assetsByKey.get(key);
				if(asset == null) {
					asset = createAsset(type, assets);
				}
			}
		}
		return asset;
	}
	
	@Override
	public PipelinedAsset getAsset(String location) {
		return assetsByLocation.get(location);
	}

	public String getKey(MimeType type, List<String> assets) {
		return type.name + "::" + StringUtils.join(assets, ':');
	}

}
