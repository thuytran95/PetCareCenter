-- =====================================================================
-- Don du lieu rac: cac cot dang chua DUNG CHUOI "null" thay vi gia tri NULL
--
-- Nguyen nhan: editUser.jsp / editPet.jsp truoc day dung <%= obj.getX() %>
-- de do gia tri ra o input. Khi gia tri la null, JSP in ra chuoi "null",
-- nguoi dung bam Luu la chuoi do di thang vao CSDL.
-- Ca hai JSP da duoc sua, day la buoc don not du lieu cu.
-- =====================================================================

BEGIN;

UPDATE user_account SET phone    = NULL WHERE lower(trim(phone))    IN ('null','undefined');
UPDATE user_account SET email    = NULL WHERE lower(trim(email))    IN ('null','undefined');
UPDATE user_account SET address  = NULL WHERE lower(trim(address))  IN ('null','undefined');
UPDATE user_account SET full_name= NULL WHERE lower(trim(full_name))IN ('null','undefined');

UPDATE pet SET breed             = NULL WHERE lower(trim(breed))             IN ('null','undefined');
UPDATE pet SET species           = NULL WHERE lower(trim(species))           IN ('null','undefined');
UPDATE pet SET fur_color         = NULL WHERE lower(trim(fur_color))         IN ('null','undefined');
UPDATE pet SET identifying_marks = NULL WHERE lower(trim(identifying_marks)) IN ('null','undefined');

COMMIT;
