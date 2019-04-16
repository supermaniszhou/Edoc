package com.seeyon.ctp.portal.portlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.portal.portlet.PortletConstants.PortletCategory;
import com.seeyon.ctp.portal.portlet.PortletConstants.PortletSize;
import com.seeyon.ctp.portal.portlet.PortletConstants.UrlType;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocListManager;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;
import com.seeyon.v3x.exchange.manager.EdocExchangeManager;

public class EdocPortlet implements BasePortlet {
	
	private static final Log log = LogFactory.getLog(EdocPortlet.class);
			
	private EdocListManager edocListManager;
	private EdocExchangeManager edocExchangeManager;
	private SuperviseManager superviseManager;
	
	
	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}

	public void setEdocExchangeManager(EdocExchangeManager edocExchangeManager) {
		this.edocExchangeManager = edocExchangeManager;
	}

	public void setEdocListManager(EdocListManager edocListManager) {
		this.edocListManager = edocListManager;
	}

	@Override
	public String getId() {
		return "edocPortlet";
	}

	@Override
	public List<ImagePortletLayout> getData() {
        System.out.println("---------------------");
		List<ImagePortletLayout> layouts = new ArrayList<ImagePortletLayout>();
		//发文管理
		layouts.add(this.getSendEdocManagerPortlet());
		//收文管理
		layouts.add(this.getRecManagerPortlet());
		//签报管理
		layouts.add(this.getSignReportPortlet());
		//公文交换
		layouts.add(this.getEdocExchangePortlet());
		//公文督办
		layouts.add(this.getEdocSupervisePortlet());
		//收文登记
		layouts.add(this.getEdocRecRegisterPortlet());
		//公文
		layouts.add(this.getEdocPortlet());
		//待办公文
		layouts.add(this.getPendingEdocPortlet());
		return layouts;
	}
	
	@Override
	public ImagePortletLayout getPortlet(String portletId) {
		List<ImagePortletLayout> layouts = this.getData();
		if(CollectionUtils.isNotEmpty(layouts)){
			for(ImagePortletLayout layout : layouts){
				if(portletId.equals(layout.getPortletId())){
					return layout;
				}
			}
		}
		return null;
	}


	@Override
	public int getDataCount(String portletId) {
		try {
			User user = AppContext.getCurrentUser();
			
			//发文、收文、签报待办数
	        if("sendManagerPortlet".equals(portletId) || "recManagerPortlet".equals(portletId) ||"signReportPortlet".equals(portletId) || "edocPortlet".equals(portletId) || "pendingEdocPortlet".equals(portletId)){
	        	int pendingCount=0;
	        	 String listType = "listPendingAll" ;
	 		    int type = EdocNavigationEnum.EdocV5ListTypeEnum.getEnumByKey(listType).getType();
	 		    Map<String, Object> condition = new HashMap<String, Object>();
	 		    int edocType=0;
	 		    if("sendManagerPortlet".equals(portletId)){
	 		    	edocType=0;
	 		    }else if("recManagerPortlet".equals(portletId)){
	 		    	edocType=1;
	 		    }else if("signReportPortlet".equals(portletId)){
	 		    	edocType=2;
	 		    }else if("edocPortlet".equals(portletId) || "pendingEdocPortlet".equals(portletId)){
	 		       edocType=-1;
	 		    }
	 		    condition.put("edocType", edocType);
	 		    condition.put("state", StateEnum.col_pending.key());
	 	        condition.put("user", user);
	 	        condition.put("listType","listPending");
	 	        condition.put("userId", user.getId()); 
	 			condition.put("accountId", user.getLoginAccount());//不需要进行分页查询，
	 			pendingCount  = edocListManager.findEdocPendingCount(type, condition);
	 			return  pendingCount;
	        }else if("edocExchangePortlet".equals(portletId)){ //待交换数据（待发送和待签收）的和
	        	int count1 = 0;
	        	int count2 = 0;
	        	Map<String, Object> condition = new HashMap<String, Object>();
	        	String listType="listExchangeToSend";
	    		condition.put("listType", "listExchangeToSend");
	    		condition.put("modelType", "toSend");
	    		condition.put("user", user);
	    		int type = EdocNavigationEnum.EdocV5ListTypeEnum.getTypeName(listType);
	    		count1 =  edocExchangeManager.findEdocExchangeRecordCount(type, condition);
	        	listType="listExchangeToRecieve";
	    		condition.put("listType", "listExchangeToRecieve");
	    		condition.put("modelType", "toSend");
	    		condition.put("user", user);
	    		type = EdocNavigationEnum.EdocV5ListTypeEnum.getTypeName(listType);
	    		count2 =  edocExchangeManager.findEdocExchangeRecordCount(type, condition);
	        	return count1+count2;
	        	
	        }else if("edocSupervisePortlet".equals(portletId)){ //公文督办
	        	Map<String, String> query = new HashMap<String, String>();
	        	query.put("app", "4");
	        	int dateCount=0;
	        	
    	        FlipInfo a = superviseManager.getSuperviseList4App(new FlipInfo(), query);
    	        if(a!=null){
    	        	dateCount = a.getTotal();
    	        }
    	        
	        	return dateCount;
	        }
		   
		} catch (BusinessException e) {
			log.error("首页工作桌面公文查询待办数异常",e);
			return 0;
		}
		
		return -1;
		

	}

    @Override
    public boolean isAllowDataUsed(String portletId) {
        if ("edocPortlet".equals(portletId)) {
            return AppContext.hasResourceCode("F07_sendManager") || AppContext.hasResourceCode("F07_recManager") || AppContext.hasResourceCode("F07_signReport");
        }
        return true;
    }

	@Override
	public boolean isAllowUsed() {
		return true;
	}
	
	/**
	 * 发文管理
	 * @return
	 */
	private ImagePortletLayout getSendEdocManagerPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
	    layout.setResourceCode("F07_sendManager");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.DocDispatch");
        layout.setOrder(410);
        layout.setPortletId("sendManagerPortlet");
        layout.setPortletName(ResourceUtil.getString("menu.sendManager.label"));//发文管理
        layout.setMobileUrl("/seeyon/m3/apps/v5/edoc/html/edocList.html?listType=send");
        layout.setPortletUrl("/edocController.do?method=entryManager&entry=sendManager");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        layout.setNeedNumber(1);
        layout.setSpaceTypes("personal,personal_custom,leader,outer,custom,department,corporation,public_custom,group,public_custom_group,cooperation_work,objective_manage,edoc_manage,meeting_manage,performance_analysis,form_application,related_project_space,m3mobile,weixinmobile");
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DocDispatch");
        image1.setSummary("");
        image1.setImageUrl("d_docdispatch.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	/**
	 * 收文管理
	 * @return
	 */
	private ImagePortletLayout getRecManagerPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
	    layout.setResourceCode("F07_recManager");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.DocReceiving");
        layout.setOrder(415);
        layout.setPortletId("recManagerPortlet");
        layout.setPortletName(ResourceUtil.getString("menu.receiveManager.label"));//收文管理
        layout.setMobileUrl("/seeyon/m3/apps/v5/edoc/html/edocList.html?listType=receive");
        layout.setPortletUrl("/edocController.do?method=entryManager&entry=recManager");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        layout.setSpaceTypes("personal,personal_custom,leader,outer,custom,department,corporation,public_custom,group,public_custom_group,cooperation_work,objective_manage,edoc_manage,meeting_manage,performance_analysis,form_application,related_project_space,m3mobile,weixinmobile");
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DocReceiving");
        image1.setSummary("");
        image1.setImageUrl("d_docreceiving.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	/**
	 * 签报管理
	 * @return
	 */
	private ImagePortletLayout getSignReportPortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F07_signReport");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.SignReceipt");
        layout.setOrder(420);
        layout.setPortletId("signReportPortlet");
        layout.setPortletName(ResourceUtil.getString("menu.signManager.label"));//签报管理
        layout.setMobileUrl("/seeyon/m3/apps/v5/edoc/html/edocList.html?listType=report");
        layout.setPortletUrl("/edocController.do?method=entryManager&entry=signReport");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        layout.setSpaceTypes("personal,personal_custom,leader,outer,custom,department,corporation,public_custom,group,public_custom_group,cooperation_work,objective_manage,edoc_manage,meeting_manage,performance_analysis,form_application,related_project_space,m3mobile,weixinmobile");
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.SignReceipt");
        image1.setSummary("");
        image1.setImageUrl("d_signreceipt.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	
	/**
	 * 公文交换
	 * @return
	 */
	private ImagePortletLayout getEdocExchangePortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F07_edocExchange");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.DocExchage");
        layout.setOrder(425);
        layout.setPortletId("edocExchangePortlet");
        layout.setPortletName(ResourceUtil.getString("menu.exchangeManager.label"));//公文交换
        layout.setPortletUrl("/exchangeEdoc.do?method=listMainEntry");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DocExchage");
        image1.setSummary("");
        image1.setImageUrl("d_docexchage.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	
	/**
	 * 公文督办
	 * @return
	 */
	private ImagePortletLayout getEdocSupervisePortlet() {
	    ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F07_edocSupervise");
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.edoc.name());
        layout.setDisplayName("system.menuname.DocSupervision");
        layout.setOrder(435);
        layout.setPortletId("edocSupervisePortlet");
        layout.setPortletName(ResourceUtil.getString("menu.superviseManager.label"));//公文督办
        layout.setPortletUrl("/supervise/supervise.do?method=listSupervise&app=4");
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.DocSupervision");
        image1.setSummary("");
        image1.setImageUrl("d_docsupervision.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	
	/**
	 * 收文登记
	 * @return
	 */
	private ImagePortletLayout getEdocRecRegisterPortlet() {
		ImagePortletLayout layout = new ImagePortletLayout();
		if(EdocHelper.isG6Version()){
			layout.setResourceCode("F07_recListRegistering");
		}else{
			layout.setResourceCode("F07_recRegister");
		}
        layout.setPluginId("edoc");
        layout.setCategory(PortletCategory.common.name());
        layout.setDisplayName("system.menuname.edocdengji");
        layout.setOrder(72);
        layout.setPortletId("edocRecRegisterPortlet");
        layout.setPortletName(ResourceUtil.getString("menu.edocRecNew.label"));//收文登记
        //A8的url
        String url = "/edocController.do?method=entryManager&entry=recManager&listType=newEdoc&edocType=1&listType=listV5Register";
        //g6的url
        if(EdocHelper.isG6Version()){
        	url = "/edocController.do?method=entryManager&entry=recManager&listType=newEdocRegister&comm=create&edocType=1&registerType=2&sendUnitId=-1&registerId=-1&listType=registerPending&recListType=registerPending";
        }
        if(EdocSwitchHelper.isOpenRegister()==false){
        	url="/edocController.do?method=entryManager&entry=recManager&listType=newEdoc&edocType=1";
        }
        layout.setPortletUrl(url);
        layout.setPortletUrlType(UrlType.workspace.name());
        layout.setSize(PortletSize.middle.ordinal());
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("system.menuname.edocdengji");
        image1.setSummary("");
        image1.setImageUrl("d_edocdengji.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
	}
	
	/**
     * 公文
     * @return
     */
    private ImagePortletLayout getEdocPortlet() {
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F07_sendManager,F07_recManager,F07_signReport");
        layout.setPluginId("edoc");
        layout.setCategory("common,edoc");
        layout.setOrder(55);
        layout.setmOrder(130);
        layout.setPortletId("edocPortlet");
        layout.setPortletName("公文");
        layout.setDisplayName("pending.edoc.label");
        layout.setMobileUrl("/seeyon/m3/apps/v5/edoc/html/edocList.html");
        layout.setSize(PortletSize.middle.ordinal());
        layout.setSpaceTypes("m3mobile,weixinmobile,mobile_application");
        layout.setNeedNumber(1);
        List<ImageLayout> ims = new ArrayList<ImageLayout>();

        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("pending.edoc.label");
        image1.setSummary("");
        image1.setImageUrl("d_officthemespace.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }
    
    /**
     * 待办公文
     * @return
     */
    private ImagePortletLayout getPendingEdocPortlet() {
        ImagePortletLayout layout = new ImagePortletLayout();
        layout.setResourceCode("F07_sendManager,F07_recManager,F07_signReport");
        layout.setPluginId("edoc");
        layout.setCategory("edoc");
        layout.setOrder(405);
        layout.setPortletId("pendingEdocPortlet");
        layout.setPortletName("待办公文");
        layout.setDisplayName("edoc.section.pending.label");
        layout.setMobileUrl("/seeyon/m3/apps/v5/edoc/html/edocList.html#allPending_");
        layout.setSize(PortletSize.middle.ordinal());
        layout.setSpaceTypes("m3mobile,weixinmobile");
        layout.setNeedNumber(1);
        List<ImageLayout> ims = new ArrayList<ImageLayout>();
        
        ImageLayout image1 = new ImageLayout();
        image1.setImageTitle("edoc.section.pending.label");
        image1.setSummary("");
        image1.setImageUrl("d_officthemespace.png");
        ims.add(image1);
        layout.setImageLayouts(ims);
        return layout;
    }
}
