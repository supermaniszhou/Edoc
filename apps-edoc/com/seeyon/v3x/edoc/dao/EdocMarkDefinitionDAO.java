package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.util.Constants;

/**
 * Data access object (DAO) for domain model class EdocMarkDefinition.
 * @see .EdocMarkDefinition
 * @author MyEclipse - Hibernate Tools
 */
public class EdocMarkDefinitionDAO extends BaseHibernateDao<EdocMarkDefinition> {

    private static final Log log = LogFactory.getLog(EdocMarkDefinitionDAO.class);

	/**
     * 方法描述：保存公文文号定义
     */
    public void saveEdocMarkDefinition(EdocMarkDefinition edocMarkDefinition) {
        log.debug("saving EdocMarkDefinition instance");
        try {
            super.save(edocMarkDefinition);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }
    
    /**
     * 方法描述：修改公文文号定义
     */
    public void updateEdocMarkDefinition(EdocMarkDefinition edocMarkDefinition) {
        log.debug("updating EdocMarkDefinition instance");
        try {
            super.update(edocMarkDefinition);
            log.debug("update successful");
        } catch (RuntimeException re) {
            log.error("update failed", re);
            throw re;
        }
    }
    
    public List<EdocMarkDefinition> getEdocMarkDefsByCategoryId(Long categoryId) {
    	log.debug("getEdocMarkDefsByCategoryId()");
    	String hsql = "from EdocMarkDefinition def where def.edocMarkCategory.id=?";
    	return super.findVarargs(hsql, categoryId);
    }
    
    /**
     * 方法描述：删除公文文号定义
     */
	public void deleteEdocMarkDefinition(EdocMarkDefinition edocMarkDefinition) {
        log.debug("deleting EdocMarkDefinitions instance");
        try {
            super.delete(edocMarkDefinition);
            log.debug("delete successful");
        } catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
    
	 /**
     * 方法描述：根据公文文号定义ID查询公文文号定义
     */
    public EdocMarkDefinition findEdocMarkDefinitionById(Long id) {
        log.debug("getting EdocMarkDefinitions instance with id: " + id);
        try {
            EdocMarkDefinition instance = (EdocMarkDefinition) super.get(id);                    
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }
    
    public List<EdocMarkDefinition> findEdocMarkDefinitionListById(List<Long> ids) {
    	String hsql = " from EdocMarkDefinition d left join d.edocMarkCategory where d.id in (:ids)";
    	Map map = new HashMap();
    	map.put("ids", ids);
    	
    	return super.find(hsql,-1,-1, map);
    }
    
    
    /**
     * 方法描述：根据属性查询公文文号定义
     */
    @SuppressWarnings("unchecked")
	public List<EdocMarkDefinition> findEdocMarkDefinitionsByProperty(String propertyName, Object value,boolean loadCategory) {
    	log.debug("finding EdocMarkDefinition instances with property: " + propertyName
    			+ ", value: " + value);
    	try {
    		String queryString = "from EdocMarkDefinition as model" + (loadCategory?" left join fetch model.edocMarkCategory":"") + " where model." 
    			+ propertyName + "= ?";    		
    		return super.findVarargs(queryString,value);
    	} catch (RuntimeException re) {
    		log.error("find by property name failed", re);
    		throw re;
    	}
    }    
    
//    /**
//     * 方法描述：查询全部公文文号
//     */
//    @SuppressWarnings("unchecked")
//	public List<Object []> queryAllEdocMarkDefinitions() {
//    	log.debug("finding All EdocMarkDefinition instances");
//    	try {
//    		StringBuffer queryString = new StringBuffer();
//    		queryString.append(" select def.id , def.expression, def.wordNo , ");
//    		queryString.append(" 		def.edocMarkCategory ");
//    		queryString.append(" from EdocMarkDefinition as def");
//    		
//    		Query queryObject = getSession().createQuery(queryString.toString());
//    		List<Object []> list = queryObject.list();
//    		return list;
//    	} catch (RuntimeException re) {
//    		log.error("finding All EdocMarkDefinition instances failed", re);
//    		throw re;
//    	}
//    }
    
    /**
     * @param loadCategory  是否加载edocMarkCategory
     */
    public List<EdocMarkDefinition> getMyEdocMarkDefs(String deptIds,boolean loadCategory,int markType) {
    	String hsql =""; 
    	Map<String,Object> namedParameter = new HashMap<String,Object>();
    	List<Long> depIds = new ArrayList<Long>();
    	StringBuffer inStr = new StringBuffer();
    	String[] tmp = deptIds.split(",");
    	for(String depId:tmp){
    		if(Strings.isNotBlank(depId)){
    			depIds.add(Long.valueOf(depId));
    		}
    	}
    	//当in的参数大于1000的时候，就超过数据库支持的极限了
    	if(depIds.size()<1000){
    		inStr.append("markAcl.deptId in (:deptId)");
    		namedParameter.put("deptId", depIds);
    	}else{
    		List<Long>[] array = Strings.splitList(depIds,1000);
            int len = array.length;
            for(int i = 0;i<len;i++){
                List<Long> list = array[i];
                inStr.append("markAcl.deptId in (:deptId").append(i).append(")");
                if(i<len-1){
                	inStr.append(" or ");
                }
                namedParameter.put("deptId"+i,list);
            }
    	}
    	hsql = "select distinct markDef from EdocMarkDefinition as markDef" + (loadCategory?" left join fetch markDef.edocMarkCategory":"") + ",EdocMarkAcl as markAcl where markDef.id=markAcl.edocMarkDefinition.id"
				+ " and "+inStr.toString() +" and markDef.status != " + Constants.EDOC_MARK_DEFINITION_DELETED + " and markDef.markType = :markType order by markDef.wordNo";
    	namedParameter.put("markType", markType);
    	//String countSql = "select count(*) " + hsql.substring(hsql.indexOf("from"),hsql.indexOf("order")).replace(" left join fetch markDef.edocMarkCategory", "");
    	//return super.findWithCount(hsql,countSql,namedParameter);
    	//注释上面两行代码的原因：上面查询的时候进行了分页查询，而此处不需要分页。
    	return super.find(hsql,-1,-1,namedParameter);
    }
	
    /**
     * 
     * @param domainId
     * @param loadCategory  是否加载edocMarkCategory
     * @return
     */
	public List<EdocMarkDefinition> getEdocMarkDefs1(Long domainId,boolean loadCategory) {
		String hsql = "from EdocMarkDefinition as markDef" +(loadCategory?" left join fetch markDef.edocMarkCategory":"") + " where markDef.domainId=? and markDef.status!=? order by markDef.wordNo";
		Object[] values = {domainId, Constants.EDOC_MARK_DEFINITION_DELETED};
		return super.findVarargs(hsql, values);
	}
	
    /**
     * 
     * @param domainId
     * @param loadCategory  是否加载edocMarkCategory
     * @return
     */
	public List<Object[]> getEdocMarkDefs2(Long domainId,Long depId,boolean loadCategory) {
		List<Object[]> list = new ArrayList<Object[]>();
		String hsql = "select markDef.id,markDef.wordNo,markDef.markType,markDef.expression,markDef.length,markCategory.id,markCategory.categoryName,markCategory.currentNo,markCategory.minNo,markCategory.maxNo,markCategory.readonly,markCategory.domainId,markCategory.yearEnabled,acl.aclType,acl.deptId from EdocMarkDefinition as markDef, EdocMarkCategory as markCategory,EdocMarkAcl as acl where markCategory.id = markDef.edocMarkCategory.id and acl.edocMarkDefinition.id = markDef.id and (markDef.domainId=? or acl.deptId=? or acl.deptId=?) and markDef.status!=? order by markDef.wordNo";
		Object[] values = {domainId,domainId,depId, Constants.EDOC_MARK_DEFINITION_DELETED};
		return super.findVarargs(hsql, values);
	}
	
	public List<Object[]> getEdocMarkDefsIncludeAccountAndAcl(Long domainId,List<Long> orgIds) {
		
	    List<Object[]> list = new ArrayList<Object[]>();
		orgIds.add(domainId);
		
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuilder hsql = new StringBuilder("select ");
		hsql.append("markDef.id,markDef.wordNo,markDef.markType,markDef.expression,markDef.length,markCategory.id,markCategory.categoryName,markCategory.codeMode")
           .append(",markCategory.currentNo,markCategory.minNo,markCategory.maxNo,markCategory.readonly,markCategory.domainId,markCategory.yearEnabled")
           .append(",acl.aclType,acl.deptId from EdocMarkDefinition as markDef, EdocMarkCategory as markCategory,EdocMarkAcl as acl ")
           .append(" where ")
           .append(" markCategory.id = markDef.edocMarkCategory.id ")
           .append(" and acl.edocMarkDefinition.id = markDef.id and ")
           .append("(markDef.domainId=:domainId ");
	   
       
		//Oracle数据超过1000报错兼容
       int pageSize = 999;
       int index = 0;
       int size = orgIds.size();
       int pageIndex = 0;
       while(index < size){
           int toIndex = Math.min(index + pageSize, size);
           
           List<Long> subList = orgIds.subList(index, toIndex);
           
           hsql.append(" or acl.deptId IN (:deptids"+pageIndex+") ");
           parameterMap.put("deptids" + pageIndex, subList);
           index = toIndex;
           pageIndex++;
       }
		
       hsql.append(") ")
           .append("and markDef.status != :stat order by markDef.wordNo");
		
		
	    parameterMap.put("domainId", domainId);
	    
	    parameterMap.put("stat", Constants.EDOC_MARK_DEFINITION_DELETED);
	    
	    List l = DBAgent.find(hsql.toString(), parameterMap);
	    return l;
	}
	
	
	public Boolean containEdocMarkDef(String wordNo, long domainId,int markType) {
		String hsql = "from EdocMarkDefinition as markDef where markDef.domainId=? and markDef.status!=? and markDef.wordNo=? and markDef.markType=? ";
		Object[] values = {domainId, Constants.EDOC_MARK_DEFINITION_DELETED,wordNo,markType};
		Type[] types = {Hibernate.LONG,Hibernate.SHORT,Hibernate.STRING,Hibernate.INTEGER};
		/*List<EdocMarkDefinition> markDefs = super.find(hsql, values);
		if (markDefs != null && markDefs.size() > 0) {
			return true;
		}
		return false;*/
		//直接按count查询
		return super.getQueryCount(hsql, values, types)>0;
	}
	
	public Boolean containEdocMarkDef(long markDefId, String wordNo, long domainId,int markType) {
		String hsql = "from EdocMarkDefinition as markDef where markDef.domainId=? and markDef.status!=? and markDef.wordNo=? and markDef.id!=? and markDef.markType=? ";
		Object[] values = {domainId, Constants.EDOC_MARK_DEFINITION_DELETED, wordNo,markDefId,markType};
		/*List<EdocMarkDefinition> markDefs = super.find(hsql, values);
		if (markDefs != null && markDefs.size() > 0) {
			return true;
		}
		return false;*/
		//直接按count查询
		Type[] types = {Hibernate.LONG,Hibernate.SHORT,Hibernate.STRING,Hibernate.LONG,Hibernate.INTEGER};
		return super.getQueryCount(hsql, values, types)>0;
	}
	
	public void updateMarkDefinitionStatus(long definitionId, short status){
        log.debug("logical deleting EdocMarkDefinitions instance");
		try{
		String hsql = "update EdocMarkDefinition as markDef set markDef.status = ? where markDef.id = ?";
		super.bulkUpdate(hsql,null,status,definitionId);
			log.debug("logical delete successful");
		}catch(RuntimeException re){
			log.debug("logical delete failed", re);
		}
	}
	 /**
     * 判断公文文号定义是否已经被删除
     * @param	文号定义表ID
     * @return 0:已经删除  1：存在
     */
	public int judgeEdocDefinitionExsit(Long definitionId){
		String hql="from EdocMarkDefinition as markDef where markDef.status!=2 and id=?";
		int count=super.getQueryCount(hql, new Object[]{definitionId}, new Type[]{Hibernate.LONG});
		if(count>0){
			return 1;
		}else {
			return 0;
		}
	}
}