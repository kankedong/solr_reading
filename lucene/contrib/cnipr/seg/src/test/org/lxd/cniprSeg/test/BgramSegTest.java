package org.lxd.cniprSeg.test; 

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.lxd.cniprSeg.BgramSeg;
import org.lxd.cniprSeg.WordByWordSegNode;


/** 
 * 类说明 
 * @author  添加者 E-mail: lixaodong@gintong.com;
 * @author  修改者 E-mail:暂无
 * @version 创建时间：2013-9-25 下午3:20:24;
 * @version 修改时间:暂无 
 */
public class BgramSegTest {
	
	@Test
	public void test()
	{
//		String sentence = "刘毅：男，1968年6月出生，毕业于北京师范大学，本科。";
		
		String sentence = "刘毅：男，1968年6月出生，毕业于北京师范大学，本科。历任中国基建物资总公司会计、中国华通物产集团公司期货部风险控制部经理、中国电子信息产业发展研究院财务处副处长。";
		
		sentence = "瞿佳,女,1964年6月出生,研究生学历,毕业于对外经济贸易大学国际工商管理学院,高级经济师。历任中国电子报社人事处副处长、中国电子信息产业发展研究院人事处副处长,本公司总经理助理兼人力资源部总监。2002年5月至2009年6月任公司第六届监事会监事、第七届监事会监事。现任公司董事会秘书。MANAGER_SEPARATOR";
		
//		sentence = "办理人民币存、贷、结算、汇兑业务;aaa人民币票据承兑和贴现;各项信托业务;经监管机构批准发行或买卖人民币有价证券;发行金融债券;代理发行、代理兑付、承销政府债券;买卖政府债券;外汇存款、汇款;境内境外借款;从事同业拆借;外汇借款;外汇担保;在境内境外发行或代理发行外币有价证券;买卖或代客买卖外汇及外币有价证券、自营外汇买卖;贸易、非贸易结算;办理国2内结算;国际结算;外币票据的承兑和贴现;外汇贷款;资信调查、咨询、见证业务;保险兼业代理业务;代理收付款项;黄金进口业务;提供信用证服务及担保;提供保管箱服务;外币兑换;结汇、售汇;信用卡业务;经有关监管机构批准或允许的其他业务。COMPANY_SEPARATOR";
//		
//		sentence = "2012年8月2日,公司名称由深圳发展银行股份有限公司变更为平安银行股份有限公司。公司是中国第一家面向社会公众公开发行股票并上市的商业银行。深发展于1987年5月10日以自由认购形式首次向社会公开发售人民币普通股,并于1987年12月22日正式宣告成立。COMPANY_SEPARATOR";
//		
////		sentence = "广东省深圳市罗湖区深南东路5047号";
//		
//		sentence = "5047号";
//		
//		sentence = "2012年8月2日,公司名称由深圳发展银行股份有限公司变更为平安银行股份有限公司。公司是中国第一家面向社会公众公开发行股票并上市的商业银行。深发展于1987年5月10日以自由认购形式首次向社会公开发售人民币普通股,并于1987年12月22日正式宣告成立。COMPANY_SEPARATOR";
//		
//		sentence = "我公司专为企业提供资金帮助，公司的资金是自有资金，直投，不和银行合作，额度：1000万元---几亿人民币。年限1至5年。合作方式：股权和债权抵押都可以。项目的地域和行业不限，项目前期自己已经有投入而不是空壳项目。项目资料可发我的邮箱 mozhenyue@yeah.net 联系人：莫总监 电话 1348877760";
//		
//		sentence = "办理人民币存、贷、结算、汇兑业务;人民币票据承兑和贴现;各项信托业务;经监管机构批准发行或买卖人民币有价证券;发行金融债券;代理发行、代理兑付、承销政府债券;买卖政府债券;外汇存款、汇款;境内境外借款;从事同业拆借;外汇借款;外汇担保;在境内境外发行或代理发行外币有价证券;买卖或代客买卖外汇及外币有价证券、自营外汇买卖;贸易、非贸易结算;办理国2内结算;国际结算;外币票据的承兑和贴现;外汇贷款;资信调查、咨询、见证业务;保险兼业代理业务;代理收付款项;黄金进口业务;提供信用证服务及担保;提供保管箱服务;外币兑换;结汇、售汇;信用卡业务;经有关监管机构批准或允许的其他业务。COMPANY_SEPARATOR";
//		
////		String sentence = "2009年，金桐网这家互联网公司成立了";//数字、字母的处理问题需要进一步解决
//		
////		sentence = "2009年，金桐网这家互联网公司，它成立了";//数字、字母的处理问题需要进一步解决
//		
//		sentence = "2009年，金桐网这家互联网公司,它成立了";//倒数第一个逗号是英文中的逗号时，存在问题，计划将所有中文标点对应的英文标点，以相同方式引入到词典文件中
//
//		
//		sentence = "矿产资源";
		
//		sentence = "★您在当地是全国最多景区免门票旅游服务提供商，为车主提供更广的旅游服务★您在当地拥有提供最优惠车务服";
		
//		sentence = "航天与国防";
		
		sentence = "11航天与aaa111国防";
		
		sentence = "appfog不再提供2g的内存了.只有512m了.所以新词发现和nlp方式的分词无法演示.谁有大内存的免费app引擎可以给我推荐下!";
		
		
//		sentence = "航天与国防";

			
//			BgramSeg seg = new BgramSeg(sentence);
//			
//			List<List<WordByWordSegNode>> list = seg.getSegResult();
//			
//			for( List<WordByWordSegNode> resultList:list )
//			{
//				System.out.println( "" );
//				for( int i=resultList.size()-1;i>=0;i-- )
//				{
//					System.out.println( "[word:"+resultList.get(i).getWord()+"|start:"+resultList.get(i).getStart()+"|end:"+resultList.get(i).getEnd()+"|accumulationProbability:"+resultList.get(i).getAccumulationProbability() );					
//				}
//			}
//			
//			for( List<WordByWordSegNode> resultList:list )
//			{
//				seg.getBestSegTagging(resultList);
//			}
			
			
			
	
	}
	
}
 