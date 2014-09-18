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
	
	//�洢�ļ��б�
	private LinkedList<String> fileList = new LinkedList<String>();
	

	//��ȡҪת���������ӳ��
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
	
	
	/*	UTF-8 �Ǳ䳤�ı��룬�����1~4 byte ����
	 *  0xxx xxxx ��ʾ��һ��ASCII�룬��Ϊ1���ֽ�
	 *  10xx xxxx ��ʾ���Ƕ��ֽڱ�ʾ�е�ĳһ�ֽ�
	 *  110x xxxx ��ʾ���������ֽڵ�ͷһ���ֽ�
	 *  1110 xxxx ��ʾ���������ֽڵ�ͷһ���ֽ�
	 *  1111 0xxx ��ʾ�����ĸ��ֽڵ�ͷһ���ֽ�
	 *  ���ｫ�䳤��UTF-8����ͳһ��4�ֽڣ���ʵ���������ĵĻ���3�ֽ�Ӧ��Ҳ����
	 */
	private HashSet<Integer> getChars()
	{		
		HashSet<Integer> intSet = new HashSet<>();
		InputStream in = null;

		for(String filePath : fileList){
			try {
				in = new FileInputStream(filePath);
				int readbyte;
				//һ��byteһ��byte�Ķ�				
	            while ((readbyte = in.read()) != -1) {
	               int index = 0;	               
	               if((readbyte >> 3) == 0x1E){				//4�ֽڱ�ʾ  
	            	   index |= readbyte;
	            	   index |= in.read() << 8;
	            	   index |= in.read() << 16;
	            	   index |= in.read() << 24;
	               }else if((readbyte >> 4) == 0x0E){		//3�ֽڱ�ʾ	            	   
	            	   index |= readbyte;
	            	   index |= in.read() << 8;
	            	   index |= in.read() << 16;
	               }else if((readbyte >> 5) == 0x06){		//2�ֽڱ�ʾ	            	   
	            	   index |= readbyte;
	            	   index |= in.read() << 8;
	               }else if((readbyte >> 7) == 0x00){		//1�ֽڱ�ʾ
	            	   index |= readbyte;
	               }
	               //else{	 								�϶�����UTF-8����
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
	
	
	//�����ļ���	
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
	
	//дbyte���ݵ�disk��
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
}