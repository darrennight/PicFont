package com.wtl.picfont;

public class Main {
	
	private static final int MAX_LENGTH = 4; 
	private static final int MIN_LENGTH = 3; 
	public static void main(String[] args) {		
		
		String filePath  = null;
		String fileOut   = null;
		String fontSize  = null;
		String fontStyle = null;
		
		
		
		if (args.length == 0){
			//测试用的一些路径
			filePath = "F:\\CodeTest";
			fileOut  = "F:\\CodeTestOut";
			fontSize = "20";
			fontStyle = null;
		}
		
		if(args.length > MAX_LENGTH || args.length < MIN_LENGTH){
			System.out.println("error args num");
			//return;
		}
		
		switch(args.length)
		{				
		case 3:
			filePath = args[0];
			fileOut  = args[1];
			fontSize = args[2];
			fontStyle = null;			
			break;
		case 4:
			filePath = args[0];
			fileOut  = args[1];
			fontSize = args[2];
			fontStyle = args[3];			
			break;
		}
		
		PicFontCreate picFontCreate = new PicFontCreate();
		picFontCreate.setPath(filePath, fileOut, fontSize, fontStyle);
		picFontCreate.fontImgCodeCreate();
	}

}