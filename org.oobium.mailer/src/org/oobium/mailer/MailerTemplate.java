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
package org.oobium.mailer;

import static org.oobium.utils.StringUtils.camelCase;

import java.lang.reflect.Constructor;

import org.oobium.app.routing.IUrlRouting;
import org.oobium.app.routing.Path;
import org.oobium.app.http.Action;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;

public class MailerTemplate implements IUrlRouting {

	protected Logger logger;
	protected AbstractMailer mailer;
	private MailerTemplate child;
	private String layoutName;
	private Class<? extends MailerTemplate> layout;
	private int altCount;

	public String alt() {
		return alt("alt");
	}
	
	public String alt(String s) {
		return (altCount++ % 2 == 0) ? s : "";
	}
	
	public String concat(String base, String opt, boolean condition) {
		if(condition) {
			return base.concat(opt);
		}
		return base;
	}
	
	protected void doRender(StringBuilder sb) throws Exception {
		// subclasses to override if necessary
	}
	
	public MailerTemplate getChild() {
		return child;
	}
	
	public MailerTemplate getLayout() {
		Class<?> layout = this.layout;
		
		if(layout == null) {
			String pname = getClass().getPackage().getName();
			int ix = pname.lastIndexOf('.');
			if(layoutName != null) {
				try {
					String fname = pname.substring(0, ix) + "._layouts." + layoutName;
					layout = Class.forName(fname, true, getClass().getClassLoader());
				} catch(ClassNotFoundException e) {
					// oh well...
				}
			} else {
				// look for a mailer specific layout
				try {
					String fname = pname.substring(0, ix) + "._layouts." + camelCase(pname.substring(ix+1)) + "Layout";
					layout = Class.forName(fname, true, getClass().getClassLoader());
				} catch(ClassNotFoundException e1) {
					// look for the default mailer layout (cache...)
					try {
						String fname = pname.substring(0, ix) + "._layouts._Layout";
						layout = Class.forName(fname, true, getClass().getClassLoader());
					} catch(ClassNotFoundException e2) {
						// oh well...
					}
				}
			}
		}
		
		if(layout != null) {
			if(MailerTemplate.class.isAssignableFrom(layout)) {
				try {
					Constructor<?> c = layout.getConstructor();
					return (MailerTemplate) c.newInstance();
				} catch(Exception e) {
					// oh well...
				}
			}
		}
		return null;
	}
	
	public String getMailerName() {
		String name = mailer.getClass().getSimpleName();
		return name.substring(0, name.length() - 6);
	}
	
	public boolean hasChild() {
		return child != null;
	}
	
	public void render(StringBuilder sb) {
		try {
			doRender(sb);
		} catch(Exception e) {
			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException("Exception thrown during render", e);
			}
		}
	}

	public void setChild(MailerTemplate child) {
		this.child = child;
	}

	public void setLayout(String layoutName) {
		this.layoutName = layoutName;
	}

	public void setLayout(Class<? extends MailerTemplate> layout) {
		this.layout = layout;
	}

	public void setMailer(AbstractMailer mailer) {
		this.mailer = mailer;
		if(this.mailer == null) {
			logger = null;
		} else {
			logger = mailer.getLogger();
		}
	}

	@Override
	public Path urlTo(Class<? extends Model> modelClass) {
		return mailer.urlTo(modelClass);
	}

	@Override
	public Path urlTo(Class<? extends Model> modelClass, Action action) {
		return mailer.urlTo(modelClass, action);
	}

	@Override
	public Path urlTo(Model model) {
		return mailer.urlTo(model);
	}

	@Override
	public Path urlTo(Model model, Action action) {
		return mailer.urlTo(model, action);
	}

	@Override
	public Path urlTo(Model parent, String field) {
		return mailer.urlTo(parent, field);
	}

	@Override
	public Path urlTo(Model parent, String field, Action action) {
		return mailer.urlTo(parent, field, action);
	}

	@Override
	public Path urlTo(String routeName) {
		return mailer.urlTo(routeName);
	}

	@Override
	public Path urlTo(String routeName, Model model) {
		return mailer.urlTo(routeName, model);
	}
	
	@Override
	public Path urlTo(String routeName, Object... params) {
		return mailer.urlTo(routeName, params);
	}

	protected void yield(StringBuilder sb) {
		if(hasChild()) {
			child.render(sb);
		}
	}
	
}
