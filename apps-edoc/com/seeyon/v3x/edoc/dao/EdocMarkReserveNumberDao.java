package com.seeyon.v3x.edoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;
import com.seeyon.ctp.util.Strings;

public class EdocMarkReserveNumberDao extends BaseHibernateDao<EdocMarkReserveNumber> {

	public void deleteByReservedId(List<Long> delReservedIdList) throws BusinessException {
		if(Strings.isNotEmpty(delReservedIdList)) {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("delReservedIdList", delReservedIdList);
			super.bulkUpdate("delete from EdocMarkReserveNumber where reserveId in (:delReservedIdList)", paramMap);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<EdocMarkReserveNumber> findAll() throws BusinessException {
		List<EdocMarkReserveNumber> list = super.findVarargs("from EdocMarkReserveNumber");
		return list;
	}
	
}
