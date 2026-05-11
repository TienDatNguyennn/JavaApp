package com.mycompany.myapp.model; // Nhớ đổi tên package cho khớp máy bạn

public class SubjectDTO {
    private int subjectId;
    private String subjectName;
    private String description;
    private String status; // Hứng trạng thái luận lý từ is_deleted

    // Constructor rỗng (Bắt buộc cho DAO)
    public SubjectDTO() {
    }

    // Constructor đầy đủ
    public SubjectDTO(int subjectId, String subjectName, String description, String status) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.description = description;
        this.status = status;
    }

    // --- GETTER & SETTER ---
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}