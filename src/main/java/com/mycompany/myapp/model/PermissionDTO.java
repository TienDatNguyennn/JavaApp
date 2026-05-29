package com.mycompany.myapp.model;

public class PermissionDTO {
    public int roleId;
    public int functionId;
    public String functionName;
    public boolean canView;
    public boolean canAdd;
    public boolean canEdit;
    public boolean canDelete;

    public PermissionDTO(
            int roleId,
            int functionId,
            String functionName,
            boolean canView,
            boolean canAdd,
            boolean canEdit,
            boolean canDelete
    ) {
        this.roleId = roleId;
        this.functionId = functionId;
        this.functionName = functionName;
        this.canView = canView;
        this.canAdd = canAdd;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }
}
