package com.airm2m.luat_dev.handlers;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.Platform;

import SevenZip.LzmaAlone;


public class ComBin {
	static ShowConsole console=new ShowConsole("combin");
	String work_space_path=null;
	String TempPath=null;
	String Plat_Type=null;
	File tempZipfile;
    String WorkSpace_Path;
	private ArrayList<String> FileList = new ArrayList<String>();
	private ArrayList<String> SrcFileList = new ArrayList<String>();
	private ArrayList<String> ResFileList = new ArrayList<String>();
	ArrayList<String> ALLList = new ArrayList<String>();
	static boolean ComBINsTATES=false;
	long CUSTM_MAX=536576;
	long CUSTM_RES_MAX=81920;
	String Press;
	public char getUnsignedByte (long data){     
		return (char) (data&0x0FF);
		}

	public short getUnsignedShort (long data){      
		return (short) ((data)&0x0FFFF);
	}

	public int getUnsignedInt (long data){     
		return (int)(data)&0x0FFFFFFFF;
	}
	private  void zipPress(String path) {  

		String[] ss=new String[3];
    	ss[0]= "e";
    	ss[1]=path;
    	ss[2]=path+".zip";
    	try {
			new LzmaAlone().main(ss);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

        try{
            String command = "cmd /c 7lzma.exe e "+ path+" "+path+".zip";
            Process proc=Runtime.getRuntime().exec(command);  
            InputStream fis=proc.getInputStream();    
            //用一个读输出流类去读    
             InputStreamReader isr=new InputStreamReader(fis);    
            //用缓冲器读行    
             BufferedReader br=new BufferedReader(isr);    
             String line=null;    
            //直到读完为止    
            while((line=br.readLine())!=null)    
             {    
            	console.Print(line);    
             }    
           } 
         catch(Exception e)
        {
        	 console.Print(e.toString());            
         }

    }  
	private String GetScriptName(String path)
	{
		return path.trim().substring(path.trim().lastIndexOf("\\")+1);  
	}
	private  String GetTrueModuleName(String ModuleName)
	{
		String TrueModuleName="";
		int step1_find_flg=1;        // 为1的时候，代表开始寻找，2的时候代表找的require的头部，3的时候代表寻找结束
		int num=0;
		int strnum=0;
		int endnum=0;
		byte ModuleNameBYTE[] = ModuleName.getBytes();
		for(byte temp:ModuleNameBYTE)
		{
			if(step1_find_flg == 1)
			{
				if(temp==34)
				{
					strnum=num+1;
					step1_find_flg=2;
				}
				else if(temp != 32)
				{
					return "";
				}
				
			}
			else if(step1_find_flg==2)
			{
				if(temp==34)
				{
					endnum=num;
					step1_find_flg=3;
				}
			}
			else if(step1_find_flg==3)
			{
				break;
			}
			num=num+1;
		}
		TrueModuleName=ModuleName.substring(strnum,endnum);
		return TrueModuleName; 
	}
	private  int GetNumberStr(String context,String target)
	{
		int ss=0;
		int target_flg=context.indexOf(target,0);
		while(target_flg!=-1)
		{
			ss=ss+1;
			target_flg=context.indexOf(target,target_flg+target.length());
		}
		return ss;
	}
	private  String Concat_String(String Context,int start,int  end)
	{
		String Temp_Context;
		int line_num=0;
		String replaceString="";
		Temp_Context =Context.substring(start, end);
		line_num=GetNumberStr(Temp_Context,"\n");
		for(int i=0;i<line_num;i++)
		{
			replaceString=replaceString+"\n";
		}
		return Context.substring(0,start)+replaceString+Context.substring(end);
	}
	private  String  Del_Note_Handle(String Context,int Note_start)
	{
		int Note_end=0;
	
		if( (Context.length()>= (Note_start+8)) && Context.substring(Note_start+2,Note_start+8).equals("[====["))
		{
			//console.Print("--[====[");
			Note_end= Context.indexOf("]====]", Note_start);
			if(Note_end==-1)
			{
				return "";
			}
			//console.Print(""+Note_end);
			return Concat_String(Context,Note_start,Note_end+6);
		}
		else if((Context.length()>= (Note_start+4)) && Context.substring(Note_start+2,Note_start+4).equals("[["))
		{
			//console.Print("--[[");
			Note_end= Context.indexOf("]]", Note_start);
			if(Note_end==-1)
			{
				return "";
			}
			//console.Print(""+Note_end);
			return Concat_String(Context,Note_start,Note_end+2);
		}
		else
		{
			//console.Print("--");
			Note_end= Context.indexOf("\n", Note_start);
			//console.Print("Note_end:"+Note_end+" Note_start:"+Note_start);
			if(Note_end==-1)
			{
				return "";
			}
			
			return Concat_String(Context,Note_start,Note_end);
		}
	}
	private  int Find_True_Note(String context,int index)
	{
		int truestartIndex=0;
		int startIndex =0;
		int QuotesIndex=0;
		int QuotesIndex1=0;
		int QuotesIndex2=0;	
		int Quotes_END;
		startIndex=context.indexOf("--", index);
		QuotesIndex1=context.indexOf("\"", index);
		QuotesIndex2=context.indexOf("\'", index);
		//System.out.println("startIndex:"+startIndex+" QuotesIndex1:"+QuotesIndex1+" QuotesIndex2"+QuotesIndex2);
		if(QuotesIndex1 ==-1)
		{
			QuotesIndex=QuotesIndex2;
			truestartIndex=2;
		}
		else if(QuotesIndex2 == -1)
		{
			QuotesIndex=QuotesIndex1;
			truestartIndex=1;
		}
		else if(QuotesIndex1 >QuotesIndex2 )
		{
			QuotesIndex=QuotesIndex2;
			truestartIndex=2;
		}
		else if(QuotesIndex2 >QuotesIndex1)
		{
			QuotesIndex=QuotesIndex1;
			truestartIndex=1;
		}
		if(startIndex!=-1)            
		{
			if(QuotesIndex!=-1 && QuotesIndex<startIndex)
			{
				if(truestartIndex==1)
				{
					Quotes_END=context.indexOf("\"",QuotesIndex+1);
					if(Quotes_END==-1)
					{
						console.Print("双引号不配对");
						return -2;                      //双引号不配对
					}
				}
				else
				{
					Quotes_END=context.indexOf("\'",QuotesIndex+1);
					if(Quotes_END==-1)
					{
						console.Print("单引号不配对");
						return -3;                      //单引号不配对
					}
				}
				return Find_True_Note(context,Quotes_END+1);
			}
			else
				return startIndex;
		}		
		return -1;
	}
	
	private  String Del_Modul_note(String context)
	{ 
		int startIndex =0;
	
		String context_temp=context;

		startIndex=Find_True_Note(context_temp,startIndex);
		//console.Print("Del_Modul_note");
		//console.Print(""+startIndex);
		while (startIndex!=-1 && startIndex!=-2 && startIndex!=-3)
		{
			//System.out.println("context_temp: "+context_temp+" startIndex: "+startIndex);
			context_temp=Del_Note_Handle(context_temp,startIndex);
			if(context_temp.equals(""))
			{
				return "";
			}
			//console.Print(context_temp);
			startIndex=Find_True_Note(context_temp,0);
			//console.Print(""+startIndex);
			//console.Print(temp_modle_name);  
		}
		return context_temp;
	}
	private  void GetModul(String ss, ArrayList<String>  listmodle)
	{ 
		int startIndex =0;
		String temp_modle_name = null;
		startIndex=ss.indexOf("require", startIndex);

		if (startIndex!=-1)
		{
			temp_modle_name=GetTrueModuleName(ss.substring(startIndex+7));
			if(!temp_modle_name.equals(""))
			{
				temp_modle_name=temp_modle_name+".lua";
				listmodle.add(temp_modle_name);
			}
			//console.Print(temp_modle_name);  
			while(startIndex!=-1)
			{
				 startIndex=ss.indexOf("require", startIndex+7);
				 if(startIndex !=-1)
				 {
					 temp_modle_name=GetTrueModuleName(ss.substring(startIndex+7));
					if(!temp_modle_name.equals(""))
					{
						temp_modle_name=temp_modle_name+".lua";
						listmodle.add(temp_modle_name);
					}
				 }
				 else
				 {
					 break;
				 }
			} 
		}
	}
	public  boolean createFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {// 判断文件是否存在
			System.out.println("目标文件已存在" + filePath);
			return false;
		}
		if (filePath.endsWith(File.separator)) {// 判断文件是否为目录
			System.out.println("目标文件不能为目录！");
			return false;
		}
		if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
			// 如果目标文件所在的文件夹不存在，则创建父文件夹
			System.out.println("目标文件所在目录不存在，准备创建它！");
			if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
				System.out.println("创建目标文件所在的目录失败！");
				return false;
			}
		}
		try {
			if (file.createNewFile()) {// 创建目标文件
				System.out.println("创建文件成功:" + filePath);
				return true;
			} else {
				System.out.println("创建文件失败！");
				return false;
			}
		} catch (IOException e) {// 捕获异常
			e.printStackTrace();
			System.out.println("创建文件失败！" + e.getMessage());
			return false;
		}
	}
    public  boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }
    public  boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }
	public  boolean deletefilemu(String fileName) {
	        File file = new File(fileName);
            if (!file.isFile())
                 return deleteDirectory(fileName);
	        return true;
	}
	     
	private  ArrayList<String> filtrate(ArrayList<String> filelist)
	{
		ArrayList<String>  truemodle=new ArrayList<String>();
		ArrayList<String> Tempfilelist=new ArrayList<String>();     
		String[] bArray = {"coroutine.lua","string.lua","table.lua", "math.lua", "io.lua","os.lua","bit.lua","cpu.lua", 
							"disp.lua", "i2c.lua","pack.lua","pio.lua", "pmd.lua","rtos.lua","uart.lua", "adc.lua", "iconv.lua",
							"audiocore.lua","zlib.lua","crypto.lua","json.lua","coroutine.lua","table.lua","debug.lua","package.lua",
							"tcpipsock.lua","watchdog.lua"};  
		Tempfilelist=(ArrayList<String>) filelist.clone();
		int flag_Main=0;
		int copy_num=0;
		int file_exist_flg=0;
		String Del_after = null;
		console.Print("正在检查是否有重复的脚本...");
		for(String tmp_re:filelist)                        //检查是否有重复的脚本
		{
			File tempFile1 =new File( tmp_re.trim());  
        	String fileName1 = tempFile1.getName();  
			for(String tmp_re0:filelist)                 
			{
				File tempFile2 =new File( tmp_re0.trim());  
	        	String fileName2 = tempFile2.getName();  
				if(fileName2.equals(fileName1))
				{
					if(copy_num >0)
					{	
						console.Print("文件"+fileName2+"重复，请删除一个");  
	            		JOptionPane.showMessageDialog(null, "文件"+fileName1+"重复，请删除一个", "错误", JOptionPane.INFORMATION_MESSAGE);
	            		return null;
					}
					else
					{
						copy_num=copy_num+1;
					}
				}
			}
			copy_num=0;
		}
		console.Print("正在删除脚本注释..........");
		if(!deletefilemu(work_space_path+"/.metadata/ClearScr/"))
		{
			console.Print("删除原有临时文件失败");
		}
		for(String tmp:filelist)
		{
            ReadFile FILE= new ReadFile(tmp);
            byte[] Cellbody = new byte[FILE.len()];
            FILE.read(Cellbody);
            String t = new String(Cellbody);
    		//console.Print("文件里寻找require文件:"+tmp);
			File tempFile2 =new File( tmp.trim());  
        	String fileName2 = tempFile2.getName(); 
        	System.out.println(work_space_path+"/.metadata/ClearScr/"+fileName2);
        	if(t.length()>0)
        		{
        			Del_after=Del_Modul_note(t);
        			if(Del_after.equals(""))
        			{
        				console.Print("文件"+fileName2+"注释不对,请检查(如果检查没问题,请注意文件结尾要加上回车换行)");  
        				JOptionPane.showMessageDialog(null, "文件"+fileName2+"注释不对,请检查(如果检查没问题,请注意文件结尾要加上回车换行)", "错误", JOptionPane.INFORMATION_MESSAGE);
        				return null;
        			}
        		}	
        	else
        		Del_after=t;
        	
        	createFile(work_space_path+"/.metadata/ClearScr/"+fileName2);
        	WriteFile file=new WriteFile(work_space_path+"/.metadata/ClearScr/"+fileName2);
        	file.write(Del_after.getBytes());
		}
		
		filelist=new ArrayList<String>();
		File file = new File(work_space_path+"/.metadata/ClearScr/");
		
        File[] files = file.listFiles();
        for (File file2 : files) {
        	filelist.add(file2.getAbsolutePath());
        }
        Tempfilelist=(ArrayList<String>) filelist.clone();
		console.Print("正在筛选不需要的脚本...");
		
        for(String tmp:filelist){
            //console.Print(tmp);
        	File tempFile5 =new File( tmp.trim());  
            String fileName5 = tempFile5.getName();  
            if(fileName5.indexOf(".lua",-3)!=-1)
            {
	        	ReadFile FILE= new ReadFile(tmp);
	            byte[] Cellbody = new byte[FILE.len()];
	            FILE.read(Cellbody);
	            String t = new String(Cellbody);
	    		//console.Print("文件里寻找require文件:"+tmp);
	            GetModul(t,truemodle);
            }
            else
            {
            	System.out.println("lua 外文件"+fileName5);
            	truemodle.add(fileName5);   //如果不是.lua文件，就直接添加入fileName5
            }
        } 
        console.Print("正在检查是否和扩展库重名...");
        for(String tem1:filelist)
        {
        	File tempFile =new File( tem1.trim());  
            String fileName = tempFile.getName();  
            if(fileName.equals("main.lua") )
            {
            	truemodle.add("main.lua");
            	flag_Main=1;
            }
            for(String tmp_lib:bArray)
            {
            	//console.Print(tmp_lib+"  "+fileName);
            	if(tmp_lib.equals(fileName) )
                {
            		console.Print("文件"+tmp_lib+"和扩展库命名重复，请重新命名");  
            		JOptionPane.showMessageDialog(null, "文件"+tmp_lib+"和扩展库命名重复，请重新命名", "错误", JOptionPane.INFORMATION_MESSAGE);
            		return null;
                }
            }
        	if(truemodle.indexOf(fileName)==-1)                   //查询是否需要这个模块，如果不需要就删除这个模块
        	{
        		Tempfilelist.remove(tem1);
        	}
        	
        }
        console.Print("正在检查是否缺少客户require文件....");
        for(String tem4:truemodle)                                //查询是否缺少这个模块
        {
        	File tempFile4 =new File( tem4.trim());  
            String fileName4 = tempFile4.getName();  
            for(String tem5:filelist)                             //在用户脚本中查询
            {
            	File tempFile5 =new File( tem5.trim());  
                String fileName5 = tempFile5.getName();  
                if(fileName4.equals(fileName5))
                {
                	file_exist_flg=1;
                }
            }
            if(file_exist_flg==0)									//在用户脚本里面没有发现这个脚本时
            {
	            for(String tem6:bArray)								//在扩展库和lua库列表查询
	            {
	            	if(tem6.equals(fileName4))
	            	{
	            		file_exist_flg=1;
	            	}
	            }
	            if(file_exist_flg==0)
	            {
	            	console.Print("require文件"+fileName4+"但是没有添加入workspace");
	            	JOptionPane.showMessageDialog(null, "require文件"+fileName4+"但是没有添加入workspace", "错误", JOptionPane.INFORMATION_MESSAGE);
	            	return null;
	            }
            }
            file_exist_flg=0;
        }
        if(flag_Main==0)
        {
        	JOptionPane.showMessageDialog(null, "工作空间没有包含main.lua文件", "错误", JOptionPane.INFORMATION_MESSAGE);
        	return null;
        }
		return Tempfilelist;
	}
	private void GetALLFile(String path) {
		File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                    	if(!file2.getName().equals(".settings"))
                    	{
                    		GetALLFile(file2.getAbsolutePath());
                    	}
                        } 
                	else {
	                	if(!file2.getName().equals(".buildpath") && !file2.getName().equals(".project")&& !file2.getName().equals(".classpath"))
	                	{
	                		//console.Print("file add :"+file2.getName());
	                		ALLList.add(file2.getAbsolutePath());
	                	}
                    }
                }
            }
        } else {
        }
	}
	private short GetChecksum(byte[] data,short num)
	{
		int sum=0;
		short temp;
		for (int i = 0; i < num; i++) {
			if (data[i]<0)
			{
				temp=(short) (data[i]&0x00FF);
			}
			else
			{
				temp=data[i];
			}
			sum=sum+temp;
		}
		return getUnsignedShort(sum);
	}
	private void WriteHead(WriteFile file,ArrayList<String> fileList)
	{
		byte[] pump_on;
		ByteBuffer pump_on_buf=ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
		pump_on_buf.putShort((short) 0x0401).putInt(getUnsignedInt(0xA55AA55A));           
		pump_on_buf.putShort((short) 0x0202).putShort((short) 1);                          
		pump_on_buf.putShort((short) 0x0403).putInt(getUnsignedInt(0x18));				   
		pump_on_buf.putShort((short) 0x0204).putShort((byte) getUnsignedByte(fileList.size()));
		pump_on_buf.putShort((short) 0x02FE);											   	
		pump_on = pump_on_buf.array();
		pump_on_buf.putShort(GetChecksum(pump_on,(short) 22));                             
		pump_on=pump_on_buf.array();
		file.write(pump_on);
	}
	private byte[] CellHead(byte[] body,String name)
	{ 
		byte[] cellhead = null;
		ByteBuffer cellheadbuff=ByteBuffer.allocate(name.length()+18).order(ByteOrder.LITTLE_ENDIAN);
		cellheadbuff.putShort((short) 0x0401).putInt(getUnsignedInt(0xA55AA55A));                                
		cellheadbuff.put((byte) 2).put((byte) getUnsignedByte(name.length())).put(name.getBytes());               
		cellheadbuff.put((byte) 3).put((byte) 4).putInt(getUnsignedInt(body.length));  
		cellheadbuff.putShort((short) 0x02FE);                                                                       
		cellhead = cellheadbuff.array();
		cellheadbuff.putShort(GetChecksum(cellhead,(short)(name.length()+18)));                                   
		cellhead = cellheadbuff.array();
		return cellhead;
	}
	private void CellBody(String path,WriteFile file,boolean isZIP)
	{
		if(isZIP)
		{	
			zipPress(path);
			path=path+".zip";
		}
		ReadFile recv=new ReadFile(path);
		byte[] Cellbody = new byte[recv.len()];
		recv.read(Cellbody);
		file.write(CellHead(Cellbody,GetScriptName(path)));
		file.write(Cellbody);
		if(isZIP)
			recv.delet();
	}
	private void WriteBody(ArrayList<String> filelist,WriteFile file,boolean isZIP)
	{
        for(String tmp:filelist){
        	CellBody(tmp,file,isZIP);
        }
	}
	private String GetScriptSrc(String path)
	{
		return path.trim().substring(0, path.trim().lastIndexOf("\\"));  
	}
	private void GetNecessaryScr(ArrayList<String> Scrlist)
	{
		ArrayList<String> AllScrNameList = new ArrayList<String>();
		for(String i:Scrlist)
		{
			AllScrNameList.add(GetScriptSrc(i));
		}
	}
	private void getmsg()
	{
		Properties prop = new Properties(); 
		work_space_path=Platform.getInstanceLocation().getURL().getPath();
		console.Print("work_path0:"+work_space_path);
		InputStream in = null;
		try {
			 in= new BufferedInputStream (new FileInputStream(work_space_path+"\\luat.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ;
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
		 TempPath=prop.getProperty("Active_Project");
		 Plat_Type=prop.getProperty("Plat_Type");
		if(TempPath==null || TempPath.equals(""))
		{
			JOptionPane.showMessageDialog(null, "未选择工程，或未建立工程", "错误", JOptionPane.INFORMATION_MESSAGE);
			ComBINsTATES=false;
			return ;
		}
		
	}
	public void  Merge(boolean hostcompress)
	{
		getmsg();
		WorkSpace_Path=work_space_path+"\\"+TempPath;
		String outputname;
		if(Plat_Type.equals("RDA"))
			{
				outputname="Update_RDA";
				WriteFile file=new WriteFile(work_space_path+"\\"+outputname);
				GetALLFile(WorkSpace_Path);
		        ALLList=filtrate(ALLList);
		        if(ALLList ==null)
		        {
		        	return ;
		        }
		        console.Print("文件总数："+ALLList.size()); 
				FileList=ALLList;
				ALLList=new ArrayList<String>();
				if(FileList!=null)
				{
					WriteHead(file,FileList);
					boolean compress;
					if(hostcompress)
						compress=false;
					else
						compress=true;
					WriteBody(FileList,file,compress);
					file.close();
					console.Print("合并完毕"+FileList.toString());
				}
				else
				{
					JOptionPane.showMessageDialog(null, "工程脚本文件为空,或工程位置设置错误", "错误", JOptionPane.INFORMATION_MESSAGE);
					ComBINsTATES=false;
					
					return ;
				}
			}
		else
			{
				outputname="Update_MTK";
				WriteFile file=new WriteFile(work_space_path+"\\"+outputname);
				GetALLFile(WorkSpace_Path);
		        ALLList=filtrate(ALLList);
		        if(ALLList ==null)
		        {
		        	return ;
		        }
				FileList=ALLList;
				ALLList=new ArrayList<String>();
				WriteMTKHeadSrc(file);
				WriteHead(file,FileList);
				WriteBody(FileList,file,false);
				//WriteBlackSrc(file_scr);
				file.close();
				console.Print("合并脚本完毕"+FileList.toString());
			}

	}
	public  ComBin()
	{
		
	}

	public  boolean LodComBin(String platform,boolean hostcompress)
	{	
		getmsg();
		WorkSpace_Path=work_space_path+"\\"+TempPath;
		console.Print("active_project:"+WorkSpace_Path);
		if(platform.equals("RDA"))
		{
			WriteFile file=new WriteFile(work_space_path+"\\RdaCombin.bin");
			GetALLFile(WorkSpace_Path);
			ALLList=filtrate(ALLList);
	        if(ALLList ==null)
	        {
	        	return false ;
	        }
			FileList=ALLList;
			ALLList=new ArrayList<String>();
			if(FileList!=null)
			{
				WriteHead(file,FileList);
				boolean compress;
				if(hostcompress)
					compress=false;
				else
					compress=true;
				WriteBody(FileList,file,compress);
				file.close();
				console.Print("合并完毕"+FileList.toString());
				return true;
			}
			else
			{
				JOptionPane.showMessageDialog(null, "工程脚本文件为空,或工程位置设置错误", "错误", JOptionPane.INFORMATION_MESSAGE);
				ComBINsTATES=false;
				
				return false ;
			}
		}
		else if(platform.equals("MTK"))
		{
			WriteFile file_scr=new WriteFile(work_space_path+"\\CUSTOMER");
			GetAllScrFile(WorkSpace_Path);
	        ALLList=filtrate(ALLList);
	        if(ALLList ==null)
	        {
	        	return false ;
	        }
			SrcFileList=ALLList;
			ALLList=new ArrayList<String>();
			if(SrcFileList!=null && !(SrcFileList.isEmpty()))
			{
				WriteMTKHeadSrc(file_scr);
				WriteHead(file_scr,SrcFileList);
				WriteBody(SrcFileList,file_scr,false);
				//WriteBlackSrc(file_scr);
				file_scr.close();
				console.Print("合并脚本完毕"+SrcFileList.toString());
				WriteFile file_res=new WriteFile(work_space_path+"\\CUSTOMER_RES");
				GetAllResFile(WorkSpace_Path);
				ResFileList=ALLList;
				ALLList=new ArrayList<String>();
				if(ResFileList!=null)
				{
					WriteMTKHeadRes(file_res);
					WriteHead(file_res,ResFileList);
					WriteBody(ResFileList,file_res,false);
					//WriteBlackRes(file_res);
					file_scr.close();
					console.Print("合并资源完毕"+ResFileList.toString());
					return true;
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "工程脚本文件为空,或工程位置设置错误", "错误", JOptionPane.INFORMATION_MESSAGE);
				ComBINsTATES=false;
			}

		}
	  ComBINsTATES=true;
	  return false;
	}
	public boolean getCombinStatus()
	{
		return ComBINsTATES;
	}
	
	
	private void GetAllResFile(String file_res) {
		// TODO Auto-generated method stub
		File file = new File(file_res);
		ArrayList<String> resList = new ArrayList<String>();
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                    	if(!file2.getName().equals(".settings"))
                    	{
                    		GetAllResFile(file2.getAbsolutePath());
                    	}
                    } else {
                    	if((file2.getName().indexOf(".lua",-4)==-1)  && (!file2.getName().equals(".buildpath")) && (!file2.getName().equals(".project")))
                    		ALLList.add(file2.getAbsolutePath());
                    }
                }
            }
        } else {
        }
	}

	private void WriteBlackSrc(WriteFile file_scr) {
		// TODO Auto-generated method stub
		long len=file_scr.getlen();
		byte[] ff={(byte) 0xff};
		int i=0;
		for(i=0;i<CUSTM_MAX-len;i++)
		{
			file_scr.write(ff);
		}
	}
	private void WriteBlackRes(WriteFile file_scr) {
		// TODO Auto-generated method stub
		long len=file_scr.getlen();
		int i=0;
		byte[] ff={(byte) 0xff};
		for(i=0;i<CUSTM_RES_MAX-len;i++)
		{
			file_scr.write(ff);
		}
	}

	private void WriteMTKHeadSrc(WriteFile file_scr) {
		// TODO Auto-generated method stub
		byte MtkSrcHead[]={0x4D,0x4D,0x4D,0x01,0x38,0x00,0x00,0x00,0x46,0x49,0x4c,0x45,0x5f,0x49,0x4e,0x46,
						   0x4f,0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x70,0x07,0x00,0x00,(byte)0xd0,0x31,0x10,
    	                   0x00,0x30,0x08,0x00,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x38,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
	                       0x00,0x00,0x00,0x00,0x03,0x00,0x00,0x00};
		file_scr.write(MtkSrcHead);
		
	}
	private void WriteMTKHeadRes(WriteFile file_res) {
		// TODO Auto-generated method stub
		byte MtkResHead[]={0x4D,0x4D,0x4D,0x01,0x38,0x00,0x00,0x00,0x46,0x49,0x4c,0x45,0x5f,0x49,0x4e,0x46,
						   0x4f,0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x70,0x07,0x00,0x00,(byte)0x90,0x30,0x10,
    	                   0x00,0x40,0x01,0x00,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0x38,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
	                       0x00,0x00,0x00,0x00,0x03,0x00,0x00,0x00};
		
		file_res.write(MtkResHead);
	}

	private void GetAllScrFile(String path) {
		File file = new File(path);
		ArrayList<String> SrcList = new ArrayList<String>();
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                    	if(!file2.getName().equals(".settings"))
                    		GetAllScrFile(file2.getAbsolutePath());
                    } else {
                    	if((file2.getName().indexOf(".lua",-4)!=-1) && (!file2.getName().equals(".buildpath")) && (!file2.getName().equals(".project")))
                    		ALLList.add(file2.getAbsolutePath());
                    }
                }
            }
        }
        else {
        }
		
	}
	public String Find_Scrip_Port(String ProPath)
	{
		
		return "host";
	}

}
