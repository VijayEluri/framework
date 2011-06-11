package org.oobium.logging;

import org.junit.Test;

public class LoggerTests {

	@Test
	public void testMessageWithValues() {
		Logger logger = LogProvider.getLogger(getClass());
		logger.setConsoleLevel(Logger.DEBUG);
		logger.log(Logger.DEBUG, "{}", "hello");
		logger.log(Logger.DEBUG, "\\{}", "hello");
		logger.log(Logger.DEBUG, "{} {} {}", "hello", "from", "me");
		logger.log(Logger.DEBUG, "{} \\{} {}", "hello", "me");
		logger.log(Logger.DEBUG, "hello {} {}", "from", "me");
		logger.log(Logger.DEBUG, "{} from {}", "hello", "me");
		logger.log(Logger.DEBUG, "{} {} me", "hello", "from");
		
		logger.debug("Set {1,2} differs from {{}}", "3");
		
		logger.debug("File name is C:\\\\{}.", "file.zip");
	}
	
}
