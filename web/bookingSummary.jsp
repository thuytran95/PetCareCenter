<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.petweb.model.Booking" %>
<%@ page import="com.petweb.model.BookingLine" %>
<%@ page import="com.petweb.model.BookingLineItem" %>
<%
    // Bảng tạm tính hiển thị cạnh form dịch vụ, kiểu giỏ hàng.
    // "draft" do BookingServlet.exposeDraft() nạp; phần đang chọn được JS cộng thêm
    // vào #liveItems / #liveSubtotal ngay khi người dùng tick chọn.
    Booking summaryDraft = (Booking) request.getAttribute("draft");
    java.math.BigDecimal addedTotal = (summaryDraft == null)
            ? java.math.BigDecimal.ZERO : summaryDraft.sumLineTotals();
%>
<aside class="summary-panel" id="summaryPanel"
       data-added-total="<%= addedTotal.toPlainString() %>">

    <div class="summary-head">
        <i class="fa-solid fa-receipt"></i>
        <span>Tạm tính đơn hàng</span>
    </div>

    <% if (summaryDraft != null) { %>
    <div class="summary-pet">
        <i class="fa-solid fa-paw"></i>
        <span><%= summaryDraft.getPetName() %><%= summaryDraft.getPetSpecies() == null ? "" : " · " + summaryDraft.getPetSpecies() %></span>
    </div>
    <% } %>

    <%-- Các dịch vụ đã thêm vào đơn ở những bước trước --%>
    <% if (summaryDraft != null && !summaryDraft.getLines().isEmpty()) { %>
    <div class="summary-section-title">Đã thêm vào đơn</div>
    <div class="summary-added">
        <% for (BookingLine sLine : summaryDraft.getLines()) { %>
        <div class="summary-line">
            <div class="d-flex justify-content-between gap-2">
                <span class="fw-semibold"><%= sLine.getServiceLabel() %></span>
                <span class="fw-bold" style="white-space:nowrap;">
                    <%= String.format("%,.0f", sLine.getLineTotal()) %> đ
                </span>
            </div>
            <div class="summary-line-meta">
                <% if (sLine.isHotel()) { %>
                    <%= sLine.getNote() == null ? sLine.getRoomCode() : sLine.getNote() %>
                    · <%= sLine.getQuantity() %> ngày
                <% } else { %>
                    <%= sLine.getItems().size() %> dịch vụ · <%= sLine.getFormattedStartAt() %>
                <% } %>
            </div>
            <% if (!sLine.getItems().isEmpty()) { %>
            <ul class="summary-sub">
                <% for (BookingLineItem sItem : sLine.getItems()) { %>
                <li>
                    <span><%= sItem.getItemName() %></span>
                    <span><%= String.format("%,.0f", sItem.getItemPrice()) %> đ</span>
                </li>
                <% } %>
            </ul>
            <% } %>
        </div>
        <% } %>
    </div>
    <% } %>

    <%-- Phần đang chọn ở form bên trái, JS cập nhật realtime --%>
    <div class="summary-section-title">Đang chọn</div>
    <div id="liveItems" class="summary-live">
        <div class="summary-empty" id="liveEmpty">Chưa chọn dịch vụ nào</div>
    </div>

    <div class="summary-row">
        <span>Đã thêm vào đơn</span>
        <span class="fw-semibold"><%= String.format("%,.0f", addedTotal) %> đ</span>
    </div>
    <div class="summary-row">
        <span>Đang chọn</span>
        <span class="fw-semibold" id="liveSubtotal">0 đ</span>
    </div>

    <div class="summary-total">
        <span>Tổng cộng</span>
        <span id="grandTotal"><%= String.format("%,.0f", addedTotal) %> đ</span>
    </div>

    <p class="summary-note">
        Số tiền cuối cùng được hệ thống tính lại theo bảng giá khi bạn bấm xác nhận.
    </p>
</aside>
