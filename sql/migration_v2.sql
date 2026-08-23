-- =====================================================================
-- PetCareCenter - Migration v2: thiet ke lai nghiep vu dat lich
-- =====================================================================
-- Chay duoc nhieu lan (idempotent).
--
-- GIU NGUYEN: user_account, pet, spa_service_item, medical_service_item
-- XOA (deu dang rong): booking_info, service_info,
--                      hotel_detail, spa_detail, medical_detail,
--                      spa_detail_item, medical_detail_item
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Xoa cac bang giao dich cu (rong, khong co du lieu can giu)
--    Thu tu xoa theo chieu phu thuoc khoa ngoai.
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS spa_detail_item      CASCADE;
DROP TABLE IF EXISTS medical_detail_item  CASCADE;
DROP TABLE IF EXISTS hotel_detail         CASCADE;
DROP TABLE IF EXISTS spa_detail           CASCADE;
DROP TABLE IF EXISTS medical_detail       CASCADE;
DROP TABLE IF EXISTS service_info         CASCADE;
DROP TABLE IF EXISTS booking_info         CASCADE;

-- booking_detail duoc code cu tham chieu nhung chua bao gio ton tai trong DB
DROP TABLE IF EXISTS booking_detail       CASCADE;

-- Cac bang moi (neu chay lai migration)
DROP TABLE IF EXISTS booking_line_item    CASCADE;
DROP TABLE IF EXISTS booking_line         CASCADE;
DROP TABLE IF EXISTS booking              CASCADE;


-- ---------------------------------------------------------------------
-- 2. Bang gia phong khach san
--    Truoc day gia nam cung trong HotelDetail.getGiaTheoLoaiPhong().
--    total_rooms dung de kiem tra con phong trong khi dat.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS room_type (
    room_code     varchar(20)   PRIMARY KEY,
    room_name     varchar(100)  NOT NULL,
    price_per_day numeric(12,2) NOT NULL CHECK (price_per_day >= 0),
    description   varchar(255),
    total_rooms   int           NOT NULL DEFAULT 5 CHECK (total_rooms > 0),
    active        boolean       NOT NULL DEFAULT true
);

-- Seed dung 3 muc gia cu trong code Java
INSERT INTO room_type (room_code, room_name, price_per_day, description, total_rooms) VALUES
    ('vip1', 'VIP 1', 100000, 'Phong tieu chuan, am cung',        8),
    ('vip2', 'VIP 2', 200000, 'Phong rong rai, co khu vui choi',  5),
    ('vip3', 'VIP 3', 500000, 'Phong cao cap, cham soc rieng',    3)
ON CONFLICT (room_code) DO NOTHING;


-- ---------------------------------------------------------------------
-- 3. Don dat lich
--    user_id NULL  => khach vang lai (thong tin nam o guest_*)
--    pet_id  NULL  => thu cung cua khach vang lai (khong luu vao bang pet)
--    pet_name/pet_species la snapshot: sua hoac xoa pet khong lam doi don cu.
-- ---------------------------------------------------------------------
CREATE TABLE booking (
    booking_id   serial        PRIMARY KEY,

    user_id      int           REFERENCES user_account(id) ON DELETE SET NULL,
    guest_name   varchar(150),
    guest_phone  varchar(30),
    guest_email  varchar(150),

    pet_id       int           REFERENCES pet(pet_id) ON DELETE SET NULL,
    pet_name     varchar(100)  NOT NULL,
    pet_species  varchar(50),

    status       varchar(20)   NOT NULL DEFAULT 'DRAFT',
    total_price  numeric(12,2) NOT NULL DEFAULT 0 CHECK (total_price >= 0),
    created_at   timestamp     NOT NULL DEFAULT now(),
    confirmed_at timestamp,

    CONSTRAINT booking_owner_chk
        CHECK (user_id IS NOT NULL OR guest_name IS NOT NULL),
    CONSTRAINT booking_status_chk
        CHECK (status IN ('DRAFT','CONFIRMED','CANCELLED','COMPLETED'))
);

CREATE INDEX idx_booking_user    ON booking (user_id);
CREATE INDEX idx_booking_status  ON booking (status);


-- ---------------------------------------------------------------------
-- 4. Dong dich vu trong don
--    Thay cho service_info + hotel_detail + spa_detail + medical_detail.
--    Moi lan khach them mot dich vu = mot dong.
--      HOTEL   : start_at = check-in, end_at = check-out, room_code, quantity = so ngay
--      SPA     : start_at = ngay hen,      end_at NULL
--      MEDICAL : start_at = ngay nhap vien, end_at NULL
-- ---------------------------------------------------------------------
CREATE TABLE booking_line (
    line_id      serial        PRIMARY KEY,
    booking_id   int           NOT NULL REFERENCES booking(booking_id) ON DELETE CASCADE,

    service_type varchar(20)   NOT NULL,
    start_at     timestamp     NOT NULL,
    end_at       timestamp,
    room_code    varchar(20)   REFERENCES room_type(room_code),
    quantity     int           NOT NULL DEFAULT 1 CHECK (quantity > 0),
    line_total   numeric(12,2) NOT NULL DEFAULT 0 CHECK (line_total >= 0),
    note         text,
    created_at   timestamp     NOT NULL DEFAULT now(),

    CONSTRAINT line_type_chk
        CHECK (service_type IN ('HOTEL','SPA','MEDICAL')),
    CONSTRAINT line_period_chk
        CHECK (end_at IS NULL OR end_at > start_at),
    -- Dong HOTEL bat buoc co room_code va end_at; dong khac thi khong
    CONSTRAINT line_hotel_shape_chk
        CHECK (
            (service_type =  'HOTEL' AND room_code IS NOT NULL AND end_at IS NOT NULL)
         OR (service_type <> 'HOTEL' AND room_code IS NULL)
        )
);

CREATE INDEX idx_line_booking ON booking_line (booking_id);
CREATE INDEX idx_line_room    ON booking_line (room_code, start_at, end_at);


-- ---------------------------------------------------------------------
-- 5. Cac hang muc con cua dong SPA / MEDICAL
--    item_name + item_price la snapshot tai thoi diem dat:
--    sau nay trung tam doi bang gia thi hoa don cu van giu nguyen so tien.
--    (Truoc day spa_detail_item khong luu gia nen hoa don cu bi doi theo.)
--    item_id khong dat FK vi tro toi 2 bang khac nhau tuy service_type,
--    hai cot snapshot da du de dong nay tu dien giai duoc.
-- ---------------------------------------------------------------------
CREATE TABLE booking_line_item (
    line_item_id serial        PRIMARY KEY,
    line_id      int           NOT NULL REFERENCES booking_line(line_id) ON DELETE CASCADE,
    item_id      int           NOT NULL,
    item_name    varchar(200)  NOT NULL,
    item_price   numeric(12,2) NOT NULL CHECK (item_price >= 0)
);

CREATE INDEX idx_line_item_line ON booking_line_item (line_id);

COMMIT;
