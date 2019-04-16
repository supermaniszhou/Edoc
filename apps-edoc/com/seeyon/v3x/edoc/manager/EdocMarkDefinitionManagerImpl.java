/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.enums.EdocEnum.MarkCategory;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.v3x.edoc.dao.EdocMarkDefinitionDAO;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.EdocMarkUtil;
import com.seeyon.v3x.edoc.util.SharedWithThreadLocal;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public class EdocMarkDefinitionManagerImpl implements EdocMarkDefinitionManager{
	private final static Log log = LogFactory.getLog(EdocMarkDefinitionManagerImpl.class);
	private EdocMarkDefinitionDAO edocMarkDefinitionDAO;
	
	private EdocMarkCategoryManager edocMarkCategory;
	private EdocMarkReserveManager edocMarkReserveManager;
    private OrgManager orgManager;
    private TemplateManager templateManager;
    private EdocMarkAclManager edocMarkAclManager;
	
    public String checkExistEdocMarkDefinition(String id) {
    	EdocMarkDefinition markDef = edocMarkDefinitionDAO.get(Long.parseLong(id));
    	if(markDef == null) {
    		return "false";
    	} else {
    		return "true";
    	}
    }
    
	public EdocMarkDefinition getMarkDefinition(long id) {
		return edocMarkDefinitionDAO.get(id);
	}
	
	/**
     * 方法描述：保存公文文号定义
     */
	public void saveMarkDefinition(EdocMarkDefinition edocMarkDefinition){
		this.edocMarkDefinitionDAO.saveEdocMarkDefinition(edocMarkDefinition);
	}
	
	/**
     * 方法描述：修改公文文号定义
     */
	public void updateMarkDefinition(EdocMarkDefinition edocMarkDefinition){
		this.edocMarkDefinitionDAO.updateEdocMarkDefinition(edocMarkDefinition);
	}
	
   	/**
     * 方法描述：删除公文文号定义
     */
	public void deleteMarkDefinition(EdocMarkDefinition edocMarkDefinition){
		this.edocMarkDefinitionDAO.deleteEdocMarkDefinition(edocMarkDefinition);
	}
	
	/**
     * 方法描述：根据公文文号定义ID查询公文文号定义
     */
	public EdocMarkDefinition queryMarkDefinitionById(Long edocMarkDefinitionId){
		return this.edocMarkDefinitionDAO.findEdocMarkDefinitionById(edocMarkDefinitionId);
	}	
	
	public List<EdocMarkDefinition> queryMarkDefinitionListById(List<Long> edocMarkDefinitionIdList){
		return this.edocMarkDefinitionDAO.findEdocMarkDefinitionListById(edocMarkDefinitionIdList);
	}
	
	public EdocMarkModel markDef2Mode(EdocMarkDefinition markDef,String yearNo,Integer curentno) {
		String yearNoStr=yearNo;
		if(yearNoStr==null || "".equals(yearNoStr))
		{
			Calendar cal = Calendar.getInstance();
			yearNoStr = String.valueOf(cal.get(Calendar.YEAR));
		}
		if(markDef == null) {
			return null;
		}
		EdocMarkModel model = new EdocMarkModel();
		model.setMarkDefinitionId(markDef.getId());
		String wordNo = markDef.getWordNo();
		model.setWordNo(wordNo);
		model.setMarkType(markDef.getMarkType());
		String expression = markDef.getExpression();
		EdocMarkCategory category = markDef.getEdocMarkCategory();
		if(Strings.isNotBlank(wordNo)){
			if(wordNo.indexOf("\\")>=0) wordNo = wordNo.replaceAll("\\\\", "\\\\\\\\");
			if(wordNo.indexOf("$")>=0) wordNo = wordNo.replaceAll("\\$", "\\\\\\$");
			expression = expression.replaceFirst("\\$WORD", wordNo);
		}
		
		if(category != null){
		    
		    if(category.getYearEnabled()){
		        expression = expression.replaceFirst("\\$YEAR", yearNoStr);
		    }
		    model.setCategoryCodeMode(category.getCodeMode());
		}
		
		int currentNo =category!=null ? category.getCurrentNo():0;
		model.setCurrentNo(currentNo);
		StringBuffer flowNo = new StringBuffer();
		flowNo.append(curentno == null ? currentNo :curentno);
		int length = markDef.getLength();		
		int maxNo = category!=null ? category.getMaxNo():0;
		int curNoLen = String.valueOf(currentNo).length();
		int maxNoLen = String.valueOf(maxNo).length();
		if (length > 0 && length == maxNoLen) {
			flowNo = new StringBuffer();
			for (int j = curNoLen; j < length; j++) {
				flowNo.append("0");
			}
			if(curentno!=null){
			    
				flowNo.append(curentno);
			}else{
				flowNo.append(currentNo);
			}
		}
		expression = expression.replaceFirst("\\$NO", flowNo.toString());
		if(expression.indexOf("$WORD")!=-1){
			expression = expression.replaceFirst("\\$WORD", wordNo);
		}
		model.setMark(expression);
		model.setCodeMode(markDef.getEdocMarkCategory().getCodeMode());
		model.setDomainId(markDef.getEdocMarkCategory().getDomainId());
		return model;
	}
	
	public List<EdocMarkModel> getEdocMarkDefs(Long domainId, Long depId, String condition, String textfield) throws BusinessException {
		EdocHelper.checkDocmarkByYear();
		List<EdocMarkDefinition> markDefs = edocMarkDefinitionDAO.getEdocMarkDefs1(domainId,true);//本单位建的
		List<Long>  deptIds = new ArrayList<Long>();
		List<V3xOrgDepartment>  departmentIds = orgManager.getChildDepartments(Long.valueOf(domainId),false);
		for(V3xOrgDepartment dept :departmentIds){
			if(dept != null){
				deptIds.add(dept.getId());
			}
		}
		List<Object[]> result = edocMarkDefinitionDAO.getEdocMarkDefsIncludeAccountAndAcl(domainId,deptIds);//被授权的
		StringBuilder ids = new StringBuilder();//过滤重复值
		for(int j=0;j<markDefs.size();j++){
		    ids.append(markDefs.get(j).getId());
		    ids.append(",");
		}
		for(int i = 0; i < result.size(); i++){
			Object[] object = (Object[]) result.get(i);
			String id = String.valueOf(object[0]);
			if(ids.indexOf(id)==-1){
				EdocMarkDefinition definition = getEdocMarkDefinition(object);
				markDefs.add(definition);
			}
			ids.append(id);
			ids.append(",");
		}
		List<EdocMarkModel> results = new ArrayList<EdocMarkModel>();
		Calendar cal = Calendar.getInstance();
		String yearNo = String.valueOf(cal.get(Calendar.YEAR));
		List<String> allReserveDocMarkList = null;
		Map<Long, List<EdocMarkReserveVO>> reserveVoMap = edocMarkReserveManager.findAllEdocMarkReserveListMap();
		List<EdocMarkReserveNumber> allReserveNumberList = edocMarkReserveManager.findAllEdocMarkReserveNumberList();
		
		for (int i = 0; i < markDefs.size(); i++) {
			allReserveDocMarkList = new ArrayList<String>();
			EdocMarkDefinition markDef = markDefs.get(i);			
			EdocMarkModel model = markDef2Mode(markDef,yearNo,null);
			
			allReserveDocMarkList.addAll(EdocMarkUtil.getMarkReserveNumberList(condition, markDef, allReserveNumberList));
			String[] upAndDown = EdocMarkUtil.getMarkReserveUpAndDown(markDef, reserveVoMap.get(markDef.getId()));
			model.setMarkReserveUp(upAndDown[0]);
			model.setMarkReserveDown(upAndDown[1]);
			
			List<V3xOrgEntity> aclEntity = new ArrayList<V3xOrgEntity>();
			Set<EdocMarkAcl> markAcls = markDef.getEdocMarkAcls();
			java.util.Iterator<EdocMarkAcl> iterator = markAcls.iterator();
			while (iterator.hasNext()) {
				EdocMarkAcl markAcl = iterator.next();
				V3xOrgEntity orgEntity = orgManager.getEntity(markAcl.getAclType(), markAcl.getDeptId());
				aclEntity.add(orgEntity);
			}
			model.setAclEntity(aclEntity);
			
			if(StringUtils.isNotBlank(condition) && StringUtils.isNotBlank(textfield)){
				if("mark".equals(condition)){
					if(StringUtils.contains(model.getMark(), textfield)){
						results.add(model);
					}
				}else if("markType".equals(condition)){
					if(model.getMarkType() == NumberUtils.toInt(textfield)){
						results.add(model);
					}
				} else if("markReserveUp".equals(condition)) {
					boolean flag = false;
					if(Strings.isNotBlank(textfield)) {
						if(Strings.isNotEmpty(allReserveDocMarkList)) {
							for(String docMark : allReserveDocMarkList) {
								if(StringUtils.contains(docMark, textfield)){
									flag = true;
									break;
								}
							}
						}
					} else {
						flag = true;
					}
					if(flag) {
						results.add(model);
					}
				} else if("markReserveDown".equals(condition)) {
					boolean flag = false;
					if(Strings.isNotBlank(textfield)) {
						if(Strings.isNotEmpty(allReserveDocMarkList)) {
							for(String docMark : allReserveDocMarkList) {
								if(StringUtils.contains(docMark, textfield)){
									flag = true;
									break;
								}
							}
						}
					} else {
						flag = true;
					}
					if(flag) {
						results.add(model);
					}
				}
			}else{
				results.add(model);
			}
		}
		
		return results;
	}
	
	/**
	 * 方法描述：保存公文文号定义，同时保存公文文号类型和文号授权
	 */
	public void saveMarkDefinition(EdocMarkDefinition def,EdocMarkCategory cat){
		if(cat.getCodeMode()==Constants.MODE_SERIAL){
			
			this.edocMarkCategory.saveCategory(cat);
		}
		this.saveMarkDefinition(def);
	}
	
	

	/**
	 * 根据授权部门查找公文文号定义。
	 * @param deptIds  文号授权部门id（以,号分隔）
	 * @return List<EdocMarkModel>
	 */
	public List<EdocMarkModel> getEdocMarkDefinitions(String deptIds,int markType) {
		
		//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
		
		List<EdocMarkDefinition> markDefs = edocMarkDefinitionDAO.getMyEdocMarkDefs(deptIds,true, markType);		
		List<EdocMarkModel> results = new ArrayList<EdocMarkModel>();
		Calendar cal = Calendar.getInstance();
		String yearNo = String.valueOf(cal.get(Calendar.YEAR)); 		
		for(EdocMarkDefinition markDef : markDefs) {			
			EdocMarkModel model = markDef2Mode(markDef,yearNo,null);
			model.setCategoryCodeMode(markDef.getEdocMarkCategory().getCodeMode());
			/*
			model.setMarkDefinitionId(markDef.getId());
			model.setWordNo(markDef.getWordNo());
			String expression = markDef.getExpression();
			EdocMarkCategory category = markDef.getEdocMarkCategory();
			expression = expression.replaceFirst("\\$WORD", markDef.getWordNo());
			if (category.getYearEnabled()) {
				expression = expression.replaceFirst("\\$YEAR", yearNo);
			}
			int currentNo = category.getCurrentNo();
			model.setCurrentNo(currentNo);
			String flowNo = String.valueOf(currentNo);
			int length = markDef.getLength();			
			int maxNo = category.getMaxNo();
			int curNoLen = String.valueOf(currentNo).length();
			int maxNoLen = String.valueOf(maxNo).length();
			if (length > 0 && length == maxNoLen) {
				flowNo = "";
				for (int j = curNoLen; j < length; j++) {
					flowNo += "0";
				}
				flowNo += String.valueOf(currentNo);
			}
			expression = expression.replaceFirst("\\$NO", flowNo);
			model.setMark(expression);
			*/
			results.add(model);
		}
		return results;
	}
	public List<EdocMarkDefinition> getEdocMarkDefinitionsByCategory(Long categoryId) {
		
		//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();		
		List<EdocMarkDefinition> markDefs = edocMarkDefinitionDAO.getEdocMarkDefsByCategoryId(categoryId);
		
		return markDefs;
	}
	
	public Short judgeStreamType(Long definitionId)throws BusinessException{
		Short streamType = 0;
		EdocMarkDefinition def = this.queryMarkDefinitionById(definitionId);
		EdocMarkCategory category  = def.getEdocMarkCategory();
		if("0".equals(category.getCodeMode().toString())){
			streamType = 0;
		}else if("1".equals(category.getCodeMode().toString())){
			streamType = 1;
		}
		return streamType;
	}
	
//	public String getEdocMark(long definitionId, Integer currentNo) {
//		
//		Calendar cal = Calendar.getInstance();
//		int currentYear = cal.get(Calendar.YEAR); 
//		String currentYearStr = new Integer(currentYear).toString();
//		
//		EdocMarkDefinition def  = queryMarkDefinitionById(definitionId);
//		String expression = def.getExpression();
//		expression = expression.replaceFirst("\\$WORD", def.getWordNo());
//		
//		expression = expression.replaceFirst("\\$YEAR", currentYearStr);
//		
//		if(currentNo > 0) {
//			expression = expression.replaceFirst("\\$NO", currentNo.toString());
//		}
//		else{
//			return "";
//			//expression = expression.replace("\\$NO", def.getEdocMarkCategory().getCurrentNo().toString());
//		}
//		
//		return expression;
//	}
	
	public Boolean containEdocMarkDefinition(String wordNo, long domainId,int markType) {		
		return containEdocMarkDefinition(0, wordNo, domainId,markType);
	}
	
	public Boolean containEdocMarkDefinition(long markDefId, String wordNo, long domainId,int markType) {
		if (markDefId != 0) {
			return edocMarkDefinitionDAO.containEdocMarkDef(markDefId, wordNo, domainId,markType);
		}
		else {
			return edocMarkDefinitionDAO.containEdocMarkDef(wordNo, domainId,markType);
		}		
	}
	
	public boolean containEdocMarkDefInCategory(long categoryId) {
		List<EdocMarkDefinition> markDefs = edocMarkDefinitionDAO.getEdocMarkDefsByCategoryId(categoryId);
		if (markDefs != null && markDefs.size() > 0) {
			return true;
		}
		return false;
	}
	
	public void logicalDeleteMarkDefinition(long defId, short status){
		
		edocMarkDefinitionDAO.updateMarkDefinitionStatus(defId, status);
	}
	/**
     * 将EdocMarkCategory自增长,内部文号
     * @param markDefinitionId
     */
    public void setEdocMarkCategoryIncrement(Long markDefinitionId){
    	//----------性能优化，从SharedWithThreadLocal中取文号定义对象
//    	EdocMarkDefinition markDef = queryMarkDefinitionById(markDefinitionId);
    	EdocMarkDefinition markDef = SharedWithThreadLocal.getMarkDefinition(markDefinitionId); 
    	if(markDef == null){
    		markDef = queryMarkDefinitionById(markDefinitionId);
    	}
    	
    	EdocMarkCategory edocMarkCate = markDef.getEdocMarkCategory();
		edocMarkCate.setCurrentNo(edocMarkCate.getCurrentNo()+1);
		edocMarkCategory.updateCategory(edocMarkCate);
		setEdocMarkDefinitionPublished(markDef);
		
    }
    private void setEdocMarkDefinitionPublished(EdocMarkDefinition markDef) {
		//设置已经使用。
    	if(markDef.getStatus().shortValue() == Constants.EDOC_MARK_DEFINITION_DRAFT){
    		markDef.setStatus(Constants.EDOC_MARK_DEFINITION_PUBLISHED);
    		updateMarkDefinition(markDef);
    	}
	}
    public void setEdocMarkDefinitionUsed(Long markDefId) {
    	if(markDefId==null) return ;
    	EdocMarkDefinition markDef = queryMarkDefinitionById(markDefId);
    	if(markDef!=null)
    		setEdocMarkDefinitionPublished(markDef);
    }
    /**
     * 判断公文文号定义是否已经被删除
     */
	public int judgeEdocDefinitionExsit(Long definitionId){
		return edocMarkDefinitionDAO.judgeEdocDefinitionExsit(definitionId);
	}
	public EdocMarkModel  getEdocMarkDefinitionById(Long definitionId){

		//检查公文年度编号变更
    	EdocHelper.checkDocmarkByYear();
		
		EdocMarkDefinition markDef = edocMarkDefinitionDAO.get(definitionId);		
		Calendar cal = Calendar.getInstance();
		String yearNo = String.valueOf(cal.get(Calendar.YEAR)); 		
		EdocMarkModel model = markDef2Mode(markDef,yearNo,null);
		return model;
	}
	@Override
	public EdocMarkModel getEdocMarkByTempleteId(Long templeteId,
			MarkCategory category) {
		if(templeteId == null ) return null;
		
		CtpTemplate templete =null;
		try {
			 templete = templateManager.getCtpTemplate(templeteId);
			if(templete == null){
				log.error("查找公文模板失败EdocMarkDefinitionManagerImpl.getEdocMarkByTempleteId"+templeteId);
				return null;
			}
		} catch (BusinessException e) {
			log.error("", e);
		}
		EdocSummary tsummary = (EdocSummary)XMLCoder.decoder(templete.getSummary());
		String templeteDefinitionId = "" ; //模板绑定的文号定义ID
		if(category.ordinal() == MarkCategory.serialNo.ordinal() && Strings.isNotBlank(tsummary.getSerialNo()) ) {
			templeteDefinitionId = tsummary.getSerialNo().split("[|]")[0];
		}else if(category.ordinal() == MarkCategory.docMark.ordinal() && Strings.isNotBlank(tsummary.getDocMark())) {
			templeteDefinitionId = tsummary.getDocMark().split("[|]")[0];
		}else if(category.ordinal() == MarkCategory.docMark2.ordinal()  && Strings.isNotBlank(tsummary.getDocMark2()))  {
			templeteDefinitionId = tsummary.getDocMark2().split("[|]")[0];
		}
		if(Strings.isBlank(templeteDefinitionId)){
			return null;
		}
		//如果文号已被删除，返回空--OA-49464当只有一个公文文号时，新建模板不绑定文号，没有文号使用权限的人调用该模板默认显示了这个文号，但拟文人是没权限的
		try {
			if(judgeEdocDefinitionExsit(Long.parseLong(templeteDefinitionId))==0){
				return null;
			}
		}catch (NumberFormatException e) {
			return null;
		}
		
		return getEdocMarkDefinitionById(Long.parseLong(templeteDefinitionId));
		
		
	}
	
	public boolean isEdocMarkAclByDefinitionId(User user, EdocMarkDefinition edocMarkDefinition) throws Exception {
		return isEdocMarkAclByDefinitionId(user,edocMarkDefinition,0L);
	}	
	
	
	public boolean isEdocMarkAclByDefinitionId(User user, EdocMarkDefinition edocMarkDefinition,long templateOrgAccountId) throws Exception {
        long domainId = user.getLoginAccount();//当前登陆单位ID
        long depId = user.getDepartmentId();//当前登陆单位部门ID
        List<EdocMarkAcl> aclList = null;
        if(edocMarkDefinition != null) {
            aclList = edocMarkAclManager.getMarkAclById(edocMarkDefinition.getId());
            if(aclList != null && aclList.size()>0) {
                if(user.getLoginAccount().longValue() != user.getAccountId().longValue()) {//兼职
//                  List<V3xOrgRelationship> cntListOrg = orgManager.getAllConcurrentPostByAccount(domainId);   
                    List<MemberPost> cntListOrg = orgManager.getAllConcurrentPostByAccount(domainId);
                    if(cntListOrg!=null && cntListOrg.size()>0) {
                        for(MemberPost rel : cntListOrg) {
                            if(rel.getMemberId().longValue() == user.getId().longValue()) {
                                depId = rel.getDepId();
                                break;
                            }
                        }
                    }
                }
                for(int i=0; i<aclList.size(); i++) {
                    if(aclList.get(i).getDeptId()==domainId || aclList.get(i).getDeptId()==depId
                            || aclList.get(i).getDeptId() == templateOrgAccountId) {
                        return true;
                    }
                }                   
            }
        }
        return false;
    }   
	
	
	
	public EdocMarkDefinition getEdocMarkDefinition(Object[] object){
		EdocMarkDefinition definition = new EdocMarkDefinition();
		int i = 0;
		definition.setId((Long) object[i++]);
		definition.setWordNo((String) object[i++]);
		definition.setMarkType((Integer) object[i++]);
		definition.setExpression((String) object[i++]);
		definition.setLength((Integer) object[i++]);
		EdocMarkCategory edocMarkCategory = new EdocMarkCategory();
		Set<EdocMarkAcl> markAcls = new HashSet<EdocMarkAcl>();
		EdocMarkAcl acl = new EdocMarkAcl();
		edocMarkCategory.setId((Long) object[i++]);
		edocMarkCategory.setCategoryName((String) object[i++]);
		edocMarkCategory.setCodeMode((Short) object[i++]);
		edocMarkCategory.setCurrentNo((Integer) object[i++]);
		edocMarkCategory.setMinNo((Integer) object[i++]);
		edocMarkCategory.setMaxNo((Integer) object[i++]);
		edocMarkCategory.setReadonly((Boolean) object[i++]);
		edocMarkCategory.setDomainId((Long) object[i++]);
		edocMarkCategory.setYearEnabled((Boolean) object[i++]);
		acl.setAclType((String) object[i++]);
		acl.setDeptId((Long)object[i++]);
		definition.setEdocMarkCategory(edocMarkCategory);
		markAcls.add(acl);
		definition.setEdocMarkAcls(markAcls);
		return definition;
	}
    
	public void setEdocMarkReserveManager(EdocMarkReserveManager edocMarkReserveManager) {
		this.edocMarkReserveManager = edocMarkReserveManager;
	}
	
	public void setEdocMarkAclManager(EdocMarkAclManager edocMarkAclManager) {
		this.edocMarkAclManager = edocMarkAclManager;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}		
	public void setEdocMarkDefinitionDAO(EdocMarkDefinitionDAO edocMarkDefinitionDAO) {
		this.edocMarkDefinitionDAO = edocMarkDefinitionDAO;
	}
	public void setEdocMarkCategoryManager(EdocMarkCategoryManager edocMarkCategory){
		this.edocMarkCategory = edocMarkCategory;
	}
	
}
