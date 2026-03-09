package com.vestshop.Service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vestshop.Config.GeminiProperties;
import com.vestshop.Service.GeminiService;
import com.vestshop.dto.AI.OpenAiExtractResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiServiceImpl implements GeminiService {

    private static final BigDecimal ONE_MILLION = new BigDecimal("1000000");
    private static final BigDecimal TWO_MILLION = new BigDecimal("2000000");

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
            generationConfig.put("temperature", 0.2);
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

            String rawBody = executeGeminiWithRetry(url, entity);

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
            normalizeResponse(parsed);

            log.info("Gemini parsed normalized = {}", objectMapper.writeValueAsString(parsed));
            return parsed;

        } catch (RestClientResponseException e) {
            log.error(
                    "Gemini HTTP fail, using fallback. status={}, body={}, userMessage={}, previousContext={}",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString(),
                    userMessage,
                    previousContext,
                    e
            );
            return buildSoftFallback(userMessage, previousContext);
        } catch (ResourceAccessException e) {
            log.error(
                    "Gemini network fail, using fallback. userMessage={}, previousContext={}",
                    userMessage,
                    previousContext,
                    e
            );
            return buildSoftFallback(userMessage, previousContext);
        } catch (Exception e) {
            log.error(
                    "Gemini parse/internal fail, using fallback. userMessage={}, previousContext={}",
                    userMessage,
                    previousContext,
                    e
            );
            return buildSoftFallback(userMessage, previousContext);
        }
    }

    private String executeGeminiWithRetry(String url, HttpEntity<Map<String, Object>> entity) {
        try {
            return executeGeminiOnce(url, entity);
        } catch (RestClientResponseException e) {
            log.error(
                    "Gemini HTTP error lần 1. status={}, body={}",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

            if (e.getRawStatusCode() == 429) {
                sleepQuietly(1200);

                try {
                    return executeGeminiOnce(url, entity);
                } catch (RestClientResponseException retryEx) {
                    log.error(
                            "Gemini HTTP error lần 2. status={}, body={}",
                            retryEx.getRawStatusCode(),
                            retryEx.getResponseBodyAsString(),
                            retryEx
                    );
                    throw retryEx;
                }
            }

            throw e;
        }
    }

    private String executeGeminiOnce(String url, HttpEntity<Map<String, Object>> entity) {
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
        return response.getBody();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

Lưu ý:
- Tin nhắn mới nhất có thể chỉ là điều kiện bổ sung cho nhu cầu trước đó.
- Nếu tin nhắn mới không nhắc lại loại sản phẩm hoặc khoảng giá, hãy giữ lại thông tin hợp lý từ ngữ cảnh trước đó.
- Ví dụ:
  trước đó: "tôi muốn vest hơn 1 triệu"
  mới nhất: "màu đen size 48"
  thì vẫn hiểu:
  - intent = product_search
  - loaiSanPham = Vest
  - mauSac = đen
  - kichCo = 48
  - priceMin = 1000000
  - priceMax = null
""".formatted(previousContext);
        }

        return """
Bạn là trợ lý hiểu nhu cầu mua vest cho VestShop.

Mục tiêu:
- Chỉ trích xuất ý định và bộ lọc tìm sản phẩm thành JSON.
- Shop chỉ bán vest, nên không suy ra áo sơ mi, quần âu hay sản phẩm khác.
- Không được bịa tồn kho, giá cụ thể, mã sản phẩm, ưu đãi hoặc xác nhận có hàng.

%s

Hãy trả về JSON thuần, không markdown, không giải thích ngoài JSON, với đúng cấu trúc:

{
  "intent": "...",
  "loaiSanPham": "...",
  "mauSac": "...",
  "kichCo": "...",
  "priceMin": ...,
  "priceMax": ...,
  "fit": "...",
  "chatLieu": "..."
}

Quy tắc:
- Chỉ trả JSON hợp lệ.
- intent chỉ nhận một trong 3 giá trị:
  - "greeting"
  - "handoff"
  - "product_search"
- Nếu người dùng chỉ chào hỏi như "chào", "hello", "shop ơi" => intent = "greeting"
- Nếu người dùng muốn gặp người thật như "gặp nhân viên", "gặp CSKH" => intent = "handoff"
- Còn lại nếu đang hỏi mua hoặc lọc sản phẩm => intent = "product_search"
- Field nào không rõ thì để null.
- loaiSanPham nếu có thì chỉ trả "Vest"
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
- Không sinh reply.
- Không sinh fallbackSuggestions.

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

    private void normalizeResponse(OpenAiExtractResponse response) {
        if (response == null) return;

        if (!isBlank(response.getIntent())) {
            response.setIntent(response.getIntent().trim().toLowerCase());
        } else {
            response.setIntent("product_search");
        }

        if (!isBlank(response.getLoaiSanPham())) {
            response.setLoaiSanPham("Vest");
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

        if (!"greeting".equals(response.getIntent())
                && !"handoff".equals(response.getIntent())
                && !"product_search".equals(response.getIntent())) {
            response.setIntent("product_search");
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
    }

    private OpenAiExtractResponse extractContextByRules(String textRaw) {
        String text = normalize(textRaw);
        OpenAiExtractResponse res = new OpenAiExtractResponse();

        if (isGreeting(text)) {
            res.setIntent("greeting");
            return res;
        }

        if (isHandoff(text)) {
            res.setIntent("handoff");
            return res;
        }

        res.setIntent("product_search");

        if (text.contains("vest")) {
            res.setLoaiSanPham("Vest");
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
            res.setPriceMin(ONE_MILLION);
            res.setPriceMax(null);
        } else if (containsUnderOneMillion(text)) {
            res.setPriceMin(null);
            res.setPriceMax(ONE_MILLION);
        } else if (containsUnderTwoMillion(text)) {
            res.setPriceMin(null);
            res.setPriceMax(TWO_MILLION);
        }

        return res;
    }

    private void enrichByRules(OpenAiExtractResponse res, String userMessage) {
        if (res == null) return;

        String text = normalize(userMessage);

        if (isBlank(res.getIntent())) {
            if (isGreeting(text)) {
                res.setIntent("greeting");
            } else if (isHandoff(text)) {
                res.setIntent("handoff");
            } else {
                res.setIntent("product_search");
            }
        }

        if (isBlank(res.getLoaiSanPham()) && text.contains("vest")) {
            res.setLoaiSanPham("Vest");
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
            res.setPriceMin(ONE_MILLION);
            res.setPriceMax(null);
        } else if (containsUnderOneMillion(text)) {
            res.setPriceMin(null);
            res.setPriceMax(ONE_MILLION);
        } else if (containsUnderTwoMillion(text)) {
            res.setPriceMin(null);
            res.setPriceMax(TWO_MILLION);
        }
    }

    private OpenAiExtractResponse buildSoftFallback(String userMessage, String previousContext) {
        OpenAiExtractResponse current = extractContextByRules(userMessage);
        OpenAiExtractResponse previous = isBlank(previousContext) ? null : extractContextByRules(previousContext);

        mergeWithPreviousContext(current, previous);

        if (current == null) {
            current = new OpenAiExtractResponse();
            current.setIntent("product_search");
        }

        return current;
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

    private boolean isGreeting(String text) {
        return "chao".equals(text)
                || "chao shop".equals(text)
                || "hello".equals(text)
                || "hi".equals(text)
                || "shop oi".equals(text)
                || "alo".equals(text)
                || "xin chao".equals(text);
    }

    private boolean isHandoff(String text) {
        return text.contains("gap nhan vien")
                || text.contains("gap cskh")
                || text.contains("nhan vien tu van");
    }

    private boolean containsNavy(String text) {
        return text.contains("xanh navi")
                || text.contains("xanh navy")
                || text.contains("navy")
                || text.contains("navi");
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