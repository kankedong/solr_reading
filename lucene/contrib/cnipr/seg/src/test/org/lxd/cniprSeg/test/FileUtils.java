package org.lxd.cniprSeg.test; 

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/** 
 * 类说明 
 * @author  添加者 E-mail: lixaodong@gintong.com;
 * @author  修改者 E-mail:暂无
 * @version 创建时间：2013-9-25 上午9:22:44;
 * @version 修改时间:暂无 
 */
public class FileUtils {
	
	public static final String UTF8="UTF-8";
	public static final String GBK="GBK";
	
	/**
	 * 获取文件中的全部内容
	 * @author  添加者 E-mail: lixaodong@gintong.com;
	 * @author  修改者 E-mail:无
	 * @version 创建时间：2013-9-25 上午9:47:19;
	 * @version 修改时间:无 
	 * @param file
	 * @param encode
	 * @return
	 */
	public static String getContentFromFile( File file,String encode,String lineSeparator )
	{
		if( file==null )
			return "";
		
		if( !file.exists() )
			return "";
		
		try {
			StringBuffer sb = new StringBuffer(4096);
			BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream(file),encode) );
			
			String line=null;
			while( (line=br.readLine())!=null )
			{
				sb.append(line);
				sb.append(lineSeparator);
			}
			return sb.toString();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * 向文件中写入内容
	 * @author  添加者 E-mail: lixaodong@gintong.com;
	 * @author  修改者 E-mail:无
	 * @version 创建时间：2013-9-25 上午9:47:23;
	 * @version 修改时间:无 
	 * @param file
	 * @param content
	 * @param encode
	 */
	public static void writeFile( File file,String content,String encode )
	{
		try {

			BufferedWriter fw = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ),encode ));
			fw.append(content);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
 