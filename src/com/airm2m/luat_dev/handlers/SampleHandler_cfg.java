package com.airm2m.luat_dev.handlers;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;

import com.airm2m.serialPort.SerialTool;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

class ShowConfig extends ApplicationWindow {
	Combo combo_1;
	Combo combo_2;
	Combo combo_3;
	Combo combo_4;
	Combo combo_5;
	Combo combo_6;
	
	String Plat_Type="";
	String Debug_port="";
	String Port_Type="";
	String Active_Project="";
	ArrayList<String> Allport;
	ArrayList<String> ProjectList;
	Properties prop;
	String Work_cf;
	FileOutputStream oFile = null;
	ShowConfig consls;
	ShowConsole consolew=null;
	/**
	 * Create the application window.
	 */
	public ShowConfig() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}
	private void getSet()
	{
		Properties prop1 = new Properties(); 
		String work_space_path=Platform.getInstanceLocation().getURL().getPath();
		InputStream in = null;
		FileInputStream out;
		try {
			
			 out=new FileInputStream(work_space_path+"\\luat.properties");
			 in= new BufferedInputStream (out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ;
		}
		 try {
			 prop1.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 Plat_Type=prop1.getProperty("Plat_Type");
		 Debug_port=prop1.getProperty("Debug_port");
		 Active_Project=prop1.getProperty("Active_Project");
		 Port_Type=prop1.getProperty("Port_Type");
		 consolew.Print("本地配置项：平台"+Plat_Type+"   选择项目:"+Active_Project+"   通信端口:"+Debug_port+"   通信类型:"+Port_Type);
		 try {
			in.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void AddStartMsg(ArrayList<String> allport,String WorkPath,ArrayList<String> projectList,ShowConfig consl,ShowConsole con)
	{
		  Allport=allport;
		  Work_cf=WorkPath;
		  ProjectList=projectList;
		  consls=consl;
		  consolew=con;
		  getSet();
	  	  prop= new Properties();     	
		  try {
				oFile = new FileOutputStream(Work_cf+"\\luat.properties");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	  
	}
	private void closeUI(boolean result,String Plat_Type_new,String Debug_port_new,String Active_Project_new,String Port_Type_new)
	{
		if(result)
		{		 
			prop.setProperty("Plat_Type",Plat_Type_new);
			prop.setProperty("Debug_port",Debug_port_new);
			prop.setProperty("Active_Project",Active_Project_new);
			prop.setProperty("Port_Type",Port_Type_new);
			consolew.Print("当前配置项：平台"+Plat_Type_new+"   选择项目:"+Active_Project_new+"   通信端口:"+Debug_port_new+"   通信类型:"+Port_Type_new);
			try {
				prop.store(oFile, "change file");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else
		{
			prop.setProperty("Plat_Type",Plat_Type);
			prop.setProperty("Debug_port",Debug_port);
			prop.setProperty("Active_Project",Active_Project);
			prop.setProperty("Port_Type",Port_Type);
			//consolew.Print("当前配置项：平台"+Plat_Type+"   选择项目:"+Active_Project+"   通信端口:"+Debug_port+"   通信类型:"+Port_Type);
			try {
				prop.store(oFile, "change file");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	
		try {
			oFile.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		consls.close();
	}	
	private void host_protocol(TabFolder parent)
	{
		TabItem tabItem_2 = new TabItem(parent, SWT.NONE);
		tabItem_2.setText("host");

		Group grpHost = new Group(parent, SWT.NONE);
		grpHost.setText("host口下载和打印");
		tabItem_2.setControl(grpHost);
		
		Group group_3 = new Group(grpHost, SWT.NONE);
		group_3.setText("选择端口");
		group_3.setBounds(10, 33, 398, 71);
		
	    combo_1 = new Combo(group_3, SWT.READ_ONLY);
		for(int i=0;i<Allport.size();i++)
		{
			combo_1.add(Allport.get(i));
		}
		if(Debug_port!=null && Plat_Type.equals("RDA") && Port_Type.equals("host"))
			{
				combo_1.setText(Debug_port);
			}
		combo_1.setBounds(115, 24, 136, 25);
		
		Composite composite_3 = new Composite(grpHost, SWT.NONE);
		composite_3.setBounds(0, 110, 408, 42);
		
		Button button_4 = new Button(composite_3, SWT.NONE);
		button_4.setText("取消");
		button_4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				closeUI(false,null,null,null,null);
			}
		});
		button_4.setBounds(241, 10, 80, 27);
		
		Button button_5 = new Button(composite_3, SWT.NONE);
		button_5.setText("确定");
		button_5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				closeUI(true,"RDA",combo_1.getText(),combo_2.getText(),"host");
			}
		});
		button_5.setBounds(327, 10, 80, 27);
	}
	private void port_protocol(TabFolder parent)
	{
		TabItem tabItem_3 = new TabItem(parent, SWT.NONE);
		tabItem_3.setText("uart");
		
		Group group = new Group(parent, SWT.NONE);
		group.setText("普通串口下载和打印");
		tabItem_3.setControl(group);
		
		Group group_5 = new Group(group, SWT.NONE);
		group_5.setText("选择端口");
		group_5.setBounds(10, 33, 398, 71);
		
	    combo_3 = new Combo(group_5, SWT.READ_ONLY);
		for(int i=0;i<Allport.size();i++)
		{
			combo_3.add(Allport.get(i));
		}
		if(Debug_port!=null && Plat_Type.equals("RDA") && Port_Type.equals("uart"))
			combo_3.setText(Debug_port);
		combo_3.setBounds(115, 24, 136, 25);
		
		Composite composite_2 = new Composite(group, SWT.NONE);
		composite_2.setBounds(0, 110, 408, 42);
		
		Button button_2 = new Button(composite_2, SWT.NONE);
		button_2.setText("取消");
		button_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				closeUI(false,null,null,null,null);
			}
		});
		button_2.setBounds(241, 10, 80, 27);
		
		Button button_3 = new Button(composite_2, SWT.NONE);
		button_3.setText("确定");
		button_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				closeUI(true,"RDA",combo_3.getText(),combo_2.getText(),"uart");
			}
		});
		button_3.setBounds(327, 10, 80, 27);
	}
	private void air2xx(TabFolder parent)
	{
		
		
		TabItem tabItem = new TabItem(parent, SWT.NONE);
		tabItem.setText("AIR2XX");
		
		Group grpAirxx = new Group(parent, SWT.NONE);
		//grpAirxx.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
		//grpAirxx.setText("AIR2XX配置卡");
		tabItem.setControl(grpAirxx);
		
		TabFolder tabFolder_1 = new TabFolder(grpAirxx, SWT.NONE);
		tabFolder_1.setBounds(10, 97, 416, 183);
		
		if(Port_Type==null || Port_Type.equals("host"))
		{
			host_protocol(tabFolder_1);
			port_protocol(tabFolder_1);
		}
		else
		{	
			port_protocol(tabFolder_1);
			host_protocol(tabFolder_1);
		}

		
		Group group_4 = new Group(grpAirxx, SWT.NONE);
		group_4.setBounds(10, 19, 398, 62);
		group_4.setText("选择工程");
		
		combo_2 = new Combo(group_4,SWT.READ_ONLY);
		for(int i=0;i<ProjectList.size();i++)
		{
			combo_2.add(ProjectList.get(i));
		}
		if(Active_Project!=null && Plat_Type.equals("RDA"))
			combo_2.setText(Active_Project);
		combo_2.setBounds(128, 27, 136, 25);
	}
	
	private void air8xx(TabFolder parent)
	{

		TabItem tabItem_1 = new TabItem(parent, SWT.NONE);
		//tabItem_1.addListener(eventType, listener);

		
		tabItem_1.setText("AIR8XX");

		
		Group grpAirxx_1 = new Group(parent, SWT.NONE);
		//grpAirxx_1.setText("AIR8XX\u914D\u7F6E\u5361");
		tabItem_1.setControl(grpAirxx_1);
		
		Group group_2 = new Group(grpAirxx_1, SWT.NONE);
		group_2.setText("下载和打印口");
		group_2.setBounds(10, 110, 416, 111);
		
		Group group_7 = new Group(group_2, SWT.NONE);
		group_7.setText("选择端口");
		group_7.setBounds(10, 21, 396, 80);
		
	    combo_5 = new Combo(group_7, SWT.READ_ONLY);
		for(int i=0;i<Allport.size();i++)
		{
			combo_5.add(Allport.get(i));
		}
		if(Debug_port!=null && Plat_Type.equals("MTK"))
			combo_5.setText(Debug_port);
		combo_5.setBounds(133, 38, 136, 25);
		
		Group group_6 = new Group(grpAirxx_1, SWT.NONE);
		group_6.setBounds(10, 27, 396, 77);
		group_6.setText("选择工程");
		
		Combo combo_4 = new Combo(group_6, SWT.READ_ONLY);
		for(int i=0;i<ProjectList.size();i++)
		{
			combo_4.add(ProjectList.get(i));
		}

		if(Active_Project!=null && Plat_Type.equals("MTK"))
			combo_4.setText(Active_Project);
		combo_4.setBounds(144, 28, 136, 25);
		
		Composite composite = new Composite(grpAirxx_1, SWT.NONE);
		composite.setBounds(0, 227, 426, 42);
		
		Button button = new Button(composite, SWT.NONE);
		button.setBounds(241, 10, 80, 27);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				closeUI(false,null,null,null,null);
			}
		});
		button.setText("取消");
		
		Button button_1 = new Button(composite, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				closeUI(true,"MTK",combo_5.getText(),combo_4.getText(),"uart");
			}
		});
		button_1.setBounds(327, 10, 80, 27);
		button_1.setText("确定");
	}
	@Override
	protected Control createContents(Composite parent) {
		//shell = new Shell();
		//shell.setSize(443, 343);
		//shell.setText("SWT Application");
		parent.setSize(443, 363);
		Button button_1=null;
		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setBounds(0, 0, 434, 310);

		TabFolder tabFolder = new TabFolder(composite_1, SWT.NONE);
		tabFolder.setBounds(0, 0, 434, 310);
		if(Plat_Type==null || Plat_Type.equals("RDA"))
		{
			air2xx(tabFolder);
			air8xx(tabFolder);
		}
		else
		{
			air8xx(tabFolder);
			air2xx(tabFolder);
		}
		return button_1;
	

	}
	
	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	/*public static void main(String args[]) {
		try {
			testjf window = new testjf();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("LUAT CONFIG");
		newShell.setSize(443, 343);
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 344);
	}
}

class cfg_thread 
{
	ShowConsole console=new ShowConsole("cfg_log");
	ArrayList<String> allPort;
	MessageConsoleStream consoleStream = null;  
	IConsoleManager consoleManager = null;  
	final String CONSOLE_NAME = "Board Trace";
	private ArrayList<String> FileList = new ArrayList<String>();
	
	
	public void creatFile(String path)
	{
    	File f = new File(path);
    	if(!f.exists()){
    		f.mkdirs();
    	} 
    	// fileName表示你创建的文件名；为txt类型；
    	String fileName="luat.properties";
    	File file = new File(f,fileName);
    	if(!file.exists()){
	    	try {
	    		file.createNewFile();
	    	} catch (IOException e) {
	    	// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	}
    	}
	}

	private void GetALLProject(String path) {
		
		File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                return;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                    	if(!file2.getName().equals(".metadata")&& !file2.getName().equals("RemoteSystemsTempFiles") )
                    	    {
                    			FileList.add(file2.getName());
                    	    }
                    		
                    } else {
                    }
                }
            }
        } else {
        }
	}
	public void run()
	{
		console.Print("***********************开始配置***************************");
		String workSpace=Platform.getInstanceLocation().getURL().getPath();
		creatFile(workSpace);
		GetALLProject(workSpace);
		
		try {
			console.Print("findport");
			allPort=SerialTool.findPort();
			
			allPort.add("");
			console.Print("findport end");
			ShowConfig window1 = new ShowConfig();
			if(allPort.size()==1)
				console.Print("\r\n**********************提示**********************\r\n   请查看设备管理器中是否有此端口\r\n 1:如果有此端口,请先禁用再启用,\r\n 2:如果没有这个端口,请确保是否安装串口驱动\r\n 3:Air810用户请注意,请不要把usb当成了串口");

			window1.AddStartMsg(allPort,workSpace,FileList,window1,console);
			window1.setBlockOnOpen(true);
			window1.open();
			//Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		console.Print("***********************配置结束***************************");
	}
}
/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */

public class SampleHandler_cfg extends AbstractHandler {
	static boolean CFG_STATUS=false;
	public static boolean copyFile(String oldPath, String newPath) { 
		ShowConsole console=new ShowConsole("cfg_log");
		try { 
				int bytesum = 0; 
				int byteread = 0; 
				File oldfile = new File(oldPath);
				File newfilse = new File(newPath);
		 
				InputStream inStream = new FileInputStream(oldPath); //读入原文件 
				FileOutputStream fs = new FileOutputStream(newPath); 
				byte[] buffer = new byte[1444]; 
				int length; 
				while ( (byteread = inStream.read(buffer)) != -1) { 
					bytesum += byteread; //字节数 文件大小 
					System.out.println(bytesum); 
					fs.write(buffer, 0, byteread); 
				} 
					console.Print("DLL复制成功");
					inStream.close(); 

			}
			catch (Exception e) { 
				console.Print("复制DLL文件操作出错"); 
				JOptionPane.showMessageDialog(null, "请使用管理员权限打开LDT", "错误", JOptionPane.INFORMATION_MESSAGE);
				e.printStackTrace(); 
				return false;
			} 
		return true;

		}
	private boolean set_dll(String path)
	{
		
		Properties sysProperty=System.getProperties(); //系统属性
		String arch=sysProperty.getProperty("os.arch");
		/*if (arch.equals("amd64"))
		{
			System.lod(path+"\\mfz-rxtx-2.2-20081207-win-x64\\rxtxParallel.dll");
			System.load(path+"\\mfz-rxtx-2.2-20081207-win-x64\\rxtxSerial.dll");
		}*/
		File newfilse1 = new File("C:\\Windows\\System32\\rxtxParallel.dll");
		File newfilse2 = new File("C:\\Windows\\System32\\rxtxSerial.dll");
		
		long time= newfilse1.lastModified();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");         
	    cal.setTimeInMillis(time);
	    int  newFiletime=Integer.parseInt(formatter.format(cal.getTime()));
	    System.out.println("文件大小："+newfilse1.length());
	    boolean copyre=false;
		if (arch.equals("amd64"))
		{
			if(newfilse1.length() !=84480)
			{
				copyre=true;
			}
		}
		else
		{
			if(newfilse2.length() !=108032)
			{
				copyre=true;
			}
		}
		
		if(!newfilse1.exists() || !newfilse2.exists() || copyre)
		{
			if (arch.equals("amd64"))
			{
				if(!copyFile(path+"\\mfz-rxtx-2.1-win-x64\\rxtxParallel.dll","C:\\Windows\\System32\\rxtxParallel.dll"))
					return false;
				if(!copyFile(path+"\\mfz-rxtx-2.1-win-x64\\rxtxSerial.dll","C:\\Windows\\System32\\rxtxSerial.dll"))
					return false;
			}
			else
			{
				if(!copyFile(path+"\\mfz-rxtx-2.1-win-x86\\rxtxParallel.dll","C:\\Windows\\System32\\rxtxParallel.dll"))
					return false;
				if(!copyFile(path+"\\mfz-rxtx-2.1-win-x86\\rxtxSerial.dll","C:\\Windows\\System32\\rxtxSerial.dll"))
					return false;
			}
			JOptionPane.showMessageDialog(null, "因缺少驱动，添加驱动完成，即将重启,请注意使用管理员权限打开IDE", "提示", JOptionPane.INFORMATION_MESSAGE);
			PlatformUI.getWorkbench().restart();
			return false;
			
		}
		return true;
	}
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(CFG_STATUS)
		{				//检测到已经打开配置窗口
			JOptionPane.showMessageDialog(null, "配置窗口已经打开", "提示", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		CFG_STATUS =true;
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		String path = null;
		try {
			 path=Platform.asLocalURL(Platform.getBundle("com.airm2m.luat_dev").getEntry("")).getFile();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(set_dll(path))
		{
			cfg_thread cfg=new cfg_thread();
			cfg.run();
			CFG_STATUS =false;
		}

		
		return null;
	}
}
