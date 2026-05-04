import Navbar from "@/components/Navbar";
import AddToCartButton from "@/components/AddToCartButton";
import Pagination from "@/components/Pagination";
import TranslatedText from "@/components/TranslatedText";

// Backend'den gelecek ürün modeli (ProductResponse)
interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
}

// Backend'den gelecek sayfalama meta verileri
interface PageData {
  content: Product[];
  totalPages: number;
  number: number;
}

// Next.js App Router'da SSR (Server-Side) olarak direkt fetch yapıyoruz.
async function getProducts(page: number = 0): Promise<PageData> {
  try {
    const baseUrl = process.env.API_GATEWAY_URL || "http://api-gateway:8080";
    // API Gateway üzerinden product-service'e istek, page parametresini ekliyoruz
    const res = await fetch(`http://${baseUrl}:8080/api/v1/products?page=${page}&size=12`, {
      cache: "no-store", // Her seferinde güncel datayı çekmesi için
    });

    if (!res.ok) {
      console.error("Ürünler getirilirken hata oluştu. Status:", res.status);
      return { content: [], totalPages: 0, number: 0 };
    }

    const data = await res.json();

    // ApiResponse formatı -> { data: { content: [], totalPages: x, number: y } }
    if (data && data.data && Array.isArray(data.data.content)) {
      return {
        content: data.data.content,
        totalPages: data.data.totalPages || 0,
        number: data.data.number || 0,
      };
    }

    console.error("Beklenmeyen API veri formatı. Bir dizi (Array) bulunamadı:", data);
    return { content: [], totalPages: 0, number: 0 };
  } catch (error) {
    console.error("Fetch Hatası:", error);
    return { content: [], totalPages: 0, number: 0 };
  }
}

export default async function Home({
  searchParams,
}: {
  searchParams?: { page?: string };
}) {
  const currentPage = Number(searchParams?.page) || 0;
  const pageData = await getProducts(currentPage);
  const products = pageData.content;

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />

      {/* Main Content (Ürün Grid) */}
      <main className="container mx-auto px-6 py-12">
        <div className="flex justify-between items-end mb-8">
          <h2 className="text-3xl font-bold text-slate-800 tracking-tight"><TranslatedText i18nKey="home.all_products" /></h2>
        </div>

        {products.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-xl text-slate-500"><TranslatedText i18nKey="home.no_products" /></p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {products.map((item) => (
              <div key={item.id} className="bg-white p-6 rounded-2xl shadow-sm hover:shadow-md transition-shadow border border-slate-100 flex flex-col justify-between h-72 group cursor-pointer">
                <div className="w-full h-32 bg-slate-50 rounded-xl mb-4 flex items-center justify-center overflow-hidden">
                  <svg className="w-10 h-10 text-slate-200 group-hover:scale-110 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <div>
                  <h3 className="font-semibold text-slate-800 truncate">{item.name}</h3>
                  <p className="text-xs text-slate-500 mt-1 line-clamp-2">{item.description}</p>
                </div>
                <div className="flex flex-col mt-4">
                  <span className="font-bold text-orange-500 text-xl">{item.price} TL</span>
                  <AddToCartButton productId={item.id} />
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Pagination Bileşeni */}
        {products.length > 0 && (
          <Pagination totalPages={pageData.totalPages} currentPage={pageData.number} />
        )}
      </main>
    </div>
  );
}
