-- =====================================================================
-- PetCareCenter - Migration v3
--   1. Bo sung trang thai PAID (thanh toan gia lap)
--   2. Bang notification: lich su thong bao gia lap gui toi khach
--   3. Ma tra cuu don cho khach vang lai (khong co tai khoan)
-- Chay duoc nhieu lan.
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Mo rong trang thai don
--    DRAFT     : khach dang chon dich vu, chua chot
--    CONFIRMED : da chot don, CHUA thanh toan
--    PAID      : da thanh toan (mo phong, khong co cong thanh toan that)
--    COMPLETED : dich vu da dien ra xong
--    CANCELLED : da huy
-- ---------------------------------------------------------------------
ALTER TABLE booking DROP CONSTRAINT IF EXISTS booking_status_chk;
ALTER TABLE booking ADD CONSTRAINT booking_status_chk
    CHECK (status IN ('DRAFT','CONFIRMED','PAID','COMPLETED','CANCELLED'));

ALTER TABLE booking ADD COLUMN IF NOT EXISTS paid_at      timestamp;
ALTER TABLE booking ADD COLUMN IF NOT EXISTS cancelled_at timestamp;

-- ---------------------------------------------------------------------
-- 2. Ma tra cuu cho khach vang lai
--    Khach khong co tai khoan nen sau khi dong trinh duyet se khong tim
--    lai duoc don. Ma nay + so dien thoai cho phep tra cuu lai.
--    Dung ma ngau nhien thay vi booking_id de nguoi khac khong do duoc.
-- ---------------------------------------------------------------------
ALTER TABLE booking ADD COLUMN IF NOT EXISTS lookup_code varchar(12);

CREATE UNIQUE INDEX IF NOT EXISTS idx_booking_lookup
    ON booking (lookup_code) WHERE lookup_code IS NOT NULL;

-- ---------------------------------------------------------------------
-- 3. Lich su thong bao (gia lap SMS)
--    He thong khong gui SMS that; moi lan "gui" chi ghi lai mot dong o day
--    de hien thi cho khach va de bao ve do an co du lieu that.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification (
    notification_id serial       PRIMARY KEY,
    booking_id      int          REFERENCES booking(booking_id) ON DELETE CASCADE,
    user_id         int          REFERENCES user_account(id)    ON DELETE CASCADE,
    recipient       varchar(30)  NOT NULL,          -- so dien thoai nhan
    channel         varchar(20)  NOT NULL DEFAULT 'SMS',
    event_type      varchar(30)  NOT NULL,          -- BOOKING_CONFIRMED / PAYMENT / CANCELLED / REMINDER
    content         text         NOT NULL,
    created_at      timestamp    NOT NULL DEFAULT now(),
    CONSTRAINT notification_channel_chk CHECK (channel IN ('SMS','EMAIL')),
    CONSTRAINT notification_event_chk
        CHECK (event_type IN ('BOOKING_CONFIRMED','PAYMENT','CANCELLED','REMINDER'))
);

CREATE INDEX IF NOT EXISTS idx_notification_user    ON notification (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_booking ON notification (booking_id);

COMMIT;
