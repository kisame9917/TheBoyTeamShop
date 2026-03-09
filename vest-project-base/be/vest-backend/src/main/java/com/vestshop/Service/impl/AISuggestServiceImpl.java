package com.vestshop.Service.impl;

import com.vestshop.Entity.SanPham;
import com.vestshop.Entity.SanPhamChiTiet;
import com.vestshop.Repository.SanPhamChiTietRepository;
import com.vestshop.Service.AISuggestService;
import com.vestshop.Service.GeminiService;
import com.vestshop.dto.AI.AISuggestResponse;
import com.vestshop.dto.AI.OpenAiExtractResponse;
import com.vestshop.dto.AI.ProductSuggestionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AISuggestServiceImpl implements AISuggestService {

    private static final int MAX_RESULTS = 4;

    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final GeminiService geminiService;

    public AISuggestServiceImpl(
            SanPhamChiTietRepository sanPhamChiTietRepository,
            GeminiService geminiService
    ) {
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
        this.geminiService = geminiService;
    }

    @Override
    public ProductSuggestionDto toDto(SanPhamChiTiet spct) {
        ProductSuggestionDto dto = new ProductSuggestionDto();

        dto.setSanPhamChiTietId(spct.getId());
        dto.setMaSanPhamChiTiet(spct.getMaSanPhamChiTiet());
        dto.setDonGia(spct.getDonGia());
        dto.setSoLuongTon(spct.getSoLuongTon());
        dto.setAnh(spct.getAnh());
        dto.setChatLieu(spct.getChatLieu());

        if (spct.getMauSac() != null) {
            dto.setMauSac(spct.getMauSac().getTen());
        }

        if (spct.getKichCo() != null) {
            dto.setKichCo(spct.getKichCo().getSoSize());
        }

        SanPham sp = spct.getSanPham();
        if (sp != null) {
            dto.setSanPhamId(sp.getId());
            dto.setMaSanPham(sp.getMaSanPham());
            dto.setTenSanPham(sp.getTenSanPham());

            if (sp.getLoaiSanPham() != null) {
                dto.setLoaiSanPham(sp.getLoaiSanPham().getTen());
            }

            if (sp.getThuongHieu() != null) {
                dto.setThuongHieu(sp.getThuongHieu().getTen());
            }

            if (sp.getFit() != null) {
                dto.setFit(sp.getFit().getTen());
            }
        }

        return dto;
    }

    @Override
    public List<ProductSuggestionDto> toDtoList(List<SanPhamChiTiet> list) {
        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AISuggestResponse suggestProducts(String message) {
        String text = normalize(message);

        OpenAiExtractResponse ai = null;
        try {
            ai = geminiService.extractFilters(message);
            log.info(
                    "Gemini extracted -> intent={}, loaiSanPham={}, mauSac={}, kichCo={}, priceMin={}, priceMax={}, fit={}, chatLieu={}",
                    ai != null ? ai.getIntent() : null,
                    ai != null ? ai.getLoaiSanPham() : null,
                    ai != null ? ai.getMauSac() : null,
                    ai != null ? ai.getKichCo() : null,
                    ai != null ? ai.getPriceMin() : null,
                    ai != null ? ai.getPriceMax() : null,
                    ai != null ? ai.getFit() : null,
                    ai != null ? ai.getChatLieu() : null
            );
        } catch (Exception e) {
            log.error("Gemini extract failed, fallback local rules", e);
        }

        String intent = firstNonBlank(
                ai != null ? normalizeNullable(ai.getIntent()) : null,
                detectIntent(text)
        );

        if ("greeting".equals(intent)) {
            AISuggestResponse response = new AISuggestResponse();
            response.setReply("Em chào anh/chị ạ. Anh/chị đang muốn tìm mẫu vest như thế nào để em hỗ trợ nhanh hơn nhé.");
            response.setProducts(List.of());
            return response;
        }

        if ("handoff".equals(intent)) {
            AISuggestResponse response = new AISuggestResponse();
            response.setReply("Dạ em đã nhận được yêu cầu của anh/chị. Em sẽ kết nối anh/chị với nhân viên tư vấn ngay ạ.");
            response.setProducts(List.of());
            return response;
        }

        String loaiSanPham = firstNonBlank(
                ai != null ? normalizeNullable(ai.getLoaiSanPham()) : null,
                text.contains("vest") ? "vest" : null
        );

        String mauSac = ai != null ? normalizeNullable(ai.getMauSac()) : null;
        String kichCo = ai != null ? normalizeNullable(ai.getKichCo()) : null;
        String fit = ai != null ? normalizeNullable(ai.getFit()) : null;
        String chatLieu = ai != null ? normalizeNullable(ai.getChatLieu()) : null;

        BigDecimal priceMin = ai != null ? ai.getPriceMin() : null;
        BigDecimal priceMax = ai != null ? ai.getPriceMax() : null;

        log.info(
                "Final filter -> intent={}, loaiSanPham={}, mauSac={}, kichCo={}, priceMin={}, priceMax={}, fit={}, chatLieu={}",
                intent, loaiSanPham, mauSac, kichCo, priceMin, priceMax, fit, chatLieu
        );

        boolean hasAnyFilter =
                loaiSanPham != null
                        || mauSac != null
                        || kichCo != null
                        || fit != null
                        || chatLieu != null
                        || priceMin != null
                        || priceMax != null;

        if (!hasAnyFilter) {
            AISuggestResponse response = new AISuggestResponse();
            response.setReply("Anh/chị muốn em ưu tiên màu, size hay tầm giá nào để em lọc vest sát hơn nhé.");
            response.setProducts(List.of());
            return response;
        }

        List<SanPhamChiTiet> allProducts = sanPhamChiTietRepository.findAllAvailableForAI();
        log.info("Total available products={}", allProducts.size());

        List<SanPhamChiTiet> baseProducts = allProducts.stream()
                .filter(spct -> loaiSanPham == null || matchLoaiSanPham(spct, loaiSanPham))
                .collect(Collectors.toList());

        if (loaiSanPham != null && baseProducts.isEmpty()) {
            AISuggestResponse response = new AISuggestResponse();
            response.setReply("Hiện tại shop chưa có mẫu vest phù hợp ạ.");
            response.setProducts(List.of());
            return response;
        }

        List<SanPhamChiTiet> exactMatches = baseProducts.stream()
                .filter(spct -> matchMauSac(spct, mauSac))
                .filter(spct -> matchKichCo(spct, kichCo))
                .filter(spct -> matchFit(spct, fit))
                .filter(spct -> matchChatLieu(spct, chatLieu))
                .filter(spct -> matchPriceMin(spct, priceMin))
                .filter(spct -> matchPriceMax(spct, priceMax))
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());

        if (!exactMatches.isEmpty()) {
            AISuggestResponse response = new AISuggestResponse();
            response.setReply(buildExactReply(exactMatches));
            response.setProducts(toDtoList(exactMatches));
            return response;
        }

        List<SanPhamChiTiet> similarMatches = baseProducts.stream()
                .map(spct -> new ProductScore(
                        spct,
                        scoreProduct(spct, loaiSanPham, mauSac, kichCo, priceMin, priceMax, fit, chatLieu)
                ))
                .filter(x -> x.score() > 0)
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .map(ProductScore::product)
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());

        if (!similarMatches.isEmpty()) {
            AISuggestResponse response = new AISuggestResponse();
            response.setReply(buildSimilarReply(similarMatches));
            response.setProducts(toDtoList(similarMatches));
            return response;
        }

        AISuggestResponse response = new AISuggestResponse();
        response.setReply("Hiện shop chưa tìm được mẫu vest phù hợp với yêu cầu của anh/chị. Anh/chị thử cho em thêm màu, size hoặc tầm giá để em lọc lại kỹ hơn nhé.");
        response.setProducts(List.of());
        return response;
    }

    private String buildExactReply(List<SanPhamChiTiet> products) {
        if (products.size() == 1) {
            return "Em thấy mẫu này khá phù hợp với nhu cầu của anh/chị. " + buildSingleProductIntro(products.get(0));
        }
        return "Dưới đây là các sản phẩm phù hợp với yêu cầu của anh/chị:";
    }

    private String buildSimilarReply(List<SanPhamChiTiet> products) {
        if (products.size() == 1) {
            return "Hiện shop chưa có mẫu khớp hoàn toàn, nhưng đây là sản phẩm gần nhất với nhu cầu của anh/chị. "
                    + buildSingleProductIntro(products.get(0));
        }
        return "Hiện shop chưa có mẫu khớp hoàn toàn, nhưng dưới đây là các sản phẩm gần với yêu cầu của anh/chị:";
    }

    private String buildSingleProductIntro(SanPhamChiTiet spct) {
        String ten = spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Sản phẩm này";
        String loai = spct.getSanPham() != null && spct.getSanPham().getLoaiSanPham() != null
                ? spct.getSanPham().getLoaiSanPham().getTen()
                : null;
        String mau = spct.getMauSac() != null ? spct.getMauSac().getTen() : null;
        String size = spct.getKichCo() != null ? spct.getKichCo().getSoSize() : null;

        StringBuilder sb = new StringBuilder();
        sb.append(ten);

        if (loai != null && !loai.isBlank()) {
            sb.append(" là mẫu ").append(loai.toLowerCase());
        }

        if (mau != null && !mau.isBlank()) {
            sb.append(" màu ").append(mau);
        }

        if (size != null && !size.isBlank()) {
            sb.append(" size ").append(size);
        }

        if (spct.getDonGia() != null) {
            sb.append(", giá ").append(formatPrice(spct.getDonGia()));
        }

        sb.append(".");
        return sb.toString();
    }

    private String formatPrice(BigDecimal price) {
        return String.format("%,d đ", price.longValue()).replace(',', '.');
    }

    private int scoreProduct(SanPhamChiTiet spct,
                             String loaiSanPham,
                             String mauSac,
                             String kichCo,
                             BigDecimal priceMin,
                             BigDecimal priceMax,
                             String fit,
                             String chatLieu) {
        int score = 0;

        if (loaiSanPham != null && matchLoaiSanPham(spct, loaiSanPham)) {
            score += 40;
        }

        if (mauSac != null && matchMauSac(spct, mauSac)) {
            score += 25;
        }

        if (fit != null && matchFit(spct, fit)) {
            score += 10;
        }

        if (chatLieu != null && matchChatLieu(spct, chatLieu)) {
            score += 10;
        }

        if (kichCo != null) {
            Integer wanted = parseSizeNumber(kichCo);
            Integer actual = spct.getKichCo() != null ? parseSizeNumber(spct.getKichCo().getSoSize()) : null;

            if (wanted != null && actual != null) {
                int diff = Math.abs(wanted - actual);
                if (diff == 0) {
                    score += 25;
                } else if (diff == 1) {
                    score += 18;
                } else if (diff == 2) {
                    score += 10;
                }
            }
        }

        if (spct.getDonGia() != null) {
            if (priceMin != null) {
                if (spct.getDonGia().compareTo(priceMin) >= 0) {
                    score += 15;
                } else {
                    score -= 8;
                }
            }

            if (priceMax != null) {
                if (spct.getDonGia().compareTo(priceMax) <= 0) {
                    score += 15;
                } else {
                    score -= 8;
                }
            }
        }

        return score;
    }

    private boolean matchLoaiSanPham(SanPhamChiTiet spct, String loaiSanPham) {
        if (loaiSanPham == null) return true;
        if (spct.getSanPham() == null || spct.getSanPham().getLoaiSanPham() == null) return false;

        String value = normalize(spct.getSanPham().getLoaiSanPham().getTen());
        return value.contains(loaiSanPham);
    }

    private boolean matchMauSac(SanPhamChiTiet spct, String mauSac) {
        if (mauSac == null) return true;
        if (spct.getMauSac() == null || spct.getMauSac().getTen() == null) return false;

        String value = normalize(spct.getMauSac().getTen());
        return value.equals(mauSac) || value.contains(mauSac);
    }

    private boolean matchKichCo(SanPhamChiTiet spct, String kichCo) {
        if (kichCo == null) return true;
        if (spct.getKichCo() == null || spct.getKichCo().getSoSize() == null) return false;

        String value = normalize(spct.getKichCo().getSoSize());
        return value.equals(kichCo);
    }

    private boolean matchFit(SanPhamChiTiet spct, String fit) {
        if (fit == null) return true;
        if (spct.getSanPham() == null || spct.getSanPham().getFit() == null) return false;
        if (spct.getSanPham().getFit().getTen() == null) return false;

        String value = normalize(spct.getSanPham().getFit().getTen());
        return value.contains(fit);
    }

    private boolean matchChatLieu(SanPhamChiTiet spct, String chatLieu) {
        if (chatLieu == null) return true;

        String value = null;

        if (spct.getChatLieu() != null && !spct.getChatLieu().isBlank()) {
            value = normalize(spct.getChatLieu());
        } else if (spct.getSanPham() != null
                && spct.getSanPham().getChatLieu() != null
                && spct.getSanPham().getChatLieu().getTen() != null) {
            value = normalize(spct.getSanPham().getChatLieu().getTen());
        }

        if (value == null) return false;
        return value.contains(chatLieu);
    }

    private boolean matchPriceMin(SanPhamChiTiet spct, BigDecimal priceMin) {
        if (priceMin == null) return true;
        if (spct.getDonGia() == null) return false;

        return spct.getDonGia().compareTo(priceMin) >= 0;
    }

    private boolean matchPriceMax(SanPhamChiTiet spct, BigDecimal priceMax) {
        if (priceMax == null) return true;
        if (spct.getDonGia() == null) return false;

        return spct.getDonGia().compareTo(priceMax) <= 0;
    }

    private String detectIntent(String text) {
        if (isGreeting(text)) return "greeting";
        if (isHandoff(text)) return "handoff";
        return "product_search";
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

    private Integer parseSizeNumber(String size) {
        try {
            return Integer.parseInt(size);
        } catch (Exception e) {
            return null;
        }
    }

    private String normalize(String input) {
        if (input == null) return "";

        String text = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d")
                .replace("Đ", "D")
                .toLowerCase(Locale.ROOT)
                .trim();

        return text.replaceAll("\\s+", " ");
    }

    private String normalizeNullable(String input) {
        if (input == null || input.isBlank()) return null;
        return normalize(input);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return null;
    }

    private record ProductScore(SanPhamChiTiet product, int score) {
    }
}