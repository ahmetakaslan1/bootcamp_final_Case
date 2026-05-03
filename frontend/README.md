# 🎨 N11 E-Ticaret — Next.js Frontend

[![Next.js](https://img.shields.io/badge/Next.js-14-black?logo=next.js)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?logo=typescript)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/TailwindCSS-3-38B2AC?logo=tailwind-css)](https://tailwindcss.com/)

Bu klasör, N11 Bootcamp bitirme projesinin **Next.js 14 App Router** tabanlı frontend uygulamasını içermektedir. Backend Spring Boot mikroservisleriyle Keycloak üzerinden OAuth2 (OIDC) kimlik doğrulaması yaparak iletişim kurar.

---

## 📁 Klasör Yapısı

```
frontend/
├── public/                     # Statik dosyalar
├── src/
│   ├── app/                    # Next.js App Router sayfaları
│   │   ├── page.tsx            # Ana Sayfa — Ürün listeleme (SSR)
│   │   ├── cart/page.tsx       # Sepet sayfası
│   │   ├── checkout/page.tsx   # Ödeme & teslimat formu
│   │   ├── orders/page.tsx     # Siparişlerim — geçmiş siparişler
│   │   ├── layout.tsx          # Root layout
│   │   ├── providers.tsx       # SessionProvider + i18n
│   │   └── api/auth/           # NextAuth.js Keycloak route
│   ├── components/
│   │   ├── Navbar.tsx          # Dil değiştirici, sepet ikonu, auth butonu
│   │   ├── AddToCartButton.tsx # Sepete ekle (client component)
│   │   ├── Pagination.tsx      # Sayfalama
│   │   └── TranslatedText.tsx  # Server Component içinde çeviri köprüsü
│   ├── store/
│   │   └── useCartStore.ts     # Zustand — global sepet state
│   ├── lib/
│   │   └── fetcher.ts          # JWT token'lı fetch helper
│   ├── locales/
│   │   ├── tr.json             # Türkçe çeviriler
│   │   └── en.json             # İngilizce çeviriler
│   └── i18n.ts                 # i18next konfigürasyonu
├── .env.local.example          # Lokal ortam değişkenleri şablonu
├── next.config.mjs             # Proxy rewrites + standalone build
├── Dockerfile                  # Production Docker imajı
└── package.json
```

---

## ✨ Özellikler

| Özellik | Açıklama |
|---------|----------|
| 🔐 **Keycloak OAuth2** | NextAuth.js ile OIDC akışı, JWT token yönetimi |
| 🛍️ **Ürün Listeleme** | SSR ile sunucudan çekilen, sayfalanmış ürün kartları |
| 🛒 **Sepet Yönetimi** | Zustand ile global state, ürün ekleme/çıkarma/temizleme |
| 💳 **Checkout** | Teslimat bilgileri (adres, alıcı, telefon) + ödeme formu |
| 📦 **Siparişlerim** | Renk kodlu durum rozetleri (Tamamlandı / İşleniyor / Başarısız) |
| 🔍 **Sipariş Detay Modalı** | Her sipariş için ürün listesi, alıcı ve adres bilgileri |
| 🌍 **TR / EN i18n** | React i18next ile tam iki dil desteği, Navbar'dan anında geçiş |
| 📱 **Responsive Tasarım** | Mobil uyumlu Tailwind CSS layout |

---

## 🔧 Kurulum

### 1. Bağımlılıkları Yükle
```bash
npm install
```

### 2. Ortam Değişkenlerini Ayarla
`.env.local` dosyası oluşturun:
```bash
cp .env.local.example .env.local
```

Sonra `.env.local` içeriğini düzenleyin:

```env
# Backend API Gateway URL (Next.js proxy için)
NEXT_PUBLIC_API_URL=http://localhost:3000/api/v1

# NextAuth.js — Keycloak OIDC
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=super-secret-nextauth-key-change-this

# Keycloak Client Bilgileri
KEYCLOAK_CLIENT_ID=n11-client
KEYCLOAK_CLIENT_SECRET=your-client-secret-from-keycloak
KEYCLOAK_ISSUER=http://localhost:9090/realms/akaslan
```

> **Keycloak Client Secret'ı nereden alırsınız?**  
> Keycloak Admin → Clients → `n11-client` → Credentials → Secret

### 3. Geliştirme Sunucusunu Başlat
```bash
npm run dev
```
Tarayıcıda [http://localhost:3000](http://localhost:3000) adresine gidin.

---

## 🗺️ Sayfalar

### Ana Sayfa (`/`)
- Next.js **Server-Side Rendering (SSR)** ile her istekte güncel ürünleri çeker
- Ürün kartları: isim, açıklama, fiyat ve "Sepete Ekle" butonu
- Sayfalama (Pagination) desteği
- Giriş yapılmadan ürünler görüntülenebilir; sepete eklemek için giriş gerekir

### Sepet (`/cart`)
- Zustand store üzerinden gerçek zamanlı güncelleme
- Ürün miktarı artırma/azaltma, tek tek silme, tümünü temizleme
- Sipariş özeti: Ara toplam, Kargo (Bedava), Toplam
- "Satın Al" butonu → `/checkout`'a yönlendirir

### Satın Alma (`/checkout`)
- **Teslimat Formu:** Adres, Alıcı Adı, Telefon numarası
- **Kart Bilgileri:** Kart no, son kullanma, CVV (mock)
- Sağ panel: Anlık sipariş özeti
- Başarılı siparişte onay mesajı + otomatik sepet temizleme

### Siparişlerim (`/orders`)
- Kullanıcının tüm geçmiş siparişleri tablo halinde listelenir
- **Durum Rozetleri:**
  - 🟢 `COMPLETED` — Tamamlandı
  - 🟡 `PENDING / CREATED` — İşleniyor
  - 🔴 `FAILED` — Başarısız
- **Detay Modalı:** Her siparişe tıklayarak ürün listesi, alıcı adı, telefon ve adres görüntülenir

---

## 🌍 Dil Desteği (i18n)

Navbar'daki **TR | EN** butonlarıyla dil anında değişir. Tüm çeviriler `src/locales/` klasöründe JSON formatında tutulur:

```
src/locales/
├── tr.json   ← Türkçe (varsayılan)
└── en.json   ← İngilizce
```

Yeni bir çeviri eklemek için her iki dosyaya da aynı anahtarı ekleyin:
```json
// tr.json
{ "navbar": { "new_key": "Yeni Metin" } }

// en.json
{ "navbar": { "new_key": "New Text" } }
```

---

## 🐳 Docker ile Çalıştırma

```bash
# Ana dizinden (root'tan) çalıştırın:
docker-compose -f docker-compose-prod.yml up -d --build frontend
```

Veya sadece frontend'i build etmek için:
```bash
docker build -t n11-frontend ./frontend
docker run -p 3000:3000 n11-frontend
```

---

## 🛠️ Teknoloji Detayları

| Teknoloji | Versiyon | Kullanım Amacı |
|-----------|---------|----------------|
| Next.js | 14 | SSR, App Router, API routes |
| TypeScript | 5 | Tip güvenliği |
| Tailwind CSS | 3 | Utility-first CSS |
| Zustand | 4 | Global client state (sepet) |
| NextAuth.js | 4 | Keycloak OAuth2/OIDC |
| react-i18next | latest | TR/EN dil desteği |
| Lucide React | latest | İkonlar |
| shadcn/ui | latest | Button, Card bileşenleri |
