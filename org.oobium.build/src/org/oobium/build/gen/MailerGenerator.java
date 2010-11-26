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
package org.oobium.build.gen;

import static org.oobium.utils.StringUtils.underscored;
import static org.oobium.build.util.SourceFile.find;
import static org.oobium.utils.FileUtils.readFile;
import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.StringUtils.packageName;
import static org.oobium.utils.StringUtils.simpleName;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.InternetAddress;

import org.oobium.build.util.SourceFile;
import org.oobium.build.workspace.Module;
import org.oobium.mailer.AbstractMailer;
import org.oobium.mailer.Name;
import org.oobium.utils.StringUtils;


public class MailerGenerator {

	private static final char[] CLASS = {'c','l','a','s','s'};
	private static final char[] IMPORT = {'i','m','p','o','r','t'};

	private static final Pattern methodPattern = Pattern.compile("(?<!private)\\svoid\\s+setup([^\\{]+)\\{");

	/**
	 * Add the given methods to the given mailer, if it exists.
	 * @param module the module containing the mailer
	 * @param name the name of the mailer
	 * @param methods the methods to add the mailer (will be added to the end of the class)
	 * @return the mailer file if the methods could be added; null otherwise
	 */
	public static File addMethod(Module module, String name, String...methods) {
		File mailer = module.getMailer(name);
		if(mailer.isFile()) {
			StringBuilder sb = readFile(mailer);
			for(int i = sb.length()-1; i >= 0; i--) {
				if(sb.charAt(i) == '}') { // assume end of class (it ain't pretty...)
					while(i >= 0) {
						if(sb.charAt(i) == '\n') break;
					}
					for(String methodName : methods) {
						if(!methodName.startsWith("setup")) {
							methodName = "setup" + StringUtils.camelCase(methodName);
						}
						String method = "\n\tprotected void " + methodName + "() {\n\t\t// TODO auto-generated method\n\t}";
						sb.insert(i, method);
						i += method.length();
					}
					return writeFile(mailer, sb.toString());
				}
			}
		}
		return null;
	}
	
	public static File createLayout(Module module, String mailerName) {
		MailerGenerator gen = new MailerGenerator(module, mailerName);
		return gen.createLayout();
	}

	public static File createLayout(Module module) {
		StringBuilder sb = new StringBuilder();
		sb.append("yield\n");

		File folder = new File(module.mailers, "_layouts");
		return writeFile(folder, "_Layout.emt", sb.toString());
	}

	public static File createTemplate(Module module, String mailerName, String methodName) {
		MailerGenerator gen = new MailerGenerator(module, mailerName);
		return gen.createTemplate(methodName);
	}

	public static List<File> createTemplates(Module module, String mailerName) {
		MailerGenerator gen = new MailerGenerator(module, mailerName);
		return gen.createTemplates();
	}

	public static List<File> generate(Module module, File mailer) {
		int len = module.mailers.getAbsolutePath().length() + 1;
		String name = mailer.getAbsolutePath();
		name = name.substring(len, name.length() - 5);
		return generate(module, name);
	}

	static List<File> generate(Module module, List<File> mailers) {
		List<File> generated = new ArrayList<File>();
		if(mailers != null && !mailers.isEmpty()) {
			int len = module.mailers.getAbsolutePath().length() + 1;
			for(File mailer : mailers) {
				String name = mailer.getAbsolutePath();
				name = name.substring(len, name.length() - 5);
				generated.addAll(generate(module, name));
			}
		}
		return generated;
	}

	/**
	 * return File object pointing to the modified java file or files - the generated
	 * Java super class and the mailer file itself if it was modified in the process.
	 * @param module
	 * @param simpleName
	 * @return
	 */
	private static List<File> generate(Module module, String simpleName) {
		String name = StringUtils.camelCase(simpleName);
		if(!name.endsWith("Mailer")) {
			name = name + "Mailer";
		}

		MailerGenerator gen = new MailerGenerator(module, simpleName);
		return gen.generate();
	}

	private static String[] findMailerMethodSignatures(String src) {
		Matcher matcher = methodPattern.matcher(src);
		
		List<String> sigs = new ArrayList<String>();
		while(matcher.find()) {
			sigs.add(matcher.group(1).trim());
		}
		
		return sigs.toArray(new String[sigs.size()]);
	}

	private static boolean modifySrcFile(String genName, String name, File file) {
		StringBuilder sb = readFile(file);
		Pattern pattern = Pattern.compile("class\\s+" + name + "\\s+extends\\s+Abstract" + name + "\\s+(implements|\\{)");
		Matcher matcher = pattern.matcher(sb);
		if(!matcher.find()) {
			String s = "class " + name;
			int start = sb.indexOf(s) + s.length();
			int end = sb.indexOf("implements", start);
			if(end == -1) {
				end = sb.indexOf("{", start);
			}
			sb.replace(start, end, " extends Abstract" + name + " ");
			writeFile(file, sb.toString());
			return true;
		}
		return false;
	}
	private final String canonicalName;
	private final String simpleName;
	private final String src;
	private final File srcFile;
	private final File genFile;
	private final File mailersFolder;
	private final List<String> imports;
	
	private SourceFile sf;
	
	private MailerGenerator(Module module, String mailerType) {
		this.simpleName = mailerType;
		
		this.srcFile = module.getMailer(mailerType);
		this.genFile = module.getGenMailer(mailerType);

		this.canonicalName = module.packageName(srcFile.getParentFile()) + "." + mailerType;
		this.src = readFile(srcFile).toString();

		this.mailersFolder = module.mailers;
		
		this.imports = new ArrayList<String>();
		StringBuilder sb = new StringBuilder(src);
		int stop = find(sb, 0, sb.length(), CLASS);
		int start = find(sb, 0, stop, IMPORT);
		while(start != -1) {
			int end = find(sb, start, stop, ';');
			imports.add(sb.substring(start+IMPORT.length, end));
			start = find(sb, end, stop, IMPORT);
		}
	}

	private void addCreateMethod(String method) {
		String[] sa = method.split("\\s*\\(\\s*");
		String name = "create" + sa[0];
		
		StringBuilder sb = new StringBuilder();
		sb.append("\tpublic static ").append(simpleName).append(' ').append(name).append('(').append(sa[1]).append(" {\n");
		sb.append("\t\t").append(simpleName).append(" mailer = new ").append(simpleName).append("();\n");
		sb.append("\t\tmailer.setup").append(sa[0]).append('(');
		String[] args = getArgs(method);
		if(args.length > 0) {
			for(int i = 0; i < args.length; i++) {
				if(i != 0) sb.append(", ");
				sb.append(args[i].split("\\s+")[1]);
			}
		}
		sb.append(");\n");
		sb.append("\t\treturn mailer;\n");
		sb.append("\t}");
		
		sf.staticMethods.put(name, sb.toString());
	}
	
	private void addImports(String methodSig) {
		String[] args = getArgs(methodSig);
		if(args.length > 0) {
			for(String arg : args) {
				String[] sa = arg.split("\\s+");
				for(String imp : imports) {
					if(imp.endsWith(sa[0])) {
						sf.imports.add(imp);
					}
				}
			}
		}
	}
	
	private void addSendMethod(String method) {
		String[] sa = method.split("\\s*\\(\\s*");
		String name = "send" + sa[0];
		
		StringBuilder sb = new StringBuilder();
		sb.append("\tpublic static ").append(simpleName).append(' ').append(name).append('(').append(sa[1]).append(" {\n");
		sb.append("\t\treturn create").append(sa[0]).append('(');
		String[] args = getArgs(method);
		if(args.length > 0) {
			for(int i = 0; i < args.length; i++) {
				if(i != 0) sb.append(", ");
				sb.append(args[i].split("\\s+")[1]);
			}
		}
		sb.append(").send();\n");
		sb.append("\t}");
		
		sf.staticMethods.put(name, sb.toString());
	}
	
	private List<File> generate() {
		List<File> generated = new ArrayList<File>();
		
		sf = new SourceFile();

		sf.isAbstract = true;
		sf.simpleName = "Abstract" + simpleName(canonicalName);
		sf.packageName = packageName(canonicalName);
		sf.superName = AbstractMailer.class.getSimpleName();
		
		sf.imports.add(AbstractMailer.class.getCanonicalName());

		addOverrideMethods(sf, simpleName);
		
		String[] methods = findMailerMethodSignatures(src);
		for(String method : methods) {
			addCreateMethod(method);
			addSendMethod(method);
			addImports(method);
		}
		writeFile(genFile, sf.toSource());
		generated.add(genFile);

		if(modifySrcFile(canonicalName, simpleName(canonicalName), srcFile)) {
			generated.add(srcFile);
		}
		
		return generated;
	}
	
	private void addOverrideMethods(SourceFile sf, String simpleName) {
		for(Method method : AbstractMailer.class.getMethods()) {
			if(method.getReturnType() == AbstractMailer.class) {
				StringBuilder name = new StringBuilder();
				name.append(method.getName()).append(':');
				StringBuilder sb = new StringBuilder();
				sb.append("\tpublic ").append(simpleName).append(' ').append(method.getName()).append('(');
				Type[] types = method.getGenericParameterTypes();
				Annotation[][] anns = method.getParameterAnnotations();
				for(int i = 0; i < types.length; i++) {
					String tname;
					String imp;
					
					if(types[i] instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType) types[i];
						Class<?> c = (Class<?>) pt.getRawType();
						Type[] ta = pt.getActualTypeArguments();
						StringBuilder s = new StringBuilder();
						s.append(c.getSimpleName()).append('<');
						for(int j = 0; j < ta.length; j++) {
							if(j != 0) s.append(',').append(' ');
							if(ta[j] instanceof Class<?>) {
								s.append(((Class<?>) ta[j]).getSimpleName());
								String timp = ((Class<?>) ta[j]).getCanonicalName();
								if(!timp.startsWith("java.lang")) sf.imports.add(timp);
							} else {
								// TODO needs to be recursive to get more than just 1 level...
								s.append(ta[j]);
							}
						}
						s.append('>');
						tname = s.toString();
						imp = c.getCanonicalName();
					} else {
						Class<?> c = (Class<?>) types[i];
						tname = c.getSimpleName();
						imp = c.getCanonicalName();
					}

					if(!imp.startsWith("java.lang")) {
						if(imp.endsWith("[]")) imp = imp.substring(0, imp.length()-2);
						sf.imports.add(imp);
					}

					if(i != 0) name.append(',');
					name.append(tname);

					if(i != 0) sb.append(',').append(' ');
					sb.append(tname);

					if(i == types.length-1 && method.isVarArgs()) {
						sb.delete(sb.length()-2, sb.length());
						sb.append("...");
					} else {
						sb.append(' ');
					}
					sb.append((anns[i].length > 0 && anns[i][0] != null) ? ((Name) anns[i][0]).value() : ("arg" + i));
				}
				sb.append(')').append(' ');
				Class<?>[] etypes = method.getExceptionTypes();
				if(etypes.length > 0) {
					sb.append("throws ");
					for(int i = 0; i < etypes.length; i++) {
						if(i != 0) sb.append(',').append(' ');
						sb.append(etypes[i].getSimpleName());
						sf.imports.add(etypes[i].getCanonicalName());
					}
				}
				sb.append(" {\n\t\treturn (").append(simpleName).append(") super.").append(method.getName()).append('(');
				for(int i = 0; i < anns.length; i++) {
					if(i != 0) sb.append(',').append(' ');
					sb.append((anns[i].length > 0 && anns[i][0] != null) ? ((Name) anns[i][0]).value() : ("arg" + i));
				}
				sb.append(");\n\t}");
				sf.methods.put(name.toString(), sb.toString());
			}
		}
	}
	
	private List<File> createTemplates() {
		List<File> generated = new ArrayList<File>();
		
		String[] methods = findMailerMethodSignatures(src);
		for(String method : methods) {
			generated.add(createTemplate(method));
		}

		if(modifySrcFile(canonicalName, simpleName(canonicalName), srcFile)) {
			generated.add(srcFile);
		}
		
		return generated;
	}
	
	private File createTemplate(String method) {
		String[] sa = method.split("\\s*\\(\\s*");
		String methodName = sa[0];

		String viewName = (methodName.startsWith("setup") ? methodName.substring(5) : methodName);

		StringBuilder sb = new StringBuilder();
		sb.append("import java.util.Date\n");
		sb.append("import ").append(InternetAddress.class.getCanonicalName()).append('\n');
		sb.append('\n');
		sb.append("div Date: { new Date() }\n");
		sb.append("div From: { mailer.getFrom() }\n");
		sb.append("div To: { asString(mailer.getTo()) }\n");
		sb.append("div Subject: { mailer.getSubject() }\n");
		sb.append('\n');
		sb.append("div This is an auto-generated email template\n");

		File folder = new File(mailersFolder, underscored(simpleName.substring(0, simpleName.length() - 6)));
		return writeFile(folder, viewName + ".emt", sb.toString());
	}
	
	private File createLayout() {
		String layoutName = simpleName.substring(0, simpleName.length() - 6) + "Layout";
		
		StringBuilder sb = new StringBuilder();
		sb.append("yield\n");

		File folder = new File(mailersFolder, "_layouts");
		return writeFile(folder, layoutName + ".emt", sb.toString());
	}
	
	private String[] getArgs(String methodSig) {
		int i1 = methodSig.indexOf('(');
		int i2 = methodSig.indexOf(')');
		String args = methodSig.substring(i1+1, i2).trim();
		if(args.isEmpty()) {
			return new String[0];
		} else {
			return args.split("\\s*,\\s*");
		}
	}
	
}
