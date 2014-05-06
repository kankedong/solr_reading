package org.lxd.cniprSeg.test;

import java.io.IOException;
import org.junit.Test;
import org.lxd.cniprSeg.dic.BgramDictionaryReader;


public class BgramDictionaryReaderTest {
	
	@Test
	public void test() throws IOException
	{
		BgramDictionaryReader reader = new BgramDictionaryReader();
		reader.getDictionaryByFileName();
	}
	
	
	
}
