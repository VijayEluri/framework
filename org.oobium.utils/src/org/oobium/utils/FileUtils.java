/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.oobium.logging.Logger;

public class FileUtils {

	public static final int NONE					= 0;
	public static final int EXECUTABLE				= 1 << 0;
	public static final int READ_ONLY				= 1 << 1;
	public static final int OVER_WRITE				= 1 << 2;
	public static final int PERSIST_LAST_MODIFIED 	= 1 << 3;

	private static final Logger logger = Logger.getLogger(FileUtils.class);
	
	private static void addFiles(List<File> list, File folder, final String name) {
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return (file.isDirectory() || name == null || file.getName().equals(name));
			}
		});
		for(File file : files) {
			if(file.isDirectory()) {
				addFiles(list, file, name);
			} else {
				list.add(file);
			}
		}
	}
	
	private static boolean endsWith(String name, String[] endsWith) {
		for(String end : endsWith) {
			if(name.endsWith(end)) {
				return true;
			}
		}
		return false;
	}
	
	private static void addFiles(List<File> list, File folder, final boolean accept$, final String...endsWith) {
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return !file.isHidden() &&
						(file.isDirectory() || endsWith.length == 0 || endsWith(file.getName(), endsWith)) &&
						(accept$ || !file.getName().contains("$"));
			}
		});
		for(File file : files) {
			if(file.isDirectory()) {
				addFiles(list, file, accept$, endsWith);
			} else {
				list.add(file);
			}
		}
	}
	
	public static File appendToFile(File file, String src, boolean newLine) {
		if(file.isFile()) {
			StringBuilder sb = readFile(file);
			if(newLine) sb.append("\n");
			sb.append(src);
			writeFile(file, sb.toString());
		} else {
			writeFile(file, src);
		}
		return file;
	}
	
	/**
	 * Copy the given source File to the given destination File.  If the source File
	 * is a directory then a deep copy will be performed, including hidden files.
	 * <p>This is a convenience method that is equivalent to: <code>copy(src, dst, NONE, false)</code>.</p>
	 * @param src the source File (method simply returns if null)
	 * @param dst the destination File (method simply returns if null)
	 * @throws IOException
	 * @see {@link #copy(File, File, int, boolean)}
	 */
	public static void copy(File src, File dst) throws IOException {
		copy(src, dst, NONE, false);
	}
	
	/**
	 * Copy the given source File to the given destination File.  If the source File
	 * is a directory then a deep copy will be performed, or skipping hidden
	 * files based on the value of the skipHidden parameter.
	 * <p>This is a convenience method that is equivalent to: <code>copy(src, dst, NONE, skipHidden)</code>.</p>
	 * @param src the source File (method simply returns if null)
	 * @param dst the destination File (method simply returns if null)
	 * @param skipHidden hidden files will not be copied if true; only valid if src is a directory
	 * @throws IOException
	 * @see {@link #copy(File, File, int, boolean)}
	 */
	public static void copy(File src, File dst, boolean skipHidden) throws IOException {
		copy(src, dst, NONE, skipHidden);
	}
	
	/**
	 * Copy the given source File to the given destination File.  If the source File
	 * is a directory then a deep copy will be performed, including hidden files.
	 * <p>This is a convenience method that is equivalent to: <code>copy(src, dst, flags, false)</code>.</p>
	 * @param src the source File (method simply returns if null)
	 * @param dst the destination File (method simply returns if null)
	 * @param flags bitwise OR'ed flags used to indicate what type of copy
	 * @throws IOException
	 * @see {@link #copy(File, File, int, boolean)}
	 */
	public static void copy(File src, File dst, int flags) throws IOException {
		copy(src, dst, flags, false);
	}

	/**
	 * Copy the given source File to the given destination File.  If the source File
	 * is a directory then a deep copy will be performed, including or skipping hidden
	 * files based on the value of the skipHidden parameter.
	 * @param src the source File (method simply returns if null)
	 * @param dst the destination File (method simply returns if null)
	 * @param flags bitwise OR'ed flags used to indicate what type of copy
	 * @param skipHidden hidden files will not be copied if true; only valid if src is a directory
	 * @throws IOException
	 * @see #NONE
	 * @see #EXECUTABLE
	 * @see #OVER_WRITE
	 * @see #PERSIST_LAST_MODIFIED
	 * @see #READ_ONLY
	 */
	public static void copy(File src, File dst, int flags, boolean skipHidden) throws IOException {
		if(src != null && dst != null) {
			if(src.isFile()) {
				doCopy(src, dst, flags);
			} else if(src.isDirectory()) {
				int beginIndex = src.getAbsolutePath().length();
				File[] sfiles = skipHidden ? findFiles(src) : findAll(src);
				for(File sfile : sfiles) {
					File dfile = new File(dst, sfile.getAbsolutePath().substring(beginIndex));
					doCopy(sfile, dfile, flags);
				}
			} // else nothing to do...
		}
	}

	public static File createFolder(File parent, String...paths) {
		StringBuilder sb = new StringBuilder();
		sb.append(parent.getAbsolutePath());
		for(String path : paths) {
			String[] parts = path.split("/");
			for(String part : parts) {
				sb.append(File.separator).append(part);
			}
		}
		File folder = new File(sb.toString());
		folder.mkdirs();
		return folder;
	}
	
	public static File createJar(File jar, Map<String, File> files) throws IOException {
		return createJar(jar, files, null);
	}
	
	public static File createJar(File jar, long lastModified, String[]...entries) throws IOException {
		if(!jar.exists()) {
			jar.getParentFile().mkdirs();
			jar.createNewFile();
		}

		FileOutputStream fos = null;
		JarOutputStream jos = null;
		try {
			try {
				fos = new FileOutputStream(jar);
			} catch(FileNotFoundException e) {
				// really really shouldn't ever happen...
				Logger.getLogger(FileUtils.class).error(e);
				throw new IOException("file not created, or was moved: " + jar);
			}
			jos = new JarOutputStream(fos);
			
			for(String[] entry : entries) {
				if(entry != null && entry.length > 0) {
					JarEntry jentry = new JarEntry(entry[0]);
					jentry.setTime(lastModified);
					jos.putNextEntry(jentry);
					if(entry.length > 1) {
						jos.write(entry[1].getBytes());
					}
				}
			}
		} finally {
			if(jos != null) {
				try {
					jos.close();
				} catch(IOException e) {
					// throw away
				}
			}
			if(fos != null) {
				try {
					fos.close();
				} catch(IOException e) {
					// throw away
				}
			}
		}
		
		return jar;
	}
	
	public static File createJar(File jar, Map<String, File> files, Manifest manifest) throws IOException {
		if(!jar.exists()) {
			jar.getParentFile().mkdirs();
			jar.createNewFile();
		}

		FileOutputStream fos = null;
		JarOutputStream jos = null;
		try {
			fos = new FileOutputStream(jar);
			jos = (manifest != null) ? new JarOutputStream(fos, manifest) : new JarOutputStream(fos);
			
			for(String name : files.keySet()) {
				if(name.equals("META-INF/MANIFEST.MF") && manifest != null) {
					continue; // we created one above
				}
				File file = files.get(name);
				JarEntry entry = new JarEntry(name);
				entry.setTime(file.lastModified());
				jos.putNextEntry(entry);

				FileInputStream in = null;
				try {
					in = new FileInputStream(file);
					byte buffer[] = new byte[1024];
					int read;
					while((read = in.read(buffer, 0, buffer.length)) != -1) {
						jos.write(buffer, 0, read);
					}
				} finally {
					if(in != null) {
						in.close();
					}
				}
			}
		} catch(Exception e) {
			Logger.getLogger(FileUtils.class).error(e);
		} finally {
			if(jos != null) {
				try {
					jos.close();
				} catch(IOException e) {
					// throw away
				}
			}
			if(fos != null) {
				try {
					fos.close();
				} catch(IOException e) {
					// throw away
				}
			}
		}
		
		return jar;
	}
	
	public static List<File> delete(File...files) {
		List<File> deleted = new ArrayList<File>();
		for(File file : files) {
			deleteContents(file, deleted);
		}
		for(File file : files) {
			if(file.delete()) {
				deleted.add(file);
			}
		}
		return deleted;
	}
	
	public static List<File> deleteContents(File...folders) {
		List<File> deleted = new ArrayList<File>();
		for(File folder : folders) {
			deleteContents(folder, deleted);
		}
		return deleted;
	}
	
	private static void deleteContents(File folder, List<File> deleted) {
		File[] files = (folder != null) ? folder.listFiles() : null;
		if(files != null) {
			for(File file : files) {
				if(file.isDirectory()) {
					deleteContents(file, deleted);
				}
				if(file.delete()) {
					deleted.add(file);
				}
			}
		}
	}
	
	public static void doCopy(File src, File dst, int flags) throws IOException {
		if(dst.exists() && ((flags & OVER_WRITE) != 0)) {
			if(logger.isLoggingDebug()) {
				logger.debug("skipping " + dst.getName());
			}
		} else {
			if(logger.isLoggingDebug()) {
				logger.debug("copying file " + dst.getName());
			}
			if(!dst.exists()) {
				File folder = dst.getParentFile();
				if(!folder.exists()) {
					folder.mkdirs();
				}
				dst.createNewFile();
			}
			BufferedInputStream in = null;
			BufferedOutputStream out = null;
			try {
				in = new BufferedInputStream(new FileInputStream(src));
				out = new BufferedOutputStream(new FileOutputStream(dst));

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				if(flags > 0) {
					if((flags & EXECUTABLE) != 0) {
						dst.setExecutable(true);
					}
					if((flags & READ_ONLY) != 0) {
						dst.setReadOnly();
					}
					if((flags & PERSIST_LAST_MODIFIED) != 0) {
						dst.setLastModified(src.lastModified());
					}
				}
			} finally {
				if(in != null) {
					try {
						in.close();
					} catch(IOException e) {
						// throw away
					}
				}
				if(out != null) {
					try {
						out.close();
					} catch(IOException e) {
						// throw away
					}
				}
			}
		}
	}

	public static boolean fileExists(File dir, String fileName) {
		return fileExists(dir.getAbsolutePath(), fileName);
	}

	public static boolean fileExists(String path, String fileName) {
		File file = new File(path, fileName);
		return file.exists();
	}
	
	/**
	 * Find the File with the given name in the given folder, or its  subfolders.
	 * @param folder the folder in which to start searching
	 * @param name the name of the file to search for
	 * @return the file if found; null otherwise. Also returns null if the give name
	 * is blank, the given folder is null or not a directory.
	 */
	public static File find(File folder, final String name) {
		if(name != null && name.length() > 0 && folder != null && folder.isDirectory()) {
			File[] files = folder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return (file.isDirectory() || file.getName().equals(name));
				}
			});
			for(File file : files) {
				if(file.isDirectory()) {
					return find(file, name);
				} else {
					return file;
				}
			}
		}
		return null;
	}

	/**
	 * Find all files within the given folder and all of its subfolders.
	 * Unlike {@link #findFiles(File)}, this method also returns hidden files.
	 * @param folder the folder in which to start the search
	 * @return an array of Files found; never null
	 * @see #findFiles(File)
	 */
	public static File[] findAll(File folder) {
		if(folder != null && folder.isDirectory()) {
			List<File> list = new ArrayList<File>();
			addFiles(list, folder, null);
			return list.toArray(new File[list.size()]);
		} else {
			return new File[0];
		}
	}
	
	public static File[] findAll(File folder, String name) {
		if(name != null && name.length() > 0 && folder != null && folder.isDirectory()) {
			List<File> list = new ArrayList<File>();
			addFiles(list, folder, name);
			return list.toArray(new File[list.size()]);
		} else {
			return new File[0];
		}
	}
	
	public static File[] findClassFiles(File folder, boolean acceptInnerClasses) {
		return findFiles(folder, acceptInnerClasses, ".class");
	}

	/**
	 * Find all files in the given folder and its subfolders.
	 * Note this only returns visible files - directories and hidden files are not included.
	 * @param folder
	 * @return all files in the given folder and its subfolders.
	 */
	public static File[] findFiles(File folder) {
		return findFiles(folder, true, new String[0]);
	}

	public static File[] findFiles(File folder, String...endsWith) {
		return findFiles(folder, true, endsWith);
	}
	
	private static File[] findFiles(File folder, boolean accept$, String...endsWith) {
		if(folder != null && folder.isDirectory()) {
			List<File> list = new ArrayList<File>();
			addFiles(list, folder, accept$, endsWith);
			return list.toArray(new File[list.size()]);
		} else {
			return new File[0];
		}
	}
	
	public static File[] findJavaFiles(File folder) {
		return findFiles(folder, true, ".java");
	}
	
	public static long getLastModified(File...files) {
		if(files.length == 1) {
			return getLastModified(files[0], 0);
		} else {
			long lastModified = 0;
			for(File file : files) {
				lastModified = Math.max(lastModified, getLastModified(file, lastModified));
			}
			return lastModified;
		}
	}

	private static long getLastModified(File file, long lastModified) {
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			for(File f : files) {
				lastModified = getLastModified(f, lastModified);
			}
		} else {
			lastModified = Math.max(file.lastModified(), lastModified);
		}
		return lastModified;
	}
	
	public static long getLastModified(File file, String endsWith) {
		if(endsWith.charAt(0) == '!') {
			return getLastModified(file, endsWith.substring(1), true, 0);
		} else {
			return getLastModified(file, endsWith, false, 0);
		}
	}

	private static long getLastModified(File file, String endsWith, boolean not, long lastModified) {
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			for(File f : files) {
				lastModified = getLastModified(f, lastModified);
			}
		} else {
			boolean match = file.getName().endsWith(endsWith);
			if(match != not) {
				lastModified = Math.max(file.lastModified(), lastModified);
			}
		}
		return lastModified;
	}
	
	public static boolean javaFileExists(String path, String canonicalClassName) {
		return fileExists(path, canonicalClassName.replace('.', File.separatorChar)+".java");
	}

	public static void move(File src, File dst) throws IOException {
		copy(src, dst);
		delete(src);
	}

	public static void move(File src, File dst, int flags) throws IOException {
		copy(src, dst, flags);
		delete(src);
	}
	
	public static StringBuilder readFile(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			int c;
			while((c = reader.read()) != -1) {
				sb.append((char) c);
			}
			return sb;
		} catch(IOException e) {
			logger.warn(e);
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException e) {
					// throw away
				}
			}
		}
		return null;
	}
	
	public static byte[] readFile(File file, byte[] buffer) {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			int off = 0;
			int len = buffer.length;
			while(off < buffer.length && (len = in.read(buffer, off, len)) != -1) {
				off = off + len;
				len = buffer.length - off;
			}
		} catch(IOException e) {
			logger.warn(e);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					// throw away
				}
			}
		}
		return buffer;
	}
	
	public static StringBuilder readFile(File dir, String fileName) {
		return readFile(dir.getAbsolutePath(), fileName);
	}
	
	public static StringBuilder readFile(String path, String fileName) {
		File file = new File(path, fileName);
		return readFile(file);
	}

	public static String readJarEntry(File jarFile, String entryName) {
		if(!jarFile.isFile()) {
			return null;
		}
		JarFile jar = null;
		BufferedInputStream in = null;
		try {
			jar = new JarFile(jarFile);
			ZipEntry entry = jar.getEntry(entryName);
			if(entry == null) {
				return null;
			}
			in = new BufferedInputStream(jar.getInputStream(entry));
			StringBuilder sb = new StringBuilder();
			int c;
			while((c = in.read()) != -1) {
				sb.append((char) c);
			}
			return sb.toString();
		} catch(IOException e) {
			logger.warn(e);
		} finally {
			if(jar != null) {
				try {
					jar.close();
				} catch(IOException e) {
					// throw away
				}
			}
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					// throw away
				}
			}
		}
		return null;
	}
	
	public static StringBuilder readJavaFile(String path, String canonicalClassName) {
		return readFile(path, canonicalClassName.replace('.', File.separatorChar)+".java");
	}
	
	public static File writeFile(File file, String src) {
		return writeFile(file, src, -1);
	}
	
	public static File writeFile(File file, String src, int flags) {
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch(IOException e) {
				logger.warn(e);
				return file;
			}
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file);
			writer.write(src);
			writer.flush();
			if(flags > 0) {
				if((flags & EXECUTABLE) != 0) {
					file.setExecutable(true);
				}
				if((flags & READ_ONLY) != 0) {
					file.setReadOnly();
				}
			}
		} catch(FileNotFoundException e) {
			logger.warn(e);
		} finally {
			if(writer != null) {
				writer.close();
			}
		}
		return file;
	}

	public static File writeFile(File dir, String fileName, String src) {
		File file = new File(dir, fileName);
		writeFile(file, src, -1);
		return file;
	}
	
	public static File writeFile(File dir, String fileName, String src, int flags) {
		File file = new File(dir, fileName);
		writeFile(file, src, flags);
		return file;
	}

	public static File writeFile(String path, String fileName, String src) {
		File file = new File(path, fileName);
		writeFile(file, src, -1);
		return file;
	}
	
	public static File writeFile(String path, String fileName, String src, int flags) {
		File file = new File(path, fileName);
		writeFile(file, src, flags);
		return file;
	}

	public static File writeJavaFile(String path, String canonicalClassName, String src) {
		return writeFile(path, canonicalClassName.replace('.', File.separatorChar)+".java", src);
	}

}
