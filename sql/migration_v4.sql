-- =====================================================================
-- PetCareCenter - Migration v4: So tiem chung & kham dinh ky
--
--   1. Phan loai hang muc y te + chu ky nhac lai
--   2. Bang pet_health_record: so suc khoe cua tung thu cung
--
-- Chay duoc nhieu lan.
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Phan loai hang muc trong bang gia y te
--    category    : VACCINE | CHECKUP | DEWORM | OTHER
--    repeat_months: sau bao nhieu thang thi can lam lai (NULL = khong lap)
-- ---------------------------------------------------------------------
ALTER TABLE medical_service_item ADD COLUMN IF NOT EXISTS category      varchar(20) NOT NULL DEFAULT 'OTHER';
ALTER TABLE medical_service_item ADD COLUMN IF NOT EXISTS repeat_months int;

ALTER TABLE medical_service_item DROP CONSTRAINT IF EXISTS medical_item_category_chk;
ALTER TABLE medical_service_item ADD CONSTRAINT medical_item_category_chk
    CHECK (category IN ('VACCINE','CHECKUP','DEWORM','OTHER'));

-- Gan phan loai theo ten hang muc san co.
-- Chi cap nhat cac dong con dang de mac dinh, de khong de len chinh sua thu cong.
UPDATE medical_service_item
   SET category = 'VACCINE', repeat_months = 12
 WHERE category = 'OTHER'
   AND (item_name ILIKE '%tiêm%' OR item_name ILIKE '%vaccine%');

UPDATE medical_service_item
   SET category = 'DEWORM', repeat_months = 3
 WHERE category = 'OTHER'
   AND item_name ILIKE '%giun%';

UPDATE medical_service_item
   SET category = 'CHECKUP', repeat_months = 6
 WHERE category = 'OTHER'
   AND (item_name ILIKE '%khám%' OR item_name ILIKE '%xét nghiệm%' OR item_name ILIKE '%siêu âm%');

-- ---------------------------------------------------------------------
-- 2. So suc khoe thu cung
--    Moi dong = mot lan da thuc hien dich vu y te cho mot be.
--    Duoc sinh tu don da thanh toan (xem HealthRecordService), khong nhap tay,
--    nen khong bao gio lech voi lich su dat lich.
--
--    item_name la ban chup: doi ten dich vu ve sau khong lam sai so cu.
--    next_due_at duoc tinh san khi ghi, de truy van nhac lich khong phai tinh lai.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pet_health_record (
    record_id    serial       PRIMARY KEY,
    pet_id       int          NOT NULL REFERENCES pet(pet_id)         ON DELETE CASCADE,
    booking_id   int          REFERENCES booking(booking_id)          ON DELETE SET NULL,
    line_id      int,
    record_type  varchar(20)  NOT NULL,
    item_id      int,
    item_name    varchar(200) NOT NULL,
    performed_at timestamp    NOT NULL,
    next_due_at  timestamp,
    note         text,
    created_at   timestamp    NOT NULL DEFAULT now(),

    CONSTRAINT health_record_type_chk
        CHECK (record_type IN ('VACCINE','CHECKUP','DEWORM','OTHER'))
);

CREATE INDEX IF NOT EXISTS idx_health_pet      ON pet_health_record (pet_id, performed_at DESC);
CREATE INDEX IF NOT EXISTS idx_health_due      ON pet_health_record (next_due_at) WHERE next_due_at IS NOT NULL;

-- Mot hang muc cua mot dong dich vu chi duoc ghi mot lan, tranh trung khi
-- thanh toan bi goi lai hoac tac vu nen chay lai.
CREATE UNIQUE INDEX IF NOT EXISTS idx_health_unique
    ON pet_health_record (booking_id, line_id, item_id)
    WHERE booking_id IS NOT NULL AND line_id IS NOT NULL AND item_id IS NOT NULL;

COMMIT;
