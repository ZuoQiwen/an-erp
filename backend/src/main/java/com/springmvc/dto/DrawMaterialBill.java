package com.springmvc.dto;

import com.springmvc.exception.BadRequestException;
import com.springmvc.pojo.DrawMaterialBillEntity;
import com.springmvc.pojo.DrawMaterialBillMaterialQuery;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class DrawMaterialBill extends DrawMaterialBillEntity {

    private String toPrincipalName;

    private String warehousePrincipalName;

    private String auditName;

    private String finishName;

    private List<DrawMaterialBillMaterial> materialList;

    public String getToPrincipalName() {
        return toPrincipalName;
    }

    public void setToPrincipalName(String toPrincipalName) {
        this.toPrincipalName = toPrincipalName;
    }

    public String getWarehousePrincipalName() {
        return warehousePrincipalName;
    }

    public void setWarehousePrincipalName(String warehousePrincipalName) {
        this.warehousePrincipalName = warehousePrincipalName;
    }

    public String getAuditName() {
        return auditName;
    }

    public void setAuditName(String auditName) {
        this.auditName = auditName;
    }

    public String getFinishName() {
        return finishName;
    }

    public void setFinishName(String finishName) {
        this.finishName = finishName;
    }

    public List<DrawMaterialBillMaterial> getMaterialList() {
        return materialList;
    }

    public void setMaterialList(List<DrawMaterialBillMaterial> materialList) {
        this.materialList = materialList;
    }
}