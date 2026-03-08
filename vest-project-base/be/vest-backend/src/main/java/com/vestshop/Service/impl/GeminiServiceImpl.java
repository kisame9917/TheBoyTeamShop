package com.vestshop.Service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vestshop.Config.GeminiProperties;
import com.vestshop.Service.GeminiService;
import com.vestshop.dto.AI.OpenAiExtractResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiServiceImpl implements GeminiService {

    private final GeminiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiServiceImpl(GeminiProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public OpenAiExtractResponse extractFilters(String userMessage) {
        return extractFilters(userMessage, null);
    }

    @Override
    public OpenAiExtractResponse extractFilters(String userMessage, String previousContext) {
        try {
            log.info("Gemini key present = {}", properties.getApiKey() != null && !properties.getApiKey().isBlank());
            log.info("Gemini model = {}", properties.getModel());
            log.info("Gemini userMessage = {}", userMessage);
            log.info("Gemini previousContext = {}", previousContext);

            validateConfig();

            String prompt = buildPrompt(userMessage, previousContext);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(textPart));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.35);
            generationConfig.put("responseMimeType", "application/json");

            Map<String, Object> body = new HashMap<>();
            body.put("contents", List.of(content));
            body.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + properties.getModel()
                    + ":generateContent?key="
                    + properties.getApiKey();

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String rawBody = response.getBody();
            log.info("Gemini raw response = {}", rawBody);

            if (rawBody == null || rawBody.isBlank()) {
                throw new RuntimeException("Gemini response body is empty");
            }

            JsonNode root = objectMapper.readTree(rawBody);

            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            String outputText = textNode.asText(null);

            if (outputText == null || outputText.isBlank()) {
                throw new RuntimeException("Gemini did not return text");
            }

            log.info("Gemini output text before clean = {}", outputText);

            String cleanedJson = cleanJson(outputText);
            log.info("Gemini cleaned json = {}", cleanedJson);

            OpenAiExtractResponse parsed = objectMapper.readValue(cleanedJson, OpenAiExtractResponse.class);

            OpenAiExtractResponse previousExtracted = null;
            if (!isBlank(previousContext)) {
                previousExtracted = extractContextByRules(previousContext);
            }

            enrichByRules(parsed, userMessage);
            mergeWithPreviousContext(parsed, previousExtracted);
            normalizeResponse(parsed, userMessage, previousContext);

            return parsed;

        } catch (Exception e) {
            log.error("Gemini extractFilters failed", e);
            return buildSmartFallback(userMessage, previousContext);
        }
    }

    private void validateConfig() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new RuntimeException("Gemini apiKey is missing");
        }

        if (properties.getModel() == null || properties.getModel().isBlank()) {
            throw new RuntimeException("Gemini model is missing");
        }
    }

    private String buildPrompt(String userMessage, String previousContext) {
        String contextBlock = "";
        if (!isBlank(previousContext)) {
            contextBlock = """
Ngữ cảnh hội thoại trước đó:
"%s"

Lưu ý rất quan trọng:
- Tin nhắn mới nhất có thể chỉ là điều kiện bổ sung cho nhu cầu trước đó.
- Nếu tin nhắn mới không nhắc lại loại sản phẩm hoặc khoảng giá, hãy giữ lại thông tin hợp lý từ ngữ cảnh trước đó.
- Nếu trước đó khách nói "tôi muốn vest hơn 1 triệu", sau đó nói "màu đen và size 48", thì vẫn hiểu là:
  - loaiSanPham = Vest
  - mauSac = đen
  - kichCo = 48
  - priceMin = 1000000
  - priceMax = null
""".formatted(previousContext);
        }

        return """
Bạn là trợ lý tư vấn bán hàng cho shop VestShop, chuyên về vest nam, áo sơ mi, quần âu và phụ kiện nam.

Nhiệm vụ:
1. Đọc tin nhắn người dùng.
2. Kết hợp với ngữ cảnh hội thoại trước đó nếu có.
3. Trích xuất nhu cầu mua hàng thành dữ liệu JSON.
4. Với câu bổ sung như "màu đen", "size 48", "xanh navi", "dưới 1 triệu", hãy hiểu đây có thể là phần bổ sung cho nhu cầu trước.
5. Không làm mất các điều kiện cũ nếu tin nhắn mới không phủ định chúng.
6. Tạo câu trả lời tiếng Việt tự nhiên, thân thiện.
7. Không bịa mã sản phẩm, tồn kho hay giá cụ thể nếu không có dữ liệu xác nhận.
8. Nếu giá là 999.999 đồng thì vẫn là dưới 1 triệu đồng và nếu khach hàng hỏi hơn 1 triệu thì không đề xuất
9. Nếu không co size nào trong db thi gợi ý cho khách hàng cac sản phẩm khác
%s

Hãy trả về JSON thuần, không markdown, không giải thích ngoài JSON, với đúng cấu trúc:

{
  "loaiSanPham": "...",
  "mauSac": "...",
  "kichCo": "...",
  "priceMin": ...,
  "priceMax": ...,
  "fit": "...",
  "chatLieu": "...",
  "occasion": "...",
  "reply": "...",
  "fallbackSuggestions": ["...", "...", "..."]
}

Quy tắc:
- Chỉ trả JSON hợp lệ.
- Nếu không rõ field nào thì để null.
- "priceMin" là giá tối thiểu, "priceMax" là giá tối đa.
- "xanh navi", "xanh navy", "navy" => "xanh navy"
- "đen", "den" => "đen"
- Nếu người dùng nói "trên 1 triệu", "hơn 1 triệu", "từ 1 triệu trở lên":
  - priceMin = 1000000
  - priceMax = null
- Nếu người dùng nói "dưới 1 triệu", "không quá 1 triệu", "tối đa 1 triệu":
  - priceMin = null
  - priceMax = 1000000
- Nếu người dùng nói "dưới 2 triệu", "không quá 2 triệu", "tối đa 2 triệu":
  - priceMin = null
  - priceMax = 2000000
- Nếu tin nhắn mới chỉ bổ sung màu, size, fit hoặc giá, hãy giữ lại loại sản phẩm và các điều kiện trước đó từ context.
- Nếu người dùng hỏi size 49, có thể gợi ý thêm 48 hoặc 50 trong reply hoặc fallbackSuggestions.
- reply phải ngắn gọn, tự nhiên, lịch sự, 2 đến 4 câu.
- fallbackSuggestions là 2 đến 4 gợi ý ngắn gọn, dễ bấm tiếp.
- Không bịa thông tin xác nhận tồn kho, số lượng, giá cụ thể, ưu đãi hoặc mã sản phẩm.

Ví dụ:
Ngữ cảnh trước đó: "tôi muốn vest hơn 1 triệu"
Người dùng mới nhất: "màu đen và size 48"
Kết quả:
{
  "loaiSanPham": "Vest",
  "mauSac": "đen",
  "kichCo": "48",
  "priceMin": 1000000,
  "priceMax": null,
  "fit": null,
  "chatLieu": null,
  "occasion": null,
  "reply": "Dạ anh đang muốn tìm vest màu đen size 48 trong tầm giá từ 1 triệu trở lên đúng không ạ? Nếu anh muốn, em có thể gợi ý thêm các mẫu phù hợp theo form hoặc phong cách để mình dễ chọn hơn.",
  "fallbackSuggestions": ["Vest đen size 48", "Vest đen", "Vest trên 1 triệu", "Gặp CSKH"]
}

Tin nhắn người dùng mới nhất: "%s"
""".formatted(contextBlock, userMessage);
    }

    private String cleanJson(String raw) {
        if (raw == null) return null;

        String cleaned = raw.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }

        return cleaned;
    }

    private void normalizeResponse(OpenAiExtractResponse response, String userMessage, String previousContext) {
        if (response == null) return;

        OpenAiExtractResponse smartFallback = buildSmartFallback(userMessage, previousContext);

        if (isBlank(response.getReply())) {
            response.setReply(smartFallback.getReply());
        }

        if (response.getFallbackSuggestions() == null || response.getFallbackSuggestions().isEmpty()) {
            response.setFallbackSuggestions(smartFallback.getFallbackSuggestions());
        }

        if (!isBlank(response.getLoaiSanPham())) {
            response.setLoaiSanPham(response.getLoaiSanPham().trim());
        }

        if (!isBlank(response.getMauSac())) {
            response.setMauSac(normalizeColorLabel(response.getMauSac().trim()));
        }

        if (!isBlank(response.getKichCo())) {
            response.setKichCo(response.getKichCo().trim());
        }

        if (!isBlank(response.getFit())) {
            response.setFit(response.getFit().trim());
        }

        if (!isBlank(response.getChatLieu())) {
            response.setChatLieu(response.getChatLieu().trim());
        }

        if (!isBlank(response.getOccasion())) {
            response.setOccasion(response.getOccasion().trim());
        }
    }

    private void mergeWithPreviousContext(OpenAiExtractResponse current, OpenAiExtractResponse previous) {
        if (current == null || previous == null) return;

        if (isBlank(current.getLoaiSanPham())) {
            current.setLoaiSanPham(previous.getLoaiSanPham());
        }

        if (isBlank(current.getMauSac())) {
            current.setMauSac(previous.getMauSac());
        }

        if (isBlank(current.getKichCo())) {
            current.setKichCo(previous.getKichCo());
        }

        if (current.getPriceMin() == null) {
            current.setPriceMin(previous.getPriceMin());
        }

        if (current.getPriceMax() == null) {
            current.setPriceMax(previous.getPriceMax());
        }

        if (isBlank(current.getFit())) {
            current.setFit(previous.getFit());
        }

        if (isBlank(current.getChatLieu())) {
            current.setChatLieu(previous.getChatLieu());
        }

        if (isBlank(current.getOccasion())) {
            current.setOccasion(previous.getOccasion());
        }
    }

    private OpenAiExtractResponse extractContextByRules(String textRaw) {
        String text = normalize(textRaw);
        OpenAiExtractResponse res = new OpenAiExtractResponse();

        if (text.contains("vest")) {
            res.setLoaiSanPham("Vest");
        } else if (text.contains("ao so mi") || text.contains("so mi")) {
            res.setLoaiSanPham("Áo sơ mi");
        } else if (text.contains("quan au") || text.contains("quan tay")) {
            res.setLoaiSanPham("Quần âu");
        }

        if (containsNavy(text)) {
            res.setMauSac("xanh navy");
        } else if (containsBlack(text)) {
            res.setMauSac("đen");
        } else if (containsWhite(text)) {
            res.setMauSac("trắng");
        } else if (containsGray(text)) {
            res.setMauSac("xám");
        }

        if (text.contains("size 48") || text.contains("size48")) {
            res.setKichCo("48");
        } else if (text.contains("size 49") || text.contains("size49")) {
            res.setKichCo("49");
        } else if (text.contains("size 50") || text.contains("size50")) {
            res.setKichCo("50");
        }

        if (containsAboveOneMillion(text)) {
            res.setPriceMin(new BigDecimal("1000000"));
            res.setPriceMax(null);
        }

        if (containsUnderOneMillion(text)) {
            res.setPriceMin(null);
            res.setPriceMax(new BigDecimal("1000000"));
        }

        if (containsUnderTwoMillion(text)) {
            res.setPriceMin(null);
            res.setPriceMax(new BigDecimal("2000000"));
        }

        return res;
    }

    private void enrichByRules(OpenAiExtractResponse res, String userMessage) {
        if (res == null) return;

        String text = normalize(userMessage);

        if (isBlank(res.getLoaiSanPham())) {
            if (text.contains("vest")) {
                res.setLoaiSanPham("Vest");
            } else if (text.contains("ao so mi") || text.contains("so mi")) {
                res.setLoaiSanPham("Áo sơ mi");
            } else if (text.contains("quan au") || text.contains("quan tay")) {
                res.setLoaiSanPham("Quần âu");
            }
        }

        if (isBlank(res.getMauSac())) {
            if (containsNavy(text)) {
                res.setMauSac("xanh navy");
            } else if (containsBlack(text)) {
                res.setMauSac("đen");
            } else if (containsWhite(text)) {
                res.setMauSac("trắng");
            } else if (containsGray(text)) {
                res.setMauSac("xám");
            }
        }

        if (isBlank(res.getKichCo())) {
            if (text.contains("size 48") || text.contains("size48")) {
                res.setKichCo("48");
            } else if (text.contains("size 49") || text.contains("size49")) {
                res.setKichCo("49");
            } else if (text.contains("size 50") || text.contains("size50")) {
                res.setKichCo("50");
            }
        }

        if (containsAboveOneMillion(text)) {
            res.setPriceMin(new BigDecimal("1000000"));
            res.setPriceMax(null);
        } else if (containsUnderOneMillion(text)) {
            res.setPriceMin(null);
            res.setPriceMax(new BigDecimal("1000000"));
        } else if (containsUnderTwoMillion(text)) {
            res.setPriceMin(null);
            res.setPriceMax(new BigDecimal("2000000"));
        }
    }

    private OpenAiExtractResponse buildSmartFallback(String userMessage, String previousContext) {
        OpenAiExtractResponse current = extractContextByRules(userMessage);
        OpenAiExtractResponse previous = isBlank(previousContext) ? null : extractContextByRules(previousContext);

        mergeWithPreviousContext(current, previous);

        if (current == null) {
            current = new OpenAiExtractResponse();
        }

        if (isBlank(current.getLoaiSanPham())
                && isBlank(current.getMauSac())
                && isBlank(current.getKichCo())
                && current.getPriceMin() == null
                && current.getPriceMax() == null) {
            current.setReply("Dạ em đã nhận được nhu cầu của anh/chị. Anh/chị có thể nói rõ thêm loại sản phẩm, màu, size hoặc tầm giá để em hỗ trợ sát hơn nhé.");
            current.setFallbackSuggestions(List.of("Vest đen", "Áo sơ mi trắng", "Quần âu", "Gặp CSKH"));
            return current;
        }

        String loai = defaultValue(current.getLoaiSanPham(), "sản phẩm");
        String mau = current.getMauSac();
        String size = current.getKichCo();

        if ("49".equals(size)) {
            current.setReply("Dạ anh đang cần " + loai.toLowerCase() + " size 49 đúng không ạ? Size này hơi đặc thù nên nếu chưa có đúng mẫu mong muốn, anh có thể tham khảo thêm size lân cận như 48 hoặc 50 để dễ chọn form phù hợp hơn. Nếu anh muốn, em có thể gợi ý tiếp theo màu hoặc kiểu dáng anh đang tìm.");
            current.setFallbackSuggestions(List.of(loai + " size 48", loai + " size 50", loai + " đen", "Gặp CSKH"));
            return current;
        }

        if (current.getPriceMin() != null && current.getPriceMax() == null) {
            current.setReply("Dạ anh đang cần " + buildReadableNeed(loai, mau, size) + " trong tầm giá từ 1 triệu trở lên đúng không ạ? Nếu anh muốn, em có thể gợi ý thêm các mẫu phù hợp theo form hoặc phong cách để mình dễ chọn hơn.");
            current.setFallbackSuggestions(List.of(
                    buildQuickLabel(loai, mau, size),
                    loai + " trên 1 triệu",
                    loai + " công sở",
                    "Gặp CSKH"
            ));
            return current;
        }

        if (current.getPriceMin() == null
                && current.getPriceMax() != null
                && current.getPriceMax().compareTo(new BigDecimal("1000000")) == 0) {
            current.setReply("Dạ anh đang muốn tìm " + buildReadableNeed(loai, mau, size) + " trong tầm giá dưới 1 triệu đúng không ạ? Nếu anh muốn, em có thể gợi ý thêm theo size, form hoặc kiểu để lọc sát hơn.");
            current.setFallbackSuggestions(List.of(
                    buildQuickLabel(loai, mau, size),
                    loai + " dưới 1 triệu",
                    loai + " công sở",
                    "Gặp CSKH"
            ));
            return current;
        }

        if (current.getPriceMin() == null
                && current.getPriceMax() != null
                && current.getPriceMax().compareTo(new BigDecimal("2000000")) == 0) {
            current.setReply("Dạ anh đang muốn tìm " + buildReadableNeed(loai, mau, size) + " trong tầm giá dưới 2 triệu đúng không ạ? Với mức này mình vẫn có thể ưu tiên các mẫu cơ bản, dễ mặc và phù hợp nhiều dịp. Nếu anh muốn, em gợi ý thêm theo màu hoặc size để sát hơn nhé.");
            current.setFallbackSuggestions(List.of(
                    buildQuickLabel(loai, mau, size),
                    loai + " dưới 2 triệu",
                    loai + " công sở",
                    "Gặp CSKH"
            ));
            return current;
        }

        current.setReply("Dạ em đã ghi nhận nhu cầu của anh/chị về " + buildReadableNeed(loai, mau, size) + ". Anh/chị có thể nói thêm về tầm giá hoặc kiểu dáng mong muốn để em hỗ trợ sát hơn nhé.");
        current.setFallbackSuggestions(List.of(
                buildQuickLabel(loai, mau, size),
                loai + " đen",
                loai + " công sở",
                "Gặp CSKH"
        ));
        return current;
    }

    private String buildReadableNeed(String loai, String mau, String size) {
        StringBuilder sb = new StringBuilder(loai.toLowerCase());
        if (!isBlank(mau)) {
            sb.append(" màu ").append(mau);
        }
        if (!isBlank(size)) {
            sb.append(" size ").append(size);
        }
        return sb.toString();
    }

    private String buildQuickLabel(String loai, String mau, String size) {
        StringBuilder sb = new StringBuilder(loai);
        if (!isBlank(mau)) {
            sb.append(" ").append(mau);
        }
        if (!isBlank(size)) {
            sb.append(" size ").append(size);
        }
        return sb.toString().trim();
    }

    private String defaultValue(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String normalizeColorLabel(String color) {
        String text = normalize(color);
        if (containsNavy(text)) return "xanh navy";
        if (containsBlack(text)) return "đen";
        if (containsWhite(text)) return "trắng";
        if (containsGray(text)) return "xám";
        return color;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isBlank();
    }

    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replace("đ", "d")
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsNavy(String text) {
        return text.contains("xanh navi") || text.contains("xanh navy") || text.contains("navy") || text.contains("navi");
    }

    private boolean containsBlack(String text) {
        return text.contains(" mau den")
                || text.startsWith("den")
                || text.contains(" den ")
                || text.endsWith(" den")
                || text.contains("mau den");
    }

    private boolean containsWhite(String text) {
        return text.contains(" mau trang")
                || text.startsWith("trang")
                || text.contains(" trang ")
                || text.endsWith(" trang")
                || text.contains("mau trang");
    }

    private boolean containsGray(String text) {
        return text.contains(" mau xam")
                || text.startsWith("xam")
                || text.contains(" xam ")
                || text.endsWith(" xam")
                || text.contains("mau xam");
    }

    private boolean containsAboveOneMillion(String text) {
        return text.contains("tren 1 trieu")
                || text.contains("tren 1tr")
                || text.contains("tren 1000000")
                || text.contains("tu 1 trieu tro len")
                || text.contains("hon 1 trieu")
                || text.contains("hon 1tr");
    }

    private boolean containsUnderOneMillion(String text) {
        return text.contains("duoi 1 trieu")
                || text.contains("duoi gia 1 trieu")
                || text.contains("khong qua 1 trieu")
                || text.contains("toi da 1 trieu")
                || text.contains("duoi 1tr")
                || text.contains("duoi 1000000");
    }

    private boolean containsUnderTwoMillion(String text) {
        return text.contains("duoi 2 trieu")
                || text.contains("toi da 2 trieu")
                || text.contains("khong qua 2 trieu")
                || text.contains("duoi 2tr")
                || text.contains("duoi 2000000");
    }
}