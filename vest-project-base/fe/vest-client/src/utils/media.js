const DEFAULT_API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').trim();

const FALLBACK_LOGO =
    "data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='240' height='64' viewBox='0 0 240 64'%3E%3Crect width='240' height='64' rx='8' fill='%23000f51'/%3E%3Ctext x='50%25' y='52%25' dominant-baseline='middle' text-anchor='middle' fill='%23ffffff' font-size='26' font-family='Arial, sans-serif' font-weight='700'%3EVestShop%3C/text%3E%3C/svg%3E";

function safeTrim(value) {
    return String(value ?? '').trim();
}

export function getBackendOrigin(baseUrl = DEFAULT_API_BASE) {
    const raw = safeTrim(baseUrl);
    try {
        if (raw.startsWith('http://') || raw.startsWith('https://')) {
            return new URL(raw).origin;
        }
    } catch {}
    return 'http://localhost:8080';
}

function extractStringFromObject(obj) {
    if (!obj || typeof obj !== 'object') return '';

    const directKeys = [
        'secureUrl',
        'secure_url',
        'url',
        'imageUrl',
        'thumbnailUrl',
        'primaryImageUrl',
        'avatarUrl',
        'coverUrl',
        'anhDaiDien',
        'anh',
        'image',
        'hinhAnh',
        'duongDan',
    ];

    for (const key of directKeys) {
        const value = obj[key];
        if (typeof value === 'string' && value.trim()) return value.trim();
    }

    const nestedKeys = [
        'mediaAsset',
        'media',
        'mediaCover',
        'mediaPrimary',
        'coverMedia',
        'primaryMedia',
        'avatarMedia',
    ];

    for (const key of nestedKeys) {
        const nested = obj[key];
        const found = extractStringFromObject(nested);
        if (found) return found;
    }

    if (Array.isArray(obj.gallery)) {
        for (const item of obj.gallery) {
            const found = resolveMediaUrl(item);
            if (found) return found;
        }
    }

    return '';
}

export function resolveMediaUrl(value, baseUrl = DEFAULT_API_BASE) {
    if (!value) return '';

    if (Array.isArray(value)) {
        for (const item of value) {
            const found = resolveMediaUrl(item, baseUrl);
            if (found) return found;
        }
        return '';
    }

    if (typeof value === 'object') {
        const extracted = extractStringFromObject(value);
        return extracted ? resolveMediaUrl(extracted, baseUrl) : '';
    }

    const raw = safeTrim(value).replace(/\\/g, '/');
    if (!raw) return '';

    if (/^(https?:)?\/\//i.test(raw) || raw.startsWith('data:image') || raw.startsWith('blob:')) {
        return raw;
    }

    const origin = getBackendOrigin(baseUrl);

    if (raw.startsWith('/')) return origin + raw;
    if (raw.startsWith('uploads/')) return `${origin}/${raw}`;
    if (raw.startsWith('images/')) return `${origin}/${raw}`;

    return `${origin}/uploads/${raw}`;
}

export function pickFirstMediaUrl(...values) {
    for (const value of values) {
        const found = resolveMediaUrl(value);
        if (found) return found;
    }
    return '';
}

export function pickVariantImage(variant, fallback = '') {
    return (
        pickFirstMediaUrl(
            variant?.imageUrl,
            variant?.primaryImageUrl,
            variant?.gallery,
            variant?.hinhAnh,
            variant?.anh,
            variant?.image,
            variant?.mediaAsset,
            variant?.media,
        ) || fallback
    );
}

export function pickProductImage(product, variants = [], fallback = '') {
    const variantImages = (Array.isArray(variants) ? variants : []).map((variant) =>
        pickVariantImage(variant),
    );

    return (
        pickFirstMediaUrl(
            product?.imageUrl,
            product?.thumbnailUrl,
            product?.coverUrl,
            product?.anhDaiDien,
            product?.hinhAnh,
            product?.anh,
            product?.image,
            product?.mediaAsset,
            product?.media,
            variantImages,
        ) || fallback
    );
}

export function sortNewestFirst(items = []) {
    return [...(Array.isArray(items) ? items : [])].sort((a, b) => {
        const timeA = Date.parse(a?.ngayTao || a?.createdAt || a?.created_at || '') || 0;
        const timeB = Date.parse(b?.ngayTao || b?.createdAt || b?.created_at || '') || 0;
        if (timeA !== timeB) return timeB - timeA;

        const updatedA = Date.parse(a?.ngayCapNhat || a?.updatedAt || a?.updated_at || '') || 0;
        const updatedB = Date.parse(b?.ngayCapNhat || b?.updatedAt || b?.updated_at || '') || 0;
        if (updatedA !== updatedB) return updatedB - updatedA;

        return Number(b?.id || 0) - Number(a?.id || 0);
    });
}

export function parseMediaList(raw) {
    return String(raw || '')
        .split(/[,\n]/)
        .map((item) => resolveMediaUrl(item))
        .filter(Boolean);
}

export function getSiteLogoUrl() {
    return resolveMediaUrl(import.meta.env.VITE_SITE_LOGO_URL) || FALLBACK_LOGO;
}
