package org.oobium.pipeline;

import java.util.List;

import org.oobium.app.http.MimeType;

public interface AssetPipeline {

	public abstract PipelinedAsset createAsset(MimeType type, List<String> assets);

	public abstract PipelinedAsset deleteAsset(MimeType type, List<String> assets);

	public abstract PipelinedAsset getAsset(MimeType type, List<String> assets);

	public abstract PipelinedAsset getAsset(String location);

}
