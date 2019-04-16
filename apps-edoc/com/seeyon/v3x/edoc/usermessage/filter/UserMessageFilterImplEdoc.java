/**
 * 
 */
package com.seeyon.v3x.edoc.usermessage.filter;

import java.util.Set;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.usermessage.UserMessageFilter;
import com.seeyon.ctp.common.usermessage.pipeline.MessagePipeline;

/**
 * 
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2008-3-22
 */
public class UserMessageFilterImplEdoc implements UserMessageFilter {

	/**
	 * 第一个参数是：类别<br>
	        参考：EdocEnum.MessageFilterParam 枚举
	 * </pre>
	 * 
	 * 第二个参数是重要程度(暂不作考虑) 
	 */

	public boolean doFilter(MessagePipeline pipelineName, int messageCategory, Set<String> configItems, Object... args) {
	
        if(configItems == null || args == null || args.length == 0){
            return pipelineName.isDefaultSend();
        }

        if(configItems.contains(String.valueOf(args[0])) || configItems.contains("ALL")){
            return true;
        }
        
        return false;
	}

    @Override
    public Integer getAppCategoryId() {
        return ApplicationCategoryEnum.edoc.key();
    }

}
