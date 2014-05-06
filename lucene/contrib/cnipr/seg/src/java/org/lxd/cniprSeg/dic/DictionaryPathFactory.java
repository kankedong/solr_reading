package org.lxd.cniprSeg.dic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;
/**
 * 类说明:用于获取存放字典的路径
 * 创建者:lixiaodong@cnir.com
 * 修改者:
 * 创建时间:2014-3-11 下午4:11:19
 * 修改时间:2014-3-11 下午4:11:19
 */
public class DictionaryPathFactory {
	
	static final Logger log = Logger.getLogger(DictionaryPathFactory.class.getName());
	
	/**
	 * 获取文件的存储路径
	 * @param fileName
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-3-11 下午4:21:40
	 * 修改时间:2014-3-11 下午4:21:40
	 */
	public static File getDicFilePath( String fileName ){
		
		String defPath = System.getProperty("cnipr.dic.path");
		log.info("look up in cnipr.dic.path="+defPath);
		if(defPath == null) {
			URL url = DictionaryPathFactory.class.getClassLoader().getResource("conf");
			System.out.println( "url:"+url );
			if(url != null) {
				defPath = url.getFile();
				log.info("look up in classpath="+defPath);
			} else {
				defPath = System.getProperty("user.dir")+"/conf";
				log.info("look up in user.dir="+defPath);
			}
		}

		File defalutPath = new File(defPath+File.separator+fileName);
//		if(!defalutPath.exists()) {
//			log.warning("defalut dic path="+defalutPath+" not exist");
//			return null;
//		}else{
//			return defalutPath;
//		}
		return defalutPath;
	}
	
	
	/**
	 * 得到jar包里的词典文件流
	 * @param fileName
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-3-11 下午5:01:30
	 * 修改时间:2014-3-11 下午5:01:30
	 */
	public static InputStream getDicFileInputStream( String fileName ){
		
		String defPath = System.getProperty("cnipr.dic.path");
		if( defPath!=null ){
			log.info("cnipr.dic.path is:" + defPath + "|dic name is:"+fileName);
			System.out.println("cnipr.dic.path is:" + defPath + "|dic name is:"+fileName);	
			InputStream is = null;
			try {
				is = new FileInputStream(new File(defPath+File.separator+fileName));
			} catch (FileNotFoundException e) {
				log.info("dic file "+defPath+File.separator+fileName+" does not exist");
			}
			if( is==null ) System.out.println("dic name is:"+fileName+",it is null");	
			return is;
		}else{
			log.info("dic name is:"+fileName);
			System.out.println("dic name is:"+fileName);	
			InputStream is=DictionaryPathFactory.class.getClass().getResourceAsStream("/conf/"+fileName);
			if( is==null ) System.out.println("dic name is:"+fileName+",it is null");	
			return is;
		}
		
		
	}
}
