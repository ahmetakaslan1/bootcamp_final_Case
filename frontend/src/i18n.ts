"use client";

import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import tr from "./locales/tr.json";
import en from "./locales/en.json";

// İstemci tarafında çalışan (Client-side) konfigürasyon
i18n
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      tr: { translation: tr }
    },
    lng: "tr", // Başlangıç dili
    fallbackLng: "tr",
    interpolation: {
      escapeValue: false // React zaten XSS koruması sağlıyor
    }
  });

export default i18n;
