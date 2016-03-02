package bt;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class BtLogger {

	private static final Level DEFAULT_LEVEL = Level.FINE;

	/**
	 * Gets a logger for the class specified by <code>c</code>.
	 * The logger will reside in the machine-dependent temporary folder.
	 * 
	 * @param c the class.
	 * @return the logger.
	 */
	public static Logger getLogger(Class<? extends Object> c) {
		return getLogger(c, "%t/" + c.getSimpleName() + "%g.log");
	}

	/**
	 * Gets a logger for the class specified by <code>c</code>.
	 * 
	 * @param c the class.
	 * @param pattern the pattern.
	 * @return the logger.
	 */
	public static Logger getLogger(Class<? extends Object> c, String pattern) {
		
		// Creates a log handler
		Handler handler;
		try {
			handler = new FileHandler(pattern);
		} catch (IOException e) {
			return null;
		}
		handler.setFormatter(new VerySimpleFormatter());
		
		// Creates logger
		Logger logger = Logger.getLogger(c.getName());
		logger.setUseParentHandlers(false);
		logger.addHandler(handler);
		logger.setLevel(DEFAULT_LEVEL);
		
		return logger;
	}
	
	/**
	 * A very simple log formatter.
	 * 
	 * @author suwimont
	 *
	 */
	private static class VerySimpleFormatter extends Formatter {
		
		@Override
		public String format(LogRecord record) {
			return String.format(record.getMessage() + "%n", record.getParameters());
		}
	}
}
