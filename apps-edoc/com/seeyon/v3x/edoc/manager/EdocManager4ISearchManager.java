package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.v3x.isearch.manager.ISearchManager;
import com.seeyon.v3x.isearch.model.ConditionModel;
import com.seeyon.v3x.isearch.model.ResultModel;

public class EdocManager4ISearchManager extends ISearchManager {

    private static final long serialVersionUID = -3203706106690275684L;

    @Override
    public Integer getAppEnumKey() {
        return ApplicationCategoryEnum.edoc.getKey();
    }

    @Override
    public int getSortId() {
        return this.getAppEnumKey();
    }

    @Override
    public List<ResultModel> iSearch(ConditionModel cModel) {
        EdocManager edocManager = (EdocManager) AppContext.getBean("edocManager");
        return edocManager.iSearch(cModel);
    }

}
