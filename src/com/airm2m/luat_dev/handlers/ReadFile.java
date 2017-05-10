package com.airm2m.luat_dev.handlers;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ReadFile {
	FileInputStream in=null;
	File ff;
	public ReadFile(String path)
	{
		try {
			ff=new File(path);
			in=new FileInputStream(ff);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void read(byte[] receive)
	{
		try {
			in.read(receive);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean delet()
	{
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ff.delete();
	}
	public int len()
	{
		int temp = 0;
		try {
			temp= in.available();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}
	
}
