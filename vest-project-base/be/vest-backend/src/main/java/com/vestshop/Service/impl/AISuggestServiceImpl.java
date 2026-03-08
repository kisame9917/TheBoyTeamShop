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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AISuggestServiceImpl implements AISuggestService {

    private static final int MIN_VALID_SIZE = 44;
    private static final int MAX_VALID_SIZE = 58;
    private static final int MAX_SIZE_DISTANCE = 2;

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
            log.info("Gemini extracted -> loaiSanPham={}, mauSac={}, kichCo={}, priceMin={}, priceMax={}, fit={}, chatLieu={}, occasion={}, reply={}",
                    ai != null ? ai.getLoaiSanPham() : null,
                    ai != null ? ai.getMauSac() : null,
                    ai != null ? ai.getKichCo() : null,
                    ai != null ? ai.getPriceMin() : null,
                    ai != null ? ai.getPriceMax() : null,
                    ai != null ? ai.getFit() : null,
                    ai != null ? ai.getChatLieu() : null,
                    ai != null ? ai.getOccasion() : null,
                    ai != null ? ai.getReply() : null
            );
        } catch (Exception e) {
            log.error("Gemini extract failed, fallback to rule-based", e);
        }

        String loaiSanPham = firstNonBlank(
                ai != null ? normalizeNullable(ai.getLoaiSanPham()) : null,
                extractLoaiSanPham(text)
        );

        String mauSac = firstNonBlank(
                ai != null ? normalizeNullable(ai.getMauSac()) : null,
                extractMauSac(text)
        );

        String kichCo = firstNonBlank(
                ai != null ? normalizeNullable(ai.getKichCo()) : null,
                extractKichCo(text)
        );

        String fit = firstNonBlank(
                ai != null ? normalizeNullable(ai.getFit()) : null,
                null
        );

        String chatLieu = firstNonBlank(
                ai != null ? normalizeNullable(ai.getChatLieu()) : null,
                null
        );

        String occasion = firstNonBlank(
                ai != null ? normalizeNullable(ai.getOccasion()) : null,
                null
        );

        BigDecimal priceMin = ai != null && ai.getPriceMin() != null
                ? ai.getPriceMin()
                : extractPriceMin(text);

        BigDecimal priceMax = ai != null && ai.getPriceMax() != null
                ? ai.getPriceMax()
                : extractPriceMax(text);

        String aiReply = ai != null ? ai.getReply() : null;

        log.info("Final filter -> loaiSanPham={}, mauSac={}, kichCo={}, priceMin={}, priceMax={}, fit={}, chatLieu={}, occasion={}",
                loaiSanPham, mauSac, kichCo, priceMin, priceMax, fit, chatLieu, occasion);

        List<SanPhamChiTiet> allProducts = sanPhamChiTietRepository.findAllAvailableForAI();
        log.info("Total available products={}", allProducts.size());

        AISuggestResponse response = new AISuggestResponse();

        List<SanPhamChiTiet> afterLoai = allProducts.stream()
                .filter(spct -> matchLoaiSanPham(spct, loaiSanPham))
                .collect(Collectors.toList());

        if (loaiSanPham != null && afterLoai.isEmpty()) {
            response.setReply("Hiện tại shop chưa có sản phẩm loại " + loaiSanPham + " phù hợp ạ.");
            response.setProducts(List.of());
            return response;
        }

        List<SanPhamChiTiet> afterMau = afterLoai.stream()
                .filter(spct -> matchMauSac(spct, mauSac))
                .collect(Collectors.toList());

        boolean missingColor = mauSac != null && afterMau.isEmpty();

        List<SanPhamChiTiet> afterSize = afterLoai.stream()
                .filter(spct -> matchKichCo(spct, kichCo))
                .collect(Collectors.toList());

        boolean missingSize = kichCo != null && afterSize.isEmpty();

        if (missingColor && missingSize) {
            List<Integer> nearestSizes = findNearestAvailableSizes(kichCo, afterMau);

            if (!nearestSizes.isEmpty()) {
                List<SanPhamChiTiet> fallbackByNearestSize = afterMau.stream()
                        .filter(spct -> matchNearestSizes(spct, nearestSizes))
                        .filter(spct -> matchFit(spct, fit))
                        .filter(spct -> matchChatLieu(spct, chatLieu))
                        .filter(spct -> matchPriceMin(spct, priceMin))
                        .filter(spct -> matchPriceMax(spct, priceMax))
                        .limit(5)
                        .collect(Collectors.toList());

                if (!fallbackByNearestSize.isEmpty()) {
                    String sizeText = nearestSizes.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(" hoặc "));

                    response.setReply("Hiện tại shop chưa có đúng màu " + mauSac + " và size " + kichCo
                            + " ạ. Tuy nhiên anh/chị có thể tham khảo một số mẫu gần nhất với size "
                            + sizeText + " để dễ chọn hơn.");
                    response.setProducts(toDtoList(fallbackByNearestSize));
                    return response;
                }
            }

            response.setReply("Hiện tại shop chưa có sản phẩm đúng màu " + mauSac + " và size " + kichCo + " ạ.");
            response.setProducts(List.of());
            return response;
        }

        if (missingColor) {
            response.setReply("Hiện tại shop chưa có sản phẩm đúng màu " + mauSac + " ạ.");
            response.setProducts(List.of());
            return response;
        }

        if (missingSize) {
            List<SanPhamChiTiet> baseForNearestSize = afterLoai.stream()
                    .filter(spct -> matchMauSac(spct, mauSac))
                    .filter(spct -> matchFit(spct, fit))
                    .filter(spct -> matchChatLieu(spct, chatLieu))
                    .filter(spct -> matchPriceMin(spct, priceMin))
                    .filter(spct -> matchPriceMax(spct, priceMax))
                    .collect(Collectors.toList());

            List<Integer> nearestSizes = findNearestAvailableSizes(kichCo, baseForNearestSize);

            if (!nearestSizes.isEmpty()) {
                List<SanPhamChiTiet> fallbackByNearestSize = baseForNearestSize.stream()
                        .filter(spct -> matchNearestSizes(spct, nearestSizes))
                        .limit(5)
                        .collect(Collectors.toList());

                if (!fallbackByNearestSize.isEmpty()) {
                    String sizeText = nearestSizes.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(" hoặc "));

                    response.setReply("Hiện tại shop chưa có đúng size " + kichCo
                            + " ạ. Anh/chị có thể tham khảo size gần nhất như "
                            + sizeText + " để dễ chọn form phù hợp hơn.");
                    response.setProducts(toDtoList(fallbackByNearestSize));
                    return response;
                }
            }

            response.setReply("Hiện tại shop chưa có sản phẩm đúng size " + kichCo + " ạ.");
            response.setProducts(List.of());
            return response;
        }

        List<SanPhamChiTiet> matched = afterLoai.stream()
                .filter(spct -> matchMauSac(spct, mauSac))
                .filter(spct -> matchKichCo(spct, kichCo))
                .filter(spct -> matchFit(spct, fit))
                .filter(spct -> matchChatLieu(spct, chatLieu))
                .filter(spct -> matchPriceMin(spct, priceMin))
                .filter(spct -> matchPriceMax(spct, priceMax))
                .limit(5)
                .collect(Collectors.toList());

        log.info("Matched products={}", matched.size());

        matched.forEach(spct -> log.info(
                "Matched -> spctId={}, tenSanPham={}, loai={}, mau={}, size={}, fit={}, chatLieu={}, gia={}",
                spct.getId(),
                spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : null,
                spct.getSanPham() != null && spct.getSanPham().getLoaiSanPham() != null
                        ? spct.getSanPham().getLoaiSanPham().getTen()
                        : null,
                spct.getMauSac() != null ? spct.getMauSac().getTen() : null,
                spct.getKichCo() != null ? spct.getKichCo().getSoSize() : null,
                spct.getSanPham() != null && spct.getSanPham().getFit() != null
                        ? spct.getSanPham().getFit().getTen()
                        : null,
                spct.getChatLieu(),
                spct.getDonGia()
        ));

        if (matched.isEmpty()) {
            response.setReply(buildNoMatchReply(loaiSanPham, mauSac, kichCo, priceMin, priceMax, fit, chatLieu, occasion));
            response.setProducts(List.of());
            return response;
        }

        if (aiReply != null && !aiReply.isBlank()) {
            response.setReply(aiReply);
        } else {
            response.setReply(buildReply(loaiSanPham, mauSac, kichCo, matched.size(), occasion));
        }

        response.setProducts(toDtoList(matched));
        return response;
    }

    private String buildReply(String loaiSanPham, String mauSac, String kichCo, int count, String occasion) {
        StringBuilder sb = new StringBuilder("Em tìm thấy ");
        sb.append(count).append(" sản phẩm");

        if (loaiSanPham != null) {
            sb.append(" ").append(loaiSanPham);
        }
        if (mauSac != null) {
            sb.append(" màu ").append(mauSac);
        }
        if (kichCo != null) {
            sb.append(" size ").append(kichCo);
        }
        if (occasion != null) {
            sb.append(" phù hợp cho ").append(occasion);
        }

        sb.append(" ạ.");
        return sb.toString();
    }

    private String buildNoMatchReply(String loaiSanPham,
                                     String mauSac,
                                     String kichCo,
                                     BigDecimal priceMin,
                                     BigDecimal priceMax,
                                     String fit,
                                     String chatLieu,
                                     String occasion) {
        StringBuilder sb = new StringBuilder("Em chưa tìm thấy sản phẩm");

        if (loaiSanPham != null) {
            sb.append(" ").append(loaiSanPham);
        }
        if (mauSac != null) {
            sb.append(" màu ").append(mauSac);
        }
        if (kichCo != null) {
            sb.append(" size ").append(kichCo);
        }
        if (fit != null) {
            sb.append(" ").append(fit);
        }
        if (chatLieu != null) {
            sb.append(" chất liệu ").append(chatLieu);
        }
        if (priceMin != null || priceMax != null) {
            sb.append(" trong mức giá yêu cầu");
        }
        if (occasion != null) {
            sb.append(" cho ").append(occasion);
        }

        sb.append(" ạ.");
        return sb.toString();
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

    private boolean matchNearestSizes(SanPhamChiTiet spct, List<Integer> nearestSizes) {
        if (nearestSizes == null || nearestSizes.isEmpty()) return false;
        if (spct.getKichCo() == null || spct.getKichCo().getSoSize() == null) return false;

        Integer value = parseSizeNumber(spct.getKichCo().getSoSize());
        return value != null && nearestSizes.contains(value);
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

    private String extractLoaiSanPham(String text) {
        if (text.contains("vest")) return "vest";
        if (text.contains("ao so mi") || text.contains("so mi") || text.contains("somi")) return "so mi";
        if (text.contains("quan au") || text.contains("quan tay")) return "quan";
        return null;
    }

    private String extractMauSac(String text) {
        if (text.contains("den")) return "den";
        if (text.contains("trang")) return "trang";
        if (text.contains("xanh")) return "xanh";
        if (text.contains("do")) return "do";
        if (text.contains("xam")) return "xam";
        if (text.contains("nau")) return "nau";
        return null;
    }

    private String extractKichCo(String text) {
        Matcher m = Pattern.compile("\\bsize\\s*(\\d{2,3})\\b").matcher(text);
        if (m.find()) {
            return m.group(1);
        }

        Matcher m2 = Pattern.compile("\\b(44|45|46|47|48|49|50|51|52|53|54|55|56|57|58)\\b").matcher(text);
        if (m2.find() && text.contains("size")) {
            return m2.group(1);
        }

        return null;
    }

    private BigDecimal extractPriceMin(String text) {
        if (text.contains("tren 500k") || text.contains("hon 500k") || text.contains("tren 500 nghin")) {
            return new BigDecimal("500000");
        }
        if (text.contains("tren 1 trieu") || text.contains("hon 1 trieu") || text.contains("tren 1m") || text.contains("hon 1m")) {
            return new BigDecimal("1000000");
        }
        if (text.contains("tren 2 trieu") || text.contains("hon 2 trieu") || text.contains("tren 2m") || text.contains("hon 2m")) {
            return new BigDecimal("2000000");
        }
        if (text.contains("tren 3 trieu") || text.contains("hon 3 trieu") || text.contains("tren 3m") || text.contains("hon 3m")) {
            return new BigDecimal("3000000");
        }
        return null;
    }

    private BigDecimal extractPriceMax(String text) {
        if (text.contains("duoi 500k") || text.contains("duoi 500 nghin")) {
            return new BigDecimal("500000");
        }
        if (text.contains("duoi 1 trieu") || text.contains("duoi 1m")) {
            return new BigDecimal("1000000");
        }
        if (text.contains("duoi 2 trieu") || text.contains("duoi 2m")) {
            return new BigDecimal("2000000");
        }
        if (text.contains("duoi 3 trieu") || text.contains("duoi 3m")) {
            return new BigDecimal("3000000");
        }
        return null;
    }

    private Integer parseSizeNumber(String size) {
        try {
            return Integer.parseInt(size);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Integer> extractAvailableSizes(List<SanPhamChiTiet> products) {
        return products.stream()
                .map(SanPhamChiTiet::getKichCo)
                .filter(Objects::nonNull)
                .map(k -> k.getSoSize())
                .filter(s -> s != null && !s.isBlank())
                .map(this::parseSizeNumber)
                .filter(Objects::nonNull)
                .filter(size -> size >= MIN_VALID_SIZE && size <= MAX_VALID_SIZE)
                .distinct()
                .sorted()
                .toList();
    }

    private List<Integer> findNearestAvailableSizes(String requestedSize, List<SanPhamChiTiet> products) {
        Integer requested = parseSizeNumber(requestedSize);
        if (requested == null) return List.of();

        if (requested < MIN_VALID_SIZE || requested > MAX_VALID_SIZE) {
            return List.of();
        }

        List<Integer> availableSizes = extractAvailableSizes(products);
        if (availableSizes.isEmpty()) return List.of();

        return availableSizes.stream()
                .filter(size -> !size.equals(requested))
                .filter(size -> Math.abs(size - requested) <= MAX_SIZE_DISTANCE)
                .sorted(Comparator.comparingInt(size -> Math.abs(size - requested)))
                .limit(2)
                .toList();
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
}