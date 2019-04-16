package com.seeyon.v3x.edoc.domain;

import java.io.Serializable;

import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.v3x.common.domain.BaseModel;
/**
 * EdocMarkAcl generated by MyEclipse - Hibernate Tools
 */
public class EdocMarkAcl extends BaseModel implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8604248374250260320L;
	// Fields    
//	private Long edocMarkDefinitionId;
	private EdocMarkDefinition edocMarkDefinition;
	private Long deptId;
	private String aclType; 
	//用于前端显示的属性,无须orm映射
	private V3xOrgEntity orgEntity;

    // Constructors

    /** default constructor */
    public EdocMarkAcl() {
    }

    
    /** full constructor */
    public EdocMarkAcl(Long id, EdocMarkDefinition edocMarkDefinition, Long deptId) {
        this.id = id;
        this.edocMarkDefinition = edocMarkDefinition;
        this.deptId = deptId;
    }

    // Property accessors
    
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
//    public Long getEdocMarkDefinitionId() {
//    	return edocMarkDefinitionId;
//    }
//    
//    public void setEdocMarkDefinitionId(Long edocMarkDefinitionId) {
//    	this.edocMarkDefinitionId = edocMarkDefinitionId;
//    }
    
    public EdocMarkDefinition getEdocMarkDefinition() {
        return this.edocMarkDefinition;
    }
    
    public void setEdocMarkDefinition(EdocMarkDefinition edocMarkDefinition) {
        this.edocMarkDefinition = edocMarkDefinition;
    }

    public Long getDeptId() {
        return this.deptId;
    }
    
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

	public String getType(){
		return V3xOrgEntity.ORGENT_TYPE_DEPARTMENT;
	}


	public V3xOrgEntity getOrgEntity() {
		return orgEntity;
	}


	public void setOrgEntity(V3xOrgEntity orgEntity) {
		this.orgEntity = orgEntity;
	}


	public String getAclType() {
		return aclType;
	}


	public void setAclType(String aclType) {
		this.aclType = aclType;
	}
}