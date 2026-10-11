export function safeRedirect(candidate, role) {
  const fallback = role === "ADMIN" ? "/admin" : "/portal/records";
  if (
    typeof candidate !== "string" ||
    !candidate.startsWith("/") ||
    candidate.startsWith("//") ||
    /[\\\u0000-\u001f]/.test(candidate)
  )
    return fallback;
  let url;
  try {
    url = new URL(candidate, "http://local.test");
  } catch {
    return fallback;
  }
  if (url.origin !== "http://local.test") return fallback;
  let path;
  try {
    path = decodeURIComponent(url.pathname);
  } catch {
    return fallback;
  }
  if (
    /[\\\u0000-\u001f]/.test(path) ||
    path.startsWith("//") ||
    path === "/login"
  )
    return fallback;
  if (role !== "ADMIN" && /^\/admin(?:\/|$)/.test(path)) return fallback;
  return url.pathname + url.search + url.hash;
}
