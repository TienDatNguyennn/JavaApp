-- Fix trg_PreventRoomCollision
-- Vấn đề 1: ORA-04091 mutating table khi UPDATE (cần PRAGMA AUTONOMOUS_TRANSACTION)
-- Vấn đề 2: Khi UPDATE, trigger tìm thấy chính row đang sửa → báo lỗi trùng giả
--           → thêm điều kiện loại trừ schedule_id hiện tại
-- Vấn đề 3: Thiếu AND is_deleted = 0 → bản ghi đã xóa mềm cũng bị tính là trùng

CREATE OR REPLACE TRIGGER trg_PreventRoomCollision
BEFORE INSERT OR UPDATE ON CLASS_SCHEDULE
FOR EACH ROW
DECLARE
    PRAGMA AUTONOMOUS_TRANSACTION;
    v_count NUMBER;
BEGIN
    -- Bỏ qua kiểm tra khi xóa mềm (UPDATE SET is_deleted=1)
    IF :NEW.is_deleted = 1 THEN
        COMMIT;
        RETURN;
    END IF;

    SELECT COUNT(*)
    INTO v_count
    FROM CLASS_SCHEDULE
    WHERE room_id    = :NEW.room_id
      AND day_of_week = :NEW.day_of_week
      AND is_deleted  = 0
      -- Loại trừ chính row đang được UPDATE (schedule_id đã tồn tại)
      AND (:NEW.schedule_id IS NULL OR schedule_id <> :NEW.schedule_id)
      -- Kiểm tra giao thời gian
      AND :NEW.start_time < end_time
      AND :NEW.end_time   > start_time;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Phong hoc bi trung lich trong khung gio nay');
    END IF;

    COMMIT;
END;
/
