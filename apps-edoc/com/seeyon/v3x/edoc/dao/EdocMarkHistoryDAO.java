package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;

import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
/**
 * Data access object (DAO) for domain model class EdocMarkHistory.
 * @see .EdocMarkHistory
 * @author MyEclipse - Hibernate Tools
 */
public class EdocMarkHistoryDAO extends BaseHibernateDao<EdocMarkHistory> {

    private static final Log log = LogFactory.getLog(EdocMarkHistoryDAO.class);	

    /**
     * 方法描述：保存公文文号历史
     */
    public void save(EdocMarkHistory edocMarkHistory) {
        log.debug("saving EdocMarkHistory instance");
        super.save(edocMarkHistory);
//        try {
//            getSession().save(edocMarkHistory);
//            log.debug("save successful");
//        } catch (RuntimeException re) {
//            log.error("save failed", re);
//            throw re;
//        }
    }
    
    public void deleteEdocMarkHistoryByEdocId(Long edocId) {
    	String hql="delete from EdocMarkHistory as mark where mark.edocId = :edocId";
    	Map<String,Object> nameParameters=new HashMap<String,Object>();
    	nameParameters.put("edocId", edocId);
    	super.bulkUpdate(hql, nameParameters);
    }
    
    /**
     * 查询相同文号数
     * @param edocMark
     * @param edocId
     * @return
     */
    public int getCount(String edocMark,Long edocId) {
    	StringBuffer hql = new StringBuffer("from EdocMarkHistory where docMark=?");
    	List<Object> values = new ArrayList<Object>();
    	List<Type> typeList = new ArrayList<Type>();
    	values.add(edocMark);
    	typeList.add(Hibernate.STRING);
    	if(edocId != null) {
    		hql.append(" and edocId<>?");
    		values.add(edocId);
    		typeList.add(Hibernate.LONG);
    	}
    	Type[] types = new Type[typeList.size()];
    	int i = 0;
    	for(Type type: typeList) {
    		types[i] = type;
    		i++;
    	}
    	return super.getQueryCount(hql.toString(), values.toArray(), types);
    }
    
    public EdocMarkHistory findEdocMarkHistoryByEdocSummaryIdAndEdocMark(Long edocSummaryId,String edocMark,int markNum){
    	
        String _edocMark = edocMark;
        if(_edocMark!=null) {
    		_edocMark=SQLWildcardUtil.escape(_edocMark.trim());
    	}
    	List<EdocMarkHistory> list = super.findVarargs("from EdocMarkHistory as mark where mark.edocId = ? and mark.docMark=? and mark.markNum=?  order by mark.docMarkNo desc ", edocSummaryId,_edocMark,markNum);
    	if(list!=null && list.size()>0) {
    		return list.get(0);
    	}
    	return null;
    }
    
    /**
     * @方法描述: 根据公文id删除文号
     * @param summaryId 公文Id
     */
    
    public void deleteMarkIdBySummaryId(Long summaryId){
    	 String hql="delete from EdocMarkHistory where edocId =?";
  	   super.bulkUpdate(hql, null,new Object[]{summaryId});
    }
    
}