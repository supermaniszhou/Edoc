package com.seeyon.ctp.portal.section;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete.OPEN_TYPE;
import com.seeyon.ctp.portal.section.templete.ChessboardTemplete;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.domain.EdocRegisterCondition;
import com.seeyon.v3x.edoc.manager.EdocManager;

public class EdocSendRegisterSection extends BaseSectionImpl {
    private static final Log LOG = CtpLogFactory.getLog(EdocSendRegisterSection.class);
    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getId() {
        return "edocSendRegisterSection";
    }

    @Override
    public String getBaseName(Map<String, String> preference) {
        String name = preference.get("columnsName");
        if(Strings.isBlank(name)){
            name = ResourceUtil.getString("edoc.send.register");//发文登记簿
        }
        return name;
    }

    @Override
    public String getName(Map<String, String> preference) {
        //栏目显示的名字，必须实现国际化，在栏目属性的“columnsName”中存储
        String name = preference.get("columnsName");
        if(Strings.isBlank(name)){
            return ResourceUtil.getString("edoc.send.register");
        }else{
            return name;
        }
    }

    @Override
    public Integer getTotal(Map<String, String> preference) {
        //User user = AppContext.getCurrentUser();
        //int total = manager.getEdocRegisterConditionTotal(user.getLoginAccount(),1,null);
        //return total;
        return null;
    }

    @Override
    public BaseSectionTemplete projection(Map<String, String> preference) {
    	EdocManager manager = (EdocManager)AppContext.getBean("edocManager");
        ChessboardTemplete c = new ChessboardTemplete();
        int[] chessBoardInfo= c.getPageSize(preference); 
        int count= chessBoardInfo[0];
        int row = chessBoardInfo[1];
        int column = chessBoardInfo[2];
        c.setLayout(row, column);
        c.setDataNum(count);
        User user = AppContext.getCurrentUser();
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("count", count);
        paramMap.put("type", 1);
        List<EdocRegisterCondition> list =  manager.getEdocRegisterCondition(user.getLoginAccount(),paramMap, user);
        //【更多】
        String s = "";
        try {
            s = URLEncoder.encode(this.getName(preference),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        c.addBottomButton(BaseSectionTemplete.BOTTOM_BUTTON_LABEL_MORE,"/edocController.do?method=getEdocRegisterConditions&type=1"+"&columnsName="+s);
        for (EdocRegisterCondition register : list) {
            ChessboardTemplete.Item item = c.addItem();
            
            String url = "/edocController.do?method=listSendEdocSearchReultByDocManager&listType=section&conditionId="+register.getId();
            
            item.setLink(url);
            item.setOpenType(OPEN_TYPE.openWorkSpace);
            item.setName(Strings.toHTML(register.getTitle()));
            item.addExtIcon("posting_registration_thin_16");
            item.setTitle(register.getTitle());
        }
        return c;
    }

    /**
	 * 是否允许添加-使用该栏目，默认允许，如果需要特别控制，需要重载该方法，当前登录信息从CurrentUser中取
	 * @return
	 */
    @Override
	public boolean isAllowUsed() {
		User user = CurrentUser.get();
		return user.hasResourceCode("F07_sendDJSearch") || user.isAdmin() || user.isGroupAdmin();
	}
	
}
