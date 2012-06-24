package org.oobium.pipeline.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.oobium.app.http.MimeType;
import org.oobium.client.Client;
import org.oobium.client.ClientResponse;

public class AssetCreator implements Runnable {

	private final AssetPipelineService service;
	private final Asset asset;
	private final MimeType type;
	private final List<String> urls;
	
	public AssetCreator(AssetPipelineService service, MimeType type, List<String> urls) {
		this.service = service;
		this.type = type;
		this.urls = urls;
		this.asset = new Asset(type);
	}

	public Asset getAsset() {
		return asset;
	}
	
	@Override
	public void run() {
		try {
			File file = File.createTempFile("pipelined_asset-", "." + type.extension() + ".gz");
			System.out.println("file: " + file);
			GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(file));
			for(String url : urls) {
				System.out.println("downloading: " + url);
				Client client = Client.client(url);
				ClientResponse response = client.get();
				if(response.isSuccess()) {
					out.write(response.getContent());
					System.out.println("downloaded: " + url);
				} else {
					if(response.exceptionThrown()) {
						response.getException().printStackTrace();
					} else {
						System.err.println("error: " + response.getContent());
					}
				}
			}
			out.close();
			asset.setFile(file);
			service.addAsset(asset);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
