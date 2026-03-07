package com.vestshop.Chatbot;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class RuleBasedChatbotService {

    private final List<ChatRule> rules = new ArrayList<>();

    public RuleBasedChatbotService() {
        rules.add(new ChatRule(
                "greeting",
                Arrays.asList("xin chao", "hello", "hi", "chao shop", "shop oi", "alo", "chào"),
                "Chào bạn, shop có thể hỗ trợ gì cho bạn?",
                10
        ));

        rules.add(new ChatRule(
                "shipping",
                Arrays.asList("ship", "giao hang", "phi ship", "van chuyen"),
                "Shop hỗ trợ giao hàng toàn quốc. Bạn muốn mình kiểm tra phí ship khu vực nào?",
                8
        ));

        rules.add(new ChatRule(
                "order_status",
                Arrays.asList("don hang", "kiem tra don", "tinh trang don", "order"),
                "Bạn vui lòng gửi mã đơn hàng để shop kiểm tra tình trạng đơn nhé.",
                9
        ));

        rules.add(new ChatRule(
                "payment",
                Arrays.asList("thanh toan", "cod", "chuyen khoan"),
                "Shop hỗ trợ COD và chuyển khoản. Bạn muốn thanh toán theo cách nào?",
                7
        ));

        rules.add(new ChatRule(
                "size_guide",
                Arrays.asList("size", "bang size", "tu van size", "mac vua"),
                "Bạn cho mình chiều cao và cân nặng, shop sẽ tư vấn size phù hợp nhé.",
                8
        ));

        rules.add(new ChatRule(
                "contact_human",
                Arrays.asList("cskh", "gap nhan vien", "nhan vien", "nguoi that", "tu van truc tiep", "ho tro truc tiep"),
                "Dạ, bạn hãy đợi một chút để bộ phận CSKH sẽ hỗ trợ bạn tốt hơn nhé.",
                100
        ));
    }

    public String findBestReply(String message) {
        ChatRule bestRule = findBestRule(message);
        return bestRule != null ? bestRule.getResponse() : null;
    }

    public ChatRule findBestRule(String message) {
        String normalizedMessage = normalize(message);

        return rules.stream()
                .filter(rule -> matches(rule, normalizedMessage))
                .max(Comparator
                        .comparingInt((ChatRule rule) -> score(rule, normalizedMessage))
                        .thenComparingInt(ChatRule::getPriority))
                .orElse(null);
    }

    private boolean matches(ChatRule rule, String message) {
        return rule.getKeywords().stream().anyMatch(message::contains);
    }

    private int score(ChatRule rule, String message) {
        int count = 0;
        for (String keyword : rule.getKeywords()) {
            if (message.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    private String normalize(String input) {
        if (input == null) return "";

        String text = input.toLowerCase().trim();
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}", "");
        text = text.replace("đ", "d");
        text = text.replaceAll("[^a-z0-9\\s]", " ");
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }
}