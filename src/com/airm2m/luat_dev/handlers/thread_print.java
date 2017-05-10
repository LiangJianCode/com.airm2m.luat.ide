package com.airm2m.luat_dev.handlers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import gnu.io.SerialPort;

public class thread_print extends Thread{
	byte[] uart_data_buf_uart;
	byte[] uart_data_buf_host;
	byte[] uart_data_buf_host_temp;
	byte[] uart_data_buf_uart_temp;
	boolean uart_flag=false;
	boolean host_flag=false;
	ShowConsole console=new ShowConsole("user_log");
	byte[] FLAG_WORD={(byte) 0xad};
	int temprcvchainingFlag=0;
	byte[] temprcvchaining=new byte[2280];
	byte[] all_dl_data=null;
	byte[] EVENT={(byte) 0xff};
	byte[] TRACE={(byte) 0x80};
	SerialPort DownPort;
	String RellPort="";
	boolean Log_type=true;
	
	public void run()
	{
		Timer timer = new Timer();
		console.Print("打印线程开始");
	    timer.schedule(new TimerTask() {
		      public void run() {
		        if(uart_flag)
		        {
		        	console.Print("收到uart数据");
		        	uart_flag=false;
		        }
		        else if(host_flag)
		        {
		        	//console.Print("收到host数据1");
		        	uart_data_buf_host_temp=uart_data_buf_host;
		        	host_flag=false;
					int i=0;
					for(i=0;i<uart_data_buf_host_temp.length;i++)
					{
						handleRcvByte(uart_data_buf_host_temp[i]);
					}
					
		        }
		        //console.Print("轮询串口数据");
		      }
		    }, 10,10);// 设定指定的时间time,此处为2000毫秒
	}
	public thread_print()
	{
		
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
	private void handleRcvByte(byte data_rcv)
	{
		//console.Print("rcv:"+temprcvchainingFlag);
		byte[] data={data_rcv};
		//console.printHexString(data);
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
    		int datalen=ByteArryToInt(tempdatalenarry);
    		if(datalen>=0)
    		{
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
    		else
    		{
    			temprcvchainingFlag=0;
    		}

	 }

	}
	public void put_data(byte[] data,String type)
	{
		byte[] temp;
		if(type.equals("uart"))
		{
			//uart_data_buf_uart.add(data);
		}
		else
		{
			//console.Print("收到host数据:"+host_flag);
			//console.printHexString(data);
			if(host_flag)
			{
				temp=uart_data_buf_host;
				uart_data_buf_host=new byte[temp.length+data.length];
				System.arraycopy(temp, 0, uart_data_buf_host, 0,temp.length);
				System.arraycopy(data, 0, uart_data_buf_host, temp.length,data.length);
			}
			else
			{
				uart_data_buf_host=data;
				host_flag=true;
			}
				
			
		}	
	}
	
}
