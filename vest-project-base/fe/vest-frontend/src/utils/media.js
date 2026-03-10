const DEFAULT_API_BASE = (import.meta.env.VITE_API_URL || import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").trim();

function safeTrim(value) {
    return String(value ?? "").trim();
}

export function getBackendOrigin(baseUrl = DEFAULT_API_BASE) {
    const raw = safeTrim(baseUrl);
    try {
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return new URL(raw).origin;
        }
    } catch {}
    return "http://localhost:8080";
}

function extractStringFromObject(obj) {
    if (!obj || typeof obj !== "object") return "";
    const directKeys = [
        "secureUrl", "secure_url", "url", "imageUrl", "avatarUrl", "primaryImageUrl",
        "thumbnailUrl", "anhDaiDien", "anh", "image", "hinhAnh", "duongDan"
    ];
    for (const key of directKeys) {
        const value = obj[key];
        if (typeof value === "string" && value.trim()) return value.trim();
    }

    const nestedKeys = ["mediaAsset", "media", "coverMedia", "primaryMedia", "avatarMedia"];
    for (const key of nestedKeys) {
        const nested = obj[key];
        const found = extractStringFromObject(nested);
        if (found) return found;
    }

    return "";
}

export function resolveMediaUrl(value, baseUrl = DEFAULT_API_BASE) {
    if (!value) return "";

    if (Array.isArray(value)) {
        for (const item of value) {
            const found = resolveMediaUrl(item, baseUrl);
            if (found) return found;
        }
        return "";
    }

    if (typeof value === "object") {
        const extracted = extractStringFromObject(value);
        return extracted ? resolveMediaUrl(extracted, baseUrl) : "";
    }

    const raw = safeTrim(value).replace(/\\/g, "/");
    if (!raw) return "";
    if (/^(https?:)?\/\//i.test(raw) || raw.startsWith("data:image") || raw.startsWith("blob:")) return raw;

    const origin = getBackendOrigin(baseUrl);
    if (raw.startsWith("/")) return origin + raw;
    return `${origin}/${raw}`;
}

export function pickFirstMediaUrl(...values) {
    for (const value of values) {
        const found = resolveMediaUrl(value);
        if (found) return found;
    }
    return "";
}

export function normalizeUploadResponse(payload) {
    const data = payload?.data ?? payload ?? {};
    return {
        url: resolveMediaUrl(data.url || data.secureUrl || data.secure_url || data.imageUrl || ""),
        mediaAssetId: data.mediaAssetId ?? data.media_asset_id ?? data.idMediaAsset ?? null,
        publicId: data.publicId ?? data.public_id ?? null,
        assetId: data.assetId ?? data.asset_id ?? null,
        format: data.format ?? null,
        width: data.width ?? null,
        height: data.height ?? null,
        bytes: data.bytes ?? null,
    };
}
