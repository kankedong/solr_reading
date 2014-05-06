package org.lxd.cniprSeg;

import java.util.HashMap;
import java.util.Map;


public class Utils {
	
	
public static HashMap<String,PosEnum> values;
	
	static{
		values = new HashMap<String,PosEnum>();
		values.put("start",PosEnum.start);//�?��
		values.put("end",PosEnum.end);//结束
		values.put("a",PosEnum.a);
		values.put("a",PosEnum.a);//形容�?
		values.put("ad",PosEnum.ad);//副形�?
		values.put("ag",PosEnum.ag);//形语�?
		values.put("an",PosEnum.an);//名形�?
		values.put("b",PosEnum.b);//区别�?
		values.put("c",PosEnum.c);//连词
		values.put("d",PosEnum.d);//副词
		values.put("dg",PosEnum.dg);//副语�?
		values.put("e",PosEnum.e);//叹词
		values.put("f",PosEnum.f);//方位�?
		values.put("g",PosEnum.g);//语素
		values.put("h",PosEnum.h);//前接成分
		values.put("i",PosEnum.i);//成语
		values.put("j",PosEnum.j);//�?��略语
		values.put("k",PosEnum.k);//后接成分
		values.put("l",PosEnum.l);//习用�?
		values.put("m",PosEnum.m);//数词
		values.put("n",PosEnum.n);//名词
		values.put("ng",PosEnum.ng);//名语�?
		values.put("nr",PosEnum.nr);//人名
		values.put("ns",PosEnum.ns);//地名
		values.put("nt",PosEnum.nt);//机构团体
		values.put("nx",PosEnum.nx);//字母专名
		values.put("nz",PosEnum.nz);//其他专名
		values.put("o",PosEnum.o);//拟声�?
		values.put("p",PosEnum.p);//介词
		values.put("q",PosEnum.q);//量词
		values.put("r",PosEnum.r);//代词
		values.put("s",PosEnum.s);//处所�?
		values.put("t",PosEnum.t);//时间�?
		values.put("tg",PosEnum.tg);//时语�?
		values.put("u",PosEnum.u);//助词
		values.put("ud",PosEnum.ud);//结构助词
		values.put("ug",PosEnum.ug);//时�?助词
		values.put("uj",PosEnum.uj);//结构助词�?
		values.put("ul",PosEnum.ul);//时�?助词�?
		values.put("uv",PosEnum.uv);//结构助词�?
		values.put("uz",PosEnum.uz);//时�?助词�?
		values.put("v",PosEnum.v);//动词
		values.put("vd",PosEnum.vd);//副动�?
		values.put("vg",PosEnum.vg);//动语�?
		values.put("vn",PosEnum.vn);//名动�?
		values.put("w",PosEnum.w);//标点符号
		values.put("x",PosEnum.x);//非语素字
		values.put("y",PosEnum.y);//语气�?
		values.put("z",PosEnum.z);//状�?�?
		values.put("unknow",PosEnum.unknow); //未知		
	}
	
	public static PosEnum getPosEnumValue( String pos )
	{
		PosEnum value = values.get(pos);
		if( value!=null )
		{
			return value;
		}
		else
		{
			return PosEnum.unknow;
		}
			
	}
	
	
	public static double transitionProbability( WordByWordSegNode preNode,WordByWordSegNode currentNode ,double totalFrequence )
	{
		double d1=0.2;
		double d2=0.7;
		double d3=0.1;
				
		if( currentNode.getStart()==0 ) //虚拟前驱节点
		{
			return 0.00001;
		}
		else
		{
			int preFrequence        = preNode.getMainDicNode().getFrequence();
			int currentFrequence    = currentNode.getMainDicNode().getFrequence();
			int transitionFrequence = 0;
			
			String word = currentNode.getWord();
			BgramDictionaryNode bgramDictionaryNode = preNode.getBgramDicNode();
			if( bgramDictionaryNode!=null )
			{
				Map<String, Integer> map = bgramDictionaryNode.getBgramFrequenceHashMap();
				if( map!=null )
				{
					Integer value = map.get(word);
					if( value!=null )
					{
						transitionFrequence = value;
					}
				}
			}

//			double p1 = d1*(preNode.getMainDicNode().getFrequence()/totalFrequence);
			double p1 = d1*(preFrequence/totalFrequence);
			double p3 = d3*(currentFrequence/totalFrequence);
			double p2 = 0;
			
			if( preFrequence==0 )
			{
				p2 = 0;
			}
			else
			{
				p2 = d2*(transitionFrequence/preFrequence);
			}
			
			//
			
			if( preFrequence==0&&currentFrequence==0 )
			{
				return 0.00001; 
			}
			
			return (p1+p2+p3);
		}
	}
	
	
	
//	private double transitionProbability( WordByWordSegNode preNode,WordByWordSegNode currentNode ,double totalFrequence )
//	{
//		double d1=0.099999999009;
//		double d2=0.9;
//		double d3=0.000000000000009;
//		
//		if( currentNode.getStart()==0 ) //虚拟前驱节点
//		{
//			return 0.0001;
//		}
//		else
//		{
//			double preFrequence        = preNode.getMainDicNode().getFrequence();
//			double transitionFrequence = 0;
//			
//			String word = currentNode.getWord();
//			Map<String, Integer> map = preNode.getBgramDicNode().getBgramFrequenceHashMap();
//			if( map!=null )
//			{
//				Integer value = map.get(word);
//				if( value!=null )
//				{
//					transitionFrequence = value;
//				}
//			}
//			
//			double p1 = d1*(preNode.getMainDicNode().getFrequence()/totalFrequence);
//			double p2 = 0;
//			
//			if( preFrequence==0 )
//			{
//				p2 = 0;
//			}
//			else
//			{
//				p2 = d2*(transitionFrequence/preFrequence);
//			}
//			
//			
//			
//			return (p1+p2+d3);
//		}
//	}
	
}
