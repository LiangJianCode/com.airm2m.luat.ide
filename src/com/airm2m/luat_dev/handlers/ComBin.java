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
	ShowConsole console=new ShowConsole("combin");
	String work_space_path=null;
	String TempPath=null;
	String Plat_Type=null;
	File tempZipfile;
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
	                	if(!file2.getName().equals(".buildpath") && !file2.getName().equals(".project"))
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
	public void deleteFile(String sPath) {  
	    File file = new File(sPath);  
	    // 路径为文件且不为空则进行删除  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	    }  
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
		String path=work_space_path+"\\"+TempPath;
		String outputname;
		if(Plat_Type.equals("RDA"))
			outputname="Update_Air2xx";
		else
			outputname="Update_Air8xx";
		WriteFile file=new WriteFile(work_space_path+"\\"+outputname);
		GetALLFile(path);
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
	public  ComBin()
	{
		
	}
	public  void LodComBin(String platform,boolean hostcompress)
	{	
		getmsg();
		String path=work_space_path+"\\"+TempPath;
		console.Print("active_project:"+path);
		if(platform.equals("RDA"))
		{
			WriteFile file=new WriteFile(work_space_path+"\\RdaCombin.bin");
			GetALLFile(path);
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
		else if(platform.equals("MTK"))
		{
			WriteFile file_scr=new WriteFile(work_space_path+"\\CUSTOMER");
			GetAllScrFile(path);
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
				GetAllResFile(path);
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
				}
				
			}
			else
			{
				JOptionPane.showMessageDialog(null, "工程脚本文件为空,或工程位置设置错误", "错误", JOptionPane.INFORMATION_MESSAGE);
				ComBINsTATES=false;
			}

		}
	  ComBINsTATES=true;
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
