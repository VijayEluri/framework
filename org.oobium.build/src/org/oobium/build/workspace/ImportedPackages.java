package org.oobium.build.workspace;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImportedPackages implements Iterable<ImportedPackage> {

	private Map<String, ImportedPackage> map;
	private Set<ImportedPackage> set;

	public ImportedPackages() {
		set = new HashSet<ImportedPackage>();
	}

	private Map<String, ImportedPackage> map() {
		if(map == null) {
			for(ImportedPackage pkg : set) {
				map.put(pkg.name, pkg);
			}
		}
		return map;
	}

	public boolean add(ImportedPackage pkg) {
		map = null;
		return set.add(pkg);
	}
	
	public boolean contains(ImportedPackage importedPackage) {
		return set.contains(importedPackage);
	}
	
	public boolean contains(String packageName) {
		return map().containsKey(packageName);
	}
	
	public ImportedPackage get(String packageName) {
		return map().get(packageName);
	}
	
	public Set<ImportedPackage> getAll() {
		return new HashSet<ImportedPackage>(set);
	}

//	public ImportedPackage getFor(ImportedPackage importedPackage) {
//		ImportedPackage importedPackage = map().get(importedPackage.name);
//		if(importedPackage != null && importedPackage.version.resolves(importedPackage.versionRange)) {
//			return importedPackage;
//		}
//		return null;
//	}

	public java.util.Iterator<ImportedPackage> iterator() {
		return set.iterator();
	};
	
	public boolean remove(ImportedPackage pkg) {
		map = null;
		return set.remove(pkg);
	}
	
//	public boolean resolves(ImportedPackage importedPackage) {
//		ImportedPackage importedPackage = map().get(importedPackage.name);
//		return (importedPackage != null && importedPackage.version.resolves(importedPackage.versionRange));
//	}

	public int size() {
		return set.size();
	}
	
}
