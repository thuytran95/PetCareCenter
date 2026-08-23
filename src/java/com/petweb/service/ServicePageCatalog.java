package com.petweb.service;

import com.petweb.model.ServicePage;
import com.petweb.model.ServicePage.Faq;
import com.petweb.model.ServicePage.Highlight;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Nội dung giới thiệu của 4 trang dịch vụ.
 *
 * Đặt ở một chỗ duy nhất thay vì viết cứng trong 4 file JSP gần giống nhau:
 * sửa nội dung chỉ động vào đây, và thêm dịch vụ mới chỉ là thêm một mục
 * trong map bên dưới.
 *
 * Lưu ý nghiệp vụ: "Tiêm vaccine" và "Khám bệnh" là hai trang giới thiệu riêng
 * nhưng khi đặt lịch đều thuộc cùng một loại dịch vụ MEDICAL, nên cùng dùng
 * chung bảng giá medical_service_item.
 */
public final class ServicePageCatalog {

    public static final String PRICE_SPA = "SPA_ITEMS";
    public static final String PRICE_MEDICAL = "MEDICAL_ITEMS";
    public static final String PRICE_ROOMS = "ROOM_TYPES";

    private static final Map<String, ServicePage> PAGES = new LinkedHashMap<>();

    static {
        PAGES.put("spa", new ServicePage(
                "spa",
                "Spa thú cưng",
                "Thư giãn, nâng niu từng phút giây cho các bé",
                "Dịch vụ spa của Pet Care Center giúp thú cưng sạch sẽ, thơm tho và thoải mái. "
                        + "Mỗi bé được tắm bằng sản phẩm phù hợp với loại da và độ tuổi, sấy khô hoàn toàn "
                        + "để tránh nấm da, kết hợp cắt tỉa theo đúng đặc điểm từng giống.",
                "fa-spa", "pink", "spa",
                List.of(
                        new Highlight("fa-droplet", "Sản phẩm phù hợp từng bé",
                                "Sữa tắm chọn theo loại da, lông và độ tuổi, ưu tiên thành phần dịu nhẹ."),
                        new Highlight("fa-wind", "Sấy khô hoàn toàn",
                                "Sấy kỹ tới tận chân lông để hạn chế nấm và viêm da sau khi tắm."),
                        new Highlight("fa-scissors", "Cắt tỉa đúng giống",
                                "Kiểu lông được tỉa theo đặc điểm từng giống, gọn gàng mà vẫn tự nhiên."),
                        new Highlight("fa-heart", "Nhẹ nhàng, không ép buộc",
                                "Nhân viên làm quen trước với bé, dừng lại nếu bé quá căng thẳng.")
                ),
                List.of(
                        "Kiểm tra tình trạng da, lông và tai trước khi bắt đầu",
                        "Tắm và massage thư giãn bằng sản phẩm phù hợp",
                        "Sấy khô hoàn toàn, chải gỡ rối",
                        "Cắt tỉa lông, cắt móng, vệ sinh tai",
                        "Bàn giao và dặn dò cách chăm sóc tại nhà"
                ),
                List.of(
                        new Faq("Bé nhà tôi sợ nước thì có làm được không?",
                                "Được. Nhân viên sẽ làm quen với bé trước, tắm từ từ và dừng lại nếu bé quá căng thẳng."),
                        new Faq("Một buổi spa mất bao lâu?",
                                "Thường 45–90 phút tùy kích cỡ, độ dày lông và các dịch vụ bạn chọn thêm."),
                        new Faq("Có cần đặt lịch trước không?",
                                "Nên đặt trước để trung tâm sắp xếp nhân viên, tránh phải chờ lâu.")
                ),
                PRICE_SPA,
                "Giá theo từng hạng mục, bạn có thể chọn nhiều mục trong cùng một lần đặt."
        ));

        PAGES.put("hotel", new ServicePage(
                "hotel",
                "Khách sạn thú cưng",
                "Ngôi nhà thứ hai ấm áp, an toàn và đầy yêu thương",
                "Khi bạn đi công tác hay du lịch, thú cưng vẫn cần được ăn ngủ đúng giờ và có người "
                        + "để ý. Khách sạn của chúng tôi bố trí phòng riêng theo kích cỡ, có khu vận động "
                        + "và nhân viên trực để bé không bị bỏ một mình cả ngày.",
                "fa-house", "blue", "hotel",
                List.of(
                        new Highlight("fa-bed", "Phòng riêng theo kích cỡ",
                                "Ba hạng phòng để chọn theo thể trạng và thói quen sinh hoạt của bé."),
                        new Highlight("fa-utensils", "Ăn uống đúng giờ",
                                "Giữ nguyên loại thức ăn bé đang dùng, cho ăn theo lịch bạn dặn."),
                        new Highlight("fa-person-running", "Có giờ vận động",
                                "Bé được ra khu vui chơi mỗi ngày thay vì ở trong phòng liên tục."),
                        new Highlight("fa-camera", "Cập nhật tình hình",
                                "Gửi hình ảnh và tình trạng của bé cho chủ trong thời gian lưu trú.")
                ),
                List.of(
                        "Chọn hạng phòng và ngày nhận, ngày trả",
                        "Gửi bé kèm thức ăn quen dùng và ghi chú thói quen",
                        "Nhân viên kiểm tra sức khỏe ban đầu, ghi nhận lưu ý",
                        "Chăm sóc và cập nhật tình hình hằng ngày",
                        "Bàn giao lại bé đúng ngày trả phòng"
                ),
                List.of(
                        new Faq("Tính tiền theo ngày hay theo đêm?",
                                "Theo ngày. Nhận và trả trong cùng một ngày vẫn được tính tròn một ngày."),
                        new Faq("Tôi gửi thức ăn riêng cho bé được không?",
                                "Rất nên. Giữ nguyên thức ăn quen thuộc giúp bé đỡ rối loạn tiêu hóa khi đổi chỗ ở."),
                        new Faq("Hết phòng thì sao?",
                                "Hệ thống kiểm tra số phòng trống ngay khi bạn đặt, nếu hết sẽ báo để bạn đổi hạng phòng hoặc đổi ngày.")
                ),
                PRICE_ROOMS,
                "Giá tính theo ngày, nhân với số ngày lưu trú."
        ));

        PAGES.put("vaccine", new ServicePage(
                "vaccine",
                "Tiêm vaccine",
                "Bảo vệ thú cưng bằng phương pháp phòng bệnh tốt nhất",
                "Tiêm phòng đầy đủ và đúng lịch là cách rẻ nhất để tránh những bệnh nguy hiểm như "
                        + "dại, care, parvo. Trung tâm theo dõi lịch tiêm cho từng bé và nhắc bạn khi "
                        + "đến hạn mũi tiếp theo.",
                "fa-syringe", "amber", "medical",
                List.of(
                        new Highlight("fa-shield-halved", "Phòng bệnh nguy hiểm",
                                "Bảo vệ trước các bệnh có tỷ lệ tử vong cao như dại, care, parvo."),
                        new Highlight("fa-stethoscope", "Khám sàng lọc trước tiêm",
                                "Chỉ tiêm khi bé đủ sức khỏe, tránh phản ứng do đang ốm sẵn."),
                        new Highlight("fa-calendar-check", "Theo dõi đúng lịch",
                                "Ghi nhận từng mũi đã tiêm và nhắc bạn khi tới hạn mũi kế tiếp."),
                        new Highlight("fa-clock", "Theo dõi sau tiêm",
                                "Giữ bé lại theo dõi một lúc để xử lý ngay nếu có phản ứng bất thường.")
                ),
                List.of(
                        "Khám sàng lọc, đo thân nhiệt trước khi tiêm",
                        "Tư vấn loại vaccine phù hợp với độ tuổi và tình trạng bé",
                        "Tiêm và ghi nhận vào sổ theo dõi",
                        "Theo dõi tại chỗ khoảng 15–30 phút",
                        "Hẹn lịch mũi tiếp theo"
                ),
                List.of(
                        new Faq("Bao lâu nên tiêm nhắc lại?",
                                "Tùy loại vaccine, thường nhắc lại hằng năm. Bác sĩ sẽ hẹn cụ thể sau mỗi mũi."),
                        new Faq("Bé đang ốm có tiêm được không?",
                                "Không nên. Trung tâm khám sàng lọc trước và sẽ hoãn nếu bé đang không khỏe."),
                        new Faq("Sau tiêm bé mệt có sao không?",
                                "Hơi mệt hoặc ăn ít trong 1–2 ngày là bình thường. Nếu bé nôn, sưng mặt hay khó thở, hãy liên hệ ngay.")
                ),
                PRICE_MEDICAL,
                "Bảng giá chung của nhóm dịch vụ y tế; bạn có thể chọn nhiều mục trong một lần đặt."
        ));

        PAGES.put("medical", new ServicePage(
                "medical",
                "Khám bệnh",
                "Mang lại sức khỏe và sự bình an cho thú cưng",
                "Thú cưng thường giấu bệnh cho tới khi đã nặng. Khám định kỳ giúp phát hiện sớm những "
                        + "vấn đề về tiêu hóa, da liễu, răng miệng hay nội tạng, khi việc điều trị còn "
                        + "đơn giản và ít tốn kém.",
                "fa-briefcase-medical", "teal", "medical",
                List.of(
                        new Highlight("fa-magnifying-glass", "Phát hiện sớm",
                                "Nhiều bệnh chỉ lộ triệu chứng khi đã nặng; khám định kỳ giúp bắt sớm hơn."),
                        new Highlight("fa-flask", "Xét nghiệm và siêu âm",
                                "Hỗ trợ chẩn đoán bằng xét nghiệm máu, siêu âm khi cần thiết."),
                        new Highlight("fa-file-medical", "Lưu hồ sơ theo từng bé",
                                "Mỗi lần khám đều lưu lại để so sánh với những lần trước."),
                        new Highlight("fa-comments", "Tư vấn rõ ràng",
                                "Giải thích tình trạng và các lựa chọn điều trị trước khi bạn quyết định.")
                ),
                List.of(
                        "Ghi nhận triệu chứng và bệnh sử của bé",
                        "Khám lâm sàng tổng quát",
                        "Chỉ định xét nghiệm hoặc siêu âm nếu cần",
                        "Chẩn đoán và tư vấn hướng điều trị",
                        "Kê đơn, hẹn tái khám và lưu hồ sơ"
                ),
                List.of(
                        new Faq("Bao lâu nên khám định kỳ một lần?",
                                "Thông thường 6–12 tháng một lần; bé lớn tuổi hoặc có bệnh nền nên khám dày hơn."),
                        new Faq("Tôi cần chuẩn bị gì khi đưa bé đi khám?",
                                "Mang theo sổ tiêm phòng (nếu có) và ghi lại các biểu hiện bất thường gần đây."),
                        new Faq("Có nhận khám ngoài giờ không?",
                                "Vui lòng liên hệ trung tâm để được sắp xếp theo tình trạng cụ thể.")
                ),
                PRICE_MEDICAL,
                "Bảng giá chung của nhóm dịch vụ y tế; bạn có thể chọn nhiều mục trong một lần đặt."
        ));
    }

    private ServicePageCatalog() {
    }

    /** Trả về nội dung trang theo mã, hoặc null nếu mã không hợp lệ. */
    public static ServicePage find(String code) {
        if (code == null) return null;
        return PAGES.get(code.trim().toLowerCase());
    }

    /** Toàn bộ trang dịch vụ, dùng cho phần "dịch vụ khác" ở cuối trang. */
    public static List<ServicePage> all() {
        return List.copyOf(PAGES.values());
    }
}
