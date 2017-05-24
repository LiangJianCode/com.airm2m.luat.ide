package com.airm2m.luat_dev.handlers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.Platform;


import com.airm2m.serialException.NoSuchPort;
import com.airm2m.serialException.NotASerialPort;
import com.airm2m.serialException.PortInUse;
import com.airm2m.serialException.ReadDataFromSerialPortFailure;
import com.airm2m.serialException.SendDataToSerialPortFailure;
import com.airm2m.serialException.SerialPortInputStreamCloseFailure;
import com.airm2m.serialException.SerialPortOutputStreamCloseFailure;
import com.airm2m.serialException.SerialPortParameterFailure;
import com.airm2m.serialException.TooManyListeners;
import com.airm2m.serialPort.SerialTool;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class OriginalDownload {
	ShowConsole console=new ShowConsole("OriginalDownload");
	byte[] FLAG_WORD={(byte) 0xad};
	int temprcvchainingFlag=0;
	byte[] temprcvchaining=new byte[2280];
	byte[] all_dl_data=null;
	byte[] EVENT={(byte) 0xff};
	String LOG_type=null;
	byte[] TRACE={(byte) 0x80};
	byte[] CMD_RD_DWORD        		   			    = {0x02};
	byte[] CMD_ID             		   				= {(byte) 0xFF};
	byte[] CMD_RD_REG         		   				= {0x04};
	byte[] CMD_WR_DWORD       		   				= {(byte) 0x82};
	byte[] CMD_WR_REG          	       			    = {(byte) 0x84};
	byte[] CMD_WR_BLOCK	               			    = {(byte) 0x83};
	
	int CTRL_SET_REG						   		= 0x00000000;
	int CTRL_CLR_REG						   		= 0x00000001;
	int CTRL_CFG_REG						   		= 0x00000003;
	int CHIP_ID_REG						            = 0x01a24000;
	int RESET_CAUSE_REG 					   		= 0x01a000a0; 
	int HOST_MONITOR_CTRL 					   		= 0x81c000a0;
	int H2P_STATUS_REG 	                            = 0x00000005;
	int GALLITE_HALFIXED_ADDR 				   		= 0x81c0027c;
	
	int HOST_MAX_PACKET          	   				= 4096;
	int EVENT_FLASH_PROG_READY       			    = 0xF0;
	int TEMP_SCR_ADDER  							= 0xc0530c83;
	
	byte[] FPC_NONE 	   		    	   			= {0x00,0x00,0x00,0x00};
	byte[] FPC_PROGRAM 	        	   			    = {0x01,0x00,0x00,0x00};
	byte[] FPC_ERASE_SECTOR 	 		   			= {0x02,0x00,0x00,0x00};
	int FPC_END	    		 	   				    = 0x04;
	byte[] FPC_CHECK_FCS 	           				= {0x05,0x00,0x00,0x00};
	int FPC_RESTART                      			= 0x07;
	
	byte[] Transfer1 				   				={0x11};
	byte[] Transfer2 				   				={0x13};
	byte[] Transfer3 				   				={0x5c};
	static byte[] BACK_DATA=null;
	int section=32*1024;
	int FLASH_ERARE_SIZE=64*1024;
    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
	byte[] SYNC_BACK={0x11,0x09,0x09,0x10};
	SerialPort DownPort=null;
	byte read_id=0;
	int SCRIPT_DATA_BASE=0x002A0000;
	int SCRIPT_DATA_LEN=0;
	String comport;
	ComBin combin=null;
	static boolean DownlodState=false;
	private void exitForUser()
	{
		SerialTool.closePort(DownPort);
		console.Print("***********************正在取消下载(host)***************************");
	}
	public OriginalDownload()
	{

	}
	public void outCLOSE()
	{
		if(DownlodState)
		{
			SerialTool.closePort(DownPort);
			console.Print("***********************取消HOST下载***************************");
			DownlodState=false;
		}
	}
	private void handleOldPort()
	{
		log logs=new log();
		if(logs.getSerPort() )
		{
			console.Print("关闭trace的占用");
			logs.CancleLog();
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		console.Print("trace 没有打开");
	}
	public void runlod(String LodFile,String com)
	{
		String path = null;
		boolean lodResult=false;
		if(DownlodState)
		{
			DownlodState=false;            //检测到正在下载，取消下载
			exitForUser();
			return ;
		}
		DownlodState=true;
		comport=com;
		handleOldPort();
		console.Print("***********************开始下载(host)***************************");
		combin=new ComBin();
		combin.LodComBin("RDA",true);
		try {
			 path=Platform.asLocalURL(Platform.getBundle("com.airm2m.luat_dev").getEntry("")).getFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			lodResult=start(path+"\\ramrun\\flsh_spi32m_CUSTOMER_host_ramrun.lod",LodFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(lodResult)
		{
			console.Print("***********************下载结束(host)***************************");
			
			log logs=new log();
			logs.start();
		}
		else
			console.Print("***********************下载失败(host)***************************");
		DownlodState=false;
		return ;
	}
	private  void chip_xfbp()
	{
		write_value(CMD_WR_REG, CTRL_CFG_REG, new byte[] {(byte) 0x80});
		read_value(CMD_RD_REG, CTRL_SET_REG,0);
	    write_value(CMD_WR_REG, CTRL_SET_REG, new byte[] {(byte) 0x08});
	    write_value(CMD_WR_REG, CTRL_CFG_REG, new byte[] {(byte) 0x80});
	}
	private  boolean enter_host_mode()
	{
		try {
			write_value(CMD_WR_DWORD,0x01a000a0,  new byte[] {(byte)0x00,0x00,0x2a,0x00});
			Thread.sleep(200);
			write_value(CMD_WR_REG,0x00,  new byte[] {(byte)0x05});
			Thread.sleep(600);
			write_value(CMD_WR_REG,0x03,  new byte[] {(byte)0x80});
			Thread.sleep(100);
			write_value(CMD_WR_REG,0x01,  new byte[] {(byte)0x02});
			Thread.sleep(100);
			write_value(CMD_WR_REG,0x05,  new byte[] {(byte)0xfd});
			Thread.sleep(100);
			write_value(CMD_WR_DWORD,0x01a000a0,  new byte[] {(byte)0x00,0x00,0x2a,0x00});
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
        //self._cmdbuf_index = 0
	}
	private  boolean download_ramrun(List<byte[]> ramList)
	{
		console.Print("开始下载RAMRUN");
		if(ramList.size()!=4)
		{
			return false;
		}
		write_block(ramList.get(0),ramList.get(1));
		int packNum=((ramList.get(3).length)/HOST_MAX_PACKET);
		int adder=ByteArryToInt_Little(ramList.get(2),0,ramList.get(2).length-1);
		//console.Print("总长度："+ramList.get(3).length+"需要下载包总数:"+packNum+"基础地址是:"+adder);
		for(int i=0;i<packNum;i++)
		{
			write_block(IntToByte_Little(adder+i*HOST_MAX_PACKET),Arrays.copyOfRange(ramList.get(3), i*HOST_MAX_PACKET,i*HOST_MAX_PACKET+HOST_MAX_PACKET));
		}
		if(ramList.get(3).length-packNum*HOST_MAX_PACKET>0)
		{
			write_block(IntToByte_Little(adder+packNum*HOST_MAX_PACKET),Arrays.copyOfRange(ramList.get(3), packNum*HOST_MAX_PACKET,ramList.get(3).length));
		}
		write_value(CMD_WR_DWORD, HOST_MONITOR_CTRL, new byte[] {(byte)0xff,0x00,0x00,0x00});
        write_value(CMD_WR_REG, H2P_STATUS_REG, new byte[] {(byte)0xff});
        if(wait_event(0xff000004,7000) &&  wait_event(0x00000057,7000) && wait_event(EVENT_FLASH_PROG_READY,7000))
        	return true;
        else
        	return false;
	}
	private List<Integer> read_fpc_access()
	{
		List<Integer> access = new ArrayList<Integer>();
        //读取基地址
        int base_addr = read_value(CMD_RD_DWORD, GALLITE_HALFIXED_ADDR,2000);

        //读取FPC访问信息基地址
        long fpc_base_addr = read_value(CMD_RD_DWORD, base_addr+0xA4,2000); //FPC_ACCESS_OFFSET = 0xA4

        //access.add(fpc_base_addr);
        //console.Print("read_fpc_access1:"+base_addr);
        access.add((int)(fpc_base_addr+4));   //cmd缓冲区1
        access.add((int)(fpc_base_addr+24));  //cmd缓冲区2

        //读取FPC RAMBUF信息
        int rambuf_info = read_value(CMD_RD_DWORD, (int)(fpc_base_addr),2000);
        //console.Print("read_fpc_access2:"+rambuf_info);
        int addr = (int)(fpc_base_addr + 44);
        
        for(int i=0;i<3;i++) //ram缓冲区1~3
        {
        	int rambuf_addr = read_value(CMD_RD_DWORD, addr,2000);
        	//console.Print("read_fpc_access_rambuf:"+i+":"+rambuf_info);
        	access.add(rambuf_addr);
            addr = addr + 4;
        }
        int bufsize=read_value(CMD_RD_DWORD, addr,2000);
        //console.Print("read_fpc_bufsize:"+bufsize);
        access.add(bufsize);
		return access; 
	}
	private byte[] combinAndRead(String path)
	{
		byte[] downloadfile=null;
		ReadFile recv=new ReadFile(path);
		downloadfile=new byte[recv.len()];
		recv.read(downloadfile);
		console.Print("combinAndRead:"+path);	
		return downloadfile;
	}
	
	private byte[] packFpcCmd(int FPC_NONE,int addr,int fpc_rambuf,int size,int data)
	{
		ByteBuffer cellheadbuff=ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
		cellheadbuff.putInt(FPC_NONE).putInt(addr).putInt(fpc_rambuf).putInt(size).putInt(data);
		return cellheadbuff.array();
	}

	private byte[] fcsclr(byte[] data,int base_add)
	{
		FCS fcs= new  FCS();
		List<Integer> fcsbuf=fcs.getfcs(data, section);
		ByteBuffer cellheadbuff=ByteBuffer.allocate(fcsbuf.size()*12).order(ByteOrder.LITTLE_ENDIAN);
		for(int i=0;i<fcsbuf.size();i++)
		{
			int lens=section;
			if((i+1)==fcsbuf.size())
				lens=data.length-(i*section);
			cellheadbuff.putInt(base_add+(i*section)).putInt(lens).putInt(fcsbuf.get(i));
		}
		return cellheadbuff.array();
	}
	private void read_scr_are()
	{
		int base_addr = read_value(CMD_RD_DWORD, 0x88000004,2000);
		SCRIPT_DATA_BASE=base_addr;
		
		SCRIPT_DATA_LEN=read_value(CMD_RD_DWORD, 0x88000008,2000);
		console.Print("读取脚本下载区信息:起始地址"+base_addr+"区域大小:"+SCRIPT_DATA_LEN);	
	}

	
	private  boolean download_scr(String path)
	{
		int cmdbuf_index=0;
		int rambuf_index=0;
		console.Print("开始下载脚本");
		read_scr_are();
		List<Integer>  fpc_access = read_fpc_access();
		List<Integer>  fpc_access_cmd = new ArrayList<Integer>();
		fpc_access_cmd.add(fpc_access.get(0));
		fpc_access_cmd.add(fpc_access.get(1));
		List<Integer>  fpc_access_ram =new ArrayList<Integer>();
		fpc_access_ram.add(fpc_access.get(2));
		fpc_access_ram.add(fpc_access.get(3));
		fpc_access_ram.add(fpc_access.get(4));
		int bufsize=fpc_access.get(5);
		boolean wait=false;
		byte[] data=combinAndRead(path);
		if(data.length >SCRIPT_DATA_LEN)
		{
			combin.LodComBin("RDA",false);  //如果脚本文件大于脚本空间，那么就压缩
			data=combinAndRead(path);
			if(data.length >SCRIPT_DATA_LEN)
			{
				console.Print("脚本文件过大,请裁剪");	
				return false;
			}
		}
		HOST_MAX_PACKET=bufsize;
		int addr=SCRIPT_DATA_BASE;
		int esr_mor=0;
		byte[] fcsend=fcsclr(data,SCRIPT_DATA_BASE);
		int ersNum=(0x003FB000-(SCRIPT_DATA_BASE+SCRIPT_DATA_LEN))/FLASH_ERARE_SIZE;
		
		int esr_mor_user=0;
		if((0x003FB000-(SCRIPT_DATA_BASE+SCRIPT_DATA_LEN))%FLASH_ERARE_SIZE!=0)
			esr_mor_user=1;
		console.Print("开始擦除文件系统");
		for(int s=0;s<ersNum+esr_mor_user;s++)
		{
			int esr_size=FLASH_ERARE_SIZE;
			if((0x003FB000-(SCRIPT_DATA_BASE+SCRIPT_DATA_LEN)) -(s*FLASH_ERARE_SIZE)<FLASH_ERARE_SIZE)
				esr_size=(0x003FB000-(SCRIPT_DATA_BASE+SCRIPT_DATA_LEN)) -(s*FLASH_ERARE_SIZE);
			//console.Print("擦除文件:"+s+" 大小:"+esr_size);
	        write_block(IntToByte_Little(fpc_access_cmd.get(cmdbuf_index)), packFpcCmd(0,SCRIPT_DATA_BASE+SCRIPT_DATA_LEN,fpc_access_ram.get(rambuf_index%3),esr_size,0));        
	        write_value(CMD_WR_DWORD, fpc_access_cmd.get(cmdbuf_index), FPC_ERASE_SECTOR);
	        if(!wait_lod_event(EVENT_FLASH_PROG_READY+cmdbuf_index,7000))
	    		return false;
	        cmdbuf_index ^= 0x01;
		}
		console.Print("擦除文件系统完成");
		
		if((data.length)%FLASH_ERARE_SIZE!=0)
			esr_mor=1;
		for(int j=0;j<(data.length)/FLASH_ERARE_SIZE+esr_mor;j++)
		{
			int esr_Num = FLASH_ERARE_SIZE*j+FLASH_ERARE_SIZE;
			//console.Print("擦除flash:"+j+":地址:"+(addr+FLASH_ERARE_SIZE*j));
	        write_block(IntToByte_Little(fpc_access_cmd.get(cmdbuf_index)), packFpcCmd(0,SCRIPT_DATA_BASE+FLASH_ERARE_SIZE*j,fpc_access_ram.get(rambuf_index%3),FLASH_ERARE_SIZE,0));        
	        write_value(CMD_WR_DWORD, fpc_access_cmd.get(cmdbuf_index), FPC_ERASE_SECTOR);
	        int lod_mor=0;
        	if((data.length-(j*FLASH_ERARE_SIZE)>HOST_MAX_PACKET))
				lod_mor=1;
	        for(int i=j*2;i<j*2+1+lod_mor;i++)
	        {
	        	console.Print("发送下载脚本包:"+(i+1)+"/"+((data.length)/section+1));
	        	int write_num=HOST_MAX_PACKET;
				int lod_Num = HOST_MAX_PACKET*i+HOST_MAX_PACKET;
				if(lod_Num>data.length)
				{
					lod_Num=data.length;
					write_num=data.length-(i*HOST_MAX_PACKET);
				}
				write_block(IntToByte_Little(fpc_access_ram.get(rambuf_index%3)),Arrays.copyOfRange(data, i*HOST_MAX_PACKET,lod_Num));
		        if(!wait_lod_event(EVENT_FLASH_PROG_READY+cmdbuf_index,7000))
		            		return false;
		        cmdbuf_index ^= 0x01;
		        	
	            write_block(IntToByte_Little(fpc_access_cmd.get(cmdbuf_index)), packFpcCmd(0,addr,fpc_access_ram.get(rambuf_index%3),write_num,ByteArryToInt_Little(fcsend,i*12+8,i*12+11)));
	            write_value(CMD_WR_DWORD, fpc_access_cmd.get(cmdbuf_index), FPC_PROGRAM);
	           
	            rambuf_index += 1;
	            wait=true;
	            addr=addr+HOST_MAX_PACKET;
	    		if(DownlodState==false)
				{
					return false;
				}
	        }
            if(!wait_lod_event(EVENT_FLASH_PROG_READY+cmdbuf_index,7000))
        		return false;
            cmdbuf_index ^= 0x01;
		}
		console.Print("下载脚本数据完成");
		
		write_block(IntToByte_Little(fpc_access_ram.get(rambuf_index%3)), fcsend);
        write_block(IntToByte_Little(fpc_access_cmd.get(cmdbuf_index)), packFpcCmd(0,0,fpc_access_ram.get(rambuf_index%3),(fcsend.length)/12,0));
        write_value(CMD_WR_DWORD, fpc_access_cmd.get(cmdbuf_index), FPC_CHECK_FCS);
		
        cmdbuf_index ^= 0x01;
    	if(!wait_lod_event(EVENT_FLASH_PROG_READY+cmdbuf_index,7000))
    		return false;
    	console.Print("下载校验码完成");
        cmdbuf_index ^= 0x01;
        if(!wait_lod_event(EVENT_FLASH_PROG_READY+cmdbuf_index,7000))
    		return false;

        cmdbuf_index ^= 0x01;
        write_block(IntToByte_Little(fpc_access_cmd.get(cmdbuf_index)), packFpcCmd(FPC_END,0,0,0,0));
        if(!wait_lod_event(EVENT_FLASH_PROG_READY+cmdbuf_index,7000))
    		return false;
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        cmdbuf_index ^= 0x01;
        write_block(IntToByte_Little(fpc_access_cmd.get(cmdbuf_index)),packFpcCmd(FPC_RESTART,0,0,0,0));
        if(!wait_lod_event(EVENT_FLASH_PROG_READY+cmdbuf_index,7000))
    		return false;
        //self._delay(1000)
		return true;
	}
	private  boolean wait_event(int event,int timer)
	{
		long startSendTime = System.currentTimeMillis();
		//console.Print("等待事件");
		BACK_DATA=null;
		while(true)
		{
			if(BACK_DATA==null )
		    {
	   			if((System.currentTimeMillis()-startSendTime)>timer)
				   {
	   					console.Print("等待事件失败1");
	   					console.printHexString(IntToByte_Little(event));
	   					return false;
				   }
		    }
		    else
		    {
	        	if(ByteArryToInt_Little(BACK_DATA,1,BACK_DATA.length-1)==event)
	        	{
	        		return true;
	        	}
	   			if((System.currentTimeMillis()-startSendTime)>timer)
				   {
	   					console.Print("等待事件失败2,id="+ByteArryToInt_Little(BACK_DATA,1,BACK_DATA.length-1));
	   					console.printHexString(IntToByte_Little(event));
	   					return false;
				   }
		    }
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
	}
	private  boolean wait_lod_event(int event,int timer)
	{
		long startSendTime = System.currentTimeMillis();
		//console.Print("等待下载事件");
		while(true)
		{
			if(BACK_DATA==null )
		    {
	   			if((System.currentTimeMillis()-startSendTime)>timer)
				   {
	   					console.Print("等待事件失败1");
	   					console.printHexString(IntToByte_Little(event));
	   					return false;
				   }
		    }
		    else
		    {
	        	if(ByteArryToInt_Little(BACK_DATA,1,BACK_DATA.length-1)==event)
	        	{
	        		//console.Print("等待成功");
	        		return true;
	        	}
	   			if((System.currentTimeMillis()-startSendTime)>timer)
				   {
	   					console.Print("等待事件失败2,id="+ByteArryToInt_Little(BACK_DATA,1,BACK_DATA.length-1));
	   					console.printHexString(IntToByte_Little(event));
	   					return false;
				   }
		    }
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
	}
	private  byte[] IntToByte_Little(int data)
	{

	    byte[] a = new byte[4];
        a[0] = (byte) (0xff & data);
        a[1] = (byte) ((0xff00 & data) >> 8);
        a[2] = (byte) ((0xff0000 & data) >> 16);
        a[3] = (byte) ((0xff000000 & data) >> 24);
		return  a;
	}
	private  byte charToByte(char c) {   
		    return (byte) "0123456789ABCDEF".indexOf(c);   
		}  
	
	public  byte[] hexStringToBytes_Little(String hexString) {   
	    if (hexString == null || hexString.equals("")) {   
	        return null;   
	    }   
	    hexString = hexString.toUpperCase();   
	    int length = hexString.length() / 2;   
	    char[] hexChars = hexString.toCharArray();   
	    byte[] d = new byte[length];   
	    for (int i = 0; i < length; i++) {   
	        int pos = i * 2;   
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));   
	    }   
	    byte[] d1 = new byte[length];   
	    for(int i=0;i<d.length;i++)
	    {
	    	d1[i]=d[d.length-1-i];
	    }
	    return d1;   
	} 
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
                n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

	public static String byteArrayToHexString(byte[] b) {
	        StringBuffer resultSb = new StringBuffer();
	        for (int i = 0; i < b.length; i++) {
	                resultSb.append(byteToHexString(b[i]));
	        }
	        return resultSb.toString();
	}
	public String hexStringToBytes_Little_String(String hexString)
	{
		byte[] temparr=hexStringToBytes_Little(hexString);
		
		return byteArrayToHexString(temparr);
	}
	public  byte[] hexStringToBytes(String hexString) {   
	    if (hexString == null || hexString.equals("")) {   
	        return null;   
	    }   
	    hexString = hexString.toUpperCase();   
	    int length = hexString.length() / 2;   
	    char[] hexChars = hexString.toCharArray();   
	    byte[] d = new byte[length];   
	    for (int i = 0; i < length; i++) {   
	        int pos = i * 2;   
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));   
	    }   
	    return d;   
	} 
	private  int ByteArryToInt(byte[] buf,int start,int end)
	{
		int cc = 0;
		int i;
		for(i=0;i<end-start+1;i++)
		{
			cc=((buf[i+start]&0xff)<<(8*(end-start-i)))|cc;
		}
		return cc;
	}
	private  int ByteArryToInt_Little(byte[] buf,int start,int end)
	{
		int cc = 0;
		int i;
		for(i=0;i<end-start+1;i++)
		{
			cc=((buf[end-i]&0xff)<<(8*(end-start-i)))|cc;
		}
		return cc;
	}
	private  int calc_crc(byte[] data)
	{
		int i=0;
		int end=data[0];
		for(i=1;i<data.length;i++)
		{
			end=end ^ data[i];
		}
		return end;
	}
	
	private  byte[] TransferredMeaning(byte[] data)
	{
		int numTra=0;
		ByteBuffer pump_on_buf=null;
		for (int i = 0; i < data.length; i++) {
			byte[] tempArr={data[i]};
			if(Arrays.equals(tempArr,Transfer1) || Arrays.equals(tempArr,Transfer2) || Arrays.equals(tempArr,Transfer3))
			{
				numTra++;
			}
		}
		if(numTra==0)
			return data;
		else
		{
			pump_on_buf=ByteBuffer.allocate(data.length+numTra);
			for (int i = 0; i < data.length; i++) {
				byte[] tempArr={data[i]};
				byte[] Tra_Head={0x5c};
				if(Arrays.equals(tempArr,Transfer1))
				{
					byte[] Tra_End={(byte) 0xee};
					pump_on_buf.put(Tra_Head).put(Tra_End);	
				}
				else if(Arrays.equals(tempArr,Transfer2))
				{
					byte[] Tra_End={(byte) 0xec};
					pump_on_buf.put(Tra_Head).put(Tra_End);	
				}
				else if(Arrays.equals(tempArr,Transfer3))
				{
					byte[] Tra_End={(byte) 0xa3};
					pump_on_buf.put(Tra_Head).put(Tra_End);	
				}
				else
				{
					pump_on_buf.put(data[i]);
				}
			}
			return pump_on_buf.array();
		}
			
		
	}
	private  byte[] write_packet(byte[] id,byte[] data)
	{
		ByteBuffer pump_on_buf=ByteBuffer.allocate(data.length+5);
		byte crc=(byte) (id[0] ^ calc_crc(data));
		pump_on_buf.put((byte) 0xad).putShort((short) (data.length+1)).put(id).put(data).put(crc);
		return TransferredMeaning(pump_on_buf.array());
	}
	private boolean wait_read_back(int id,int timer)
	{
		long startSendTime = System.currentTimeMillis();
		//console.Print("等待回复");
		//System.out.println("等待回复");
		BACK_DATA=null;
		while(true)
		{
			if(BACK_DATA==null )
		    {
	   			if((System.currentTimeMillis()-startSendTime)>timer)
				   {
	   					return false;
				   } 	
		    }
		    else
		    {
	        	if(BACK_DATA[0]==id)
	        	{
	        		return true;
	        	}
	   			if((System.currentTimeMillis()-startSendTime)>timer)
				   {
	   					return false;
				   } 	
		    }
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
	}
	
	private  int read_value(byte[] cmd,int address,int timer)
	{
		byte[] adr= IntToByte_Little(address);
		byte flag=0;
		if(Arrays.equals(cmd,CMD_RD_DWORD))
		{
			flag=4;
		}
		else
		{
			flag=1;
		}
        if(read_id == 0xff)
            read_id = 1;
        else
            read_id += 1;
        
		ByteBuffer pump_on_buf=ByteBuffer.allocate(adr.length+cmd.length+1);
		pump_on_buf.put(cmd).put(adr).put(read_id );
		write(write_packet(CMD_ID,pump_on_buf.array()));
		if(timer>0)
		{
			if(wait_read_back(read_id,timer))
			{
					return ByteArryToInt_Little(BACK_DATA,1,BACK_DATA.length-1);
			}
		}

				
		return 0;
	}
	private  void write(byte[] data)
	{
		SendCmd(DownPort,data);
	}
	private void write_value(byte[] cmd,int address,byte[] value)
	{
		byte[] adr= IntToByte_Little(address);
		ByteBuffer pump_on_buf=ByteBuffer.allocate(adr.length+value.length+cmd.length);
		pump_on_buf.put(cmd).put(adr).put(value);
		write(write_packet(CMD_ID,pump_on_buf.array()));
	}
	private void write_block(byte[] address,byte[] value)
	{
		ByteBuffer pump_on_buf=ByteBuffer.allocate(address.length+value.length+1);
		pump_on_buf.put(CMD_WR_BLOCK).put(address).put(value);
		write(write_packet(CMD_ID,pump_on_buf.array()));
	}
	private boolean SEND_DL_SYNC()
	{
		console.Print("等待握手......................");
		//System.out.println("等待握手");
		int i=0;
		for(i=0;i<100;i++)
		{
			int value=read_value(CMD_RD_DWORD,0x01A24004,200);
			//read_value(CMD_RD_DWORD,0x01A24004);
			if(value==0x10090911 || value == 0x09120711 || value == 0xFFFFFFFF)
			{
				console.Print("握手成功");
				return true;
			}
			if(DownlodState==false)
			{
				return false;
			}
		}
		JOptionPane.showMessageDialog(null, "请确保设备处于唤醒状态(可以通过重新上电实现)", "错误", JOptionPane.INFORMATION_MESSAGE);
		return false;

	}
	public  boolean start(String RamrunpPath,String ScrpPt) throws IOException
	{
		
		DownPort=OpenDownLoadPort(comport);
		if(DownPort==null)
			return false;
		if(SEND_DL_SYNC())
		{
			chip_xfbp();
			if(enter_host_mode())
				{
					if(DownlodState==false)
					{
						return false;
					}
					List<byte[]> unpackRam=UnpackRamrun(RamrunpPath);
					if(download_ramrun(unpackRam))
					{
						console.Print("下载ramrun成功");
						if(download_scr(ScrpPt))
						{
							console.Print("脚本下载成功~~~~~~~~~~~~~~~~~");
							write_value(CMD_WR_REG, CTRL_SET_REG, new byte[] {(byte) 0x05});
							SerialTool.closePort(DownPort);
							return true;
						}
					}
					
				}
		}
		SerialTool.closePort(DownPort);
		
		return false;
	}

	private  List<byte[]> UnpackRamrun(String RamrunpPath) throws IOException
	{
		 List<byte[]> arrayList=new ArrayList<byte[]>(){ };
		 StringBuffer sb1data= new StringBuffer("");
		 FileReader reader = new FileReader(RamrunpPath);
		 BufferedReader br = new BufferedReader(reader);
		 String str = null;
		 while((str = br.readLine()) != null) {
			 if(str.substring(0,1).equals("@"))
			 {
				 if(sb1data.length()>0)
				 {
					 arrayList.add(hexStringToBytes(sb1data.toString()));
				 }
				 arrayList.add(hexStringToBytes_Little(str.substring(1,(str.length()))));
				 sb1data= new StringBuffer("");
			 }
			 else if(!str.substring(0,1).equals("#"))
			 { 
				 sb1data.append(hexStringToBytes_Little_String(str));
			 }
		     
		 }
		 arrayList.add(hexStringToBytes(sb1data.toString()));
		 
		 br.close();
		 reader.close();
		 return arrayList;
	}

	private int find0x00(byte[] data)
	{
		int i=0;
		for(i=0;i<data.length;i++)
		{
			if (data[i]==0)
			{
				return i;
			}
		}
		return data.length;
	}
	private  boolean config_ebc_ram()
	{
        int d =  read_value(CMD_RD_DWORD, RESET_CAUSE_REG,2000);
        int sw_boot_mode = (d & 0x0fffffff) << 22;
       /* if (sw_boot_mode & 1 ==0)
            raise FastpfError("Config Ebc Ram Error Boot Mode" + format(d, "08x"))
*/
        d = read_value(CMD_RD_DWORD, 0x81C00278,2000);
        if ((d < 0x81c00000 || d >= 0x81c10000) && (d < 0xa1c00000 || d >= 0xa1c10000))
        	 return false;
        
        int boot_sector_struct_addr = d;

        int ebc_tag = read_value(CMD_RD_DWORD,boot_sector_struct_addr + 4 ,2000);
        if (ebc_tag != 0xB0075EC7)
            return false;

        int ram_timing = read_value(CMD_RD_DWORD, boot_sector_struct_addr+8,2000);
        int ram_mode = read_value(CMD_RD_DWORD, boot_sector_struct_addr+12,2000);
        
        write_value(CMD_WR_DWORD, 0x01A0440C, IntToByte_Little(ram_timing));
        write_value(CMD_WR_DWORD, 0x01A04418, IntToByte_Little(ram_mode));
        return true;
	}
	
	private  boolean SendCmd(SerialPort port,byte[] sendData)
	{
		try {
			//console.printHexString(sendData);
			SerialTool.sendToPort(port,sendData);
		} catch (SendDataToSerialPortFailure e) {
			// TODO Auto-generated catch block
			console.Print("发送串口命令失败"+e.getMessage());
			SerialTool.closePort(DownPort);
			return false;
		} catch (SerialPortOutputStreamCloseFailure e) {
			// TODO Auto-generated catch block
			console.Print("发送串口命令失败"+e.getMessage());
			SerialTool.closePort(DownPort);
			return false;
		}
		return true;
	}	
	
	private void handleRcvByte(byte[] data)
	{
	 switch (temprcvchainingFlag) {
	 	case 0:
			if (Arrays.equals(data,FLAG_WORD))
			{
				System.arraycopy(data, 0, temprcvchaining, 0, 1);
				temprcvchainingFlag=1;
			}
			break;
        case 1: 
        case 2: 
        case 3: 
			System.arraycopy(data, 0, temprcvchaining, temprcvchainingFlag, 1);
			temprcvchainingFlag=temprcvchainingFlag+1;
        	break;
        default:
    		byte[] tempdatalenarry= {temprcvchaining[1],temprcvchaining[2]};    //获取数据长度
    		int datalen=ByteArryToInt(tempdatalenarry,0,1);
			if( temprcvchainingFlag<datalen+3)
			{
				System.arraycopy(data, 0, temprcvchaining, temprcvchainingFlag, 1);
				temprcvchainingFlag=temprcvchainingFlag+1;
			}
			else
			{
				System.arraycopy(data, 0, temprcvchaining, temprcvchainingFlag, 1);
				temprcvchainingFlag=0;
				all_dl_data=new byte[datalen+4];
				System.arraycopy(temprcvchaining, 0, all_dl_data, temprcvchainingFlag, datalen+4);

				byte[] ID={all_dl_data[3]};
				if (Arrays.equals(ID,EVENT))
				{
					
					BACK_DATA=new byte[all_dl_data.length-5];
					System.arraycopy(all_dl_data, 4, BACK_DATA, 0, all_dl_data.length-5);
					try {
						Thread.sleep(4);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}


			}
	 	}
	 }
	private class SerialListener implements SerialPortEventListener 
	{
		byte[] trem_head={0x5c};
		byte[] trem_end1={(byte) 0xee};
		byte[] trem_back1={(byte) 0x11};
		byte[] trem_end2={(byte) 0xec};
		byte[] trem_back2={(byte) 0x13};
		byte[] trem_end3={(byte) 0xa3};
		byte[] trem_back3={(byte) 0x5c};
		@Override
		public void serialEvent(SerialPortEvent serialPortEvent) {
			// TODO Auto-generated method stub
		    switch (serialPortEvent.getEventType()) {
            case SerialPortEvent.BI: // 10 通讯中断
            	//JOptionPane.showMessageDialog(null, "与串口设备通讯中断", "错误", JOptionPane.INFORMATION_MESSAGE);
            	//console.Print("与串口设备通讯中断");
            	//OpenDownLoadPort(RellPort);
            	break;
            case SerialPortEvent.OE: // 7 溢位（溢出）错误
            case SerialPortEvent.FE: // 9 帧错误
            case SerialPortEvent.PE: // 8 奇偶校验错误
            case SerialPortEvent.CD: // 6 载波检测
            case SerialPortEvent.CTS: // 3 清除待发送数据
            case SerialPortEvent.DSR: // 4 待发送数据准备好了
            case SerialPortEvent.RI: // 5 振铃指示
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
            	break;
            case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
            	//console.Print("发现数据");
				byte[] data = null;
				try {
					if (DownPort == null) {
						JOptionPane.showMessageDialog(null, "串口对象为空！监听失败！", "错误", JOptionPane.INFORMATION_MESSAGE);
						return ;
					}
					else {
						data = SerialTool.readFromPort(DownPort);	//读取数据，存入字节数组
						if (data == null || data.length < 1) {	//检查数据是否读取正确	
							//JOptionPane.showMessageDialog(null, "读取数据过程中未获取到有效数据！请检查设备或程序！", "错误", JOptionPane.INFORMATION_MESSAGE);
						}
						else {
							int i=0;
							byte[] ss=new byte[1];
							boolean trem_flag=false;
							for(i=0;i<data.length;i++)
							{
								System.arraycopy(data, i, ss, 0, 1);
								if(!trem_flag)
								{
									if(Arrays.equals(ss,trem_head))
									{
										trem_flag=true;
									}
									else
									{
										handleRcvByte(ss);
									}
								}
								else
								{
									if(Arrays.equals(ss,trem_end1))
									{
										handleRcvByte(trem_back1);
									}
									else if(Arrays.equals(ss,trem_end2))
									{
										handleRcvByte(trem_back2);
									}
									else if(Arrays.equals(ss,trem_end3))
									{
										handleRcvByte(trem_back3);
									}
									trem_flag=false;
								}
							}
							}
						
						}				
					}
				catch (ReadDataFromSerialPortFailure | SerialPortInputStreamCloseFailure e) {
					//JOptionPane.showMessageDialog(null, e, "错误", JOptionPane.INFORMATION_MESSAGE);
					
				}
				
		    }
		}
	}
	
	
	private SerialPort OpenDownLoadPort(String port)
	{ 	
		SerialPort tempPort = null;
    	try {
    		console.Print("尝试打开串口"+port);
    		//System.out.println("尝试打开串口");
			tempPort=SerialTool.openPortFlow(port);
		} catch (SerialPortParameterFailure e) {
			// TODO Auto-generated catch block
			console.Print("打开串口失败");
			JOptionPane.showMessageDialog(null, "打开串口失败", "错误", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		} catch (NotASerialPort e) {
			// TODO Auto-generated catch block
			console.Print("NotASerialPort");
			JOptionPane.showMessageDialog(null, "不是串口设备", "错误", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		} catch (NoSuchPort e) {
			// TODO Auto-generated catch block
			console.Print("没有这样的串口");
			JOptionPane.showMessageDialog(null, "没有这样的串口", "错误", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		} catch (PortInUse e) {
			// TODO Auto-generated catch block
			console.Print("串口在使用中");
			JOptionPane.showMessageDialog(null, "串口正在使用中", "错误", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		}
    	//System.out.println("打开串口成功");
    	console.Print("打开串口成功");
    	try {
    		console.Print("添加对串口"+tempPort+"监听");
			SerialTool.addListener(tempPort, new SerialListener());
			//System.out.println("添加对串口"+tempPort+"监听");
		} catch (TooManyListeners e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempPort;	
	}
}
