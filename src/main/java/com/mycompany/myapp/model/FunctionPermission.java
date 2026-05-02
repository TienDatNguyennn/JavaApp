/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.model;


 

public class FunctionPermission {
    private int functionId;
    private String functionName;
    private boolean canView;
    private boolean canAdd;
    private boolean canEdit;
    private boolean canDelete;

    public FunctionPermission(int functionId, String functionName, boolean v, boolean a, boolean e, boolean d) {
        this.functionId = functionId;
        this.functionName = functionName;
        this.canView = v; this.canAdd = a; this.canEdit = e; this.canDelete = d;
    }

    public int getFunctionId() { return functionId; }
    public String getFunctionName() { return functionName; }
    public boolean isCanView() { return canView; }
    public void setCanView(boolean canView) { this.canView = canView; }
    public boolean isCanAdd() { return canAdd; }
    public void setCanAdd(boolean canAdd) { this.canAdd = canAdd; }
    public boolean isCanEdit() { return canEdit; }
    public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }
    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }
}