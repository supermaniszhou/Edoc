package com.seeyon.v3x.edoc.manager;

import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

public interface EdocStatNewManager {

	/**
	 * 获取公文统计数据
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getEdocStatVoList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException;
	
	/**
	 * 获取公文统计穿透列表
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getEdocVoList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException;
	
	/**
	 * 公文统计界面-选择枚举值
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getEdocEnumitemList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException;
	
}
