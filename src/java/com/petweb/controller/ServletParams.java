package com.petweb.controller;

import com.petweb.service.BookingException;

import java.util.ArrayList;
import java.util.List;

/**
 * Tiện ích đọc tham số từ request một cách an toàn, dùng chung cho các Servlet.
 */
final class ServletParams {

    private ServletParams() {
    }

    /**
     * Chuyển mảng chuỗi id từ form thành danh sách số nguyên.
     * Một giá trị không phải số sẽ thành lỗi nghiệp vụ hiển thị được cho khách,
     * thay vì để NumberFormatException văng ra trang lỗi 500 của server.
     */
    static List<Integer> parseIds(String[] raw) throws BookingException {
        List<Integer> ids = new ArrayList<>();
        if (raw == null) return ids;
        for (String s : raw) {
            if (s == null || s.isBlank()) continue;
            try {
                ids.add(Integer.valueOf(s.trim()));
            } catch (NumberFormatException e) {
                throw new BookingException("Danh sách dịch vụ không hợp lệ, vui lòng chọn lại.");
            }
        }
        return ids;
    }
}
