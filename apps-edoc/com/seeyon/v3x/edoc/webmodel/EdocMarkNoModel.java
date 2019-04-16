package com.seeyon.v3x.edoc.webmodel;

public class EdocMarkNoModel {

	private Long edocMarkId;	
	private String markNo;
	private Integer markNumber;
	//大流水还是小流水
	private Integer markCategoryCodeMode;
	
	
	public Integer getMarkCategoryCodeMode() {
		return markCategoryCodeMode;
	}

	public void setMarkCategoryCodeMode(Integer markCategoryCodeMode) {
		this.markCategoryCodeMode = markCategoryCodeMode;
	}

	public Long getEdocMarkId() {
		return edocMarkId;
	}
	
	public void setEdocMarkId(Long edocMarkId) {
		this.edocMarkId = edocMarkId;
	}
	
	public String getMarkNo() {
		return markNo;
	}

	public void setMarkNo(String markNo) {
		this.markNo = markNo;
	}

	public Integer getMarkNumber() {
		return markNumber;
	}

	public void setMarkNumber(Integer markNumber) {
		this.markNumber = markNumber;
	}	
	
}
