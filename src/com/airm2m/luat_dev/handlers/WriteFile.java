package com.airm2m.luat_dev.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WriteFile {
	FileOutputStream out = null;
	File fl;
	public WriteFile(String path)
	{
		try {
			fl=new File(path);
			out = new FileOutputStream(fl);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 void write(byte[] context)
	{
			try {
				out.write(context);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	public void close()
	{
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public long getlen()
	{
		return fl.length();
	}
	
	
}
