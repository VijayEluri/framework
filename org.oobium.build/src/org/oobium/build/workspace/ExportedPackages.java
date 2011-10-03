package org.oobium.build.workspace;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExportedPackages {

	private Map<String, ExportedPackage> map;
	private Set<ExportedPackage> set;

	public ExportedPackages() {
		set = new HashSet<ExportedPackage>();
	}

	private Map<String, ExportedPackage> map() {
		if(map == null) {
			for(ExportedPackage pkg : set) {
				map.put(pkg.name, pkg);
			}
		}
		return map;
	}

	public boolean add(ExportedPackage pkg) {
		map = null;
		return set.add(pkg);
	}
	
	public boolean contains(ExportedPackage exportedPackage) {
		return set.contains(exportedPackage);
	}
	
	public boolean contains(String packageName) {
		return map().containsKey(packageName);
	}
	
	public ExportedPackage get(String packageName) {
		return map().get(packageName);
	}
	
	public Set<ExportedPackage> getAll() {
		return new HashSet<ExportedPackage>(set);
	}

	public ExportedPackage getFor(ImportedPackage importedPackage) {
		ExportedPackage exportedPackage = map().get(importedPackage.name);
		if(exportedPackage != null && exportedPackage.version.resolves(importedPackage.versionRange)) {
			return exportedPackage;
		}
		return null;
	}

	public boolean remove(ExportedPackage pkg) {
		map = null;
		return set.remove(pkg);
	}
	
	public boolean resolves(ImportedPackage importedPackage) {
		ExportedPackage exportedPackage = map().get(importedPackage.name);
		return (exportedPackage != null && exportedPackage.version.resolves(importedPackage.versionRange));
	}

	public int size() {
		return set.size();
	}
	
}
