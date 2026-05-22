package com.mycompany.myapp.model;

/**
 * DTO ghép thông tin từ:
 *   USERS + ACCOUNT + ACCOUNT_ASSIGN_ROLE_GROUP + ROLE_GROUP
 *   + TEACHER_PROFILE hoặc OFFICE_STAFF_PROFILE
 */
public class PersonnelDTO {

    // ── USERS ──────────────────────────────────────────
    private int    userId;
    private String fullName;
    private String email;
    private String phone;
    private String identityCard;

    // ── ACCOUNT ────────────────────────────────────────
    private int    accountId;
    private String username;
    private String accountStatus;   // ACTIVE | LOCKED

    // ── ROLE_GROUP ─────────────────────────────────────
    private int    roleGroupId;
    private String roleGroupName;

    // ── Loại nhân sự (tự suy từ profile tồn tại) ──────
    private String personnelType;   // "TEACHER" | "STAFF" | "OTHER"

    // ── TEACHER_PROFILE (null nếu không phải GV) ───────
    private String major;
    private String degree;

    // ── OFFICE_STAFF_PROFILE (null nếu không phải NV) ──
    private String position;
    private double baseSalary;
    private int    salaryGrade;

    // ═══════════════════ GETTERS / SETTERS ═════════════

    public int    getUserId()          { return userId; }
    public void   setUserId(int v)          { userId = v; }

    public String getFullName()        { return fullName; }
    public void   setFullName(String v)     { fullName = v; }

    public String getEmail()           { return email; }
    public void   setEmail(String v)        { email = v; }

    public String getPhone()           { return phone; }
    public void   setPhone(String v)        { phone = v; }

    public String getIdentityCard()    { return identityCard; }
    public void   setIdentityCard(String v) { identityCard = v; }

    public int    getAccountId()       { return accountId; }
    public void   setAccountId(int v)       { accountId = v; }

    public String getUsername()        { return username; }
    public void   setUsername(String v)     { username = v; }

    public String getAccountStatus()   { return accountStatus; }
    public void   setAccountStatus(String v){ accountStatus = v; }

    public int    getRoleGroupId()     { return roleGroupId; }
    public void   setRoleGroupId(int v)     { roleGroupId = v; }

    public String getRoleGroupName()   { return roleGroupName; }
    public void   setRoleGroupName(String v){ roleGroupName = v; }

    public String getPersonnelType()   { return personnelType; }
    public void   setPersonnelType(String v){ personnelType = v; }

    public String getMajor()           { return major; }
    public void   setMajor(String v)        { major = v; }

    public String getDegree()          { return degree; }
    public void   setDegree(String v)       { degree = v; }

    public String getPosition()        { return position; }
    public void   setPosition(String v)     { position = v; }

    public double getBaseSalary()      { return baseSalary; }
    public void   setBaseSalary(double v)   { baseSalary = v; }

    public int    getSalaryGrade()     { return salaryGrade; }
    public void   setSalaryGrade(int v)     { salaryGrade = v; }

    /** Trả về nhãn hiển thị loại nhân sự. */
    public String getPersonnelTypeLabel() {
        if ("TEACHER".equals(personnelType)) return "Giáo viên";
        if ("STAFF".equals(personnelType))   return "Nhân viên";
        return "Khác";
    }
}
