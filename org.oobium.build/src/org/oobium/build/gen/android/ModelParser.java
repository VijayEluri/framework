package org.oobium.build.gen.android;

import static org.oobium.utils.FileUtils.readFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelParser {

	private static final Pattern attrNamePattern = Pattern.compile("@Attribute\\([^\\)]*name\\=\"(\\w+)\"[^\\)]+\\)");
	private static final Pattern attrTypePattern = Pattern.compile("@Attribute\\([^\\)]*type\\=(\\w+)\\.class[^\\)]*\\)");
	
	private String src;
	
	public ModelParser(File model) {
		this.src = readFile(model).toString();
	}
	
	public List<String[]> getAttributes() {
		List<String[]> attrs = new ArrayList<String[]>();

		Matcher mType = attrTypePattern.matcher(src);
		Matcher mName = attrNamePattern.matcher(src);
		while(mType.find() && mName.find(mType.start())) {
			attrs.add(new String[] { mType.group(1), mName.group(1) });
		}

		return attrs;
	}

}
