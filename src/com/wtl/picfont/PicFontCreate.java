package com.wtl.picfont;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class PicFontCreate {
	
	final String defaultFontType = "����";
	
	HashSet<Integer> mFontIndex = null;
	String mDirPath  = null;
	String mOutPath  = null;
	String mFontName = null;	
	int mFontSize = 0;	
	
	Font mFont = null;
	FontMetrics fm = null;
	
	int imgWidth = 0;
	int imgHeight = 0;
	
	
	public PicFontCreate()
	{
		//...
	}
	
	public void setPath(String dirPath, String outPath, String fontSize, String fontName)
	{
		mDirPath  = dirPath;
		mOutPath  = outPath + "\\";
		mFontName = fontName == null ? defaultFontType : fontName;
		
		try{
			mFontSize = Integer.valueOf(fontSize).intValue();
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		
		mFont = new Font(mFontName, Font.PLAIN, mFontSize);
	}
	
	private static AffineTransform atf = new AffineTransform();
    private static FontRenderContext frc = new FontRenderContext(atf, true, true);
	public ArrayList<Byte> fontImgCodeCreate()
	{
		if(mFont == null){
			return null;
		}
		
		//���ص�byte���飬��ʽ����
		//byte  byte  byte  byte [-----------byte-------------] <--- REPEAT
		//    4byte��ͷΪ�ֵ�����	                       ѹ����ĵ��������ݴ���ÿbit��ʾ��λ������ɫ
		//    UTF-8�����byte
		ArrayList<Byte> imgCodeList  = new ArrayList<Byte>();
		
		
		//��ȡ�ַ� UTF-8����װ��Index
		FileReadWrite fw = new FileReadWrite();
		mFontIndex = fw.getCoverHashSet(mDirPath);				
		
		//�洢���ֵ�Img��������
		byte[] imageCode;

		//����һ���ַ��Ŀ��,��������Ӧ����UTF-8�������ַ������ģ���ʱ��������
		Rectangle2D rect = mFont.getStringBounds("��", frc);	
		
		//������
		int width  = (int)rect.getWidth()  + 2 ;
		int height = (int)rect.getHeight() + 2 ;
		
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR);
		
		for(int fontCode : mFontIndex){			
			imageCode = coverFontCodeToImgCode(fontCode, image);
			if(imageCode != null){				
				//д���ַ�����ֵ,�ȷŵ��ֽ�
				imgCodeList.add((byte)((fontCode) & 0xFF));
				imgCodeList.add((byte)((fontCode >> 8) & 0xFF));
				imgCodeList.add((byte)((fontCode >> 16) & 0xFF));
				imgCodeList.add((byte)((fontCode >> 24) & 0xFF));
				//������ַ���Img��������
				for(byte b : imageCode){
					imgCodeList.add(b);
				}
			}	
		}
		
		
		byte[] array = new byte[imgCodeList.size()];
		for(int i = 0; i<  imgCodeList.size(); i++){
			array[i] = imgCodeList.get(i);
		}
		fw.writeToBytes(array, mOutPath+ "font.data");
		outPutImg(imgCodeList);
		fw = null;	
		
		return imgCodeList;
	}
	
	//��byte�����ΪͼƬ��data�ļ�
	private void outPutImg(ArrayList<Byte> imgCodeList){
		
		byte[] array = new byte[(imgWidth * imgHeight)/8 + 1];
		int counter = 0;
		
		for(int i = 0; i < imgCodeList.size();){
			int fontIndex = 0;
			
			fontIndex |=  imgCodeList.get(i++) & 0xff;
			fontIndex |= (imgCodeList.get(i++) & 0xff) << 8;
			fontIndex |= (imgCodeList.get(i++) & 0xff) << 16;
			fontIndex |= (imgCodeList.get(i++) & 0xff) << 24;
			
			String s = getStringFromByte(fontIndex);
			
			for(int j = 0; j < array.length; j++){
				array[j] = imgCodeList.get(i++);
			}
			
			coverCodeToImageAtDisk(s, "" + (++counter), array, imgWidth, imgHeight);
		}
				
	}
	
	//��ȡ�ַ���Img����������
	private byte[] coverFontCodeToImgCode(int fontCode, BufferedImage image)
	{			
		byte[] imageCode = null;
		String s = getStringFromByte(fontCode);
		if(s != null){			
			imageCode = getImageCodeFormString(s, image);
		}		
		return imageCode;
	}
	
	//������װ���4-byte���ȡ��ԭӦ��ʾ���ַ�
	private String getStringFromByte(int fontCode)
	{
		String s = null;
		try {
			 s = new String(getCharArray(fontCode), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return s;
	}
	
	//���ַ�д��Img�ϣ��ٻ�ȡ����������
	private byte[] getImageCodeFormString(String str, BufferedImage image)
	{
		
		imgWidth  = image.getWidth();
		imgHeight = image.getHeight();
		
		int CodeLength = (imgWidth * imgHeight)/8 + 1;
		byte[] imageCode = new byte[CodeLength];
		
		Graphics2D g2D = image.createGraphics();
		//�����
//		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//                			(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB));
		

		g2D.setColor(Color.WHITE);						 
		g2D.fillRect(0, 0, imgWidth, imgHeight);
		g2D.setColor(Color.BLACK);						
		g2D.setFont(mFont);
		
		Rectangle2D rect = mFont.getStringBounds(str, frc);	
		
		//���л���
		g2D.drawString(str, (int)(imgWidth - rect.getWidth())>>1, 
				            g2D.getFontMetrics().getAscent());	
	
		
		int codeIndex = 0; 
		int byteIndex = 0;
		byte compact = 0;
		for(int y = 0; y < imgHeight; y++){
			for(int x = 0; x < imgWidth; x++){		
				//���ص�����ɫ����1��û����ɫ����0��������һ��byte��ʾ8λ��ѹ���ռ�
				if(byteIndex == 8){
					imageCode[codeIndex++] = compact;
					compact = 0;
					byteIndex = 0;
				}
				if(image.getRGB(x, y) != Color.WHITE.getRGB()){
					compact |= (1 << (byteIndex)); 		//��λ��ǰ��
				}
				byteIndex ++;
			}
		}
		
		
		return imageCode;
	}
	
	//����ַ���UTF-8��ʾ
	private byte[] getCharArray(int fontCode)
	{
		byte[] array = null;

		if(((fontCode & 0xFF) >> 3) == 0x1E){			//4�ֽڱ�ʾ			
			array = new byte[4];
			array[0] = (byte)(fontCode & 0xFF);
			array[1] = (byte)((fontCode >> 8) & 0xFF);
			array[2] = (byte)((fontCode >> 16) & 0xFF);
			array[3] = (byte)((fontCode >> 24) & 0xFF);     	
        }else if(((fontCode & 0xFF) >> 4) == 0x0E){		//3�ֽڱ�ʾ
        	array = new byte[3];
        	array[0] = (byte)(fontCode & 0xFF);
			array[1] = (byte)((fontCode >> 8) & 0xFF);
			array[2] = (byte)((fontCode >> 16) & 0xFF);			
        }else if(((fontCode & 0xFF) >> 5) == 0x06){		//2�ֽڱ�ʾ	            	   
        	array = new byte[2];
        	array[0] = (byte)(fontCode & 0xFF);
			array[1] = (byte)((fontCode >> 8) & 0xFF);
        }else if(((fontCode & 0xFF) >> 7) == 0x00){		//1�ֽڱ�ʾ        	  
        	array = new byte[1];	
     	    array[0] = (byte)(fontCode & 0xFF);
        }
		
		return array;
	}
	
	//дͼƬ������
	public void coverCodeToImageAtDisk(String str, String name, byte[] imageCode , 
			                           int width, int height){
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
	    						
		int argb = 0xffead678;
		
		for(int y = 0 ; y < height; y++){
	    	for(int x = 0; x < width; x++){
	    		image.setRGB(x,y,0x00000000);
	    	}
		}
		
	    for(int y = 0 ; y < height; y++){
	    	for(int x = 0; x < width; x++){
	    		int index = (y*width + x );	    		
	    		int rgb = (imageCode[index/8] >> ((index)%8)) & 0x01;
	    		boolean hasPixel = (rgb == 1);
	    		if(hasPixel){
	    			
	    			if(x > 0 && image.getRGB(x-1, y) != argb){
	    				image.setRGB(x-1, y, 0xff000000);
	    			}
	    			
	    			if(x +1  < width && image.getRGB(x+1, y) != argb){
	    				image.setRGB(x+1, y, 0xff000000);
	    			}
	    			
	    			if(y  > 0 && image.getRGB(x, y-1) != argb){
	    				image.setRGB(x, y-1, 0xff000000);
	    			}
	    			
	    			if(y + 1 < height && image.getRGB(x, y+1) != argb){
	    				image.setRGB(x, y+1, 0xff000000);
	    			}
	    			
	    			if(hasPixel){	    				    			
		    			image.setRGB(x, y, 0xffead678);
		    		}
	  
	    		}
	    	}
	    }
	    
	    
	    try {
			ImageIO.write(image, "png", new File(mOutPath + name + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

}
