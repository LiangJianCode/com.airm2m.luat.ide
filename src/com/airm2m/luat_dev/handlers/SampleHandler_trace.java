package com.airm2m.luat_dev.handlers;
import java.awt.Graphics;

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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler_trace extends AbstractHandler {
	//ShowConsole console=new ShowConsole();
	MessageConsoleStream consoleStream = null;  
	IConsoleManager consoleManager = null;  
	final String CONSOLE_NAME = "Board Trace";  
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		//DirectoryDialog dialog = new DirectoryDialog(window.getShell());
		//String  selectPath = dialog.open() ;
		//ComBin combin=new ComBin(selectPath);
		//DownLoad downLoad = new DownLoad();
		//downLoad.start();
		log logs=new log();
		logs.start();
		/*console.Print("trace start");		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		IStructuredSelection structured = (IStructuredSelection)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection("org.eclipse.jdt.ui.PackageExplorer");
		
		Object selected = structured.getFirstElement();
		//File file = (File)selected;
		System.out.println(selected.toString());*/
		return null;
	}
}
