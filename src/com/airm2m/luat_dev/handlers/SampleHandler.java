package com.airm2m.luat_dev.handlers;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {
	MessageConsole console = null;  
	MessageConsoleStream consoleStream = null;  
	IConsoleManager consoleManager = null;  
	final String CONSOLE_NAME = "Board Trace"; 
	String downlod_port;
	String trace_port;
	public String GetPath()
	{
		Properties prop = new Properties(); 
		InputStream in = null;
		try {
			 in= new BufferedInputStream (new FileInputStream("e:\\cfg\\luat.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			prop.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 downlod_port=prop.getProperty("download");
		 trace_port=prop.getProperty("trace_uart");
		 return prop.getProperty("work_path");
		
	}
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		//DirectoryDialog dialog = new DirectoryDialog(window.getShell());
		//String  selectPath = dialog.open() ;
		//ComBin combin=new ComBin("RDA");
		//DownLoad downLoad = new DownLoad();
		//downLoad.start();
		OriginalDownload Od=new OriginalDownload();
		//log logs=new log();
		//logs.start();

		return null;
	}
}
