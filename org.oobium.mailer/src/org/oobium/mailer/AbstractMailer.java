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

import static org.oobium.app.http.MimeType.*;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.underscored;
import static org.oobium.utils.json.JsonUtils.toStringMap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.oobium.app.AppService;
import org.oobium.app.routing.AppRouter;
import org.oobium.app.routing.IUrlRouting;
import org.oobium.app.routing.Path;
import org.oobium.app.http.Action;
import org.oobium.app.http.MimeType;
import org.oobium.logging.Logger;
import org.oobium.logging.LogProvider;
import org.oobium.persist.Model;
import org.oobium.utils.Config;

public abstract class AbstractMailer implements IUrlRouting {

	/**
	 * The BCC addresses of the email, either as a string (for a single address) or an array of strings (for multiple addresses)
	 */
	protected List<InternetAddress> bcc;
	
	/**
	 * The body of the email. This is either a hash (in which case it specifies the variables to pass to the template when it is rendered), 
	 * or a string, in which case it specifies the actual body of the message
	 */
	protected String body;
	
	/**
	 * The CC addresses for the email, either as a string (for a single address) or an array of strings (for multiple addresses)
	 */
	protected List<InternetAddress> cc;

//	/**
//	 * The charset to use for the email. This defaults to the default_charset specified for ActionMailer::Base.
//	 */
//	protected String charset;
	
	/**
	 * The content type for the email. This defaults to "text/plain" but the filename may specify it
	 */
	protected MimeType mimeType;

	/**
	 * The from address of the email
	 */
	protected InternetAddress from;

	/**
	 * The address (if different than the "from" address) to direct replies to this email
	 */
	protected InternetAddress replyTo;
	
	/**
	 * Additional headers to be added to the email
	 */
	protected Map<String, String> headers;
	
	/**
	 * The To addresses for the email
	 */
	protected List<InternetAddress> to;
	
	/**
	 * The timestamp on which the message was sent. If not set, the header will be set by the delivery agent
	 */
	protected Date sentAt;
	
	/**
	 * The subject of the email
	 */
	protected String subject;

	protected final Logger logger;
	private final AppService app;
	private Properties properties;
	private final AppRouter router;
	private boolean isRendered;

	public AbstractMailer() {
		app = AppService.get();
		if(app == null) {
			logger = LogProvider.getLogger();
			router = null;
		} else {
			logger = app.getLogger();
			router = app.getRouter();
		}
		
		init();
	}

	/**
	 * 
	 * @param address
	 * @return
	 * @throws InvalidParameterException if address is not valid
	 */
	public AbstractMailer addBCC(@Name("address") String address) {
		if(bcc == null) {
			bcc = new ArrayList<InternetAddress>();
		}
		bcc.add(parseAddress(address));
		return this;
	}
	
	/**
	 * 
	 * @param address
	 * @return
	 * @throws InvalidParameterException if address is not valid
	 */
	public AbstractMailer addCC(@Name("address") String address) {
		if(cc == null) {
			cc = new ArrayList<InternetAddress>();
		}
		cc.add(parseAddress(address));
		return this;
	}

	/**
	 * 
	 * @param type
	 * @param address
	 * @return
	 * @throws InvalidParameterException if address is not valid
	 */
	public AbstractMailer addRecipient(@Name("type") RecipientType type, @Name("address") String address) {
		switch(type) {
		case TO:	addRecipient(address); break;
		case CC:	addCC(address); break;
		case BCC:	addBCC(address); break;
		default: 	throw new IllegalStateException("unknown RecipientType " + type);
		}
		return this;
	}
	
	/**
	 * 
	 * @param address
	 * @return
	 * @throws InvalidParameterException if address is not valid
	 */
	public AbstractMailer addRecipient(@Name("address") String address) {
		if(to == null) {
			to = new ArrayList<InternetAddress>();
		}
		to.add(parseAddress(address));
		return this;
	}

	public AbstractMailer attach(@Name("resource") String resource) {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	protected boolean doValidate() {
		// for subclasses to override
		return true;
	}

	public List<InternetAddress> getBCC() {
		return (bcc != null) ? new ArrayList<InternetAddress>(bcc) : new ArrayList<InternetAddress>(0);
	}
	
	public String getBody() {
		return body;
	}
	
	public List<InternetAddress> getCC() {
		return (cc != null) ? new ArrayList<InternetAddress>(cc) : new ArrayList<InternetAddress>(0);
	}

	public String getFrom() {
		return (from != null) ? from.getAddress() : null;
	}
	
	public Logger getLogger() {
		return logger;
	}

	private Properties getProperties(String name) {
		if(properties != null) {
			return properties;
		} else if(app != null) {
			Config config = Config.loadConfiguration(app.getClass());
			Map<?,?> map = (Map<?,?>) config.get(name);
			if(map != null) {
				Properties properties = new Properties();
				for(Entry<?,?> entry : map.entrySet()) {
					properties.put(entry.getKey(), entry.getValue());
				}
				return properties;
			}
			String s = underscored(name).replace('_', '.');
			throw new IllegalStateException("mail." + s + " properties are not configured for: " + app);
		}
		throw new IllegalStateException("properties are not configured for: " + app);
	}

	public String getSubject() {
		return subject;
	}
	
	public List<InternetAddress> getTo() {
		return (to != null) ? new ArrayList<InternetAddress>(to) : new ArrayList<InternetAddress>(0);
	}
	
	private void init() {
		// sets the initial values to those of the annotation.  setup method will override if necessary
		Mailer ann = getClass().getAnnotation(Mailer.class);
		if(ann == null) {
			this.mimeType = PLAIN;
		} else {
			if(!blank(ann.to())) {
				try {
					setTo(ann.to());
				} catch(InvalidParameterException e) {
					logger.warn(e.getMessage());
				}
			}
			if(!blank(ann.cc())) {
				try {
					setCC(ann.cc());
				} catch(InvalidParameterException e) {
					logger.warn(e.getMessage());
				}
			}
			if(!blank(ann.bcc())) {
				try {
					setBCC(ann.bcc());
				} catch(InvalidParameterException e) {
					logger.warn(e.getMessage());
				}
			}
			if(!blank(ann.from())) {
				try {
					setFrom(ann.from());
				} catch(InvalidParameterException e) {
					logger.warn(e.getMessage());
				}
			}
			if(!blank(ann.replyTo())) {
				try {
					setReplyTo(ann.replyTo());
				} catch(InvalidParameterException e) {
					logger.warn(e.getMessage());
				}
			}
			if(!blank(ann.headers())) {
				setHeaders(toStringMap(ann.headers()));
			}
			if(!blank(ann.subject())) {
				setSubject(ann.subject());
			}
			if(ann.template() != Object.class) {
				try {
					Class<?> clazz = ann.template();
					if(MailerTemplate.class.isAssignableFrom(clazz)) {
						MailerTemplate template = clazz.asSubclass(MailerTemplate.class).newInstance();
						render(template);
					}
				} catch(InstantiationException e) {
					logger.warn(e.getMessage());
				} catch(IllegalAccessException e) {
					logger.warn(e.getMessage());
				}
			} else {
				if(!blank(ann.body())) {
					render(ann.body());
				}
				if(!blank(ann.contentType())) {
					this.mimeType = MimeType.valueOf(ann.contentType());
				} else {
					this.mimeType = PLAIN;
				}
			}
		}
	}
	
	private InternetAddress parseAddress(String input) {
		Pattern pattern = Pattern.compile("<(.+)>");
		Matcher matcher = pattern.matcher(input);
		if(matcher.find()) {
			String address = matcher.group(1).trim();
			String personal = new StringBuilder(input).delete(matcher.start(), matcher.end()).toString().trim();
			try {
				return new InternetAddress(address, personal);
			} catch(UnsupportedEncodingException e) {
				throw new InvalidParameterException("unsupported encoding in address: " + input + " (" + address + ", " + personal + ")");
			}
		} else {
			try {
				return new InternetAddress(input.trim());
			} catch(AddressException e) {
				throw new InvalidParameterException("invalid address: " + input);
			}
		}
	}

	public AbstractMailer render(@Name("template") MailerTemplate template) {
		if(template == null) {
			throw new IllegalArgumentException("template cannot be null");
		}
		if(logger.isLoggingDebug()) {
			logger.debug("start render of template " + template.getClass().getCanonicalName());
		}
		rendering();
		this.mimeType = HTML;
		template.setMailer(this);
		try {
			StringBuilder sb = new StringBuilder();
			MailerTemplate layout = template.getLayout();
			if(layout != null) {
				layout.setMailer(this);
				layout.setChild(template);
				layout.render(sb);
			} else {
				template.render(sb);
			}
			this.body = sb.toString();
		} finally {
			template.setMailer(null);
		}
		logger.debug("end render of template");
		return this;
	}
	
	public AbstractMailer render(@Name("body") String body, @Name("values") Collection<?> values) {
		render(body, ((values != null) ? values.toArray() : new Object[0]));
		return this;
	}
	
	public AbstractMailer render(@Name("body") String body, @Name("values") Map<String, Object> values) {
		rendering();
		if(values != null && values.size() > 0) {
			StringBuilder sb = new StringBuilder(body);
			Pattern pattern = Pattern.compile("#\\{(\\w+)}");
			Matcher matcher = pattern.matcher(sb);
			for(int i = 0, start = 0; matcher.find(start); i++) {
				String key = matcher.group(1);
				Object val = values.containsKey(key) ? values.get(key) : ("#{" + key + ": *** UNKNOWN ***}");
				sb.replace(matcher.start(), matcher.end(), String.valueOf(val));
				start = matcher.end();
				matcher = pattern.matcher(sb);
			}
			this.body = sb.toString();
		} else {
			this.body = body;
		}
		return this;
	}
	
	public AbstractMailer render(@Name("body") String body, @Name("values") Object...values) {
		rendering();
		if(values.length > 0) {
			StringBuilder sb = new StringBuilder(body);
			Pattern pattern = Pattern.compile("#\\{\\?}");
			Matcher matcher = pattern.matcher(sb);
			for(int i = 0, start = 0; matcher.find(start); i++) {
				sb.replace(matcher.start(), matcher.end(), String.valueOf(values[i]));
				start = matcher.end();
				matcher = pattern.matcher(sb);
			}
			this.body = sb.toString();
		} else {
			this.body = body;
		}
		return this;
	}

	private void rendering() {
		if(isRendered) {
			throw new UnsupportedOperationException("cannot render more than once");
		}
		isRendered = true;
	}
	
	public void retrieve() {
		sentAt = new Date();
		
	    Properties props = getProperties(Config.MAIL_RETRIEVE);

	    try {
		    Session session = Session.getDefaultInstance(props, null);
		    Store store = session.getStore();
		    store.connect();
	
		    Folder inbox = store.getFolder("INBOX");
		    if (inbox == null) {
		      System.out.println("No INBOX");
		      System.exit(1);
		    }
		    inbox.open(Folder.READ_ONLY);
	
		    Message[] messages = inbox.getMessages();
		    for (int i = 0; i < messages.length; i++) {
		      System.out.println("Message " + (i + 1));
		      messages[i].writeTo(System.out);
		    }
		    inbox.close(false);
		    store.close();
	    } catch(IOException e) {
			e.printStackTrace();
	    } catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public AbstractMailer send() {
		if(app == null) {
//			throw new IllegalStateException("app is null, cannot proceed");
		}
		
		if(!validate()) {
			// TODO throw exception... should validate set an errors list like Model does?
			return this;
		}

		try {
			Properties properties = getProperties(Config.MAIL_SEND);

			String protocol;
			String host;
			
			String service = (String) properties.remove("service");
			if("gmail".equalsIgnoreCase(service)) {
				protocol = (String) properties.getProperty("mail.transport.protocol", "smtps");
				host = (String) properties.getProperty("host", "smtp.gmail.com");
		        properties.put("mail.smtps.auth", "true");
			} else {
				protocol = (String) properties.getProperty("mail.transport.protocol");
				host = (String) properties.getProperty("host");
			}

			if(blank(protocol)) {
				throw new IllegalArgumentException("protocol cannot be null");
			}
			if(blank(host)) {
				throw new IllegalArgumentException("host cannot be null");
			}
			
			String user = (String) properties.remove("username");
			String pass = (String) properties.remove("password");

			Session mailSession = Session.getDefaultInstance(properties, null);
			Transport transport = mailSession.getTransport(protocol);
			
			MimeMessage message =  new MimeMessage(mailSession);

			message.setFrom(from);
			message.setReplyTo(new Address[] { (replyTo == null) ? from : replyTo });

			if(!blank(to)) {
				for(Address recipient : to) {
					message.addRecipient(Message.RecipientType.TO, recipient);
				}
			}

			if(!blank(cc)) {
				for(Address recipient : cc) {
					message.addRecipient(Message.RecipientType.CC, recipient);
				}
			}

			if(!blank(bcc)) {
				for(Address recipient : bcc) {
					message.addRecipient(Message.RecipientType.BCC, recipient);
				}
			}

			message.setSubject(subject);
			message.setContent(body, mimeType.acceptsType);

			try {
				transport.connect(host, user, pass);
				transport.sendMessage(message, message.getAllRecipients());
			} finally {
				transport.close();
			}
		} catch(MessagingException e) {
			logger.warn("failed to send message", e);
		}
		return this;
	}
	
	/**
	 * 
	 * @param addresses
	 * @return
	 * @throws InvalidParameterException if an address is not valid
	 */
	public AbstractMailer setBCC(@Name("addresses") String...addresses) {
		InternetAddress[] addrs = new InternetAddress[addresses.length];
		for(int i = 0; i < addresses.length; i++) {
			addrs[i] = parseAddress(addresses[i]);
		}
		bcc = new ArrayList<InternetAddress>(Arrays.asList(addrs));
		return this;
	}

	/**
	 * 
	 * @param addresses
	 * @return
	 * @throws InvalidParameterException if an address is not valid
	 */
	public AbstractMailer setCC(@Name("addresses") String...addresses) {
		InternetAddress[] addrs = new InternetAddress[addresses.length];
		for(int i = 0; i < addresses.length; i++) {
			addrs[i] = parseAddress(addresses[i]);
		}
		cc = new ArrayList<InternetAddress>(Arrays.asList(addrs));
		return this;
	}

	/**
	 * 
	 * @param address
	 * @return
	 * @throws InvalidParameterException if address is not valid
	 */
	public AbstractMailer setFrom(@Name("address") String address) {
		from = parseAddress(address);
		return this;
	}
	
	public AbstractMailer setHeaders(@Name("headers") Map<String, String> headers) {
		this.headers = headers;
		return this;
	}
	
	public void setProperties(Map<String, ? extends Object> props) {
		properties = new Properties();
		properties.putAll(props);
	}

	/**
	 * 
	 * @param type
	 * @param addresses
	 * @return
	 * @throws InvalidParameterException if an address is not valid
	 */
	public AbstractMailer setRecipients(@Name("type") RecipientType type, @Name("addresses") String...addresses) {
		switch(type) {
		case TO:	setTo(addresses); break;
		case CC:	setCC(addresses); break;
		case BCC:	setBCC(addresses); break;
		default: 	throw new IllegalStateException("unknown RecipientType " + type);
		}
		return this;
	}

	/**
	 * 
	 * @param address
	 * @return
	 * @throws InvalidParameterException if address is not valid
	 */
	public AbstractMailer setReplyTo(@Name("address") String address) {
		replyTo = parseAddress(address);
		return this;
	}
	
	public AbstractMailer setSubject(@Name("subject") String subject) {
		this.subject = subject;
		return this;
	}

	/**
	 * 
	 * @param addresses
	 * @return
	 * @throws InvalidParameterException if an address is not valid
	 */
	public AbstractMailer setTo(@Name("addresses") String...addresses) {
		InternetAddress[] addrs = new InternetAddress[addresses.length];
		for(int i = 0; i < addresses.length; i++) {
			addrs[i] = parseAddress(addresses[i]);
		}
		to = new ArrayList<InternetAddress>(Arrays.asList(addrs));
		return this;
	}

	@Override
	public Path urlTo(Class<? extends Model> modelClass) {
		return router.urlTo(modelClass);
	}

	@Override
	public Path urlTo(Class<? extends Model> modelClass, Action action) {
		return router.urlTo(modelClass, action);
	}

	@Override
	public Path urlTo(Model model) {
		return router.urlTo(model);
	}

	@Override
	public Path urlTo(Model model, Action action) {
		return router.urlTo(model, action);
	}

	@Override
	public Path urlTo(Model parent, String field) {
		return router.urlTo(parent, field);
	}

	@Override
	public Path urlTo(Model parent, String field, Action action) {
		return router.urlTo(parent, field, action);
	}

	@Override
	public Path urlTo(String routeName) {
		return router.urlTo(routeName);
	}

	@Override
	public Path urlTo(String routeName, Model model) {
		return router.urlTo(routeName, model);
	}
	
	@Override
	public Path urlTo(String routeName, Object... params) {
		return router.urlTo(routeName, params);
	}

	public final boolean validate() {
		if(blank(from)) {
			return false;
		}
		if(blank(to) && blank(cc) && blank(bcc)) {
			return false;
		}
		return doValidate();
	}
	
}
