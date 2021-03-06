package com.airm2m.luat_dev.handlers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FCS {
	 byte[] fcs_msb2lsb ={
	     0x00, (byte) 0x80, 0x40,(byte) 0xc0, 0x20, (byte) 0xa0, 0x60, (byte) 0xe0,
	     0x10, (byte) 0x90, 0x50,(byte) 0xd0, 0x30, (byte) 0xb0, 0x70, (byte) 0xf0,
		 0x08, (byte) 0x88, 0x48,(byte) 0xc8, 0x28, (byte) 0xa8, 0x68, (byte) 0xe8,
		 0x18, (byte) 0x98, 0x58,(byte) 0xd8, 0x38, (byte)0xb8, 0x78, (byte)0xf8,
		 0x04, (byte) 0x84, 0x44,(byte)0xc4, 0x24, (byte)0xa4, 0x64, (byte)0xe4,
		 0x14, (byte) 0x94, 0x54,(byte)0xd4, 0x34, (byte)0xb4, 0x74, (byte)0xf4,
		 0x0c, (byte)0x8c, 0x4c, (byte)0xcc, 0x2c, (byte)0xac, 0x6c, (byte)0xec,
		 0x1c, (byte)0x9c, 0x5c, (byte)0xdc, 0x3c, (byte)0xbc, 0x7c,(byte) 0xfc,
		 0x02, (byte)0x82, 0x42, (byte)0xc2, 0x22, (byte)0xa2, 0x62, (byte)0xe2,
		 0x12, (byte)0x92, 0x52, (byte)0xd2, 0x32, (byte)0xb2, 0x72, (byte)0xf2,
		 0x0a, (byte)0x8a, 0x4a, (byte)0xca, 0x2a, (byte)0xaa, 0x6a, (byte)0xea,
		 0x1a, (byte)0x9a, 0x5a, (byte)0xda, 0x3a, (byte)0xba, 0x7a, (byte)0xfa,
		 0x06, (byte)0x86, 0x46, (byte)0xc6, 0x26, (byte)0xa6, 0x66, (byte)0xe6,
		 0x16, (byte)0x96, 0x56, (byte)0xd6, 0x36, (byte)0xb6, 0x76, (byte)0xf6,
		 0x0e, (byte)0x8e, 0x4e, (byte)0xce, 0x2e,(byte) 0xae, 0x6e, (byte)0xee,
		 0x1e, (byte)0x9e, 0x5e, (byte)0xde, 0x3e, (byte)0xbe, 0x7e, (byte)0xfe,
		 0x01, (byte)0x81, 0x41, (byte)0xc1, 0x21,(byte) 0xa1, 0x61, (byte)0xe1,
		 0x11, (byte)0x91, 0x51, (byte)0xd1, 0x31, (byte)0xb1, 0x71, (byte)0xf1,
		 0x09, (byte)0x89, 0x49, (byte)0xc9, 0x29, (byte)0xa9, 0x69, (byte)0xe9,
		 0x19, (byte)0x99, 0x59, (byte)0xd9, 0x39, (byte)0xb9, 0x79, (byte)0xf9,
		 0x05, (byte)0x85, 0x45, (byte)0xc5, 0x25, (byte)0xa5, 0x65, (byte)0xe5,
		 0x15, (byte)0x95, 0x55, (byte)0xd5, 0x35, (byte)0xb5, 0x75, (byte)0xf5,
		 0x0d, (byte)0x8d, 0x4d, (byte)0xcd, 0x2d, (byte)0xad, 0x6d, (byte)0xed,
		 0x1d, (byte)0x9d, 0x5d, (byte)0xdd, 0x3d, (byte)0xbd, 0x7d, (byte)0xfd,
		 0x03, (byte)0x83, 0x43, (byte)0xc3, 0x23, (byte)0xa3, 0x63, (byte)0xe3,
		 0x13, (byte)0x93, 0x53, (byte)0xd3, 0x33, (byte)0xb3, 0x73, (byte)0xf3,
		 0x0b, (byte)0x8b, 0x4b, (byte)0xcb, 0x2b, (byte)0xab, 0x6b, (byte)0xeb,
		 0x1b, (byte)0x9b, 0x5b, (byte)0xdb, 0x3b, (byte)0xbb, 0x7b, (byte)0xfb,
		 0x07, (byte)0x87, 0x47, (byte)0xc7, 0x27, (byte)0xa7, 0x67, (byte)0xe7,
		 0x17, (byte)0x97, 0x57, (byte)0xd7, 0x37, (byte)0xb7, 0x77, (byte)0xf7,
		 0x0f, (byte)0x8f, 0x4f, (byte)0xcf, 0x2f, (byte)0xaf, 0x6f, (byte)0xef,
		 0x1f, (byte)0x9f, 0x5f, (byte)0xdf, 0x3f, (byte)0xbf, 0x7f, (byte)0xff
		};

		long[] fcs_table ={
		   0x00000000,0x01BBA1B5,0x03CCE2DF,0x0277436A,0x0722640B,0x0699C5BE,0x04EE86D4,0x05552761,
		   0x0E44C816,0x0FFF69A3,0x0D882AC9,0x0C338B7C,0x0966AC1D,0x08DD0DA8,0x0AAA4EC2,0x0B11EF77,
		   0x1C89902C,0x1D323199,0x1F4572F3,0x1EFED346,0x1BABF427,0x1A105592,0x186716F8,0x19DCB74D,
		   0x12CD583A,0x1376F98F,0x1101BAE5,0x10BA1B50,0x15EF3C31,0x14549D84,0x1623DEEE,0x17987F5B,
		   0x39A881ED,0x38132058,0x3A646332,0x3BDFC287,0x3E8AE5E6,0x3F314453,0x3D460739,0x3CFDA68C,
		   0x37EC49FB,0x3657E84E,0x3420AB24,0x359B0A91,0x30CE2DF0,0x31758C45,0x3302CF2F,0x32B96E9A,
		   0x252111C1,0x249AB074,0x26EDF31E,0x275652AB,0x220375CA,0x23B8D47F,0x21CF9715,0x207436A0,
		   0x2B65D9D7,0x2ADE7862,0x28A93B08,0x29129ABD,0x2C47BDDC,0x2DFC1C69,0x2F8B5F03,0x2E30FEB6,
		   0x73EAA26F,0x725103DA,0x702640B0,0x719DE105,0x74C8C664,0x757367D1,0x770424BB,0x76BF850E,
		   0x7DAE6A79,0x7C15CBCC,0x7E6288A6,0x7FD92913,0x7A8C0E72,0x7B37AFC7,0x7940ECAD,0x78FB4D18,
		   0x6F633243,0x6ED893F6,0x6CAFD09C,0x6D147129,0x68415648,0x69FAF7FD,0x6B8DB497,0x6A361522,
		   0x6127FA55,0x609C5BE0,0x62EB188A,0x6350B93F,0x66059E5E,0x67BE3FEB,0x65C97C81,0x6472DD34,
		   0x4A422382,0x4BF98237,0x498EC15D,0x483560E8,0x4D604789,0x4CDBE63C,0x4EACA556,0x4F1704E3,
		   0x4406EB94,0x45BD4A21,0x47CA094B,0x4671A8FE,0x43248F9F,0x429F2E2A,0x40E86D40,0x4153CCF5,
		   0x56CBB3AE,0x5770121B,0x55075171,0x54BCF0C4,0x51E9D7A5,0x50527610,0x5225357A,0x539E94CF,
		   0x588F7BB8,0x5934DA0D,0x5B439967,0x5AF838D2,0x5FAD1FB3,0x5E16BE06,0x5C61FD6C,0x5DDA5CD9,
		   0xE76EE56B,0xE6D544DE,0xE4A207B4,0xE519A601,0xE04C8160,0xE1F720D5,0xE38063BF,0xE23BC20A,
		   0xE92A2D7D,0xE8918CC8,0xEAE6CFA2,0xEB5D6E17,0xEE084976,0xEFB3E8C3,0xEDC4ABA9,0xEC7F0A1C,
		   0xFBE77547,0xFA5CD4F2,0xF82B9798,0xF990362D,0xFCC5114C,0xFD7EB0F9,0xFF09F393,0xFEB25226,
		   0xF5A3BD51,0xF4181CE4,0xF66F5F8E,0xF7D4FE3B,0xF281D95A,0xF33A78EF,0xF14D3B85,0xF0F69A30,
		   0xDEC66486,0xDF7DC533,0xDD0A8659,0xDCB127EC,0xD9E4008D,0xD85FA138,0xDA28E252,0xDB9343E7,
		   0xD082AC90,0xD1390D25,0xD34E4E4F,0xD2F5EFFA,0xD7A0C89B,0xD61B692E,0xD46C2A44,0xD5D78BF1,
		   0xC24FF4AA,0xC3F4551F,0xC1831675,0xC038B7C0,0xC56D90A1,0xC4D63114,0xC6A1727E,0xC71AD3CB,
		   0xCC0B3CBC,0xCDB09D09,0xCFC7DE63,0xCE7C7FD6,0xCB2958B7,0xCA92F902,0xC8E5BA68,0xC95E1BDD,
		   0x94844704,0x953FE6B1,0x9748A5DB,0x96F3046E,0x93A6230F,0x921D82BA,0x906AC1D0,0x91D16065,
		   0x9AC08F12,0x9B7B2EA7,0x990C6DCD,0x98B7CC78,0x9DE2EB19,0x9C594AAC,0x9E2E09C6,0x9F95A873,
		   0x880DD728,0x89B6769D,0x8BC135F7,0x8A7A9442,0x8F2FB323,0x8E941296,0x8CE351FC,0x8D58F049,
		   0x86491F3E,0x87F2BE8B,0x8585FDE1,0x843E5C54,0x816B7B35,0x80D0DA80,0x82A799EA,0x831C385F,
		   0xAD2CC6E9,0xAC97675C,0xAEE02436,0xAF5B8583,0xAA0EA2E2,0xABB50357,0xA9C2403D,0xA879E188,
		   0xA3680EFF,0xA2D3AF4A,0xA0A4EC20,0xA11F4D95,0xA44A6AF4,0xA5F1CB41,0xA786882B,0xA63D299E,
		   0xB1A556C5,0xB01EF770,0xB269B41A,0xB3D215AF,0xB68732CE,0xB73C937B,0xB54BD011,0xB4F071A4,
		   0xBFE19ED3,0xBE5A3F66,0xBC2D7C0C,0xBD96DDB9,0xB8C3FAD8,0xB9785B6D,0xBB0F1807,0xBAB4B9B2,
		};
		public int FCSFinal(int remainder)
		{
			byte[] fcs=new byte[3];
		    int R = remainder;
		    int endint=0;
		    R = ~R;  // ones complement of the remainder

		    fcs[0] = fcs_msb2lsb[(R >> 16)&0xff];
		    fcs[1] = fcs_msb2lsb[(R >> 8)&0xff];
		    fcs[2] = fcs_msb2lsb[(R)&0xff];
			for(int i=0;i<fcs.length;i++)
			{
				endint=((fcs[i]&0xff)<<(8*(fcs.length-1-i)))|endint;
			}
		    return endint;
		}
		public int  FCSUpdate( byte[] buffer)
		{
		    int i;
		    int R = 0xFFFFFFFF;

		    for (i = 0; i < buffer.length; i++)
		    	{
			        R = (int) ((R<<8) ^ fcs_table[(((R>>16)^(fcs_msb2lsb[buffer[i]&0XFF]))&0xff)]);
		    	}
	
		    
		    return R;
		}	
		public List<Integer> getfcs(byte[] buffer,int size)
		{
			int num=buffer.length/size;
			List<Integer>  fcs_buf = new ArrayList<Integer>();
			for(int i=0;i<num;i++)
			{
				int end=i*size+size;
				int fcsint=FCSUpdate(Arrays.copyOfRange(buffer,i*size,end));
				fcs_buf.add(FCSFinal(fcsint));
				
			}
			if(buffer.length-num*size>0)
			{
				int fcsint=FCSUpdate(Arrays.copyOfRange(buffer,num*size,buffer.length));
				fcs_buf.add(FCSFinal(fcsint));
			}
			return fcs_buf;
		}
}