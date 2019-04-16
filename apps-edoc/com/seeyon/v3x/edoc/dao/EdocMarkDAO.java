package com.seeyon.v3x.edoc.dao;

//import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;

import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.ctp.util.Strings;
/**
 * Data access object (DAO) for domain model class EdocMark.
 * @see .EdocMark
 * @author MyEclipse - Hibernate Tools
 */
public class EdocMarkDAO extends BaseHibernateDao<EdocMark> {

    private static final Log log = LogFactory.getLog(EdocMarkDAO.class);	

    /**
     * 方法描述：保存公文文号
     */
    public void save(EdocMark edocMark) {
        log.debug("saving EdocMark instance");
        try {
            super.save(edocMark);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }
    
    /**
     * 判断文号是否被占用
     * @param edocId     公文id
     * @param edocMark   文号
     * @return   true 被占用 false 未占用
     */
    public boolean isUsed(Long edocId){
    	
    	boolean used = false;    	
    	String hsql = "select count(*) as count from EdocMark as mark where mark.edocId=?";
    	List list = super.findVarargs(hsql, edocId);
    	if (list != null && !list.isEmpty() && list.size() > 0) {
    		if (list.get(0) != null) {
    			int count = (Integer)list.get(0);    			
    			used = count > 0;    			
    		}
    	}
    	return used;
    }
    
    /**
     * 判断文号是否被占用
     * @param edocMark   文号
     * @return   true 被占用 false 未占用
     */
    @SuppressWarnings("unchecked")
    public boolean isUsed(String markStr,String edocId,String orgAccountId){
    	
     	Long edocSummaryId=0L;
    	Long orgAcount = 0l;
    	if(Strings.isNotBlank(orgAccountId)) {
    	    orgAcount = Long.valueOf(orgAccountId);
    	}
    	boolean used = false; 
    	try{edocSummaryId=Long.parseLong(edocId);}catch(Exception e){
    	    log.error("", e);
    	}
    	String hsql = "select edocId as edocId from EdocMarkHistory as mark where mark.docMark=? and mark.edocId<>? and mark.edocId<>-1";
    	
    	markStr = markStr.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
    	
//    	if(markStr!=null){
//    		markStr=SQLWildcardUtil.escape(markStr);
//    	}
    	List<Long> edocIds = new ArrayList<Long>();
    	List<Object> list = (List<Object>)super.findVarargs(hsql, markStr,edocSummaryId);
    	
    	if (Strings.isNotEmpty(list)) {
    		for(Object id : list){
    		    edocIds.add((Long)id);
    		}
    	}
    	
    	if(Strings.isNotEmpty(edocIds)){
    	    List<Long>[]  arr = Strings.splitList(edocIds, 1000);
    	    for(List<Long> _list : arr){
    	        used = isExsitEdocInAccount(_list,orgAcount);
    	        if(used){
    	            break;
    	        }
    	    }
    	}
    	return used;
    }

    private boolean isExsitEdocInAccount(List<Long> ids,Long orgAccountId){
        String hql = " from EdocSummary where id in(:ids) and orgAccountId = :orgAccountId ";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", ids);
        params.put("orgAccountId", orgAccountId);
        int count = super.count(hql, params);
        return count > 0;
    }
    public List<EdocMark> findEdocMarkByCategoryId(Long categoryId){
    	List<EdocMark> list = super.findVarargs("from EdocMark as mark where mark.categoryId = ? order by mark.docMarkNo", categoryId);
    	return list;
    }
    
    public List<EdocMark> findEdocMarkByCategoryId(Long categoryId,Integer docMarkNo){
    	List<EdocMark> list = super.findVarargs("from EdocMark as mark where mark.categoryId = ? and mark.docMarkNo=? order by mark.docMarkNo", categoryId,docMarkNo);
    	return list;
    }
    /**
     * 判断是否有其他公文使用与此流水（包括大流水和小流水）相关的文号
     * @param categoryId     流水ID
     * @param docMarkNo		 docMark表的文号的流水值
     * @param edocId		 公文ID
     * @return               true：有其他公文使用此流水，false:无其他公文使用此流水。
     */
    public boolean judgeOtherEdocUseCategroy(Long categoryId,Integer docMarkNo,Long edocId){
    	String hql="from EdocMark as mark where mark.categoryId = ? and mark.docMarkNo=? and edocId!=?";
    	return super.getQueryCount(hql, new Object[]{categoryId,docMarkNo,edocId}, new Type[]{Hibernate.LONG,Hibernate.INTEGER,Hibernate.LONG})>0;
    }
    public void deleteEdocMarkByEdocId(Long edocId) {
    	String hql="delete from EdocMark as mark where mark.edocId = :edocId";
    	Map<String,Object> nameParameters=new HashMap<String,Object>();
    	nameParameters.put("edocId", edocId);
    	super.bulkUpdate(hql, nameParameters);
    }
    public void deleteEdocMarkByCategoryIdAndNo(Long categoryId,Integer docMarkNo){
    	String hql="delete from EdocMark as mark where mark.categoryId = :categoryId and mark.docMarkNo=:docMarkNo";
    	Map<String,Object> nameParameters=new HashMap<String,Object>();
    	nameParameters.put("categoryId", categoryId);
    	nameParameters.put("docMarkNo",docMarkNo);
    	super.bulkUpdate(hql, nameParameters);
    }
    public List<EdocMark> findEdocMarkByEdocIdOrDocMark(Long edocId,String docMark){
    	String _docMark = docMark;
        if(_docMark!=null){
    		_docMark=SQLWildcardUtil.escape(_docMark.trim());
    	}else{
    		return null;
    	}
    	List<EdocMark> list = super.findVarargs("from EdocMark as mark where mark.edocId = ? or mark.docMark=?", edocId,_docMark);
    	return list;
    }
    
    public List<EdocMark> findEdocMarkByEdocIdOrDocMark(Long edocId,String docMark,String docMark2){
    	String _docMark = docMark;
        if(_docMark!=null){
    		_docMark=SQLWildcardUtil.escape(_docMark.trim());
    	}else{
    		_docMark = "";
    	}
    	if(docMark2!=null){
    		docMark2=SQLWildcardUtil.escape(docMark2.trim());
    	}else{
    		docMark2 = "";
    	}
    	List<EdocMark> list = super.findVarargs("from EdocMark as mark where mark.edocId = ? or mark.docMark=? or mark.docMark=?", edocId,_docMark,docMark2);
    	return list;
    }
    
    public List<EdocMark> findEdocMarkByMarkDefId(Long markDefId){
    	List<EdocMark> list = super.findVarargs("from EdocMark as mark where mark.edocMarkDefinition.id = ? order by mark.docMarkNo", markDefId);
    	return list;
    }
    /**
     * 断号查询，去掉重复
     * @param markDefId
     * @return
     */
    public List<EdocMark> findEdocMarkByMarkDefId4Discontin(Long markDefId){
    	
    	List<EdocMark> list = findEdocMarkByMarkDefId(markDefId);
    	List<EdocMark> nlist = new ArrayList<EdocMark>();
    	Hashtable<String,String> hs=new Hashtable<String,String>();
    	
    	for(EdocMark em:list)
    	{
    		if(!hs.containsKey(em.getDocMark()))
    		{
    			hs.put(em.getDocMark(),em.getDocMark());
    			nlist.add(em);
    		}
    	}    	
    	return nlist;
    }
    
    public List<EdocMark> findEdocMarkByEdocSummaryId(Long edocSummaryId){
        
        //几个文号同时使用一个大流水，文号就存在多个
        List<EdocMark> list = super.findVarargs("from EdocMark as mark where mark.edocId = ? order by mark.docMarkNo", edocSummaryId);
        /*if(list!=null && list.size()>0)
        {
            return list.get(0);
        }*/
        return list;
    }
    /**
     * 根据公文文号和公文ID来查找edoc_mark表中的记录。
     * @param edocSummaryId 公文ID
     * @param edoc_mark	    公文文号
     * @param markNum	    联合发文的时候：第一套公文文号还是第二套。
     * @return
     */
    public EdocMark findEdocMarkByEdocSummaryIdAndEdocMark(Long edocSummaryId,String edocMark,int markNum){
    	if(edocMark!=null){
    		edocMark=SQLWildcardUtil.escape(edocMark.trim());
    	}
    	List<EdocMark> list = super.findVarargs("from EdocMark as mark where mark.edocId = ? and mark.docMark=? and mark.markNum=?  order by mark.docMarkNo desc ", edocSummaryId,edocMark,markNum);
    	if(list!=null && list.size()>0)
    	{
    		return list.get(0);
    	}
    	return null;
    }
    public List<EdocMark> findEdocMarkByEdocSummaryIdAndNum(Long edocSummaryId,int markNum){
    	List<EdocMark> list = super.findVarargs("from EdocMark as mark where mark.edocId = ? and mark.markNum=? order by mark.docMarkNo", edocSummaryId,markNum);
    	if(list!=null && list.size()>0)
    	{
    		return list;
    	}
    	return null;
    }
    
   public void deleteEdocMarkByIds(List<Long> ids){
	   String hql="delete from EdocMark where id in(:ids)";
	   Map<String,Object> nameParameters=new HashMap<String,Object>();
	   nameParameters.put("ids", ids);
	   super.bulkUpdate(hql, nameParameters);
   }
}