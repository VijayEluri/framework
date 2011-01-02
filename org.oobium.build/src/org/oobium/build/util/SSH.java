package org.oobium.build.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oobium.build.exceptions.OobiumException;
import org.oobium.utils.FileUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * This class is <b>NOT</b> intended to be accessed by multiple threads.
 */
public class SSH {

	private final Session session;
	private final String password;
	private boolean sudo;
	
	private OutputStream out;

	private StringBuilder sysin;
	private PrintStream sysout;
	private PrintStream syserr;
	

	/**
	 * This class is <b>NOT</b> intended to be accessed by multiple threads.
	 */
	public SSH(String host, String username, String password) throws OobiumException {
		this.password = password;
		JSch js = new JSch();
		try {
			session = js.getSession(username, host);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
		} catch(JSchException e) {
			throw new OobiumException(e.getMessage(), e);
		}
	}

	private int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if(b == 0) {
			return b;
		}
		if(b == -1) {
			return b;
		}

		if(b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while(c != '\n');

			if(b == 1) { // error
				System.out.print(sb.toString());
			}
			
			if(b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}
	
	/**
	 * Copy the src file to the dst path on the remote server.
	 * If the src file is actually a directory, then copy it (and all of its contents)
	 * to the dst path on the server. Does nothing if the src file does not exist.
	 * @param src
	 * @param dst
	 * @throws IOException
	 * @throws OobiumException
	 */
	public void copy(File src, String dst) throws IOException, OobiumException {
		if(src.isFile()) {
			sysout.println("copying " + src + " to " + dst);
			doCopy(new FileInputStream(src), src.length(), dst);
		} else if(src.isDirectory()) {
			Set<String> created = new HashSet<String>();
			
			exec("mkdir -p " + dst);
			created.add(dst);

			int len = src.getAbsolutePath().length();
			File[] files = FileUtils.findAll(src);
			for(File file : files) {
				String dpath = (dst + file.getParent().substring(len)).replace('\\', '/');
				if(!created.contains(dpath)) {
					exec("mkdir -p " + dpath);
					created.add(dpath);
				}
				String fpath = (dst + file.getPath().substring(len)).replace('\\', '/');
				sysout.println("copying " + file + " to " + dst);
				doCopy(new FileInputStream(file), file.length(), fpath);
			}
		}
	}
	
	public void copy(InputStream src, long srcSize, String dst) throws IOException, OobiumException {
		sysout.println("copying " + srcSize + "bytes to " + dst);
		doCopy(src, srcSize, dst);
	}
	
	public void copy(String src, String dst) throws IOException, OobiumException {
		copy(new ByteArrayInputStream(src.getBytes()), src.length(), dst);
	}

	public void disconnect() {
		try {
			session.disconnect();
		} catch(Exception e) {
			// discard
		}
	}

	public void doCopy(InputStream src, long srcSize, String dst) throws IOException, OobiumException {
		Channel channel = null;
		OutputStream out = null;
		try {
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand("scp -p -t " + dst);

			out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if(checkAck(in) != 0) {
				throw new IllegalStateException("error"); // TODO
			}

			StringBuilder sb = new StringBuilder(dst.length() + 15);
			sb.append("C0644 ").append(srcSize).append(' ');
			int ix = dst.lastIndexOf('/');
			if(ix != -1) {
				sb.append(dst.substring(ix+1));
			} else {
				sb.append(dst);
			}
			sb.append('\n');
			
			out.write(sb.toString().getBytes());
			out.flush();
			
			if(checkAck(in) != 0) {
				throw new IllegalStateException("error"); // TODO
			}

			byte[] buf = new byte[1024];
			int len;
			while((len = src.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			out.write('\0');
			out.flush();
			
			if(checkAck(in) != 0) {
				throw new IllegalStateException("error"); // TODO
			}
		} catch(JSchException e) {
			throw new OobiumException(e.getMessage(), e);
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch(Exception e) {
					// discard
				}
			}
			if(channel != null) {
				try {
					channel.disconnect();
				} catch(Exception e) {
					// discard
				}
			}
		}
	}
	
	public void write(char c) throws IOException {
		if(out == null) {
			throw new IOException("OutputStream is not open");
		}
		out.write(c);
	}

	public void write(String str) throws IOException {
		if(out == null) {
			throw new IOException("OutputStream is not open");
		}
		out.write(str.getBytes());
		out.flush();
	}
	
	public void writeln() throws IOException {
		if(out == null) {
			throw new IOException("OutputStream is not open");
		}
		out.write('\n');
		out.flush();
	}

	public void writeln(String str) throws IOException {
		if(out == null) {
			throw new IOException("OutputStream is not open");
		}
		out.write(str.getBytes());
		out.write('\n');
		out.flush();
	}

	private List<SSHListener> listeners;
	
	public void addListener(SSHListener listener) {
		if(listeners == null) {
			listeners = new ArrayList<SSHListener>();
			listeners.add(listener);
		} else if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public boolean removeListener(SSHListener listener) {
		if(listeners != null && listeners.remove(listener)) {
			if(listeners.isEmpty()) {
				listeners = null;
			}
			return true;
		}
		return false;
	}
	
	private void notify(String s) {
		if(listeners != null) {
			SSHEvent event = new SSHEvent(s);
			for(SSHListener listener : listeners.toArray(new SSHListener[listeners.size()])) {
				listener.handleEvent(event);
			}
		}
	}
	
	public String exec(String cmd) throws IOException, OobiumException {
		startCommand();
		
		sysout.println(cmd);

		ChannelExec channel = null;
		try {
			boolean isSudo = cmd.startsWith("sudo ");
			if(sudo && !isSudo) {
				sysout.println("executing with sudo");
				cmd = "sudo " + cmd;
				isSudo = true;
			}
			
			channel = (ChannelExec) session.openChannel("exec");
			channel.setPty(true);
			channel.setCommand(cmd);
			
			InputStream in = channel.getInputStream();
			InputStream err = channel.getErrStream();
			InputStream inExt = channel.getExtInputStream();
			out = channel.getOutputStream();
			
			channel.connect();
			
			byte[] tmp = new byte[1024];
			while(true) {
				while(err.available() > 0) {
					int i = err.read(tmp, 0, 1024);
					if(i < 0) break;
					printErr(tmp, 0, i);
				}
				while(inExt.available() > 0) {
					int i = inExt.read(tmp, 0, 1024);
					if(i < 0) break;
					printOut(tmp, 0, i);
				}
				while(in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if(i < 0) {
						break;
					}
					String s = printOut(tmp, 0, i);
					if(isSudo && s.startsWith("[sudo] password for")) {
						writeln(password);
					}
					notify(s);
				}
				if(channel.isClosed()) {
					sysout.println("exit-status: " + channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch(Exception ee) {
					break;
				}
			}
		} catch(JSchException e) {
			throw new OobiumException(e.getMessage(), e);
		} finally {
			if(channel != null) {
				try {
					channel.disconnect();
				} catch(Exception e) {
					// discard
				}
				channel = null;
				out = null;
			}
		}
		return sysin.toString();
	}
	
	public String exec(String cmd, String workingDirectory) throws IOException, OobiumException {
		if(cmd.startsWith("sudo ")) {
			return exec("sudo sh -c \"cd " + workingDirectory + " && " + cmd.substring(5) + "\"");
		} else {
			return exec("sh -c \"cd " + workingDirectory + " && " + cmd + "\"");
		}
	}
	
	public void move(File src, String dst) throws IOException, OobiumException {
		copy(new FileInputStream(src), src.length(), dst);
		src.delete();
	}
	private String printErr(byte[] bytes, int offset, int length) {
		String s = new String(bytes, offset, length);
		sysin.append(s);
		syserr.print(s);
		return s;
	}
	
	private String printOut(byte[] bytes, int offset, int length) {
		String s = new String(bytes, offset, length);
		sysin.append(s);
		sysout.print(s);
		return s;
	}

	public void setErr(PrintStream syserr) {
		this.syserr = syserr;
	}
	
	public void setOut(PrintStream sysout) {
		this.sysout = sysout;
	}
	
	public void setSudo(boolean useSudo) {
		this.sudo = useSudo;
	}
	
	private void startCommand() {
		if(sysout == null) {
			sysout = System.out;
		}
		if(syserr == null) {
			syserr = System.err;
		}
		sysin = new StringBuilder(1024);
	}
	
	public String sudo(String cmd) throws IOException, OobiumException {
		return exec("sudo " + cmd);
	}
	
	public String sudo(String cmd, String workingDirectory) throws IOException, OobiumException {
		return exec("sudo " + cmd, workingDirectory);
	}
	
}
