package com.seeyon.v3x.edoc.quartz;

import java.util.HashMap;
import java.util.Map;

import com.seeyon.apps.collaboration.quartz.ProcessAutoStopRepealCaseBO;
import com.seeyon.apps.collaboration.quartz.ProcessMsgParamBO;
import com.seeyon.apps.collaboration.quartz.WorkflowProcessOvertimeAppHandler;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMessageHelper;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;

public class WorkflowProcessOvertimEdoceHandler implements WorkflowProcessOvertimeAppHandler {
    private EdocManager edocManager;
    private EdocSummaryManager  edocSummaryManager;
    private AffairManager affairManager;
   	
   	public AffairManager getAffairManager() {
   		return affairManager;
   	}

   	public void setAffairManager(AffairManager affairManager) {
   		this.affairManager = affairManager;
   	}

	public EdocManager getEdocManager() {
		return edocManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	
	
	public EdocSummaryManager getEdocSummaryManager() {
		return edocSummaryManager;
	}

	public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
		this.edocSummaryManager = edocSummaryManager;
	}

	@Override
	public void updateProcessOvertimeInfo(Object summary) throws BusinessException {
		EdocSummary s = (EdocSummary)summary;
		
	    Boolean isOvertopTime = s.getCoverTime();
        if(isOvertopTime != null && !isOvertopTime){
        	edocSummaryManager.updateEdocSummaryCoverTime(s.getId(), true);
        }
        
        Map<String,Object> m = new HashMap<String,Object>();
		m.put("processOverTime",true);
		affairManager.update(m, new Object[][]{{"objectId",s.getId()}});
	}

	@Override
	public ProcessMsgParamBO getMessageParam(Object summary) throws BusinessException {
		
		
		EdocSummary s = (EdocSummary)summary;
		
		ProcessMsgParamBO messageBO = new ProcessMsgParamBO();
		messageBO.setSubject(s.getSubject());
		messageBO.setMessageSentLink( "message.link.edoc.done");
		messageBO.setMessagePendingLink("message.link.edoc.pending");
		
		ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.edoc;
		if (s.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SEND) {
			appEnum = ApplicationCategoryEnum.edocSend;
		} else if (s.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC) {
			appEnum = ApplicationCategoryEnum.edocRec;
		} else if (s.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
			appEnum = ApplicationCategoryEnum.edocSign;
		}
		messageBO.setAppEnum(appEnum);
		messageBO.setProcessSenderId( s.getStartUserId());
		messageBO.setImportantLevel(s.getImportantLevel());
		
		return messageBO;
	}

	@Override
	public boolean isNeedJobExcute(Object summary) throws BusinessException {
		EdocSummary s = (EdocSummary)summary;
		
		boolean isGo = true;
		//协同被删除或者完成,不做提醒
		if(s == null || s.getCompleteTime() != null) {
			isGo = false;
		}
		return isGo;
	}

	@Override
	public Object getSummaryObject(long summaryId) throws BusinessException {
		EdocSummary s = edocManager.getEdocSummaryById(summaryId, false);
		return s;
	}

	@Override
	public ApplicationCategoryEnum getAppEnum() throws BusinessException {
		return ApplicationCategoryEnum.edoc;
	}

	@Override
	public void transStopProcess(Object summary, ProcessMsgParamBO messageBO) throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer getMsgFilterKey(Object object) throws BusinessException {
		EdocSummary s = (EdocSummary)object;
		return EdocMessageHelper.getMsgFilterParamBySummary(s);
	}

    @Override
    public long getAdvanceRemind(Object summary) {
        EdocSummary s = (EdocSummary)summary;
        Long ret = s.getAdvanceRemind();
        if(ret == null){
            ret = 0l;
        }
        return ret;
    }

	@Override
	public ProcessAutoStopRepealCaseBO canAutoRepealFlow(Object arg0) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessAutoStopRepealCaseBO canAutostopflow(Object arg0) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAutoStopRepealFlag(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void transRepealProcess(Object arg0, ProcessMsgParamBO arg1) throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Long getProcessRemindInterval(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void transSendCycMessage(Object arg0, ProcessMsgParamBO arg1) throws BusinessException {
		// TODO Auto-generated method stub
		
	}

}
