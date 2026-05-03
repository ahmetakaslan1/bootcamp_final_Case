import { getSession } from "next-auth/react";
import i18n from "../i18n";

export async function fetchWithAuth(url: string, options: RequestInit = {}) {
  const session = await getSession();
  const headers = new Headers(options.headers || {});

  headers.append("Content-Type", "application/json");
  headers.append("Accept", "application/json");
  
  // Backend'e dil tercihini gönder (GlobalExceptionHandler bu sayede Türkçe/İngilizce dönecek)
  if (i18n.language) {
    headers.append("Accept-Language", i18n.language);
  } else {
    headers.append("Accept-Language", "tr");
  }

  if (session?.accessToken) {
    headers.append("Authorization", `Bearer ${session.accessToken}`);
  }

  const baseUrl = "/api/v1";

  return fetch(`${baseUrl}${url}`, {
    ...options,
    headers,
  });
}
