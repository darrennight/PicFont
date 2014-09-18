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
	
	final String defaultFontType = "楷体";
	
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
		
	}
	
	public void setPath(String dirPath, String outPath, String fontSize, String fontName)
	{
		mDirPath  = dirPath;
		mOutPath  = outPath;
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
		
		//返回的byte数组，格式如下
		//byte  byte  byte  byte [-----------byte-------------] <--- REPEAT
		//    4byte开头为字的索引	                       压缩后的的像素数据串，每bit表示该位上有颜色
		//    UTF-8最宽最byte
		ArrayList<Byte> imgCodeList  = new ArrayList<Byte>();
		
		
		//获取字符 UTF-8的组装的Index
		FileReadWrite fw = new FileReadWrite();
		fw.touchFile(mOutPath);
		mFontIndex = fw.getCoverHashSet(mDirPath);		
		
		
		
		//存储该字的Img像素索引
		byte[] imageCode;
		
		Rectangle2D rect = mFont.getStringBounds("鹏", frc);	
		
		//边留白
		int width  = (int)rect.getWidth()  + 2 ;
		int height = (int)rect.getHeight() + 2 ;
		//
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR);
		
		for(int fontCode : mFontIndex){			
			imageCode = coverFontCodeToImgCode(fontCode, image);
			if(imageCode != null){				
				//写入字符索引值,先放低字节
				imgCodeList.add((byte)((fontCode) & 0xFF));
				imgCodeList.add((byte)((fontCode >> 8) & 0xFF));
				imgCodeList.add((byte)((fontCode >> 16) & 0xFF));
				imgCodeList.add((byte)((fontCode >> 24) & 0xFF));
				//加入该字符的Img像素索引
				for(byte b : imageCode){
					imgCodeList.add(b);
				}
			}	
		}
		
		
		byte[] array = new byte[imgCodeList.size()];
		for(int i = 0; i<  imgCodeList.size(); i++){
			array[i] = imgCodeList.get(i);
		}
		
		fw.initFilePath(mOutPath);
		fw.writeToBytes(array, mOutPath + "\\" + "font.data");
		outPutImg(imgCodeList);
		fw = null;	
		
		return imgCodeList;
	}
	
	//将byte组输出为图片与data文件
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
	
	//获取字符在Img上像素索引
	private byte[] coverFontCodeToImgCode(int fontCode, BufferedImage image)
	{			
		byte[] imageCode = null;
		String s = getStringFromByte(fontCode);
		if(s != null){			
			imageCode = getImageCodeFormString(s, image);
		}		
		return imageCode;
	}
	
	//根据组装后的4-byte组获取其原应表示的字符
	private String getStringFromByte(int fontCode)
	{
		String s = null;
		try {
			byte[] array = getByteArray(fontCode);
			if(array != null){
				s = new String(array, "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return s;
	}
	
	//将字符写在Img上，再获取其像素索引
	private byte[] getImageCodeFormString(String str, BufferedImage image)
	{
		
		imgWidth  = image.getWidth();
		imgHeight = image.getHeight();
		
		int CodeLength = (imgWidth * imgHeight)/8 + 1;
		byte[] imageCode = new byte[CodeLength];
		
		Graphics2D g2D = image.createGraphics();
		//抗锯齿
		//g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        //        			  (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB));
		

		g2D.setColor(Color.WHITE);						 
		g2D.fillRect(0, 0, imgWidth, imgHeight);
		g2D.setColor(Color.BLACK);						
		g2D.setFont(mFont);
		
		Rectangle2D rect = mFont.getStringBounds(str, frc);	
		
		//居中绘制
		g2D.drawString(str, (int)(imgWidth - rect.getWidth())>>1, 
				            g2D.getFontMetrics().getAscent());	
		
		int codeIndex = 0; 
		int byteIndex = 0;
		byte compact = 0;
		for(int y = 0; y < imgHeight; y++){
			for(int x = 0; x < imgWidth; x++){		
				//像素点有颜色就是1，没有颜色就是0，所以用一个byte表示8位，压缩空间
				if(byteIndex == 8){
					imageCode[codeIndex++] = compact;
					compact = 0;
					byteIndex = 0;
				}
				if(image.getRGB(x, y) != Color.WHITE.getRGB()){
					compact |= (1 << (byteIndex)); 		//大位放前面
				}
				byteIndex ++;
			}
		}
		
		
		return imageCode;
	}
	
	//获得字符的UTF-8表示
	private byte[] getByteArray(int fontCode)
	{
		byte[] array = null;

		if(((fontCode & 0xFF) >> 3) == 0x1E){			//4字节表示			
			array = new byte[4];
			array[0] = (byte)(fontCode & 0xFF);
			array[1] = (byte)((fontCode >> 8) & 0xFF);
			array[2] = (byte)((fontCode >> 16) & 0xFF);
			array[3] = (byte)((fontCode >> 24) & 0xFF);     	
        }else if(((fontCode & 0xFF) >> 4) == 0x0E){		//3字节表示
        	array = new byte[3];
        	array[0] = (byte)(fontCode & 0xFF);
			array[1] = (byte)((fontCode >> 8) & 0xFF);
			array[2] = (byte)((fontCode >> 16) & 0xFF);			
        }else if(((fontCode & 0xFF) >> 5) == 0x06){		//2字节表示	            	   
        	array = new byte[2];
        	array[0] = (byte)(fontCode & 0xFF);
			array[1] = (byte)((fontCode >> 8) & 0xFF);
        }else if(((fontCode & 0xFF) >> 7) == 0x00){		//1字节表示        	  
        	array = new byte[1];	
     	    array[0] = (byte)(fontCode & 0xFF);
        }
		
		if(array.length == 1){
			if(array[0] == 0x20){						//空格
				return null;
			}
			
			if(array[0] == 0x0D || array[0] == 0x0A){	//回车
				return null;
			}
		}
		
		return array;
	}
	
	//写图片到本地,测试用
	public void coverCodeToImageAtDisk(String str, String name, byte[] imageCode , 
			                           int width, int height){
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
	    						
		
		
		for(int y = 0 ; y < height; y++){
	    	for(int x = 0; x < width; x++){
	    		image.setRGB(x,y,0x00000000);
	    	}
		}
		
		int fontArgb  = 0xffead678;	//金色字体
		int fontBoder = 0xff000000;	//包黑色边
		
	    for(int y = 0 ; y < height; y++){
	    	for(int x = 0; x < width; x++){
	    		int index = (y*width + x );	    		
	    		int rgb = (imageCode[index/8] >> ((index)%8)) & 0x01;
	    		boolean hasPixel = (rgb == 1);
	    		if(hasPixel){
	    			
	    			//包边
	    			if(x > 0 && image.getRGB(x-1, y) != fontArgb){
	    				image.setRGB(x-1, y, fontBoder);
	    			}
	    			
	    			if(x +1  < width && image.getRGB(x+1, y) != fontArgb){
	    				image.setRGB(x+1, y, fontBoder);
	    			}
	    			
	    			if(y  > 0 && image.getRGB(x, y-1) != fontArgb){
	    				image.setRGB(x, y-1, fontBoder);
	    			}
	    			
	    			if(y + 1 < height && image.getRGB(x, y+1) != fontArgb){
	    				image.setRGB(x, y+1, fontBoder);
	    			}
	    			
	    			if(hasPixel){	    				    			
		    			image.setRGB(x, y, 0xffead678);
		    		}
	  
	    		}
	    	}
	    }
	    
	    
	    try {
			ImageIO.write(image, "png", new File(mOutPath +"\\" + name + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

}
