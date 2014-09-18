package com.wtl.picfont;

public class Main {
	
	private static final int MAX_LENGTH = 4; 
	private static final int MIN_LENGTH = 3; 
	public static void main(String[] args) {		

		//�����ã�"F:\\CodeTest" ��Ϊһ UTF-8 ������ı�
//		PicFontCreate picFontCreate = new PicFontCreate();
//		picFontCreate.setPath("F:\\CodeTest", "F:\\CodeTestOut", "20", null);
//		picFontCreate.fontImgCodeCreate();
		
		String filePath  = null;
		String fileOut   = null;
		String fontSize  = null;
		String fontStyle = null;
		
		
		
		if(args.length > MAX_LENGTH || args.length < MIN_LENGTH){
			
		}else if (args.length == 0){
			//�����õ�һЩ·��
			filePath = "F:\\CodeTest";
			fileOut  = "F:\\CodeTestOut";
			fontSize = "20";
			fontStyle = "����";
		}
		
		PicFontCreate picFontCreate = new PicFontCreate();
		
		switch(args.length)
		{				
		case 3:
			picFontCreate.setPath(args[0], args[1], args[2], null);
			break;
		case 4:
			picFontCreate.setPath(args[0], args[1], args[2], args[3]);
			break;
		}
		
		picFontCreate.fontImgCodeCreate();
	}

}