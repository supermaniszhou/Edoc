/**
 * Author : xuqw
 *   Date : 2015年8月26日 下午1:30:12
 *
 * Copyright (C) 2015 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.v3x.edoc.util;

import com.seeyon.apps.edoc.bo.EdocBodyBO;
import com.seeyon.apps.edoc.bo.EdocElementBO;
import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.apps.edoc.bo.EdocSummaryComplexBO;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;

/**
 * <p>Title       : 公文</p>
 * <p>Description : 公文数据类型转换</p>
 * <p>Copyright   : Copyright (c) 2015</p>
 * <p>Company     : seeyon.com</p>
 */
public class DataTransUtil {

    /**
     * 将EdocSummary元素转化成对外提供的VO对象
     * @Author      : xuqw
     * @Date        : 2015年8月26日下午2:18:13
     * @param summary
     * @return
     */
    public static EdocSummaryBO transEdocSummary2BO(EdocSummary summary){
        
        EdocSummaryBO eVO = null;
        if(summary != null){
            
            eVO = new EdocSummaryBO();
            
            eVO.setId(summary.getId());
            eVO.setArchiveId(summary.getArchiveId());
            eVO.setOverWorkTime(summary.getOverWorkTime());
            eVO.setRunWorkTime(summary.getRunWorkTime());
            eVO.setCaseId(summary.getCaseId());
            eVO.setProcessId(summary.getProcessId());
            eVO.setDeadline(summary.getDeadline());
            eVO.setDeadlineDatetime(summary.getDeadlineDatetime());
            eVO.setEdocType(summary.getEdocType());
            eVO.setCreateTime(summary.getCreateTime());
            eVO.setStartUserId(summary.getStartUserId());
            eVO.setState(summary.getState());
            eVO.setAdvanceRemind(summary.getAdvanceRemind());
            eVO.setOrgAccountId(summary.getOrgAccountId());
            eVO.setTemplateId(summary.getTempleteId());
            eVO.setHasAttachments(summary.isHasAttachments());
            //以下是文单元素
            eVO.setSubject(summary.getSubject());
            eVO.setUrgentLevel(summary.getUrgentLevel());
            eVO.setSerialNo(summary.getSerialNo());
        }
        
        return eVO;
    }
    
    
    /**
     * 将公文body对象转化成VO对象
     * @Author      : xuqw
     * @Date        : 2015年8月26日下午3:46:00
     * @param body
     * @return
     */
    public static EdocBodyBO transEdocBody2BO(EdocBody body){
        
        EdocBodyBO v = null;
        if(body != null){
            v = new EdocBodyBO();
            v.setId(body.getId());
            v.setContent(body.getContent());
            v.setContentType(body.getContentType());
            v.setContentName(body.getContentName());
            v.setCreateTime(body.getCreateTime());
            v.setLastUpdate(body.getLastUpdate());
            v.setContentNo(body.getContentNo());
            v.setContentStatus(body.getContentStatus());
        }
        return v;
    }
    
    /**
     * 将EdocSummaryModel对象转化成复合的BO对象，对外提供
     * @Author      : xuqw
     * @Date        : 2015年8月27日下午11:54:44
     * @param summary
     * @return
     */
    public static EdocSummaryComplexBO transEdocSumary2CBO(EdocSummaryModel summary){
        EdocSummaryComplexBO b = null;
        
        if(summary != null){
            
            b = new EdocSummaryComplexBO();
            
            b.setDeadLineDisplayDate(summary.getDeadLineDisplayDate());
            b.setDeadLineDate(summary.getDeadLineDate());
            b.setAffairId(summary.getAffairId());
            b.setAffairState(summary.getState());//这里暂时只有时间线用到这个
            b.setAffairSubState(summary.getState());//因为EdocSummaryModel 的state有时候表示state，有时候表示substate
            b.setSubject(summary.getSubject());
            b.setEdocUnit(summary.getEdocUnit());
            b.setBodyType(summary.getBodyType());
            b.setHasAttachments(summary.isHasAttachments());
            b.setState(summary.getEdocStatus());
            
            EdocSummary eSummary = summary.getSummary();
            if(eSummary != null){
                b.setStartUserId(eSummary.getStartUserId());
                b.setUrgentLevel(eSummary.getUrgentLevel());
            }
        }
        return b;
    }
    
    /**
     * 将EdocElement转化成BO对象对外提供
     * @Author      : xuqw
     * @Date        : 2015年8月28日上午12:42:50
     * @param e
     * @return
     */
    public static EdocElementBO truansEdocElement2BO(EdocElement e){
        
        EdocElementBO eBO = null;
        if(e != null){
            eBO = new EdocElementBO();
            
            eBO.setId(e.getId());
            eBO.setElementId(e.getElementId());
            eBO.setFieldName(e.getFieldName());
            eBO.setName(e.getName());
            eBO.setInputMode(e.getInputMode());
            eBO.setType(e.getType());
            eBO.setMetadataId(e.getMetadataId());
            eBO.setSystem(e.getIsSystem());
            eBO.setStatus(e.getStatus());
            eBO.setDomainId(e.getDomainId());
            eBO.setPoFieldName(e.getPoFieldName());
            eBO.setPoName(e.getPoName());
        }
        return eBO;
    }
}
