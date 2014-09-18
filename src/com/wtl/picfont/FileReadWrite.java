package com.wtl.picfont;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;

public class FileReadWrite {
	
	//存储文件列表
	private LinkedList<String> fileList = new LinkedList<String>();
	

	//获取要转换的字体的映射
	public HashSet<Integer> getCoverHashSet(String path)
	{	

		if(path == null){
			return null;
		}
		
		
		fileList.clear();		
		traverseFolder(path);				
		HashSet<Integer> intSet = getChars();
		fileList.clear();
		
		return intSet;
	}
	
	
	/*	UTF-8 是变长的编码，编码从1~4 byte 不等
	 *  0xxx xxxx 表示是一个ASCII码，长为1个字节
	 *  10xx xxxx 表示这是多字节表示中的某一字节
	 *  110x xxxx 表示这是两个字节的头一个字节
	 *  1110 xxxx 表示这是三个字节的头一个字节
	 *  1111 0xxx 表示这是四个字节的头一个字节
	 *  这里将变长的UTF-8编码统一成4字节，其实我们用中文的话，3字节应该也够了
	 */
	private HashSet<Integer> getChars()
	{		
		HashSet<Integer> intSet = new HashSet<>();
		InputStream in = null;

		for(String filePath : fileList){
			try {
				in = new FileInputStream(filePath);
				int readbyte;
				//一个byte一个byte的读				
	            while ((readbyte = in.read()) != -1) {
	               int index = 0;	               
	               if((readbyte >> 3) == 0x1E){				//4字节表示  
	            	   index |= readbyte;
	            	   index |= in.read() << 8;
	            	   index |= in.read() << 16;
	            	   index |= in.read() << 24;
	               }else if((readbyte >> 4) == 0x0E){		//3字节表示	            	   
	            	   index |= readbyte;
	            	   index |= in.read() << 8;
	            	   index |= in.read() << 16;
	               }else if((readbyte >> 5) == 0x06){		//2字节表示	            	   
	            	   index |= readbyte;
	            	   index |= in.read() << 8;
	               }else if((readbyte >> 7) == 0x00){		//1字节表示
	            	   index |= readbyte;
	               }
	               //else{	 								肯定不是UTF-8编码
	               //}
	               intSet.add(index);
	            }
			} catch (FileNotFoundException e) {			
				e.printStackTrace();
			} catch (IOException e){
				e.printStackTrace();
			}finally{
				if(in != null){
					try {
						in.close();
					} catch (IOException e) {						
						e.printStackTrace();
					}
				}
				in = null;
			}
		}
		
		return intSet;
	}
	
	
	//遍历文件夹	
	private void traverseFolder(String path) {		  
        File file = new File(path);  
        if (file.exists()) {  
            File[] files = file.listFiles();  
            if (files.length == 0) {                  
                return;  
            } else {  
                for (File fileIndex : files) {  
                    if (fileIndex.isDirectory()) {                          
                        traverseFolder(fileIndex.getAbsolutePath());  
                    } else {  
                    	fileList.add(fileIndex.getAbsolutePath());  
                    }  
                }  
            }  
        }  
    }  
	
	//写byte数据到disk上
    public void writeToBytes(byte bytes[],String fileName){        	
        FileOutputStream fos=null;   
        try{   
            fos=new FileOutputStream(fileName);   
            fos.write(bytes);   
        }     
        catch(Exception e){   
            e.printStackTrace();                  
        }   
        finally{   
            try{
            	if(fos != null){
            		fos.close();
            	}                  
            }   
            catch(IOException iex){}   
        }   
    }   
    
    public void touchFile(String filePath){
    	File dir = null;
    	
    	dir = new File(filePath);
    	if(!dir.exists()){
    		dir.mkdirs();
    	}
    }
    
    public void initFilePath(String filePath)
    {
    	File file = new File(filePath);  
        if (file.exists()) {  
            File[] files = file.listFiles();  
            if (files.length == 0) {                  
                return;  
            } else {  
                for (File fileIndex : files) {  
                	fileIndex.delete();
                }  
            }  
        }
    }
}