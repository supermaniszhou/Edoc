/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocMarkDAO;
import com.seeyon.v3x.edoc.dao.EdocMarkHistoryDAO;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
import com.seeyon.v3x.edoc.domain.EdocMarkReserve;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.SharedWithThreadLocal;
import com.seeyon.v3x.edoc.webmodel.EdocMarkNoModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public class EdocMarkManagerImpl implements EdocMarkManager {
	private static final Log LOGGER = LogFactory.getLog(EdocMarkManagerImpl.class);
	private EdocMarkDAO edocMarkDAO;	
	private EdocMarkHistoryDAO edocMarkHistoryDAO;
	private EdocSummaryDao edocSummaryDao;
	private EdocMarkCategoryManager edocMarkCategoryManager;
	private EdocMarkDefinitionManager edocMarkDefinitionManager;
	private EdocMarkReserveManager edocMarkReserveManager;
	private EdocMarkHistoryManager edocMarkHistoryManager;
	
	public List<EdocMark> findListByMarkDefineId(Long markDefineId) {
		return (List<EdocMark>)edocMarkDAO.findVarargs("from EdocMark where edocMarkDefinition.id=?", markDefineId);
	}
	
	public void save(List<EdocMark> edocMarkList) {
		this.edocMarkDAO.savePatchAll(edocMarkList);
	}
	
	/**
     * 方法描述：保存公文文号
     */
    public void save(EdocMark edocMark) {
    	this.edocMarkDAO.save(edocMark);
    }
    
    /**
     * 根据ID返回EdocMark对象
     * @param edocMarkId  ID
     * @return
     */
    public EdocMark getEdocMark(Long edocMarkId){
    	return this.edocMarkDAO.get(edocMarkId);
    }
    
    /**
     * 方法描述：保存公文文号，并更新当前值
     * @param edocMark  公文文号对象
     * @param catId     公文类别id
     * @param currentNo 提供给用户选择的公文文号的当前值
     */
    public void save(EdocMark edocMark,Long catId,int currentNo){
    	this.save(edocMark);
        //    	更新当前值
    	this.edocMarkCategoryManager.increaseCurrentNo(catId, currentNo);
    }
    
    /**
     * 删除公文文号
     * @param id  公文文号id
     */
    public void deleteEdocMark(long id){
    	this.edocMarkDAO.delete(id);
    }
    
    /**
     * 方法描述：拟文时创建文号
     * 查询当前文号是否被使用，如果已经被使用，则不创建文号记录 add by handy,2007-10-16
     */    
    public void createMark(Long definitionId, Integer currentNo, String docMark, Long edocId,int markNum) {
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	User user = AppContext.getCurrentUser();
    	
    	//----------性能优化，从SharedWithThreadLocal中取文号定义对象
    	EdocMarkDefinition markDef = edocMarkDefinitionManager.queryMarkDefinitionById(definitionId);
    	
    	//不要这样获取， 会导致 事物ID冲突
//    	EdocMarkDefinition markDef = SharedWithThreadLocal.getMarkDefinition(definitionId);
    	if(markDef == null){
    		markDef = edocMarkDefinitionManager.queryMarkDefinitionById(definitionId);
    	}
    	 
    	if(markDef==null) {
    		return;
    	}
    	
    	EdocMark edocMark = new EdocMark();
    	edocMark.setIdIfNew();
    	edocMark.setEdocMarkDefinition(markDef);
    	edocMark.setCreateTime(new Date());
    	edocMark.setEdocId(edocId);
    	edocMark.setDocMark(docMark);
    	edocMark.setCreateUserId(user.getId());
    	edocMark.setStatus(Constants.EDOC_MARK_USED);    	
    	edocMark.setDocMarkNo(currentNo);
    	EdocMarkCategory edocMarkCategory = markDef.getEdocMarkCategory();
    	edocMark.setCategoryId(edocMarkCategory.getId());
    	edocMark.setDomainId(user.getLoginAccount());
    	edocMark.setMarkNum(markNum);
    	this.save(edocMark);
    	
    	List<EdocMarkDefinition> mds=edocMarkDefinitionManager.getEdocMarkDefinitionsByCategory(edocMarkCategory.getId());
    	if(mds!=null && mds.size()>1) {//多个公文模板共用一个流水号
    		for(EdocMarkDefinition def:mds) {
    			if(definitionId.longValue()==def.getId().longValue()){continue;}
    			edocMark = new EdocMark();
    	    	edocMark.setIdIfNew();
    	    	edocMark.setEdocMarkDefinition(def);
    	    	edocMark.setCreateTime(new Date());
    	    	edocMark.setEdocId(edocId);
    	    	edocMark.setDocMark(edocMarkDefinitionManager.markDef2Mode(def,null,currentNo).getMark());
    	    	edocMark.setCreateUserId(user.getId());
    	    	edocMark.setStatus(Constants.EDOC_MARK_USED);    	
    	    	edocMark.setDocMarkNo(currentNo);
    	    	// 这里不需要再次读取分类，下面行可以注释
    	    	edocMarkCategory = def.getEdocMarkCategory();
    	    	edocMark.setCategoryId(edocMarkCategory.getId());
    	    	edocMark.setDomainId(user.getLoginAccount());
    	    	edocMark.setMarkNum(markNum);
    	    	this.save(edocMark);
    		}
    	}
    	
    	
    	
    	int oldNo = edocMarkCategory.getCurrentNo();
		
		int addOneNo = edocMarkHistoryManager.increatementCurrentNo(markDef,currentNo,edocMarkCategory);
		
		
    	String markStr = edocMarkDefinitionManager.markDef2Mode(markDef,null,addOneNo).getMark();
    	
    	
    	boolean isUsed = isUsed(markStr ,"0", String.valueOf(AppContext.currentAccountId()));
    	
    	int count = 0;
    	
    	while(isUsed && count <1000){
    		
    		count++;
    		
    		addOneNo = edocMarkHistoryManager.increatementCurrentNo(markDef,addOneNo,edocMarkCategory);
    		
	    	markStr = edocMarkDefinitionManager.markDef2Mode(markDef,null,addOneNo).getMark();
	    	
	    	isUsed = isUsed(markStr ,"0", String.valueOf(AppContext.currentAccountId()));
	    	
    	}
    	
    	boolean isIncreatement = addOneNo >= oldNo;
    	
    	
    	
    	if(isIncreatement){
    	    //有变化的时候才更新，否则一个文单里面有2个文号的情况，后一次的值回覆盖前面的
    		edocMarkCategory.setCurrentNo(addOneNo);
    		edocMarkCategoryManager.updateCategory(edocMarkCategory);
    	}
    	setEdocMarkDefinitionPublished(markDef);
    }

	private void setEdocMarkDefinitionPublished(EdocMarkDefinition markDef) {
		//设置已经使用。
    	if(markDef.getStatus().shortValue() == Constants.EDOC_MARK_DEFINITION_DRAFT){
    		markDef.setStatus(Constants.EDOC_MARK_DEFINITION_PUBLISHED);
//    		edocMarkDefinitionManager.saveMarkDefinition(markDef);
    		edocMarkDefinitionManager.updateMarkDefinition(markDef);
    	}
	}
    //修改文单的时候，若修改了文号，断开当前公文与前文号的联系。
    public void disconnectionEdocSummary(long edocSummaryId,int markNum)
    {
    	List<EdocMark> list = edocMarkDAO.findEdocMarkByEdocSummaryIdAndNum(edocSummaryId,markNum);
    	if(list!=null&&list.size()!=0){
    		for(EdocMark edocMark:list){
    			if(edocMark==null)continue;
    			else{
    				edocMark.setEdocId(-1L);
    			}
//    			edocMarkDAO.save(edocMark);
    			edocMarkDAO.update(edocMark);
    		}
    	}
    }
    
    /**
     * 创建手工输入的公文文号。
     * @param docMark 公文文号
     * @param edocId 公文id
     */
    public void createMark(String docMark, Long edocId,int markNum) {
//    	检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	User user = AppContext.getCurrentUser();
    	EdocMark edocMark = new EdocMark();
    	edocMark.setIdIfNew();
    	edocMark.setCreateTime(new Date());
    	edocMark.setEdocId(edocId);
    	edocMark.setDocMark(docMark);
    	edocMark.setCreateUserId(user.getId());
    	edocMark.setStatus(Constants.EDOC_MARK_USED);
    	edocMark.setDocMarkNo(0);
    	edocMark.setCategoryId(0L);    	
    	edocMark.setDomainId(user.getLoginAccount());
    	edocMark.setMarkNum(markNum);
    	this.save(edocMark);
    }
    
    /**
     * 方法描述：拟文时创建文号,选断号的情况下
     * 文号如果已经被使用了呢？？？？？？？？add by handy,2007-10-16
     */    
//    public void createMarkByChooseNo(Long edocMarkId, Long edocId,int markNum) {
//   	检查公文年度编号变更
//    	EdocHelper.checkDocmarkByYear();
//		EdocMark edocMark = edocMarkDAO.get(edocMarkId);
//		edocMark.setEdocId(edocId);
//		edocMark.setStatus(Constants.EDOC_MARK_USED);
//		edocMark.setMarkNum(markNum);
//		this.save(edocMark);    	
//    } 
    /**
     * 断号也直接插入条记录 add at310sp2
     */
    public void createMarkByChooseNo(Long edocMarkId, Long edocId,int markNum) {
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	User user = AppContext.getCurrentUser();
		EdocMark mark = edocMarkDAO.get(edocMarkId);
		EdocMark edocMark=new EdocMark();
		edocMark.setIdIfNew();
		edocMark.setEdocMarkDefinition(mark.getEdocMarkDefinition());
		edocMark.setCreateTime(new Date());
		edocMark.setEdocId(edocId);
		edocMark.setDocMark(mark.getDocMark());
		edocMark.setCreateUserId(user.getId());
		edocMark.setStatus(Constants.EDOC_MARK_USED);
		edocMark.setDocMarkNo(mark.getDocMarkNo());
		edocMark.setCategoryId(mark.getCategoryId());
		edocMark.setDomainId(user.getLoginAccount());
		edocMark.setMarkNum(markNum);
		this.save(edocMark);  
    } 
    
    /**
     * 断号也直接插入条记录 add at310sp2
     */
    public void createMarkByChooseReserveNo(Long edocMarkId, Long edocId, Integer markNumber, int markNum) {
    	EdocMarkDefinition edocMarkDefinition = edocMarkDefinitionManager.getMarkDefinition(edocMarkId);
    	if(edocMarkDefinition == null) return;
    	EdocMarkReserveVO reserveVO = edocMarkReserveManager.getMarkReserveByFormat(edocMarkDefinition, markNumber);
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	User user = AppContext.getCurrentUser();
		//EdocMark mark = edocMarkDAO.get(edocMarkId);
		EdocMark edocMark=new EdocMark();
		edocMark.setIdIfNew();
		edocMark.setEdocMarkDefinition(edocMarkDefinition);
		edocMark.setCreateTime(new Date());
		edocMark.setEdocId(edocId);
		edocMark.setDocMark(reserveVO.getReserveNo());
		edocMark.setCreateUserId(user.getId());
		edocMark.setStatus(Constants.EDOC_MARK_USED);
		edocMark.setDocMarkNo(markNumber);
		edocMark.setCategoryId(edocMarkDefinition.getEdocMarkCategory().getId());
		edocMark.setDomainId(user.getLoginAccount());
		edocMark.setMarkNum(markNum);
		this.save(edocMark);  
    }
    
    /**
     * 判断文号是否被占用
     * @param edocId     公文id
     * @param edocMark   文号
     * @return   true 被占用 false 未占用
     */
    public boolean isUsed(Long edocId){
//    	检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	return edocMarkDAO.isUsed(edocId);
    }
    
    public boolean isUsed(String markStr,String edocId,String summaryOrgAccountId)
    {
    	//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
    	if(Strings.isBlank(summaryOrgAccountId)|| "0".equals(summaryOrgAccountId)){
    		LOGGER.error("文号判断重复校验，单位ID为空："+markStr+"|"+edocId+"|"+summaryOrgAccountId);
    	}
    	return edocMarkDAO.isUsed(markStr,edocId,summaryOrgAccountId);    	
    }
    
    /**
     * 按年度把公文文号归为最小值
     */
    
    public void turnoverCurrentNoAnnual(){
    	
    	User user = AppContext.getCurrentUser();
    	edocMarkCategoryManager.turnoverCurrentNoAnnual();
    //	List<EdocMarkCategory> list = edocMarkCategoryManager.findByTypeAndDomainId(Constants.EDOC_MARK_CATEGORY_BIGSTREAM,user.getLoginAccount());
//    	List<EdocMarkCategory> list = edocMarkCategoryManager.findAll();
//    	for(EdocMarkCategory category:list){
//    		if(category.getYearEnabled()){
//	    		category.setCurrentNo(category.getMinNo());
//	    		edocMarkCategoryManager.updateCategory(category);
//    		}
//    	}
    }
    
    public List<EdocMarkNoModel> getDiscontinuousMarkNos(Long edocMarkDefinitionId){
    	List<EdocMarkNoModel> results = new ArrayList<EdocMarkNoModel>();
    	List<EdocMark> edocMarks = edocMarkDAO.findEdocMarkByMarkDefId4Discontin(edocMarkDefinitionId);
    	for (EdocMark edocMark:edocMarks) {
    		EdocMarkNoModel model = new EdocMarkNoModel();    		
    		model.setEdocMarkId(edocMark.getId());
    		model.setMarkNo(edocMark.getDocMark());
    		model.setMarkNumber(edocMark.getDocMarkNo());
    		results.add(model);
    	}
    	    	
    	return results;
    } 
    
    public List<EdocMark> findByCategoryAndNo(Long categoryId,Integer docMarkNo)
    {
    	return edocMarkDAO.findEdocMarkByCategoryId(categoryId,docMarkNo);
    }
    /**
     * 发起人撤销流程后，已经调用的文号（如果是最大号）可以恢复，下次发文时可继续调用。
     * @param summary 		公文对象
     * @return
     */
   
    public void edocMarkCategoryRollBack(EdocSummary summary){
    	// 发文/收文文号撤销
        if(summary.getEdocType()==0) {
        	// 第一套文号
        	if(summary.getDocMark()!=null&&!"".equals(summary.getDocMark())){
        		//设置公文的文号为空
        		summary.setDocMark(null);
        		edocSummaryDao.update(summary);
        	}
        	// 第二套文号
        	if(summary.getIsunit()&&summary.getDocMark2()!=null&&!"".equals(summary.getDocMark2())){
        		//设置公文的文号为空
        		summary.setDocMark2(null);
        		edocSummaryDao.update(summary);
        	}
        } else if(summary.getEdocType() == 2) {//签报文号撤销
        	if(summary.getDocMark()!=null&&!"".equals(summary.getDocMark())){
        		EdocMarkHistory edocMarkHistory = edocMarkHistoryDAO.findEdocMarkHistoryByEdocSummaryIdAndEdocMark(summary.getId(), summary.getDocMark(), 1);
        		rollBackOperation(summary, edocMarkHistory, 1);
        	}
        	// 第二套文号
        	if(summary.getIsunit()&&summary.getDocMark2()!=null&&!"".equals(summary.getDocMark2())){
        		EdocMarkHistory edocMarkHistory = edocMarkHistoryDAO.findEdocMarkHistoryByEdocSummaryIdAndEdocMark(summary.getId(), summary.getDocMark2(), 2);
        		rollBackOperation(summary, edocMarkHistory, 2);
        	}
        }
    }
    //文号回滚具体操作
  	private void rollBackOperation(EdocSummary summary, EdocMarkHistory edocMarkHistory, int num) {
  		User user = AppContext.getCurrentUser();
  		//	当其他公文使用此断号时就查找不到记录
  		if(edocMarkHistory!=null) {
  			EdocMarkDefinition markDef = edocMarkHistory.getEdocMarkDefinition();
  			//	手写的时候为null
  			edocMarkHistoryDAO.deleteEdocMarkHistoryByEdocId(summary.getId());
  			if(markDef!=null) {
  				//删除断号edocMarkHistory表中相关记录
  				//新生成断号
  				EdocMark edocMark = new EdocMark();
  				edocMark.setNewId();
  				edocMark.setCategoryId(markDef.getEdocMarkCategory().getId());
  				edocMark.setEdocId(-1L);
  				edocMark.setCreateTime(DateUtil.currentDate());
  				edocMark.setCreateUserId(user.getId());
  				edocMark.setDocMark(edocMarkHistory.getDocMark());
  				edocMark.setDocMarkNo(edocMarkHistory.getDocMarkNo());
  				edocMark.setDomainId(user.getLoginAccount());
  				edocMark.setEdocMarkDefinition(markDef);
  				edocMark.setMarkNum(num);
  				edocMark.setStatus(0);
  				edocMarkDAO.save(edocMark);
  			}
  			//设置公文的文号为空
			if(num==1) {
				summary.setDocMark(null);
			} else {
				summary.setDocMark2(null);
			}
			edocSummaryDao.update(summary);
  		}
  	}
	
	public List<EdocMark> findEdocMarkByEdocSummaryId(Long edocSummaryId){
		 return edocMarkDAO.findEdocMarkByEdocSummaryId(edocSummaryId);
	 }
	
	public EdocMarkReserve getEdocMarkReserve(Long reserveId) throws BusinessException {
		return this.edocMarkReserveManager.getEdocMarkReserveById(reserveId);
	}
	
	public void setEdocSummaryDao(EdocSummaryDao edocSummaryDao) {
		this.edocSummaryDao = edocSummaryDao;
	}
	public void setEdocMarkDefinitionManager(EdocMarkDefinitionManager edocMarkDefinitionManager) {
		this.edocMarkDefinitionManager = edocMarkDefinitionManager;
	}
	public void setEdocMarkDAO(EdocMarkDAO edocMarkDAO) {
		this.edocMarkDAO = edocMarkDAO;
	}
	public void setEdocMarkCategoryManager(EdocMarkCategoryManager edocMarkCategoryManager){
		this.edocMarkCategoryManager = edocMarkCategoryManager;
	}
	public void setEdocMarkReserveManager(EdocMarkReserveManager edocMarkReserveManager) {
		this.edocMarkReserveManager = edocMarkReserveManager;
	}

	public void setEdocMarkHistoryDAO(EdocMarkHistoryDAO edocMarkHistoryDAO) {
		this.edocMarkHistoryDAO = edocMarkHistoryDAO;
	}

	public void setEdocMarkHistoryManager(EdocMarkHistoryManager edocMarkHistoryManager) {
		this.edocMarkHistoryManager = edocMarkHistoryManager;
	}
	
}

