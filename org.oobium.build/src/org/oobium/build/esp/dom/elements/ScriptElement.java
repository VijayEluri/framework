package org.oobium.build.esp.dom.elements;

import org.oobium.build.esp.dom.common.AssetElement;
import org.oobium.build.esp.dom.parts.ScriptPart;


public class ScriptElement extends MarkupElement implements AssetElement {

	private ScriptPart asset;
	
	public ScriptElement() {
		super(Type.ScriptElement);
	}

	@Override
	public ScriptPart getAsset() {
		return asset;
	}

	@Override
	public boolean hasAsset() {
		return asset != null;
	}

	public void setAsset(ScriptPart asset) {
		this.asset = asset;
	}
	
}
