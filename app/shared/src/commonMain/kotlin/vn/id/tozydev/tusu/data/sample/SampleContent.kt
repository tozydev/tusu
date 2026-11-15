package vn.id.tozydev.tusu.data.sample

import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint

// Fixture text for the debug sample data, not UI strings, so it stays in code. Fragments are
// Markdown: `**bold**`, `*italic*`, `- item` and `## heading` become real rich text.
internal object SampleContent {
    val oneLiners =
        listOf(
            "Sáng nay trời mát, **uống cà phê** một mình ở ban công.",
            "Hôm nay làm xong việc sớm, đi bộ một vòng quanh hồ.",
            "Nấu được nồi canh chua ngon bất ngờ.",
            "Mưa cả buổi chiều, ngồi nghe nhạc và đọc sách.",
            "Gặp lại người bạn cũ, nói chuyện mãi không hết.",
            "Đạp xe ngắm hoàng hôn, thấy lòng nhẹ hẳn.",
            "Một ngày chậm rãi và *bình yên*.",
            "Tập chạy được năm cây số, chân mỏi nhưng vui.",
            "Mua được cuốn sách mình tìm từ lâu.",
            "Trời nắng đẹp, phơi đồ cả buổi sáng.",
            "Ăn bún bò Huế ở quán đầu ngõ, vẫn là vị quen thuộc.",
            "Tối nay mất điện, thắp nến ngồi tâm sự với mẹ.",
            "Đọc xong chương cuối, ngồi lặng một lúc lâu.",
            "Cảm ơn một ngày đã trôi qua êm đềm.",
            "Học thêm được món mới: bánh xèo giòn rụm.",
        )

    val openers =
        listOf(
            "Sáng nay dậy sớm hơn thường lệ, pha một ly cà phê sữa đá rồi ngồi nhìn phố dần thức giấc.",
            "Hôm nay là một ngày khá bận, nhưng mình vẫn cố dành ra chút thời gian cho bản thân.",
            "Trời Sài Gòn chiều nay đổ mưa bất chợt, cả con hẻm ngập trong tiếng mưa rơi.",
            "Cuối tuần này mình quyết định không làm gì cả, chỉ nằm đọc sách và nghe nhạc.",
            "Buổi họp sáng nay kéo dài hơn dự định, về đến nhà là đã thấy đói cồn cào.",
            "Nhớ lại hồi nhỏ, mỗi chiều mẹ đều ngồi bên hiên nhặt rau và kể chuyện cho mình nghe.",
            "Hà Nội vào thu, gió se lạnh và hương hoa sữa thoang thoảng trên từng con phố.",
            "Hôm nay mình thử nấu món mới theo công thức của bà ngoại.",
            "Cả nhóm hẹn nhau đi ăn tối sau nhiều tháng chưa gặp lại.",
            "Đêm qua khó ngủ, nằm nghĩ mông lung về những điều đã qua.",
            "Mình bắt đầu một thói quen mới: viết nhật ký mỗi buổi tối trước khi ngủ.",
            "Ghé quán cà phê quen ở góc đường, chị chủ vẫn nhớ món mình hay uống.",
            "Hôm nay deadline dồn dập, nhưng cuối cùng mọi thứ cũng kịp hoàn thành.",
            "Chiều nay mình dọn lại góc làm việc, vứt bớt những thứ không còn cần đến nữa.",
            "Đi chợ sớm với ba, mua cá và rau thơm về nấu bữa trưa cho cả nhà.",
            "Chuyến tàu đêm đưa mình về quê, ngoài cửa sổ là những cánh đồng lúa chín vàng.",
            "Sáng nay mình chạy bộ dọc bờ sông, mặt trời vừa lên rất đẹp.",
            "Một buổi tối yên tĩnh, chỉ có tiếng quạt máy và trang sách lật nhẹ.",
            "Hôm nay mình đã nói ra điều vẫn giấu trong lòng bấy lâu nay.",
            "Điện thoại báo tin nhắn từ người bạn thân, cậu ấy vừa nhận được công việc mới.",
        )

    val middles =
        listOf(
            "Có lúc mình nhận ra rằng hạnh phúc đôi khi chỉ là những điều rất nhỏ, như một bữa cơm nóng hay một cuộc gọi hỏi thăm.",
            "Mình đã **học được một điều quan trọng**: đừng cố làm hài lòng tất cả mọi người.",
            "Món ăn không hoàn hảo lắm, hơi mặn một chút, nhưng cả nhà đều ăn hết sạch.",
            "Mọi thứ diễn ra chậm lại, và mình thấy mình thở đều hơn.",
            "Có những ngày chỉ cần ngồi yên cũng đủ thấy biết ơn vì mọi thứ đang ổn.",
            "Nói chuyện với anh ấy xong, mình hiểu rõ hơn điều mình thực sự muốn.",
            "Đường về nhà hôm nay dài hơn thường lệ vì kẹt xe, nhưng bài hát trên radio lại rất hay.",
            "Mình ghi lại *những điều nhỏ nhặt* này để sau này đọc lại còn nhớ cảm giác của hôm nay.",
            "Cô giáo cũ gọi điện hỏi thăm, giọng cô vẫn ấm như ngày xưa.",
            "Dù mệt, mình vẫn thấy hôm nay là một ngày đáng nhớ.",
            "Trên bàn là tách trà gừng còn nóng, hơi nước bay lên mờ cả kính cửa sổ.",
            "Mình hứa sẽ kiên nhẫn hơn với chính mình, nhất là những lúc mọi việc không như ý.",
            "Tiếng cười của lũ trẻ ngoài sân làm cả buổi chiều bỗng trở nên rộn ràng.",
            "Có một khoảnh khắc mình chợt nhớ đến ba, và thấy mắt cay cay.",
            "Kế hoạch ban đầu bị thay đổi hoàn toàn, nhưng kết quả lại tốt hơn mong đợi.",
            "Mình thấy **biết ơn** vì có những người luôn ở bên cạnh những lúc khó khăn.",
            "Ánh đèn vàng trong quán làm gương mặt ai cũng trở nên dịu dàng hơn.",
            "Mình chưa hiểu hết cảm xúc của mình lúc này, nhưng viết ra cũng đã nhẹ lòng.",
            "Đồng hồ điểm khuya mà mình vẫn không muốn đi ngủ, vì sợ ngày mai đến quá nhanh.",
            "Món cà phê muối lần đầu thử hóa ra lại hợp khẩu vị đến lạ.",
        )

    val closers =
        listOf(
            "Mong ngày mai cũng nhẹ nhàng như hôm nay.",
            "Viết đến đây thì trời đã tối hẳn, đành dừng bút vậy.",
            "Ghi lại để nhớ rằng mình đã từng có một ngày như thế này.",
            "Cảm ơn bản thân vì đã không bỏ cuộc.",
            "Ngày mai sẽ cố gắng dậy sớm hơn một chút.",
            "Chúc mình ngủ ngon và mơ một giấc mơ đẹp.",
            "Hẹn gặp lại những điều tốt đẹp ở phía trước.",
            "Thế là đủ cho một ngày.",
            "Hy vọng sẽ giữ được cảm giác này lâu thật lâu.",
            "Mình sẽ quay lại đọc đoạn này vào một ngày buồn.",
        )

    val headings =
        listOf(
            "## Ghi chú nhanh",
            "## Điều đáng nhớ",
            "## Hôm nay",
            "## Việc cần làm",
            "## Suy ngẫm",
        )

    val lists =
        listOf(
            listOf("Đi chợ mua rau và cá", "Giặt đồ", "Gọi điện cho ngoại", "Dọn lại bàn làm việc"),
            listOf("Chạy bộ ba cây số", "Đọc hai mươi trang sách", "Viết nhật ký"),
            listOf("Bánh mì", "*Cà phê* sữa", "Trái cây", "Sữa chua"),
            listOf(
                "Cảm ơn mẹ vì bữa cơm",
                "Cảm ơn bạn vì cuộc gọi",
                "Cảm ơn bản thân vì đã cố gắng",
            ),
            listOf("Hoàn thành bản thiết kế", "Họp với nhóm lúc mười giờ", "Trả lời thư còn tồn"),
            listOf("Hà Nội mùa thu", "Một chuyến tàu đêm", "Tô phở nóng buổi sáng"),
        )

    val hashtags =
        listOf(
            "#bietOn",
            "#cafe",
            "#chuyendi",
            "#giadinh",
            "#hocbai",
            "#sachhay",
            "#anngon",
            "#chaybo",
            "#binhyen",
            "#congviec",
        )

    val emoji =
        listOf(
            "😊",
            "☕",
            "🌧️",
            "📚",
            "🍜",
            "🌅",
            "🎧",
            "🚲",
            "🌸",
            "😴",
            "💪",
            "🥰",
            "✨",
            "🍃",
            "🎉",
        )

    /** Places with a pin on the map. */
    val mappedPlaces =
        listOf(
            EntryLocation(
                name = "Hồ Hoàn Kiếm",
                address = "Hàng Trống, Hoàn Kiếm, Hà Nội",
                coordinates = GeoPoint(21.0285, 105.8542),
            ),
            EntryLocation(
                name = "Nhà thờ Đức Bà",
                address = "1 Công xã Paris, Bến Nghé, Quận 1, Thành phố Hồ Chí Minh",
                coordinates = GeoPoint(10.7798, 106.6990),
            ),
            EntryLocation(
                name = "Cầu Rồng",
                address = "Nguyễn Văn Linh, Hải Châu, Đà Nẵng",
                coordinates = GeoPoint(16.0612, 108.2278),
            ),
            EntryLocation(
                name = "Hồ Xuân Hương",
                address = "Phường 1, Đà Lạt, Lâm Đồng",
                coordinates = GeoPoint(11.9437, 108.4417),
            ),
            EntryLocation(
                name = "Phố cổ Hội An",
                address = "Hội An, Quảng Nam",
                coordinates = GeoPoint(15.8801, 108.3380),
            ),
            EntryLocation(
                name = "Bãi Sao",
                address = "An Thới, Phú Quốc, Kiên Giang",
                coordinates = GeoPoint(10.0521, 104.0346),
            ),
            EntryLocation(
                name = "Công viên Tao Đàn",
                address = "Trương Định, Quận 1, Thành phố Hồ Chí Minh",
                coordinates = GeoPoint(10.7743, 106.6928),
            ),
            EntryLocation(
                name = "Cà phê Cộng",
                address = "Nguyễn Huệ, Huế",
                coordinates = GeoPoint(16.4637, 107.5909),
            ),
            EntryLocation(
                name = "Thư viện Quốc gia",
                address = "31 Tràng Thi, Hoàn Kiếm, Hà Nội",
                coordinates = GeoPoint(21.0252, 105.8450),
            ),
            EntryLocation(
                name = "Bãi biển Mỹ Khê",
                address = "Võ Nguyên Giáp, Sơn Trà, Đà Nẵng",
                coordinates = GeoPoint(16.0544, 108.2480),
            ),
        )

    /** Names typed by hand, without an address or coordinates. */
    val freeTypedPlaces =
        listOf(
            "Nhà ngoại",
            "Quán cà phê quen",
            "Ban công nhà mình",
            "Công viên đầu ngõ",
            "Văn phòng",
            "Nhà sách cũ",
            "Chợ sáng",
        )

    val peopleLabels = listOf("Mẹ", "Bạn thân", "Đồng nghiệp")

    val tripLabels = listOf("Đà Lạt", "Hội An", "Phú Quốc")
}
