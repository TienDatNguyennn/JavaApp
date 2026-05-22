-- ===========================================================
-- FIX: Cho phép tái sử dụng email / phone / username của
--      tài khoản đã bị xóa mềm (is_deleted = 1).
--
-- Cách hoạt động:
--   UNIQUE index thông thường chặn mọi giá trị trùng dù đã xóa.
--   Function-based unique index chỉ index các dòng CHƯA xóa;
--   dòng đã xóa trả về NULL → Oracle cho phép nhiều NULL trong
--   unique index → không còn xung đột.
--
-- Chạy với user: quanlytrungtam (hoặc DBA)
-- ===========================================================


-- -----------------------------------------------------------
-- BƯỚC 1: Xem các UNIQUE constraint hiện tại
--         (chạy để lấy tên constraint trước khi drop)
-- -----------------------------------------------------------
SELECT uc.TABLE_NAME,
       uc.CONSTRAINT_NAME,
       ucc.COLUMN_NAME,
       uc.CONSTRAINT_TYPE
FROM   USER_CONSTRAINTS  uc
JOIN   USER_CONS_COLUMNS ucc ON uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME
WHERE  uc.TABLE_NAME IN ('USERS', 'ACCOUNT')
  AND  uc.CONSTRAINT_TYPE = 'U'
ORDER  BY uc.TABLE_NAME, ucc.COLUMN_NAME;


-- -----------------------------------------------------------
-- BƯỚC 2: Xóa các UNIQUE constraint cũ trên USERS
--         (engine tự tìm theo cột, không cần biết tên)
-- -----------------------------------------------------------
BEGIN
    -- email
    FOR c IN (
        SELECT uc.CONSTRAINT_NAME
        FROM   USER_CONSTRAINTS  uc
        JOIN   USER_CONS_COLUMNS ucc ON uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME
        WHERE  uc.TABLE_NAME    = 'USERS'
          AND  uc.CONSTRAINT_TYPE = 'U'
          AND  ucc.COLUMN_NAME  = 'EMAIL'
    ) LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE USERS DROP CONSTRAINT ' || c.CONSTRAINT_NAME;
    END LOOP;

    -- phone
    FOR c IN (
        SELECT uc.CONSTRAINT_NAME
        FROM   USER_CONSTRAINTS  uc
        JOIN   USER_CONS_COLUMNS ucc ON uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME
        WHERE  uc.TABLE_NAME    = 'USERS'
          AND  uc.CONSTRAINT_TYPE = 'U'
          AND  ucc.COLUMN_NAME  = 'PHONE'
    ) LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE USERS DROP CONSTRAINT ' || c.CONSTRAINT_NAME;
    END LOOP;

    -- identity_card
    FOR c IN (
        SELECT uc.CONSTRAINT_NAME
        FROM   USER_CONSTRAINTS  uc
        JOIN   USER_CONS_COLUMNS ucc ON uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME
        WHERE  uc.TABLE_NAME    = 'USERS'
          AND  uc.CONSTRAINT_TYPE = 'U'
          AND  ucc.COLUMN_NAME  = 'IDENTITY_CARD'
    ) LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE USERS DROP CONSTRAINT ' || c.CONSTRAINT_NAME;
    END LOOP;
END;
/


-- -----------------------------------------------------------
-- BƯỚC 3: Xóa UNIQUE constraint cũ trên ACCOUNT.USERNAME
-- -----------------------------------------------------------
BEGIN
    FOR c IN (
        SELECT uc.CONSTRAINT_NAME
        FROM   USER_CONSTRAINTS  uc
        JOIN   USER_CONS_COLUMNS ucc ON uc.CONSTRAINT_NAME = ucc.CONSTRAINT_NAME
        WHERE  uc.TABLE_NAME    = 'ACCOUNT'
          AND  uc.CONSTRAINT_TYPE = 'U'
          AND  ucc.COLUMN_NAME  = 'USERNAME'
    ) LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE ACCOUNT DROP CONSTRAINT ' || c.CONSTRAINT_NAME;
    END LOOP;
END;
/


-- -----------------------------------------------------------
-- BƯỚC 4: Tạo function-based unique index mới
--   Khi IS_DELETED = 0  → index giá trị thực (bắt trùng)
--   Khi IS_DELETED = 1  → index NULL       (bỏ qua, cho trùng)
-- -----------------------------------------------------------

-- USERS.EMAIL (không phân biệt hoa thường)
CREATE UNIQUE INDEX UX_USERS_EMAIL_ACTIVE
    ON USERS ( CASE WHEN IS_DELETED = 0 THEN LOWER(EMAIL) ELSE NULL END );

-- USERS.PHONE
CREATE UNIQUE INDEX UX_USERS_PHONE_ACTIVE
    ON USERS ( CASE WHEN IS_DELETED = 0 THEN PHONE ELSE NULL END );

-- USERS.IDENTITY_CARD
CREATE UNIQUE INDEX UX_USERS_IDCARD_ACTIVE
    ON USERS ( CASE WHEN IS_DELETED = 0 THEN IDENTITY_CARD ELSE NULL END );

-- ACCOUNT.USERNAME (không phân biệt hoa thường)
CREATE UNIQUE INDEX UX_ACCOUNT_USERNAME_ACTIVE
    ON ACCOUNT ( CASE WHEN IS_DELETED = 0 THEN LOWER(USERNAME) ELSE NULL END );


-- -----------------------------------------------------------
-- BƯỚC 5: Kiểm tra kết quả
-- -----------------------------------------------------------
SELECT INDEX_NAME, INDEX_TYPE, TABLE_NAME, STATUS
FROM   USER_INDEXES
WHERE  TABLE_NAME IN ('USERS', 'ACCOUNT')
  AND  INDEX_NAME LIKE 'UX_%'
ORDER  BY TABLE_NAME, INDEX_NAME;

COMMIT;
