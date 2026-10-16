// 瀏覽器 ES module 範例。apiBaseUrl 例如 https://api.example.com。
// 呼叫端負責持有回傳的 JWT；此範例不將密碼、密文或 Token 寫入日誌或 localStorage。
async function postJson(apiBaseUrl, path, body) {
  const url = new URL(path, apiBaseUrl);
  if (url.protocol !== "https:") throw new Error("帳密 API 必須使用 HTTPS");
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    credentials: "omit",
    cache: "no-store",
    redirect: "error",
    body: JSON.stringify(body),
  });
  const result = await response.json();
  if (!response.ok || result.code !== 200) {
    throw new Error(`${result.message || "請求失敗"} (traceId=${result.traceId || ""})`);
  }
  return result;
}

export async function login(apiBaseUrl, account, password) {
  const keyResponse = await postJson(apiBaseUrl, "/auth/public-key", { account });
  const { publicKey, algorithm, keyFormat } = keyResponse.data;
  if (algorithm !== "RSA-OAEP-256" || keyFormat !== "SPKI") throw new Error("不支援的公鑰格式");
  const keyBytes = Uint8Array.from(atob(publicKey), char => char.charCodeAt(0));
  const key = await crypto.subtle.importKey(
    "spki", keyBytes, { name: "RSA-OAEP", hash: "SHA-256" }, false, ["encrypt"]
  );
  const passwordBytes = new TextEncoder().encode(password);
  if (passwordBytes.length === 0 || passwordBytes.length > 72) throw new Error("密碼需為 1 至 72 UTF-8 bytes");
  let encrypted;
  try {
    encrypted = await crypto.subtle.encrypt({ name: "RSA-OAEP" }, key, passwordBytes);
  } finally {
    passwordBytes.fill(0);
  }
  const ciphertext = btoa(String.fromCharCode(...new Uint8Array(encrypted)));
  return postJson(apiBaseUrl, "/auth/login", { account, password: ciphertext });
}

export async function register(apiBaseUrl, account, name, password) {
  // 尚未有帳號專屬公鑰，註冊原始密碼以 HTTPS 保護。
  return postJson(apiBaseUrl, "/auth/register", { account, name, password });
}
