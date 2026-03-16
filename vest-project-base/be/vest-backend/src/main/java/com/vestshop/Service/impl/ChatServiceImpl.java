package com.vestshop.Service.impl;

import com.vestshop.Entity.Conversation;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.Message;
import com.vestshop.Entity.MessageProductSuggestion;
import com.vestshop.Entity.SanPhamChiTiet;
import com.vestshop.Repository.ConversationRepo;
import com.vestshop.Repository.MessageProductSuggestionRepo;
import com.vestshop.Repository.MessageRepo;
import com.vestshop.Repository.SanPhamChiTietRepository;
import com.vestshop.Service.AISuggestService;
import com.vestshop.Service.ChatService;
import com.vestshop.dto.AI.AISuggestResponse;
import com.vestshop.dto.AI.ProductSuggestionDto;
import com.vestshop.dto.response.ChatMessageResponse;
import com.vestshop.dto.response.ChatSaveResult;
import com.vestshop.dto.response.ConversationSummaryResponse;
import org.springframework.stereotype.Service;
import com.vestshop.Service.NotificationRealtimeService;
import com.vestshop.dto.response.NotificationEventResponse;

import java.time.OffsetDateTime;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ConversationRepo conversationRepo;
    private final MessageRepo messageRepo;
    private final AISuggestService aiSuggestService;
    private final MessageProductSuggestionRepo messageProductSuggestionRepo;
    private final SanPhamChiTietRepository sanPhamChiTietRepo;
    private final NotificationRealtimeService notificationRealtimeService;
    public ChatServiceImpl(
            ConversationRepo conversationRepo,
            MessageRepo messageRepo,
            AISuggestService aiSuggestService,
            MessageProductSuggestionRepo messageProductSuggestionRepo,
            SanPhamChiTietRepository sanPhamChiTietRepo, NotificationRealtimeService notificationRealtimeService
    ) {
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.aiSuggestService = aiSuggestService;
        this.messageProductSuggestionRepo = messageProductSuggestionRepo;
        this.sanPhamChiTietRepo = sanPhamChiTietRepo;
        this.notificationRealtimeService = notificationRealtimeService;
    }

    @Override
    public Conversation getOrCreateOpenConversation(KhachHang customer) {
        return conversationRepo
                .findFirstByCustomerAndStatusOrderByCreatedAtDesc(customer, "OPEN")
                .orElseGet(() -> {
                    Conversation cv = new Conversation();
                    cv.setCustomer(customer);
                    cv.setGuestName(null);
                    cv.setStatus("OPEN");
                    return conversationRepo.save(cv);
                });
    }

    @Override
    public Conversation getOrCreateOpenGuestConversation(String guestName) {
        Conversation cv = new Conversation();
        cv.setCustomer(null);
        cv.setGuestName(
                guestName != null && !guestName.isBlank()
                        ? guestName.trim()
                        : "Khách vãng lai"
        );
        cv.setStatus("OPEN");
        return conversationRepo.save(cv);
    }

    @Override
    public List<ChatSaveResult> saveMessage(Long conversationId, String senderType, String senderId, String content) {
        List<ChatSaveResult> result = new ArrayList<>();

        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderType(senderType);
        msg.setSenderId(senderId);
        msg.setContent(content);

        Message saved = messageRepo.save(msg);
        result.add(new ChatSaveResult(saved, null));

        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow();
        conversation.setUpdatedAt(saved.getCreatedAt());
        conversationRepo.save(conversation);

        if ("CLIENT".equalsIgnoreCase(senderType)) {
            try {
                if (isHumanHandoffRequest(content)) {
                    pushHumanHandoffNotification(conversation, saved);

                    Message botMsg = new Message();
                    botMsg.setConversationId(conversationId);
                    botMsg.setSenderType("BOT");
                    botMsg.setSenderId("AI_SUGGEST_BOT");
                    botMsg.setContent(
                            "Dạ em đã nhận được yêu cầu của anh/chị. "
                                    + "Em sẽ kết nối anh/chị với nhân viên tư vấn của VestShop ngay ạ. "
                                    + "Anh/chị vui lòng chờ trong giây lát nhé."
                    );

                    Message botSaved = messageRepo.save(botMsg);

                    result.add(new ChatSaveResult(botSaved, List.of()));

                    conversation.setUpdatedAt(botSaved.getCreatedAt());
                    conversationRepo.save(conversation);

                    return result;
                }
                AISuggestResponse aiResponse = aiSuggestService.suggestProducts(content);

                String reply = (aiResponse != null && aiResponse.getReply() != null && !aiResponse.getReply().isBlank())
                        ? aiResponse.getReply()
                        : "Em đã nhận được yêu cầu của anh/chị. Anh/chị mô tả rõ hơn giúp em để em gợi ý sản phẩm phù hợp nhé.";

                Message botMsg = new Message();
                botMsg.setConversationId(conversationId);
                botMsg.setSenderType("BOT");
                botMsg.setSenderId("AI_SUGGEST_BOT");
                botMsg.setContent(reply);

                Message botSaved = messageRepo.save(botMsg);

                List<ProductSuggestionDto> safeProducts = shouldAttachProducts(content, reply, aiResponse)
                        ? aiResponse.getProducts()
                        : List.of();

                if (safeProducts != null && !safeProducts.isEmpty()) {
                    safeProducts.forEach(p -> {
                        if (p.getSanPhamChiTietId() == null) return;

                        MessageProductSuggestion x = new MessageProductSuggestion();
                        x.setMessageId(botSaved.getId());
                        x.setSanPhamChiTietId(p.getSanPhamChiTietId());
                        messageProductSuggestionRepo.save(x);
                    });
                }

                result.add(new ChatSaveResult(botSaved, safeProducts != null ? safeProducts : List.of()));

                conversation.setUpdatedAt(botSaved.getCreatedAt());
                conversationRepo.save(conversation);

            } catch (Exception e) {
                e.printStackTrace();

                Message fallbackMsg = new Message();
                fallbackMsg.setConversationId(conversationId);
                fallbackMsg.setSenderType("BOT");
                fallbackMsg.setSenderId("AI_SUGGEST_BOT");
                fallbackMsg.setContent("Xin lỗi, hiện tại em chưa gợi ý được sản phẩm phù hợp. Anh/chị thử mô tả rõ hơn hoặc nhắn lại sau giúp em nhé.");

                Message fallbackSaved = messageRepo.save(fallbackMsg);

                result.add(new ChatSaveResult(
                        fallbackSaved,
                        List.of()
                ));

                conversation.setUpdatedAt(fallbackSaved.getCreatedAt());
                conversationRepo.save(conversation);
            }
        }
        return result;
    }

    @Override
    public List<Message> getRecentMessages(Long conversationId) {
        return messageRepo.findTop50ByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Override
    public List<ChatMessageResponse> getRecentMessageResponses(Long conversationId) {
        List<Message> messages = messageRepo.findTop50ByConversationIdOrderByCreatedAtAsc(conversationId);

        if (messages.isEmpty()) {
            return List.of();
        }

        List<Long> messageIds = messages.stream()
                .map(Message::getId)
                .filter(Objects::nonNull)
                .toList();

        List<MessageProductSuggestion> mappings = messageProductSuggestionRepo.findByMessageIdIn(messageIds);

        Map<Long, List<Long>> productIdsByMessageId = mappings.stream()
                .collect(Collectors.groupingBy(
                        MessageProductSuggestion::getMessageId,
                        Collectors.mapping(MessageProductSuggestion::getSanPhamChiTietId, Collectors.toList())
                ));

        List<Long> allProductIds = mappings.stream()
                .map(MessageProductSuggestion::getSanPhamChiTietId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, SanPhamChiTiet> productMap = sanPhamChiTietRepo.findByIdIn(allProductIds).stream()
                .collect(Collectors.toMap(SanPhamChiTiet::getId, p -> p));

        List<ChatMessageResponse> result = new ArrayList<>();

        for (Message message : messages) {
            ChatMessageResponse res = new ChatMessageResponse();
            res.setId(message.getId());
            res.setConversationId(message.getConversationId());
            res.setSenderType(message.getSenderType());
            res.setSenderId(message.getSenderId());
            res.setContent(message.getContent());
            res.setCreatedAt(message.getCreatedAt());

            if (isHumanHandoffReply(message.getContent())) {
                res.setProducts(List.of());
                result.add(res);
                continue;
            }

            List<Long> productIds = productIdsByMessageId.getOrDefault(message.getId(), List.of());

            List<ProductSuggestionDto> products = productIds.stream()
                    .map(productMap::get)
                    .filter(Objects::nonNull)
                    .map(this::toProductSuggestionDto)
                    .toList();

            res.setProducts(products);

            result.add(res);
        }

        return result;
    }

    private ProductSuggestionDto toProductSuggestionDto(SanPhamChiTiet p) {
        ProductSuggestionDto dto = new ProductSuggestionDto();
        dto.setSanPhamChiTietId(p.getId());

        if (p.getSanPham() != null) {
            dto.setTenSanPham(p.getSanPham().getTenSanPham());

            if (p.getSanPham().getLoaiSanPham() != null) {
                dto.setLoaiSanPham(p.getSanPham().getLoaiSanPham().getTen());
            }
        }

        if (p.getMauSac() != null) {
            dto.setMauSac(p.getMauSac().getTen());
        }

        if (p.getKichCo() != null) {
            dto.setKichCo(p.getKichCo().getSoSize());
        }

        dto.setDonGia(p.getDonGia());

        if (p.getAnh() != null && !p.getAnh().isBlank()) {
            dto.setAnh(p.getAnh());
        }

        return dto;
    }

    @Override
    public List<ConversationSummaryResponse> getOpenConversationSummaries() {
        List<Conversation> cvs = conversationRepo.findByStatusOrderByUpdatedAtDesc("OPEN");

        return cvs.stream().map(cv -> {
            Message last = messageRepo.findTop1ByConversationIdOrderByCreatedAtDesc(cv.getId()).orElse(null);

            return ConversationSummaryResponse.builder()
                    .conversationId(cv.getId())
                    .customerName(resolveCustomerName(cv))
                    .lastMessage(last != null ? last.getContent() : "")
                    .lastAt(last != null ? last.getCreatedAt() : null)
                    .updatedAt(cv.getUpdatedAt())
                    .build();
        }).toList();
    }

    private String resolveCustomerName(Conversation cv) {
        if (cv.getCustomer() != null
                && cv.getCustomer().getTenKhachHang() != null
                && !cv.getCustomer().getTenKhachHang().isBlank()) {
            return cv.getCustomer().getTenKhachHang();
        }

        if (cv.getGuestName() != null && !cv.getGuestName().isBlank()) {
            return cv.getGuestName();
        }

        return "Khách vãng lai";
    }

    private boolean shouldAttachProducts(String userContent, String reply, AISuggestResponse aiResponse) {
        if (aiResponse == null || aiResponse.getProducts() == null || aiResponse.getProducts().isEmpty()) {
            return false;
        }

        if (isHumanHandoffRequest(userContent)) {
            return false;
        }

        if (isHumanHandoffReply(reply)) {
            return false;
        }

        return true;
    }

    private boolean isHumanHandoffRequest(String text) {
        String normalized = normalizeText(text);
        return normalized.equals("gap nhan vien")
                || normalized.equals("gap cskh")
                || normalized.equals("gap nhan vien ngay")
                || normalized.equals("gap nhan vien tu van")
                || normalized.contains("gap nhan vien")
                || normalized.contains("gap cskh")
                || normalized.contains("nhan vien tu van");
    }

    private boolean isHumanHandoffReply(String text) {
        String normalized = normalizeText(text);
        return normalized.contains("ket noi")
                || normalized.contains("nhan vien tu van")
                || normalized.contains("gap nhan vien")
                || normalized.contains("gap cskh")
                || normalized.contains("vui long cho trong giay lat")
                || normalized.contains("bo phan cskh")
                || normalized.contains("tu van cua vestshop ngay");
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        String s = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d")
                .replace("Đ", "D")
                .toLowerCase(Locale.ROOT)
                .trim();
        return s.replaceAll("\\s+", " ");
    }
    private void pushHumanHandoffNotification(Conversation conversation, Message clientMessage) {
        String customerName = resolveCustomerName(conversation);

        notificationRealtimeService.pushToRole(
                "ADMIN",
                NotificationEventResponse.builder()
                        .id("CHAT-HUMAN-" + conversation.getId() + "-" + System.currentTimeMillis())
                        .title("Khách hàng " + customerName + " đang yêu cầu tiếp nhận")
                        .time("Vừa xong")
                        .link("/chat-support")
                        .type("CHAT_NEEDS_HUMAN")
                        .createdAt(OffsetDateTime.now().toString())
                        .build()
        );
    }
}