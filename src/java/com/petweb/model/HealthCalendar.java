package com.petweb.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lịch tháng cho sổ sức khỏe của một thú cưng.
 *
 * Mỗi ô là một ngày trong tháng, gắn sẵn hai danh sách: những lần ĐÃ làm
 * (theo performed_at) và những mục ĐẾN HẠN phải làm lại (theo next_due_at).
 * Nhờ vậy trang JSP chỉ việc đổ ra, không phải tính toán ngày tháng.
 *
 * Lịch luôn bắt đầu từ Thứ Hai để khớp thói quen xem lịch ở Việt Nam, nên
 * đầu tháng có thể có vài ô trống — đó là các ô có {@link Day#isBlank()} true.
 */
public class HealthCalendar {

    /** Tên các cột, dùng làm hàng tiêu đề của lịch. */
    public static final String[] WEEKDAY_LABELS =
            {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    private final YearMonth month;
    private final List<Day> days = new ArrayList<>();

    private HealthCalendar(YearMonth month) {
        this.month = month;
    }

    /**
     * Dựng lịch của một tháng từ toàn bộ sổ sức khỏe của bé.
     *
     * @param month   tháng cần xem
     * @param records toàn bộ bản ghi của bé (không cần lọc trước)
     */
    public static HealthCalendar build(YearMonth month, List<HealthRecord> records) {
        HealthCalendar cal = new HealthCalendar(month);

        // Ô trống đầu tháng: thứ Hai = 1 nên trừ 1 ra số ô cần chèn
        int lead = month.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < lead; i++) {
            cal.days.add(new Day(null));
        }
        for (int d = 1; d <= month.lengthOfMonth(); d++) {
            cal.days.add(new Day(month.atDay(d)));
        }

        if (records != null) {
            for (HealthRecord r : records) {
                cal.place(r, r.getPerformedAt() == null ? null
                        : r.getPerformedAt().toLocalDateTime().toLocalDate(), true);
                cal.place(r, r.getNextDueAt() == null ? null
                        : r.getNextDueAt().toLocalDateTime().toLocalDate(), false);
            }
        }
        return cal;
    }

    /** Gắn một bản ghi vào đúng ô ngày, bỏ qua nếu ngày đó không thuộc tháng đang xem. */
    private void place(HealthRecord r, LocalDate date, boolean done) {
        if (date == null || !YearMonth.from(date).equals(month)) return;
        for (Day day : days) {
            if (date.equals(day.date)) {
                if (done) day.done.add(r);
                else day.due.add(r);
                return;
            }
        }
    }

    public YearMonth getMonth() { return month; }

    public List<Day> getDays() { return days; }

    /** Nhãn hiển thị, ví dụ "Tháng 8 / 2026". */
    public String getLabel() {
        return "Tháng " + month.getMonthValue() + " / " + month.getYear();
    }

    /** Chuỗi dùng cho tham số URL, ví dụ "2026-08". */
    public String getYm() { return month.toString(); }

    public String getPrevYm() { return month.minusMonths(1).toString(); }

    public String getNextYm() { return month.plusMonths(1).toString(); }

    /** Tổng số lần đã làm trong tháng — để hiện dòng tóm tắt dưới lịch. */
    public int getDoneCount() {
        int n = 0;
        for (Day d : days) n += d.done.size();
        return n;
    }

    /** Tổng số mục đến hạn trong tháng. */
    public int getDueCount() {
        int n = 0;
        for (Day d : days) n += d.due.size();
        return n;
    }

    public boolean isEmpty() { return getDoneCount() == 0 && getDueCount() == 0; }

    /** Một ô trong lưới lịch. */
    public static class Day {

        private final LocalDate date;
        private final List<HealthRecord> done = new ArrayList<>();
        private final List<HealthRecord> due = new ArrayList<>();

        Day(LocalDate date) {
            this.date = date;
        }

        /** Ô đệm đầu tháng, không phải ngày thật. */
        public boolean isBlank() { return date == null; }

        public int getDayOfMonth() { return date == null ? 0 : date.getDayOfMonth(); }

        public boolean isToday() { return date != null && date.equals(LocalDate.now()); }

        public boolean isPast() { return date != null && date.isBefore(LocalDate.now()); }

        public boolean isWeekend() {
            return date != null && date.getDayOfWeek().getValue() >= 6;
        }

        public List<HealthRecord> getDone() { return done; }

        public List<HealthRecord> getDue() { return due; }

        public boolean hasAnything() { return !done.isEmpty() || !due.isEmpty(); }

        /** Có mục đến hạn mà ngày đó đã trôi qua — tức là bị trễ. */
        public boolean isOverdue() { return !due.isEmpty() && isPast(); }

        /** Nội dung tooltip khi rê chuột vào ô ngày. */
        public String getTooltip() {
            StringBuilder sb = new StringBuilder();
            for (HealthRecord r : done) {
                if (sb.length() > 0) sb.append(" • ");
                sb.append("Đã làm: ").append(r.getItemName());
            }
            for (HealthRecord r : due) {
                if (sb.length() > 0) sb.append(" • ");
                sb.append("Đến hạn: ").append(r.getItemName());
            }
            return sb.toString();
        }

        /** Tên thứ đầy đủ, dùng cho phần liệt kê bên dưới lịch. */
        public String getWeekdayLabel() {
            if (date == null) return "";
            return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("vi"));
        }
    }
}
