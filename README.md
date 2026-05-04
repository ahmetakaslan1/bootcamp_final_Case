# 🛒 N11 Bootcamp — E-Ticaret Mikroservis Platformu

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-black?logo=next.js)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange?logo=rabbitmq)](https://www.rabbitmq.com/)
[![Keycloak](https://img.shields.io/badge/Keycloak-24-teal?logo=keycloak)](https://www.keycloak.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)

N11 Bootcamp bitirme projesi olarak geliştirilen bu platform; **Saga Pattern**, **CQRS**, **Pessimistic Locking** ve **Event-Driven Architecture** gibi kurumsal yazılım mimarisi kalıplarını gerçek dünya senaryolarında uygulayan, tam kapsamlı bir e-ticaret çözümüdür.

---

## 📐 Mimari Genel Bakış

```
┌─────────────────────────────────────────────────────────┐
│                    Next.js Frontend                     │
│              (Port 3000 — SSR + i18n TR/EN)             │
└─────────────────────────┬───────────────────────────────┘
                          │ HTTP
┌─────────────────────────▼───────────────────────────────┐
│               API Gateway  (Port 8080)                  │
│         Spring Cloud Gateway + JWT Doğrulama            │
└──┬──────────┬────────┬───────────┬───────────┬──────────┘
   │          │        │           │           │
   ▼          ▼        ▼           ▼           ▼
Product    Cart    Order-Svc   Inventory  Payment-Svc
 :8081      :8083    :8084       :8082       :8085
   │                  │           │           │
   └──────────────────┴───────────┴───────────┘
                       │  RabbitMQ (AMQP)
                  ┌────▼─────┐
                  │ Saga FSM │  OrderCreated → InventoryCheck
                  └────┬─────┘  → PaymentProcess → OrderUpdate
                       │
              ┌────────▼────────┐
              │   PostgreSQL    │
              │ (Her servise    │
              │  ayrı veritabanı│
              └─────────────────┘
```

**Keycloak** (Port 9090) tüm servis-to-client JWT doğrulamasını, OAuth2 + OIDC protokolüyle yönetir.  
**Eureka** (Port 8761) servislerin birbirini bulduğu servis kayıt merkezidir.

---

## 🏗️ Servisler

| Servis | Port | Veritabanı | Açıklama |
|--------|------|------------|----------|
| `api-gateway` | 8080 | — | Tüm trafiğin girdiği tek kapı, JWT doğrulama |
| `discovery-server` | 8761 | — | Eureka — Servis keşfi |
| `product-service` | 8081 | product_db | Ürün CRUD, Redis cache |
| `inventory-service` | 8082 | inventory_db | Stok yönetimi, Pessimistic Lock |
| `cart-service` | 8083 | Redis | Sepet işlemleri |
| `order-service` | 8084 | order_db | Sipariş, Saga koordinatörü |
| `payment-service` | 8085 | payment_db | İyzico ödeme entegrasyonu |
| `frontend` | 3000 | — | Next.js 14, SSR, i18n |

---

## 🔑 Öne Çıkan Mimari Kararlar

### 1. Saga Pattern (Choreography-Based)
Sipariş akışı şu sırayla gerçekleşir:
```
Order (CREATED) → [RabbitMQ] → Inventory (stok düşür)
                            → [RabbitMQ] → Payment (ödeme al)
                                        → [RabbitMQ] → Order (COMPLETED/FAILED)
```
Her servis başarısız olduğunda **telafi (compensating) işlemi** tetiklenir.

### 2. TransactionalEventListener (Phantom Update Koruması)
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onOrderCreated(OrderCreatedEvent event) {
    // Sadece DB'ye başarıyla yazıldıktan SONRA mesaj gönderilir.
    // Bu satır, "DB'ye yazıldı ama mesaj gönderilemedi" senaryosunu engeller.
    rabbitTemplate.convertAndSend(...);
}
```

### 3. Pessimistic Locking (Race Condition Koruması)
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Inventory> findByProductIdForUpdate(Long productId);
```
Aynı anda gelen binlerce istek arasında stok tutarsızlığını önler.

---

## 🚀 Lokal Kurulum

### Ön Gereksinimler
- Java 21+
- Node.js 18+
- Docker & Docker Compose
- Maven 3.9+

### Adım 1 — Ortam Değişkenlerini Ayarla
```bash
cp .env.example .env
# .env dosyasını kendi değerlerinizle düzenleyin
```

### Adım 2 — Altyapıyı Başlat (Docker)
```bash
docker-compose up -d
```
Bu komut şunları ayağa kaldırır:
- PostgreSQL (5432) — `docker/postgres/init.sql` ile tüm veritabanları otomatik oluşur
- RabbitMQ (5672 + 15672 yönetim paneli)
- Redis (6379)
- Keycloak (9090) — `realm-export.json` otomatik import edilir

### Adım 3 — Backend Servislerini Başlat
Her mikroservis için ayrı terminalde:
```bash
cd discovery-server && mvn spring-boot:run
cd api-gateway      && mvn spring-boot:run
cd product-service  && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd cart-service     && mvn spring-boot:run
cd order-service    && mvn spring-boot:run
cd payment-service  && mvn spring-boot:run
```

### Adım 4 — Frontend'i Başlat
```bash
cd frontend
cp .env.local.example .env.local   # veya mevcut .env.local'ı düzenle
npm install
npm run dev
```
Tarayıcıda `http://localhost:3000` adresine gidin.

---

## 📚 API Dokümantasyonu (Swagger UI)

Servisler ayaktayken aşağıdaki adreslere gidin:

| Servis | Swagger URL |
|--------|------------|
| Order Service | http://localhost:8084/swagger-ui/index.html |
| Product Service | http://localhost:8081/swagger-ui/index.html |
| Inventory Service | http://localhost:8082/swagger-ui/index.html |
| Payment Service | http://localhost:8085/swagger-ui/index.html |
| Cart Service | http://localhost:8083/swagger-ui/index.html |

---

## 🔐 Keycloak Yapılandırması

1. `http://localhost:9090` adresine gidin
2. Admin bilgileri: `.env` dosyasındaki `KEYCLOAK_ADMIN` / `KEYCLOAK_PASSWORD`
3. **akaslan** realm'i `realm-export.json` ile otomatik yüklenmiş olmalıdır
4. `n11-client` isimli client'ı ve `ROLE_USER` / `ROLE_ADMIN` rollerini kontrol edin

---

## 📦 Frontend Klasörü

Detaylı frontend kurulum ve mimari bilgisi için: **[frontend/README.md](./frontend/README.md)**

---

## 🛠️ Teknoloji Yığını

**Backend:**  Spring Boot 3.4, Spring Cloud Gateway, Spring Security OAuth2, Spring Data JPA, Spring AMQP, Lombok, MapStruct, springdoc-openapi 2.8.4

**Altyapı:** PostgreSQL 15, Redis 7, RabbitMQ 3, Keycloak 24, Eureka (Netflix OSS)

**Frontend:** Next.js 14, TypeScript, Tailwind CSS, Zustand, NextAuth.js, i18next, Lucide Icons

**DevOps:** Docker, Docker Compose, GCP Compute Engine

---

## 👨‍💻 Geliştirici

**Ahmet Akaslan** — N11 Bootcamp 2026 Bitirme Projesi
