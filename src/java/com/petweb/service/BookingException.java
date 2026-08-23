package com.petweb.service;

/**
 * Lỗi nghiệp vụ có thể hiển thị thẳng cho người dùng
 * (ví dụ "Hết phòng VIP 2 trong khoảng thời gian này").
 *
 * Khác với SQLException: đây không phải sự cố hệ thống mà là tình huống
 * nghiệp vụ hợp lệ, nên Servlet bắt riêng và hiện thông báo thân thiện
 * thay vì ghi log mức SEVERE.
 */
public class BookingException extends Exception {

    public BookingException(String message) {
        super(message);
    }
}
