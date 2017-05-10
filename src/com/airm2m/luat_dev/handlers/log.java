package com.airm2m.luat_dev.handlers;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.Platform;

import com.airm2m.serialException.NoSuchPort;
import com.airm2m.serialException.NotASerialPort;
import com.airm2m.serialException.PortInUse;
import com.airm2m.serialException.ReadDataFromSerialPortFailure;
import com.airm2m.serialException.SerialPortInputStreamCloseFailure;
import com.airm2m.serialException.SerialPortParameterFailure;
import com.airm2m.serialException.TooManyListeners;
import com.airm2m.serialPort.SerialTool;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class log extends Thread{
	byte[] FLAG_WORD={(byte) 0xad};
	int temprcvchainingFlag=0;
	byte[] temprcvchaining=new byte[2280];
	byte[] all_dl_data=null;
	byte[] EVENT={(byte) 0xff};
	String LOG_type=null;
	byte[] TRACE={(byte) 0x80};
	thread_print thread_p=new thread_print();
	ShowConsole console=new ShowConsole("user_log");
	static  SerialPort LogPort=null;
	String RellPort="";
	boolean Log_type=true;
	static boolean log_status=false;
	private Properties getProperties()
	{
		String cfg_file=Platform.getInstanceLocation().getURL().getPath();
		Properties prop = new Properties(); 
		InputStream in = null;
		try {
			 in= new BufferedInputStream (new FileInputStream(cfg_file+"\\luat.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "û�л��ȡ�����ļ�����", "����", JOptionPane.INFORMATION_MESSAGE);
		}
		return prop;
	}
	public log()
	{
	}
	private void handleOldPort()
	{
		DownLoad DownLoads=new DownLoad();
		SerialPort oldser=DownLoads.getSerPort();
		if(oldser!=null )
		{
			SerialTool.closePort(oldser);
		}
	}
	public boolean getSerPort()
	{
		return log_status;
	}
	public void CancleLog()
	{
		log_status=false;
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
    		byte[] tempdatalenarry= {temprcvchaining[1],temprcvchaining[2]};    //��ȡ���ݳ���
    		int datalen=ByteArryToInt(tempdatalenarry);
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
				//
				if (Arrays.equals(ID,TRACE))
				{
					//console.printHexString(all_dl_data);
					byte[] print_buf=new byte[datalen-1];
					System.arraycopy(all_dl_data, 6, print_buf, 0, datalen-3);
					int trueflag=find0x00(print_buf);
					byte[] print_buf_true=new byte[trueflag];
					System.arraycopy(print_buf, 0, print_buf_true, 0,trueflag);
					String s = null;
					try {
						s = new String(print_buf_true,"ascii");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					console.Print(s);
				}
				else
				{
					byte[] restart={(byte) 0xAD,0x00,0x06,(byte) 0xFF,0x00,0x57,0x00,0x00,0x00,(byte) 0xA8};
					if(Arrays.equals(all_dl_data,restart))
						console.Print("---------------------Detected system reset (0x57).--------------------");
				}

			}
	 }

	}
	public void run()
	{
		try {
			this.sleep(600);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(log_status)
		{
			log_status=false;
			return;
		}
		
		log_status=true;
		Properties prop = new Properties(); 
		String work_space_path=Platform.getInstanceLocation().getURL().getPath();
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
		 String host=prop.getProperty("trace_host");
		 String uart=prop.getProperty("trace_uart");
		 if((host==null || host.equals("")) && (uart==null || uart.equals("")))
		 {
			 JOptionPane.showMessageDialog(null, "û�����ô�ӡ��", "����", JOptionPane.INFORMATION_MESSAGE);
			 return ;
		 }
		 
		 if(host.length()>3)
		 {
			 RellPort=host;
			 LOG_type="host";
		 }
		 else if(uart.length()>3)
		 {
			 RellPort=uart;
			 LOG_type="uart";
		 }
		if(RellPort!="")			
		{
			//thread_p.start();
			console.Print("***********************��ʼ��ӡtrace***************************");
			LogPort=OpenDownLoadPort(RellPort);
		}
		
		Timer timer = new Timer();
	    timer.schedule(new TimerTask() {
		      public void run() {
		    	  if(!log_status)
		    	  {
		    		  SerialTool.closePort(LogPort);
		    		  LogPort=null;
		    		  console.Print("***********************�رմ�ӡtrace***************************");
		    		  this.cancel();
		    	  }
		    		  
		      }
		    }, 10,500);// �趨ָ����ʱ��time,�˴�Ϊ2000����

			
	}
	private void handle_uart(byte[] data)
	{
		int i=0;
		if(LOG_type.equals("host"))
		{
			byte[] ss=new byte[1];
			for(i=0;i<data.length;i++)
			{
				System.arraycopy(data, i, ss, 0, 1);
				handleRcvByte(ss);
			}
		}
		else
		{
			try {
				console.Print(new String(data,"ascii"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	private class SerialListenerlog implements SerialPortEventListener 
	{
		@Override
		public void serialEvent(SerialPortEvent serialPortEvent) {
			// TODO Auto-generated method stub
		    switch (serialPortEvent.getEventType()) {
            case SerialPortEvent.BI: // 10 ͨѶ�ж�
            	//JOptionPane.showMessageDialog(null, "�봮���豸ͨѶ�ж�", "����", JOptionPane.INFORMATION_MESSAGE);
            	//console.Print("�봮���豸ͨѶ�ж�");
            	//OpenDownLoadPort(RellPort);
            	break;
            case SerialPortEvent.OE: // 7 ��λ�����������
            case SerialPortEvent.FE: // 9 ֡����
            case SerialPortEvent.PE: // 8 ��żУ�����
            case SerialPortEvent.CD: // 6 �ز����
            case SerialPortEvent.CTS: // 3 �������������
            case SerialPortEvent.DSR: // 4 ����������׼������
            case SerialPortEvent.RI: // 5 ����ָʾ
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 ��������������
            	break;
            case SerialPortEvent.DATA_AVAILABLE: // 1 ���ڴ��ڿ�������
            	//console.Print("��������");
				byte[] data = null;
				try {
					if (LogPort == null) {
						JOptionPane.showMessageDialog(null, "���ڶ���Ϊ�գ�����ʧ�ܣ�", "����", JOptionPane.INFORMATION_MESSAGE);
					}
					else {
						data = SerialTool.readFromPort(LogPort);	//��ȡ���ݣ������ֽ�����
						if (data == null || data.length < 1) {	//��������Ƿ��ȡ��ȷ	
							JOptionPane.showMessageDialog(null, "��ȡ���ݹ�����δ��ȡ����Ч���ݣ������豸�����", "����", JOptionPane.INFORMATION_MESSAGE);
						}
						else {
							 //console.Print("���������ݵĳ��ȵ����ݳ���~~~~~~~~~~~~~~:"+data.length);
							 //console.printHexString(data);
							 handle_uart(data);
							 //thread_p.put_data(data,LOG_type);
							}
						}				
					}
				catch (ReadDataFromSerialPortFailure | SerialPortInputStreamCloseFailure e) {
					JOptionPane.showMessageDialog(null, e, "����", JOptionPane.INFORMATION_MESSAGE);
				}	
				
		    }
		}
	}
	private SerialPort OpenDownLoadPort(String port)
	{ 	
		SerialPort tempPort = null;
    	try {
    		int pot;
    		console.Print("���Դ򿪴���"+port);
    		if(LOG_type.equals("uart"))
    			pot=115200;
    		else
    			pot=921600;
			tempPort=SerialTool.openPort(port, pot);
		} catch (SerialPortParameterFailure e) {
			// TODO Auto-generated catch block
			console.Print("��log����ʧ��");
			//JOptionPane.showMessageDialog(null, "��log����ʧ��", "����", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		} catch (NotASerialPort e) {
			// TODO Auto-generated catch block
			console.Print("NotASerialPort");
			//JOptionPane.showMessageDialog(null, "����log�����豸", "����", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		} catch (NoSuchPort e) {
			// TODO Auto-generated catch block
			console.Print("û�������Ĵ���");
			//JOptionPane.showMessageDialog(null, "û��������log����:"+port, "����", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		} catch (PortInUse e) {
			// TODO Auto-generated catch block
			console.Print("�����Ѿ���");
				//JOptionPane.showMessageDialog(null, "log��������ʹ����", "����", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
			return null;
		}
    	console.Print("�򿪴��ڳɹ�");
    	try {
    		console.Print("��ӶԴ���"+tempPort+"����");
			SerialTool.addListener(tempPort, new SerialListenerlog());
		} catch (TooManyListeners e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempPort;	
	}

}
