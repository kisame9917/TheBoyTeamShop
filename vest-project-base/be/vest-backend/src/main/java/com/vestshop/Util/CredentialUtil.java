package com.vestshop.Util;

import java.security.SecureRandom;
import java.text.Normalizer;

/**
 * Tiện ích sinh tài khoản/mật khẩu cho nhân viên.
 *
 * Quy tắc username:
 *  - ten (từ cuối) + 2 ký tự viết tắt của họ + tên đệm + 3 số (001...)
 *  - Ví dụ: "Nguyễn Văn Hùng" => "hungnv001"
 */
public final class CredentialUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String ALL = UPPER + LOWER + DIGITS;

    private CredentialUtil() {}

    /** Chuyển tiếng Việt có dấu -> không dấu, bỏ ký tự đặc biệt, lowercase. */
    public static String normalizeAsciiLower(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.isEmpty()) return "";

        s = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.replace('đ', 'd').replace('Đ', 'D');
        s = s.replaceAll("[^A-Za-z0-9]", "");
        return s.toLowerCase();
    }

    /**
     * Sinh base username (chưa có 3 số).
     * - given name: từ cuối cùng
     * - họ: từ đầu
     * - tên đệm: từ ngay trước tên (nếu không có thì lấy lại họ)
     */
    public static String buildUsernameBase(String fullName) {
        String name = String.valueOf(fullName == null ? "" : fullName).trim();
        if (name.isEmpty()) return "user";

        String[] parts = name.split("\\s+");
        if (parts.length == 0) return "user";

        String given = normalizeAsciiLower(parts[parts.length - 1]);
        String ho = normalizeAsciiLower(parts[0]);
        String dem = normalizeAsciiLower(parts.length >= 3 ? parts[parts.length - 2] : parts[0]);

        char hoInitial = ho.isEmpty() ? 'x' : ho.charAt(0);
        char demInitial = dem.isEmpty() ? hoInitial : dem.charAt(0);

        String base = given + hoInitial + demInitial;

        // giới hạn 80 ký tự (để còn + 3 số)
        if (base.length() > 77) base = base.substring(0, 77);
        if (base.isEmpty()) base = "user";
        return base;
    }

    /** Sinh mật khẩu ngẫu nhiên (có chữ hoa + chữ thường + số). */
    public static String generateRandomPassword(int length) {
        int len = Math.max(length, 8);

        StringBuilder sb = new StringBuilder(len);
        sb.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));

        for (int i = 3; i < len; i++) {
            sb.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }

        // shuffle để không cố định vị trí
        char[] a = sb.toString().toCharArray();
        for (int i = a.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }

        return new String(a);
    }
}
