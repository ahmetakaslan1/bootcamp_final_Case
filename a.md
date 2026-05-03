Harika bir sunum kurgusu! N11 Bootcamp jürisinin karşısına çıktığında "Bu kişi sadece kod yazmamış, yazdığı sistemin mühendisliğini de yapmış" dedirtecek teknik bir metin hazırladım. Bu metin, senin sistemine tam uyumlu bir **"Mimari Analiz ve Sunum Rehberi"**dir.

N11 E-Ticaret Mikroservis Mimarisi - Teknik Analiz

1. Büyük Resim (The Flow): Verinin Yolculuğu
   Sunumda bu kısmı anlatırken ellerini kullanarak veya bir diyagram üzerinde servisleri işaretleyerek anlat.

"Kullanıcı Next.js arayüzünde 'Siparişi Tamamla' butonuna bastığında arka planda senkron ve asenkron işlemlerden oluşan bir koreografi başlar:

İstek ilk olarak API Gateway'e gelir. Token doğrulanır ve trafik Order Service'e yönlendirilir.
Order Service, kullanıcının sepetini doğrulamak için Cart Service'e senkron bir HTTP (Feign) çağrısı yapar. Eğer sepet doluysa, PostgreSQL'de durumu CREATED (Oluşturuldu) olan bir sipariş kaydı açar.
Sipariş veritabanına sorunsuz yazıldığı an (Commit sonrası), RabbitMQ üzerinden asenkron bir OrderCreatedEvent fırlatılır.
Bu mesajı dinleyen Inventory Service, ürünlerin stoklarını düşer (Pessimistic kilit ile) ve işlemi bitirince InventoryDeductedEvent fırlatır.
Son adımda Payment Service devreye girer. Stoktan düşüldüğünü görür, Iyzico ödeme altyapısıyla entegre çalışarak karttan parayı çeker. Sonuca göre ya PaymentSuccessfulEvent ya da PaymentFailedEvent fırlatır.
Order Service sonucu dinler ve veritabanındaki sipariş statüsünü COMPLETED ya da FAILED olarak günceller. Kullanıcı arayüzüne sonuç yansır." 2. Teknolojik Kararlar (The "Why")
Soru: Neden doğrudan HTTP çağrısı (Feign) yerine RabbitMQ (Message Broker) tercih edildi? "Eğer Sipariş, Stok ve Ödeme servislerini birbirine Feign ile bağlasaydım, sistem Temporal Coupling (Zamansal Bağımlılık) sorunu yaşardı. Yani Ödeme servisi çöktüğünde veya yavaşladığında, Sipariş servisi de tıkanır ve kullanıcıya hata dönerdi. RabbitMQ kullanarak servisleri birbirinden izole ettik (Decoupling). Bir servis anlık dursa bile mesaj kuyrukta bekler, servis ayağa kalktığında kaldığı yerden işlemeye devam eder. Bu bize yüksek dayanıklılık (Resilience) ve ölçeklenebilirlik sağladı."

Soru: @TransactionalEventListener kullanmanın veri tutarlılığı açısından hayati önemi nedir? "Bu bizim en kritik mimari kararlarımızdan biridir. Normal şartlarda siparişi kaydettiğimiz metodun hemen altına rabbitTemplate.convertAndSend yazabilirdik. Ancak veritabanı işlemi (Commit) sırasında veritabanı çökerse ne olur? Sipariş veritabanına yazılamaz, fakat RabbitMQ'ya mesaj çoktan gitmiş olur! Sonuç: Ortada sipariş yokken stok düşer. Bunu engellemek için Spring'in TransactionPhase.AFTER_COMMIT dinleyicisini kullandık. Sistem önce veritabanına %100 başarılı yazıldığından emin oluyor, ancak ve ancak başarılıysa mesajı kuyruğa fırlatıyor. "Hayalet Mesaj" (Phantom Message) oluşmasını engelledik."

Soru: Keycloak (OIDC) bu sistemin neresinde duruyor? "Güvenliği tekil bir noktadan (Single Sign-On) yönetmek için Keycloak konumlandırdık. Kullanıcı giriş yaptığında bir JWT (JSON Web Token) alır. Bu token, API Gateway üzerinden servislere geçerken Authorization header'ında (Bearer Token) taşınır. Servisler arası iletişimde güvenlik, doğrudan Keycloak tarafından doğrulanan roller (Role-Based Access Control) ile sağlanır."

3. Kritik Kod Parçaları (Deep Dive)
   Ekrana kodu yansıtıp şu kısımları açıklamalısın:

A) Pessimistic Lock ile Race Condition Önleme (InventoryRepository.java) "Stok güncellerken sıradan bir Update atmıyoruz. findByProductIdForUpdate metodu ile @Lock(LockModeType.PESSIMISTIC_WRITE) kullandık. Eğer 'Black Friday' gibi yoğun bir günde saniyede 100 kişi son kalan 1 adet iPhone'u almaya çalışırsa, PostgreSQL seviyesinde bu satıra donanımsal kilit koyuyoruz. İlk gelen işlemi bitirene kadar diğerleri saniyeler bazında bekletilir. Böylece e-ticaret sitelerinin korkulu rüyası olan 'Stok eksiye düşme' veya 'Aynı ürünü iki kişiye satma' (Lost Update) problemini kökünden çözdük."

B) Olmayan Ürüne Stok Ekleme Koruması (InventoryServiceImpl.java) "Stok sistemimizi sadece veritabanı CRUD'u olmaktan çıkardık. Yeni bir stok eklenirken (Upsert) önce ProductClient (Feign) ile Product Service'e 'Sende böyle bir ürün var mı?' diye soruyoruz. Eğer ürün katalogda yoksa işlemi anında Red ediyoruz. Bu, sistemimizde çöpe dönüşecek 'Yetim verilerin' (Orphan Data) oluşmasını engelliyor."

4. Hata Senaryosu: Saga Pattern ve Rollback (Compensation)
   Bu kısım projenin en "Senior" yanıdır. Jüriyi en çok etkileyecek yer burasıdır.

"Mikroservislerde tek bir büyük (Monolith) veritabanımız olmadığı için geleneksel @Transactional yapısı servisler arası çalışmaz. Dağıtık bir işlemde hata çıkarsa veriyi nasıl geri alacağız? Biz burada Choreography-based Saga Pattern kurguladık.

Örneğin; Sipariş oluşturuldu, Stok başarıyla düştü, ama müşteri limiti yetersiz olduğu için Ödeme başarısız oldu. Sistemimiz bu durumu yakalar:

Payment Service, RabbitMQ'ya PaymentFailedEvent fırlatır.
Order Service bu eventi yakalar, siparişin durumunu FAILED yapar ve ardından hemen bir telafi mesajı olan OrderFailedEvent fırlatır.
Inventory Service bu telafi mesajını dinler (handleOrderFailed). 'Demek ki ödeme alınamamış' der ve az önce düşürdüğü stokları geriye ekleyerek (restoreInventory) sistemi ilk anki tutarlı haline geri getirir (Compensating Transaction). Sistem kendi kendini onarır, manuel müdahaleye gerek kalmaz." 5. Video Sunum Notları (Altın Cümleler)
Sunum sırasında aralara şu profesyonel cümleleri sıkıştırmalısın:

🎯 "Amacım sadece çalışan bir kod yazmak değil, ölçeklenebilir ve hatalara karşı dirençli (fault-tolerant) bir sistem tasarlamaktı."
🎯 "Mikroservis mimarisinin en büyük dezavantajı olan Dağıtık Veri Tutarsızlığı (Distributed Data Inconsistency) riskini, Saga Choreography ile aştım."
🎯 "Senkron olan yerlerde Feign (Okuma işlemleri), asenkron olması gereken yerlerde RabbitMQ (Yazma işlemleri) kullanarak sistemin darboğaza (bottleneck) girmesini önledim."
🎯 "Geliştirdiğim bu altyapı, yarın sisteme yeni bir servis (örneğin Notification Service) eklendiğinde kodda hiçbir yeri değiştirmeden sadece RabbitMQ dinleyerek sisteme entegre olma esnekliği (Open-Closed Principle) sunuyor."
Bu metne hakim olduğunda, yaptığın işin arka planındaki devasa mühendislik kararlarını jüriye hissettireceksin. Başarılar dilerim! 🚀
