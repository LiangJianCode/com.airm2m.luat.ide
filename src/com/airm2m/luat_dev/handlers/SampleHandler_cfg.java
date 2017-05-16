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
import java.util.ArrayList;
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
	Combo combo_download;
	Combo combo_trace_uart;
	Combo combo_debug;
	Combo combo_ActiveProject;
	String combo_download_old="";
	String combo_trace_uart_old="";
	String combo_debug_old="";
	String combo_ActiveProject_old="";
	ArrayList<String> Allport1;
	ArrayList<String> ProjectList;
	Properties prop;
	String Work_cf;
	FileOutputStream oFile = null;
	ShowConfig consls;
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
		 combo_debug_old=prop1.getProperty("debug");
		 combo_ActiveProject_old=prop1.getProperty("active_project");
		 System.out.println("old_cfg"+combo_download_old+""+combo_trace_uart_old+""+combo_debug_old+""+combo_ActiveProject_old);
		 try {
			in.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void AddStartMsg(ArrayList<String> allport,String WorkPath,ArrayList<String> projectList,ShowConfig consl)
	{
		  Allport1=allport;
		  Work_cf=WorkPath;
		  ProjectList=projectList;
		  consls=consl;
		  getSet();
	  	  prop= new Properties();     	
		  try {
				oFile = new FileOutputStream(Work_cf+"\\luat.properties");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	  
	}
	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	/*
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.READ_ONLY);
		{
			Group group = new Group(container, SWT.READ_ONLY);
			group.setText("选择工程");
			group.setBounds(0, 0, 434, 89);
			{
				combo_ActiveProject = new Combo(group, SWT.READ_ONLY);
				int i=0;
				for(i=0;i<ProjectList.size();i++)
				{
					combo_ActiveProject.add(ProjectList.get(i));
				}
				combo_ActiveProject.setBounds(137, 26, 138, 25);

				if(combo_ActiveProject_old!=null)
					combo_ActiveProject.setText(combo_ActiveProject_old);
			}
		}
		{
			Group grpHost = new Group(container, SWT.READ_ONLY);
			grpHost.setText("选择下载调试口");
			grpHost.setBounds(0, 93, 434, 89);
			{
					combo_debug = new Combo(grpHost, SWT.READ_ONLY);
					int i=0;
					for(i=0;i<Allport1.size();i++)
					{
						combo_debug.add(Allport1.get(i));
					}
					combo_debug.setBounds(137, 26, 138, 25);
					if(combo_debug_old!=null)
						combo_debug.setText(combo_debug_old);
			}
		}
		
		Group group = new Group(container, SWT.NONE);
		group.setBounds(0, 181, 434, 40);
		
		Button button = new Button(group, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					oFile.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				consls.close();
			}
		});
		button.setBounds(258, 10, 80, 27);
		button.setText("\u53D6\u6D88");
		
		Button button_1 = new Button(group, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				prop.setProperty("debug",combo_debug.getText());
				prop.setProperty("active_project", combo_ActiveProject.getText());
				System.out.println("cfg~~~~："+combo_debug.getText()+"   "+combo_ActiveProject.getText());
				try {
					prop.store(oFile, "change file");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					oFile.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				consls.close();

			}
		});
	    button_1.setBounds(344, 10, 80, 27);
		button_1.setText("确定");
		
		return container;
	}
	*/
	
	@Override
	protected Control createContents(Composite parent) {
		//shell = new Shell();
		//shell.setSize(443, 343);
		//shell.setText("SWT Application");
		parent.setSize(443, 363);
		
		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setBounds(0, 0, 434, 310);

		
		
		TabFolder tabFolder = new TabFolder(composite_1, SWT.NONE);
		tabFolder.setBounds(0, 0, 434, 261);
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("AIR2XX");
		
		Group grpAirxx = new Group(tabFolder, SWT.NONE);
		//grpAirxx.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
		grpAirxx.setText("AIR2XX配置卡");
		tabItem.setControl(grpAirxx);
		
		TabFolder tabFolder_1 = new TabFolder(grpAirxx, SWT.NONE);
		tabFolder_1.setBounds(10, 87, 416, 144);
		
		TabItem tabItem_2 = new TabItem(tabFolder_1, SWT.NONE);
		tabItem_2.setText("host");
		
		Group grpHost = new Group(tabFolder_1, SWT.NONE);
		grpHost.setText("host口下载和打印");
		tabItem_2.setControl(grpHost);
		
		Group group_3 = new Group(grpHost, SWT.NONE);
		group_3.setText("选择端口");
		group_3.setBounds(10, 33, 398, 71);
		
		Combo combo_1 = new Combo(group_3, SWT.NONE);
		combo_1.setBounds(115, 24, 136, 25);
		
		TabItem tabItem_3 = new TabItem(tabFolder_1, SWT.NONE);
		tabItem_3.setText("uart");
		
		Group group = new Group(tabFolder_1, SWT.NONE);
		group.setText("普通串口下载和打印");
		tabItem_3.setControl(group);
		
		Group group_5 = new Group(group, SWT.NONE);
		group_5.setText("选择端口");
		group_5.setBounds(10, 33, 398, 71);
		
		Combo combo_3 = new Combo(group_5, SWT.NONE);
		combo_3.setBounds(115, 24, 136, 25);
		
		Group group_4 = new Group(grpAirxx, SWT.NONE);
		group_4.setBounds(10, 19, 398, 62);
		group_4.setText("选择工程");
		
		Combo combo_2 = new Combo(group_4, SWT.NONE);
		combo_2.setBounds(128, 27, 136, 25);
		
		TabItem tabItem_1 = new TabItem(tabFolder, SWT.NONE);
		tabItem_1.setText("AIR8XX");
		
		Group grpAirxx_1 = new Group(tabFolder, SWT.NONE);
		grpAirxx_1.setText("AIR8XX\u914D\u7F6E\u5361");
		tabItem_1.setControl(grpAirxx_1);
		
		Group group_2 = new Group(grpAirxx_1, SWT.NONE);
		group_2.setText("下载和打印口");
		group_2.setBounds(10, 110, 416, 121);
		
		Group group_7 = new Group(group_2, SWT.NONE);
		group_7.setText("选择端口");
		group_7.setBounds(10, 21, 396, 100);
		
		Combo combo_5 = new Combo(group_7, SWT.NONE);
		combo_5.setBounds(133, 38, 136, 25);
		
		Group group_6 = new Group(grpAirxx_1, SWT.NONE);
		group_6.setBounds(10, 27, 396, 77);
		group_6.setText("选择工程");
		
		Combo combo_4 = new Combo(group_6, SWT.NONE);
		combo_4.setBounds(144, 28, 136, 25);
		
		Composite composite = new Composite(composite_1, SWT.NONE);
		composite.setBounds(0, 258, 434, 42);
		
		Button button = new Button(composite, SWT.NONE);
		button.setBounds(241, 10, 80, 27);
		button.setText("取消");
		
		Button button_1 = new Button(composite, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		button_1.setBounds(327, 10, 80, 27);
		button_1.setText("确定");
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
			window1.AddStartMsg(allPort,workSpace,FileList,window1);
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
				if (!newfilse.exists()) { //dll不存在时候 
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
				else
				{
					console.Print("DLL文件已经存在");
				}
			}
			catch (Exception e) { 
				console.Print("复制DLL文件操作出错"); 
				JOptionPane.showMessageDialog(null, "请使用管理员权限打开LDT", "错误", JOptionPane.INFORMATION_MESSAGE);
				e.printStackTrace(); 
				return false;
			} 
		return true;

		}
	private void set_dll(String path)
	{
		Properties sysProperty=System.getProperties(); //系统属性
		String arch=sysProperty.getProperty("os.arch");
		File newfilse1 = new File("C:\\Windows\\System32\\rxtxParallel.dll");
		File newfilse2 = new File("C:\\Windows\\System32\\rxtxSerial.dll");
		if(!newfilse1.exists() || !newfilse2.exists())
		{
			if (arch.equals("amd64"))
			{
				if(!copyFile(path+"\\mfz-rxtx-2.2-20081207-win-x64\\rxtxParallel.dll","C:\\Windows\\System32\\rxtxParallel.dll"))
					return ;
				if(!copyFile(path+"\\mfz-rxtx-2.2-20081207-win-x64\\rxtxSerial.dll","C:\\Windows\\System32\\rxtxSerial.dll"))
					return ;
			}
			else
			{
				if(!copyFile(path+"\\mfz-rxtx-2.2-20081207-win-x86\\rxtxParallel.dll","C:\\Windows\\System32\\rxtxParallel.dll"))
					return ;
				if(!copyFile(path+"\\mfz-rxtx-2.2-20081207-win-x86\\rxtxSerial.dll","C:\\Windows\\System32\\rxtxSerial.dll"))
					return ;
			}
			JOptionPane.showMessageDialog(null, "因缺少驱动，添加驱动完成，即将重启", "提示", JOptionPane.INFORMATION_MESSAGE);
			PlatformUI.getWorkbench().restart();
		}
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
		set_dll(path);
		//DirectoryDialog dialog = new DirectoryDialog(window.getShell());
		//String  selectPath = dialog.open() ;
		cfg_thread cfg=new cfg_thread();
		cfg.run();
		CFG_STATUS =false;
		
		return null;
	}
}
