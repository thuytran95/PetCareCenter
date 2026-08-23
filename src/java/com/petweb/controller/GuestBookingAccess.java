package com.petweb.controller;

import jakarta.servlet.http.HttpSession;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Quyền thao tác trên đơn của KHÁCH VÃNG LAI trong phạm vi một phiên trình duyệt.
 *
 * Đơn của khách vãng lai không gắn với tài khoản nào, nên không thể dựa vào
 * đăng nhập để phân quyền. Thay vào đó, phiên được cấp quyền trên một đơn khi:
 *   - vừa tự tay chốt đơn đó (BookingServlet), hoặc
 *   - tra cứu thành công bằng mã + số điện thoại (BookingLookupServlet).
 *
 * Dùng tập hợp thay vì một giá trị đơn lẻ vì một khách có thể đặt nhiều đơn
 * trong cùng một phiên; lưu một id sẽ làm mất quyền trên đơn đặt trước đó.
 */
final class GuestBookingAccess {

    private static final String SESSION_KEY = "guestBookingAccess";

    /** Giới hạn số đơn nhớ trong phiên, tránh phình session nếu ai đó đặt liên tục. */
    private static final int MAX_REMEMBERED = 20;

    private GuestBookingAccess() {
    }

    /** Cấp quyền cho phiên hiện tại trên một đơn. */
    static void grant(HttpSession session, int bookingId) {
        Set<Integer> allowed = read(session);
        if (allowed == null) {
            allowed = new LinkedHashSet<>();
        }
        // Bỏ bớt đơn cũ nhất khi vượt giới hạn
        while (allowed.size() >= MAX_REMEMBERED) {
            allowed.remove(allowed.iterator().next());
        }
        allowed.add(bookingId);
        session.setAttribute(SESSION_KEY, allowed);
    }

    /** Phiên này có được thao tác trên đơn đó không. */
    static boolean has(HttpSession session, int bookingId) {
        Set<Integer> allowed = read(session);
        return allowed != null && allowed.contains(bookingId);
    }

    @SuppressWarnings("unchecked")
    private static Set<Integer> read(HttpSession session) {
        Object raw = session.getAttribute(SESSION_KEY);
        return (raw instanceof Set) ? (Set<Integer>) raw : null;
    }
}
