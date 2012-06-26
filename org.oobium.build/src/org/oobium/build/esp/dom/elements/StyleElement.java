package org.oobium.build.esp.dom.elements;

import org.oobium.build.esp.dom.common.AssetElement;
import org.oobium.build.esp.dom.parts.StylePart;


public class StyleElement extends MarkupElement implements AssetElement {

	private StylePart asset;
	
	public StyleElement() {
		super(Type.StyleElement);
	}

	@Override
	public StylePart getAsset() {
		return asset;
	}

	@Override
	public boolean hasAsset() {
		return asset != null && asset.hasRules();
	}

	public void setAsset(StylePart asset) {
		this.asset = asset;
	}
	
}
