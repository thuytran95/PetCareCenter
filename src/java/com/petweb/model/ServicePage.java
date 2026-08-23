package com.petweb.model;

import java.util.List;

/**
 * Nội dung giới thiệu của MỘT trang dịch vụ (spa / khách sạn / tiêm vaccine / khám bệnh).
 *
 * Tách riêng phần nội dung khỏi phần hiển thị: mỗi dịch vụ là một đối tượng dữ liệu,
 * còn giao diện dùng chung một khuôn. Nhờ vậy thêm dịch vụ thứ 5 chỉ cần khai báo
 * thêm dữ liệu, không phải nhân bản thêm một file JSP nữa.
 */
public class ServicePage {

    /** Mã trên URL: spa | hotel | vaccine | medical */
    private final String code;
    private final String title;
    private final String tagline;
    private final String intro;
    private final String iconClass;
    private final String colorName;
    /** Loại dịch vụ dùng khi đặt lịch (HOTEL/SPA/MEDICAL); vaccine và khám bệnh cùng dùng MEDICAL. */
    private final String bookingType;
    private final List<Highlight> highlights;
    private final List<String> steps;
    private final List<Faq> faqs;
    /** Nguồn bảng giá hiển thị: SPA_ITEMS | MEDICAL_ITEMS | ROOM_TYPES */
    private final String priceSource;
    private final String priceNote;

    public ServicePage(String code, String title, String tagline, String intro,
                       String iconClass, String colorName, String bookingType,
                       List<Highlight> highlights, List<String> steps, List<Faq> faqs,
                       String priceSource, String priceNote) {
        this.code = code;
        this.title = title;
        this.tagline = tagline;
        this.intro = intro;
        this.iconClass = iconClass;
        this.colorName = colorName;
        this.bookingType = bookingType;
        this.highlights = highlights;
        this.steps = steps;
        this.faqs = faqs;
        this.priceSource = priceSource;
        this.priceNote = priceNote;
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getTagline() { return tagline; }
    public String getIntro() { return intro; }
    public String getIconClass() { return iconClass; }
    public String getColorName() { return colorName; }
    public String getBookingType() { return bookingType; }
    public List<Highlight> getHighlights() { return highlights; }
    public List<String> getSteps() { return steps; }
    public List<Faq> getFaqs() { return faqs; }
    public String getPriceSource() { return priceSource; }
    public String getPriceNote() { return priceNote; }

    /** Một điểm nổi bật của dịch vụ. */
    public static class Highlight {
        private final String icon;
        private final String title;
        private final String description;

        public Highlight(String icon, String title, String description) {
            this.icon = icon;
            this.title = title;
            this.description = description;
        }

        public String getIcon() { return icon; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }

    /** Một câu hỏi thường gặp. */
    public static class Faq {
        private final String question;
        private final String answer;

        public Faq(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
    }
}
