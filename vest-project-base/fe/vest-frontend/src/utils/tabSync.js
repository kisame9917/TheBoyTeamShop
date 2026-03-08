export const TAB_SYNC_CHANNEL = "vest-shop-tab-sync";

export const TAB_SYNC_EVENTS = {
  VOUCHER_CHANGED: "VOUCHER_CHANGED",
  PRODUCT_CHANGED: "PRODUCT_CHANGED",
  PRODUCT_STOCK_CHANGED: "PRODUCT_STOCK_CHANGED",
};

let channel = null;

export function getTabSyncChannel() {
  if (!channel) {
    channel = new BroadcastChannel(TAB_SYNC_CHANNEL);
  }
  return channel;
}

export function emitTabSync(type, payload = {}) {
  getTabSyncChannel().postMessage({
    type,
    payload,
    at: Date.now(),
  });
}

export function onTabSync(handler) {
  const ch = getTabSyncChannel();
  ch.addEventListener("message", handler);
  return () => ch.removeEventListener("message", handler);
}