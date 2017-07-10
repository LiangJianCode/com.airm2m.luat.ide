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
		console.Print("���������ļ�"+downPath);
		ReadFile recv=new ReadFile(downPath);
		downloadfile=new byte[recv.len()];
		recv.read(downloadfile);
		console.Print("�������ļ����"+downloadfile.length);
		DL_STEP=1;
		console.Print("��ģ�鷢��ʼ��������");	
		if(SEND_DL_BEGIN(lod_type))															//���Ϳ�ʼ����
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
				if(SEND_DL_DATA(tempSendData1))										//���ͽű�
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
			console.Print("�ر�trace��ռ��");
			logs.CancleLog();
			try {
				this.sleep(600);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		console.Print("trace û�д�");
	}
	private void exitForUser()
	{
		SerialTool.closePort(DownPort);
		console.Print("***********************ȡ������uart***************************");
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
			JOptionPane.showMessageDialog(null, "û�л��ȡ�����ļ�����", "����", JOptionPane.INFORMATION_MESSAGE);
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
			JOptionPane.showMessageDialog(null, "û�л��ȡ�����ļ�����", "����", JOptionPane.INFORMATION_MESSAGE);
			return ;
		}
		
		if(Port_Type.equals("host"))                                              //����host����
		{
			if(DownlodState)
			{
				DownlodState=false;
				SerialTool.closePort(DownPort);
				console.Print("***********************�ر�uart����***************************");
			}
			OriginalDownload hostLoad=new OriginalDownload();
			hostLoad.runlod(workPath+"\\RdaCombin.bin",coms);
		}
		else  
		{
			
			if(DownlodState)
			{
				DownlodState=false;            //��⵽�������أ�ȡ������
				exitForUser();
				return ;
			}
			OriginalDownload hostLoad=new OriginalDownload();
			hostLoad.outCLOSE();
			DownlodState=true;
			if(coms== null || coms.equals(""))
			{
				 JOptionPane.showMessageDialog(null, "û���������ؿ�", "����", JOptionPane.INFORMATION_MESSAGE);
				DownlodState=false;
				return ;
			}
			console.Print("***********************��ʼ����(uart)***************************");
			handleOldPort();
			DownPort=OpenDownLoadPort(coms);
			if(DownPort!=null)
			{
				if(SEND_DL_SYNC())      									 					//������������
				{		
					ComBin combin=new ComBin();
					
					if(!combin.LodComBin(PlatForm,false))
					{
						SerialTool.closePort(DownPort);
						console.Print("***********************���ؽ������ϲ�ʧ�ܣ�***************************");
						DownlodState=false;
						return ;
					}
					if(PlatForm.equals("RDA"))
					{
						console.Print("��ʼ�����ļ�"+workPath+"\\RdaCombin.bin");
						if(! downlodFile(workPath+"\\RdaCombin.bin",RDA_LOD))
							{
								SerialTool.closePort(DownPort);
								DownlodState=false;
								console.Print("***********************����ʧ��(uart)***************************");
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
									console.Print("***********************����ʧ��(uart)***************************");
									return;
								}
							}
						else
						{
							SerialTool.closePort(DownPort);
							DownlodState=false;
							console.Print("***********************����ʧ��(uart)***************************");
						}
						
					}
					
					DownlodState=false;
					console.Print("***********************���ؽ���(uart)***************************");
					SerialTool.closePort(DownPort);
					log logs=new log();
					logs.start();
				}
				else
				{
					SerialTool.closePort(DownPort);
					DownlodState=false;
					console.Print("***********************����ʧ��(uart)***************************");
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
        		byte[] tempdatalenarry= {temprcvchaining[3],temprcvchaining[4]};    //��ȡ���ݳ���
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
					//console.Print("�յ�����:");
					//console.printHexString(all_dl_data);
				}
		 }
		}

	}
	/*�ڲ���*/
	private class SerialListener implements SerialPortEventListener 
	{
		@Override
		public void serialEvent(SerialPortEvent serialPortEvent) {
		    switch (serialPortEvent.getEventType()) {
            case SerialPortEvent.BI: // 10 ͨѶ�ж�
            	break;
            case SerialPortEvent.DATA_AVAILABLE: // 1 ���ڴ��ڿ�������
            	//console.Print("��������");
				byte[] data = null;
				try {
					if (DownPort == null) {
						JOptionPane.showMessageDialog(null, "���ڶ���Ϊ�գ�����ʧ�ܣ�", "����", JOptionPane.INFORMATION_MESSAGE);
						DownlodState=false;
						return ;
					}
					else {
						data = SerialTool.readFromPort(DownPort);	//��ȡ���ݣ������ֽ�����
						if (data == null || data.length < 1) {	//��������Ƿ��ȡ��ȷ	
							//JOptionPane.showMessageDialog(null, "��ȡ���ݹ�����δ��ȡ����Ч���ݣ������豸�����", "����", JOptionPane.INFORMATION_MESSAGE);
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
			byte[] tempflag={data[0]};   //��ȡ��־λ
			if (Arrays.equals(tempflag, FLAG_WORD))
			{
				byte[] tempdatalenarry= {data[3],data[4]};    //��ȡ���ݳ���
				if(ByteArryToInt(tempdatalenarry) == (data.length-7))
				{
					return true;
				}
				else
				{
					console.Print("���ݳ����ֶβ���:�ֶγ���Ϊ="+ByteArryToInt(tempdatalenarry)+"ʵ�ʳ���Ϊ="+(data.length-7));
					return false;
				}
			}
			else
			{
				console.Print("��ʼ��־λ�ֶβ���,����ͷΪ:"+tempflag);
				return false;
			}
				
		}
		else
		{
			console.Print("�յ����ݳ��Ȳ���");
			return false;
		}
			
	}
	private boolean CheckBackData(byte[] data)
	{
		if((data!=null)&&(data.length>=7))
		{
			byte[] tempflag={data[0]};   //��ȡ��־λ
			if (Arrays.equals(tempflag, FLAG_WORD))
			{
				byte[] tempdatalenarry= {data[3],data[4]};    //��ȡ���ݳ���
				if(ByteArryToInt(tempdatalenarry) == (data.length-7))
				{
					byte[] tempdatacrcarry={data[data.length-2],data[data.length-1]}; //��ȡcrc
					if (Arrays.equals(tempdatacrcarry,Crc16.calcCrc16(data, 1,data.length-3)))
					{
						console.Print("����У����ȷ");
						return true;
					}
					else
					{
						console.Print("CRCУ���ֶβ���,����Ϊ:");
						console.printHexString(tempdatacrcarry);
						console.Print("Ӧ��Ϊ:");
						console.printHexString(Crc16.calcCrc16(data, 1,data.length-3));
					}
				}
				else
				{
					console.Print("���ݳ����ֶβ���:�ֶγ���Ϊ="+ByteArryToInt(tempdatalenarry)+"ʵ�ʳ���Ϊ="+(data.length-7));
				}
			}
			else
			{
				console.Print("��ʼ��־λ�ֶβ���,����ͷΪ:"+tempflag);
				return false;
			}
				
		}
		else
		{
			console.Print("�յ����ݳ��Ȳ���");
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
		//console.Print("��Ҫ���͵��ļ�����"+data.length);
		//console.printHexString(temp_buf.array());
		return temp_buf.array();
	}
			
	private boolean SEND_DL_SYNC()
	{
		long startSendTime = System.currentTimeMillis();
		console.Print("�ȴ��ϵ�����......................");
		while(true)
		{
			if(PlatForm==null )
		    {
	   			if((System.currentTimeMillis()-startSendTime)>40000)
				   {
	   					JOptionPane.showMessageDialog(null, "û�м�⵽���ֻظ�,��ȷ����û�������ϵ�", "����", JOptionPane.INFORMATION_MESSAGE);
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
		        console.Print("�յ�������Ϣ,ģ��ƽ̨Ϊ"+PlatForm);
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
		console.Print("���Ϳ�ʼ��");
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
				console.Print("BEGIN��û�лظ�");
				JOptionPane.showMessageDialog(null, "BEGIN��û�лظ�", "����", JOptionPane.INFORMATION_MESSAGE);
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
		console.Print("�������ذ�"+(serial_number+1)+"/"+ALL_DL_num);
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
				//console.Print("�¸��������а�"+serial_number);
				return true;
			}
			else if((System.currentTimeMillis()-startSendTime)>6000)
			{
				console.Print("���ذ�û�лظ�");
				//SEND_DL_DATA(data);
				JOptionPane.showMessageDialog(null, "���ذ�û�лظ�", "����", JOptionPane.INFORMATION_MESSAGE);
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
		console.Print("���ͽ�������");
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
				console.Print("������û�лظ�");
				JOptionPane.showMessageDialog(null, "������û�лظ�", "����", JOptionPane.INFORMATION_MESSAGE);
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
			console.Print("���ʹ�������ʧ��"+e.getMessage());
			SerialTool.closePort(DownPort);
			return false;
		} catch (SerialPortOutputStreamCloseFailure e) {
			// TODO Auto-generated catch block
			console.Print("���ʹ�������ʧ��"+e.getMessage());
			SerialTool.closePort(DownPort);
			return false;
		}
		return true;
	}
	
	private SerialPort OpenDownLoadPort(String port)
	{
		SerialPort tempPort = null;
    	try {
    		console.Print("���Դ򿪴���"+port);
			tempPort=SerialTool.openPort(port, 115200);
		} catch (SerialPortParameterFailure e) {
			// TODO Auto-generated catch block
			console.Print("�򿪴���ʧ��");
			JOptionPane.showMessageDialog(null, "�򿪴���ʧ��", "����", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		} catch (NotASerialPort e) {
			// TODO Auto-generated catch block
			console.Print("NotASerialPort");
			JOptionPane.showMessageDialog(null, "���Ǵ����豸", "����", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		} catch (NoSuchPort e) {
			// TODO Auto-generated catch block
			console.Print("û�������Ĵ���");
			JOptionPane.showMessageDialog(null, "û�������Ĵ���", "����", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		} catch (PortInUse e) {
			// TODO Auto-generated catch block
			console.Print("������ʹ����");
			JOptionPane.showMessageDialog(null, "��������ʹ����", "����", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			console.Print("***********************����ʧ��***************************");
			DownlodState=false;    
			return null;
		}
    	console.Print("�򿪴��ڳɹ�");

    	try {
    		console.Print("��ӶԴ���"+tempPort+"����");
			SerialTool.addListener(tempPort, new SerialListener());
		} catch (TooManyListeners e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempPort;	
	}
}
