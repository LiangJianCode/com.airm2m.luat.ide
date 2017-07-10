package com.airm2m.luat_dev.handlers;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

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


public class DownLoad extends Thread{
	ShowConsole console=new ShowConsole("download");
	ArrayList<String> commlist;
	static SerialPort DownPort=null;
	ArrayList<SerialPort>  portlist=new ArrayList<SerialPort>();;
	String DownFilePath="e:\\addfile.bin";
	String PlatForm=null;
	byte RDA_LOD=-1;
	byte MTK_LOD_1=1;
	byte MTK_LOD_2=0;
	int DL_STEP=0;
	int ALL_DL_num=0;
	int serial_number=0;
	byte[] temprcvchaining=new byte[128];
	int temprcvchainingFlag=0;
	int temprcvchainingFlagWUSHOU=0;
	boolean syncEnd=false;
	boolean recvEnd=true;
	byte[] downloadfile;
	byte[] SYNC_WORD={(byte) 0XB5,(byte) 0XB5};
	byte[] SYNC_WORD_BACK={(byte) 0X5B};
	byte[] SYNC_WORD_BACK_RDA={(byte) 0X5B};
	byte[] SYNC_WORD_BACK_MTK={(byte) 0XB5};
	byte[] FLAG_WORD={(byte) 0XAA};
	int MTU=0;
	long chainingStartTime;
	short CMD_DL_BEGIN=1;
	short CMD_DL_BEGIN_RSP=2;
	short CMD_DL_DATA=3;
	short CMD_DL_DATA_RSP=4;
	short CMD_DL_END=5;
	short CMD_DL_END_RSP=6;
	byte[] all_dl_data=null;
	int SYNC_WORD_NUM=0;
	static boolean DownlodState=false;
	static List<byte[]>  data_buf = new ArrayList<byte[]>();
	public DownLoad()
	{
	}
	public boolean downlodFile(String downPath,byte lod_type)
	{
		console.Print("启动下载文件"+downPath);
		ReadFile recv=new ReadFile(downPath);
		downloadfile=new byte[recv.len()];
		recv.read(downloadfile);
		console.Print("读下载文件完成"+downloadfile.length);
		DL_STEP=1;
		console.Print("对模块发起开始下载命令");	
		if(SEND_DL_BEGIN(lod_type))															//发送开始命令
		{	
			DL_STEP=2;
			int i=0;
			ALL_DL_num=GetDLNums();
			for(i=0;i<ALL_DL_num;i++)
			{
				int SendLen=MTU;
				recvEnd=true;
				all_dl_data=null;
				if((downloadfile.length-i*MTU)<MTU)
					 SendLen=downloadfile.length-i*MTU;
				byte[] tempSendData1=new byte[SendLen];
				System.arraycopy(downloadfile, i*MTU, tempSendData1, 0, SendLen);
				if(SEND_DL_DATA(tempSendData1))										//发送脚本
					tempSendData1=null;
				else
					return false;
			}
			recvEnd=true;
			all_dl_data=null;
			DL_STEP=3;
			if(SEND_DL_END())	
			{
				ALL_DL_num=0;
				all_dl_data=null;
				serial_number=0;
				return true;
			}
			else
			{
				return false;
			}
					
		}
		else
		{
			return false;
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
				this.sleep(600);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		console.Print("trace 没有打开");
	}
	private void exitForUser()
	{
		SerialTool.closePort(DownPort);
		console.Print("***********************取消下载uart***************************");
	}
	
	public void run()
	{
		String workPath=Platform.getInstanceLocation().getURL().getPath();
		Properties prop = new Properties(); 
		InputStream in = null;
		try {
			 in= new BufferedInputStream (new FileInputStream(workPath+"\\luat.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "没有或读取配置文件出错", "错误", JOptionPane.INFORMATION_MESSAGE);
			DownlodState=false;
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
		String Port_Type=prop.getProperty("Port_Type");
		String coms=prop.getProperty("Debug_port");
		
		if(Port_Type == null)
		{
			JOptionPane.showMessageDialog(null, "没有或读取配置文件出错", "错误", JOptionPane.INFORMATION_MESSAGE);
			return ;
		}
		
		if(Port_Type.equals("host"))                                              //进入host下载
		{
			if(DownlodState)
			{
				DownlodState=false;
				SerialTool.closePort(DownPort);
				console.Print("***********************关闭uart下载***************************");
			}
			OriginalDownload hostLoad=new OriginalDownload();
			hostLoad.runlod(workPath+"\\RdaCombin.bin",coms);
		}
		else  
		{
			
			if(DownlodState)
			{
				DownlodState=false;            //检测到正在下载，取消下载
				exitForUser();
				return ;
			}
			OriginalDownload hostLoad=new OriginalDownload();
			hostLoad.outCLOSE();
			DownlodState=true;
			if(coms== null || coms.equals(""))
			{
				 JOptionPane.showMessageDialog(null, "没有设置下载口", "错误", JOptionPane.INFORMATION_MESSAGE);
				DownlodState=false;
				return ;
			}
			console.Print("***********************开始下载(uart)***************************");
			handleOldPort();
			DownPort=OpenDownLoadPort(coms);
			if(DownPort!=null)
			{
				if(SEND_DL_SYNC())      									 					//发送握手命令
				{		
					ComBin combin=new ComBin();
					
					if(!combin.LodComBin(PlatForm,false))
					{
						SerialTool.closePort(DownPort);
						console.Print("***********************下载结束（合并失败）***************************");
						DownlodState=false;
						return ;
					}
					if(PlatForm.equals("RDA"))
					{
						console.Print("开始下载文件"+workPath+"\\RdaCombin.bin");
						if(! downlodFile(workPath+"\\RdaCombin.bin",RDA_LOD))
							{
								SerialTool.closePort(DownPort);
								DownlodState=false;
								console.Print("***********************下载失败(uart)***************************");
								return ;
							}
					}
					else if(PlatForm.equals("MTK"))
					{
						if(downlodFile(workPath+"\\CUSTOMER",MTK_LOD_1))
							{
								if(!downlodFile(workPath+"\\CUSTOMER_RES",MTK_LOD_2))
								{
									SerialTool.closePort(DownPort);
									DownlodState=false;
									console.Print("***********************下载失败(uart)***************************");
									return;
								}
							}
						else
						{
							SerialTool.closePort(DownPort);
							DownlodState=false;
							console.Print("***********************下载失败(uart)***************************");
						}
						
					}
					
					DownlodState=false;
					console.Print("***********************下载结束(uart)***************************");
					SerialTool.closePort(DownPort);
					log logs=new log();
					logs.start();
				}
				else
				{
					SerialTool.closePort(DownPort);
					DownlodState=false;
					console.Print("***********************下载失败(uart)***************************");
				}
				
			}
			

		}

					
	}
	public SerialPort getSerPort()
	{
		return DownPort;
	}
	private int GetDLNums()
	{
		int cc=downloadfile.length/MTU;
		if(downloadfile.length%MTU!=0)
		{
			cc=cc+1;
		}
		return cc;
	}
	private void handleRcvByte(byte[] data)
	{
		if(DL_STEP==0)
		{
			 switch (temprcvchainingFlagWUSHOU) {
				 case 0:
					 if(Arrays.equals(data,SYNC_WORD_BACK))	
						 temprcvchainingFlagWUSHOU++;
					 break;
				 case 1:
					 if(Arrays.equals(data,SYNC_WORD_BACK_RDA))	
						 PlatForm="RDA";                           
					 else if(Arrays.equals(data,SYNC_WORD_BACK_MTK))
						 PlatForm="MTK";
					 else
						 temprcvchainingFlagWUSHOU=0;
			 }
		}
		else
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
            case 4: 
            case 5: 
				System.arraycopy(data, 0, temprcvchaining, temprcvchainingFlag, 1);
				temprcvchainingFlag=temprcvchainingFlag+1;
            	break;
            default:
        		byte[] tempdatalenarry= {temprcvchaining[3],temprcvchaining[4]};    //获取数据长度
        		int datalen=ByteArryToInt(tempdatalenarry);
				if( temprcvchainingFlag<datalen+6)
				{
					System.arraycopy(data, 0, temprcvchaining, temprcvchainingFlag, 1);
					temprcvchainingFlag=temprcvchainingFlag+1;
				}
				else
				{
					System.arraycopy(data, 0, temprcvchaining, temprcvchainingFlag, 1);
					temprcvchainingFlag=temprcvchainingFlag+1;
					temprcvchainingFlag=0;
					all_dl_data=new byte[datalen+7];
					System.arraycopy(temprcvchaining, 0, all_dl_data, temprcvchainingFlag, datalen+7);
					//console.Print("收到数据:");
					//console.printHexString(all_dl_data);
				}
		 }
		}

	}
	/*内部类*/
	private class SerialListener implements SerialPortEventListener 
	{
		@Override
		public void serialEvent(SerialPortEvent serialPortEvent) {
		    switch (serialPortEvent.getEventType()) {
            case SerialPortEvent.BI: // 10 通讯中断
            	break;
            case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
            	//console.Print("发现数据");
				byte[] data = null;
				try {
					if (DownPort == null) {
						JOptionPane.showMessageDialog(null, "串口对象为空！监听失败！", "错误", JOptionPane.INFORMATION_MESSAGE);
						DownlodState=false;
						return ;
					}
					else {
						data = SerialTool.readFromPort(DownPort);	//读取数据，存入字节数组
						if (data == null || data.length < 1) {	//检查数据是否读取正确	
							//JOptionPane.showMessageDialog(null, "读取数据过程中未获取到有效数据！请检查设备或程序！", "错误", JOptionPane.INFORMATION_MESSAGE);
						}
						else {
							 //ListenerRcvHandle(data);
							int i=0;
							byte[] ss=new byte[1];
							for(i=0;i<data.length;i++)
							{
								System.arraycopy(data, i, ss, 0, 1);
								handleRcvByte(ss);
							}
							}
						}				
					}
				catch (ReadDataFromSerialPortFailure | SerialPortInputStreamCloseFailure e) {
				}	
				
		    }
		}
	}
	private boolean CheckBackDataCp(byte[] data)
	{
		if((data!=null)&&(data.length>=7))
		{
			byte[] tempflag={data[0]};   //获取标志位
			if (Arrays.equals(tempflag, FLAG_WORD))
			{
				byte[] tempdatalenarry= {data[3],data[4]};    //获取数据长度
				if(ByteArryToInt(tempdatalenarry) == (data.length-7))
				{
					return true;
				}
				else
				{
					console.Print("数据长度字段不对:字段长度为="+ByteArryToInt(tempdatalenarry)+"实际长度为="+(data.length-7));
					return false;
				}
			}
			else
			{
				console.Print("起始标志位字段不对,返回头为:"+tempflag);
				return false;
			}
				
		}
		else
		{
			console.Print("收到数据长度不对");
			return false;
		}
			
	}
	private boolean CheckBackData(byte[] data)
	{
		if((data!=null)&&(data.length>=7))
		{
			byte[] tempflag={data[0]};   //获取标志位
			if (Arrays.equals(tempflag, FLAG_WORD))
			{
				byte[] tempdatalenarry= {data[3],data[4]};    //获取数据长度
				if(ByteArryToInt(tempdatalenarry) == (data.length-7))
				{
					byte[] tempdatacrcarry={data[data.length-2],data[data.length-1]}; //获取crc
					if (Arrays.equals(tempdatacrcarry,Crc16.calcCrc16(data, 1,data.length-3)))
					{
						console.Print("数据校验正确");
						return true;
					}
					else
					{
						console.Print("CRC校验字段不对,返回为:");
						console.printHexString(tempdatacrcarry);
						console.Print("应该为:");
						console.printHexString(Crc16.calcCrc16(data, 1,data.length-3));
					}
				}
				else
				{
					console.Print("数据长度字段不对:字段长度为="+ByteArryToInt(tempdatalenarry)+"实际长度为="+(data.length-7));
				}
			}
			else
			{
				console.Print("起始标志位字段不对,返回头为:"+tempflag);
				return false;
			}
				
		}
		else
		{
			console.Print("收到数据长度不对");
			return false;
		}
		return false;
			
	}
	private byte[] IntToByte(int data)
	{
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		try {
			doutput.writeInt(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return  boutput.toByteArray();
	}
	private int ByteArryToInt(byte[] buf)
	{
		int cc = 0;
		int i;
		for(i=0;i<buf.length;i++)
		{
			cc=((buf[i]&0xff)<<(8*(buf.length-1-i)))|cc;
		}
		return cc;
	}
	private byte[] CreatSendBuff(short cmd,byte[] data)
	{
		int dataLen=0;
		if (data!=null)
		{
			dataLen=data.length;
		}
		ByteBuffer temp_buf=ByteBuffer.allocate(7+dataLen).order(ByteOrder.BIG_ENDIAN);
		temp_buf.put((byte) 0xAA).putShort(cmd);
		temp_buf.putShort((short) dataLen);
		if(data!=null)
		{
			temp_buf.put(data);
		}
		byte[] getcrc=Crc16.calcCrc16(temp_buf.array(), 1,4+dataLen);
		//console.printHexString(getcrc);
		temp_buf.put(getcrc);
		//console.Print("需要发送的文件长度"+data.length);
		//console.printHexString(temp_buf.array());
		return temp_buf.array();
	}
			
	private boolean SEND_DL_SYNC()
	{
		long startSendTime = System.currentTimeMillis();
		console.Print("等待上电握手......................");
		while(true)
		{
			if(PlatForm==null )
		    {
	   			if((System.currentTimeMillis()-startSendTime)>40000)
				   {
	   					JOptionPane.showMessageDialog(null, "没有检测到握手回复,请确认有没有重新上电", "错误", JOptionPane.INFORMATION_MESSAGE);
	   					return false;
				   }
	   			else if((!DownlodState))
	   			{
	   				return false;
	   			}
	   			else
	   			{
	   				if(!SendCmd(DownPort,SYNC_WORD))
	   					return false;
	   			}    	
		    }
		    else
		    {
		        console.Print("收到握手信息,模块平台为"+PlatForm);
		        return true;
		    }
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
		
	}
	private boolean SEND_DL_BEGIN(byte lod_type)
	{
		console.Print("发送开始包");
		byte[] temp_bigin;
		if(lod_type>=0)
		{
			temp_bigin=new byte[5];
			System.arraycopy(IntToByte(downloadfile.length), 0,temp_bigin, 0,  4);
			byte[] ss={lod_type};
			System.arraycopy(ss, 0,temp_bigin, 4, 1);
		}
		else
		{
			temp_bigin=IntToByte(downloadfile.length);
		}
		
		byte[] SendData=CreatSendBuff(CMD_DL_BEGIN,temp_bigin);
		long startSendTime = System.currentTimeMillis();
		if(!SendCmd(DownPort,SendData))
				return false;
		while(true)
		{
			if(all_dl_data!=null)
			{
				MTU=(all_dl_data[7]<<8)|(all_dl_data[8]&0xff)-11;
				console.Print("mtu="+MTU);
				return true;
			}
			else if((System.currentTimeMillis()-startSendTime)>10000)
			{
				console.Print("BEGIN包没有回复");
				JOptionPane.showMessageDialog(null, "BEGIN包没有回复", "错误", JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
   			else if((!DownlodState))
   			{
   				exitForUser();
   				return false;
   			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
	}
	private boolean SEND_DL_DATA(byte[] data)
	{
		long startSendTime = System.currentTimeMillis();
		console.Print("发送下载包"+(serial_number+1)+"/"+ALL_DL_num);
		ByteBuffer downbuf=ByteBuffer.allocate(data.length+4).order(ByteOrder.BIG_ENDIAN);
		downbuf.putInt(serial_number).put(data);
		byte[] SendData=CreatSendBuff(CMD_DL_DATA,downbuf.array());
		if(!SendCmd(DownPort,SendData))
				return false;
		while(true)
		{
			if(all_dl_data!=null)
			{
				byte[] ss={all_dl_data[7],all_dl_data[8],all_dl_data[9],all_dl_data[10]};
				serial_number=ByteArryToInt(ss);
				//console.Print("下个发送序列包"+serial_number);
				return true;
			}
			else if((System.currentTimeMillis()-startSendTime)>6000)
			{
				console.Print("下载包没有回复");
				//SEND_DL_DATA(data);
				JOptionPane.showMessageDialog(null, "下载包没有回复", "错误", JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
   			else if((!DownlodState))
   			{
   				exitForUser();
   				return false;
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
	private boolean SEND_DL_END()
	{
		console.Print("发送结束命令");
		long startSendTime = System.currentTimeMillis();
		byte[] SendData=CreatSendBuff(CMD_DL_END,null);
		if(!SendCmd(DownPort,SendData))
				return false;
		while(true)
		{
			if(all_dl_data!=null)
			{
				return true;
			}
			else if((System.currentTimeMillis()-startSendTime)>25000)
			{
				console.Print("结束包没有回复");
				JOptionPane.showMessageDialog(null, "结束包没有回复", "错误", JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
   			else if((!DownlodState))
   			{
   				exitForUser();
   				return false;
   			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	private boolean SendCmd(SerialPort port,byte[] sendData)
	{
		try {
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
	
	private SerialPort OpenDownLoadPort(String port)
	{
		SerialPort tempPort = null;
    	try {
    		console.Print("尝试打开串口"+port);
			tempPort=SerialTool.openPort(port, 115200);
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
			console.Print("***********************下载失败***************************");
			DownlodState=false;    
			return null;
		}
    	console.Print("打开串口成功");

    	try {
    		console.Print("添加对串口"+tempPort+"监听");
			SerialTool.addListener(tempPort, new SerialListener());
		} catch (TooManyListeners e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempPort;	
	}
}
