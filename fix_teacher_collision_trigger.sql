-- ===========================================================
-- FIX: Trigger TRG_PREVENTTEACHERCOLLISION bị INVALID
--
-- Bug 1 (ORA-04098): :NEW.assignment_id → đúng phải là :NEW.assign_id
-- Bug 2 (ORA-04091): Row-level trigger SELECT từ bảng đang mutate
--                    → Fix bằng PRAGMA AUTONOMOUS_TRANSACTION
--
-- Chạy với user: quanlytrungtam  |  Tool: DataGrip / SQL Developer
-- ===========================================================

CREATE OR REPLACE TRIGGER trg_PreventTeacherCollision
BEFORE INSERT OR UPDATE ON TEACHING_ASSIGNMENT
FOR EACH ROW
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM CLASS_SCHEDULE cs_new
    JOIN CLASS_SCHEDULE cs_old ON cs_new.day_of_week = cs_old.day_of_week
    JOIN TEACHING_ASSIGNMENT ta ON ta.class_id = cs_old.class_id
    WHERE cs_new.class_id = :NEW.class_id
      AND ta.teacher_id = :NEW.teacher_id
      AND ta.is_deleted = 0
      AND cs_new.is_deleted = 0
      AND cs_old.is_deleted = 0
      AND (:NEW.assign_id IS NULL OR ta.assign_id <> :NEW.assign_id)
      AND cs_new.start_time < cs_old.end_time
      AND cs_new.end_time > cs_old.start_time;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20003,
            'Giao vien da co lich day o mot lop khac trong khung gio nay');
    END IF;
    COMMIT;
END;
/

-- Kiểm tra: STATUS phải là VALID
SELECT trigger_name, status FROM USER_TRIGGERS
WHERE trigger_name = 'TRG_PREVENTTEACHERCOLLISION';
