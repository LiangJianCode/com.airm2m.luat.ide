package com.airm2m.luat_dev.handlers;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.console.IHyperlink;


public class ShowConsole {
	MessageConsole console = null;  
	MessageConsoleStream consoleStream = null;  
	IConsoleManager consoleManager = null;  
	SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss|SSS");
	final String CONSOLE_NAME = "Luat Board Trace"; 
	FileOutputStream out = null;
	String log_name=null;
	private void initConsole() {  
        consoleManager = ConsolePlugin.getDefault().getConsoleManager();    
        IConsole[] existing = consoleManager.getConsoles();
        if(existing.length>0)
        {
        	consoleManager.addConsoles(existing);
        	console=(MessageConsole) existing[0];
        }
        else
        {
        	console = new MessageConsole(CONSOLE_NAME, null);  
        	consoleManager.addConsoles(new IConsole[] { console });
        }

        consoleStream = console.newMessageStream();  
	}
	/** 
	 * 开启console， 打印相关消息 
	 * @param message 消息内容 
	 */  
	public void printMessage(String message) {  
	    if (message != null) {  
	        if (console == null) {  
	            initConsole();  
	        }  
	        // 显示Console视图  
	        consoleManager.showConsoleView(console);  
	        // 打印消息  
	        
	        
	       // consoleStream.println("red");  
	        //consoleStream.setColor(new Color(Display.getDefault(),255,0,0));
	        try {
				out.write((message+"\r\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	       
	        consoleStream.print(message + "\n");  
	    
	    }  
	}  
	public void ShowUrl(String url)
	{
		   String time = new java.util.Date().toString();  
		   IHyperlink hyperlink;

		   //FileLink fileLink = new FileLink(file, null, 0, 0, 0);
		   //console.addHyperlink(fileLink, 0, 0);
		//console.addHyperlink(hyperlink, 0, 3);
		   //consoleStream.println(time);  
		   //console.getDocument().set(time+"\n");  
		  //console.addHyperlink(new MyHyperLink(url), 0, 3);
		   //MyHyperLink SS=new MyHyperLink(url);
//		   console.addHyperlink(SS, 0, 3);
		   //console.addHyperlink(SS, 0, 0);
		//console.addHyperlink(, 0,3);  
	}
	public ShowConsole(String name)
	{
		String work_space_path=Platform.getInstanceLocation().getURL().getPath();
		log_name=work_space_path+"\\"+name+".txt";
		File logfile = new File(log_name);
		if(!logfile.exists())
		{
			try {
				logfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			out = new FileOutputStream(new File(log_name),true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void  PrintColour(String message,String color)
	{
		consoleStream.println(color);  
        consoleStream.setColor(new Color(Display.getDefault(),255,0,0));
		printMessage(sf.format(new Date())+"  "+message);
		consoleStream.setColor(new Color(Display.getDefault(),0,0,0));
	}
	public void  Print(String message)
	{
		
		printMessage(sf.format(new Date())+"  "+message);
		System.out.println(sf.format(new Date())+"  "+message);
	}
    public  void printHexString(byte[] b)
    {
    	String AllHex="";
        for (int i = 0; i < b.length; i++)
        {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            AllHex=AllHex+hex.toUpperCase() + " ";
        }
        printMessage(AllHex);
    }
}
