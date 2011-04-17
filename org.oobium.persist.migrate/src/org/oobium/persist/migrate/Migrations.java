package org.oobium.persist.migrate;

import java.util.ArrayList;
import java.util.List;

public class Migrations {

	private final List<Class<? extends Migration>> migrationClasses;
	
	public Migrations() {
		this.migrationClasses = new ArrayList<Class<? extends Migration>>();
	}
	
	public void add(Class<? extends Migration> migrationClass) {
		migrationClasses.add(migrationClass);
	}
	
	public boolean contains(Class<? extends Migration> migrationClass) {
		return migrationClasses.contains(migrationClass);
	}

	Migration get(int index) throws InstantiationException, IllegalAccessException {
		return migrationClasses.get(index).newInstance();
	}

	List<String> getNames() {
		List<String> names = new ArrayList<String>();
		for(Class<?> migrationClass : migrationClasses) {
			names.add(migrationClass.getSimpleName());
		}
		return names;
	}
	
	public Class<? extends Migration> remove(int index) {
		return migrationClasses.remove(index);
	}
	
	public boolean remove(Class<? extends Migration> migrationClass) {
		return migrationClasses.remove(migrationClass);
	}

	public int size() {
		return migrationClasses.size();
	}
	
}
