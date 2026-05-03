"use client";

import { useEffect, useState } from "react";
import { useSession, signIn } from "next-auth/react";
import Navbar from "@/components/Navbar";
import { fetchWithAuth } from "@/lib/fetcher";
import { Package, X, FileText, MapPin, Phone, User, Calendar, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useTranslation } from "react-i18next";

interface OrderItem {
  productId: number;
  quantity: number;
  price: number;
}

interface Order {
  orderNumber: string;
  totalPrice: number;
  status: "CREATED" | "PENDING" | "COMPLETED" | "FAILED";
  paymentFailureReason: string | null;
  shippingAddress: string;
  receiverName: string;
  phoneNumber: string;
  createdAt: string;
  items: OrderItem[];
}

export default function OrdersPage() {
  const { t } = useTranslation();
  const { status, data: session } = useSession();
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);

  useEffect(() => {
    if (session?.accessToken) {
      loadOrders();
    } else if (status === "unauthenticated") {
      setIsLoading(false);
    }
  }, [session, status]);

  const loadOrders = async () => {
    try {
      const response = await fetchWithAuth("/orders");
      if (response.ok) {
        const json = await response.json();
        setOrders(json.data || []);
      }
    } catch (error) {
      console.error("Siparişler getirilemedi:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusBadge = (orderStatus: string) => {
    switch (orderStatus) {
      case "COMPLETED":
        return <span className="px-3 py-1 bg-emerald-100 text-emerald-700 rounded-full text-xs font-bold uppercase tracking-wider">Tamamlandı</span>;
      case "FAILED":
        return <span className="px-3 py-1 bg-red-100 text-red-700 rounded-full text-xs font-bold uppercase tracking-wider">Başarısız</span>;
      case "PENDING":
      case "CREATED":
        return <span className="px-3 py-1 bg-amber-100 text-amber-700 rounded-full text-xs font-bold uppercase tracking-wider">İşleniyor</span>;
      default:
        return <span className="px-3 py-1 bg-slate-100 text-slate-700 rounded-full text-xs font-bold uppercase tracking-wider">{orderStatus}</span>;
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return "Bilinmiyor";
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('tr-TR', { 
      day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' 
    }).format(date);
  };

  if (status === "loading" || (isLoading && status === "authenticated")) {
    return (
      <div className="min-h-screen bg-slate-50">
        <Navbar />
        <div className="container mx-auto px-6 py-20 flex justify-center items-center">
          <div className="animate-pulse flex flex-col items-center">
            <Package className="w-12 h-12 text-slate-300 mb-4" />
            <p className="text-slate-500 font-medium">Siparişleriniz Yükleniyor...</p>
          </div>
        </div>
      </div>
    );
  }

  if (status === "unauthenticated") {
    return (
      <div className="min-h-screen bg-slate-50">
        <Navbar />
        <div className="container mx-auto px-6 py-20 text-center">
          <Package className="w-16 h-16 text-slate-300 mx-auto mb-6" />
          <h2 className="text-2xl font-bold text-slate-800 mb-4">Geçmiş Siparişleriniz</h2>
          <p className="text-slate-500 mb-8">Sipariş geçmişinizi görüntülemek için lütfen giriş yapın.</p>
          <Button onClick={() => signIn("keycloak")} className="bg-orange-500 hover:bg-orange-600">Giriş Yap</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      
      <main className="container mx-auto px-6 py-12 max-w-6xl">
        <div className="flex items-center gap-3 mb-8">
          <FileText className="w-8 h-8 text-orange-500" />
          <h2 className="text-3xl font-bold text-slate-800">Siparişlerim</h2>
        </div>

        {orders.length === 0 ? (
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-12 text-center">
            <Package className="w-16 h-16 text-slate-200 mx-auto mb-4" />
            <h3 className="text-xl font-medium text-slate-600 mb-2">Henüz hiç siparişiniz yok</h3>
            <p className="text-slate-500 mb-6">Alışverişe başlayarak ilk siparişinizi oluşturabilirsiniz.</p>
          </div>
        ) : (
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-slate-600">
                <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase font-semibold">
                  <tr>
                    <th className="px-6 py-4">Sipariş Numarası</th>
                    <th className="px-6 py-4">Tarih</th>
                    <th className="px-6 py-4">Durum</th>
                    <th className="px-6 py-4">Toplam Tutar</th>
                    <th className="px-6 py-4">Teslimat</th>
                    <th className="px-6 py-4 text-right">İşlem</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {orders.map((order) => (
                    <tr key={order.orderNumber} className="hover:bg-slate-50 transition-colors">
                      <td className="px-6 py-5 font-mono text-xs text-slate-500">{order.orderNumber}</td>
                      <td className="px-6 py-5 whitespace-nowrap">{formatDate(order.createdAt)}</td>
                      <td className="px-6 py-5">{getStatusBadge(order.status)}</td>
                      <td className="px-6 py-5 font-bold text-slate-900 text-base">{order.totalPrice} TL</td>
                      <td className="px-6 py-5">
                        <div className="flex items-center gap-2 max-w-[200px]">
                          <MapPin className="w-4 h-4 text-slate-400 shrink-0" />
                          <span className="truncate" title={order.shippingAddress}>{order.shippingAddress || "Belirtilmedi"}</span>
                        </div>
                      </td>
                      <td className="px-6 py-5 text-right">
                        <Button 
                          variant="outline" 
                          size="sm" 
                          className="border-orange-200 text-orange-600 hover:bg-orange-50 hover:border-orange-300"
                          onClick={() => setSelectedOrder(order)}
                        >
                          Detay
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>

      {/* Sipariş Detay Modalı */}
      {selectedOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4 bg-slate-50">
              <div>
                <h3 className="text-lg font-bold text-slate-800">Sipariş Detayı</h3>
                <p className="text-xs font-mono text-slate-500 mt-1">{selectedOrder.orderNumber}</p>
              </div>
              <button 
                onClick={() => setSelectedOrder(null)}
                className="p-2 text-slate-400 hover:text-slate-700 hover:bg-slate-200 rounded-full transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="p-6 max-h-[70vh] overflow-y-auto">
              {/* Uyarılar (Eğer hata varsa) */}
              {selectedOrder.status === "FAILED" && selectedOrder.paymentFailureReason && (
                <div className="mb-6 p-4 bg-red-50 border border-red-100 rounded-xl flex gap-3 text-red-700">
                  <AlertCircle className="w-5 h-5 shrink-0" />
                  <div>
                    <strong className="block text-sm">Ödeme Alınamadı</strong>
                    <span className="text-xs">{selectedOrder.paymentFailureReason}</span>
                  </div>
                </div>
              )}

              {/* Müşteri ve Teslimat Bilgileri */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-8">
                <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                  <h4 className="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2 border-b border-slate-200 pb-2">
                    <User className="w-4 h-4 text-orange-500" /> Alıcı Bilgileri
                  </h4>
                  <div className="space-y-2 text-sm">
                    <div className="flex items-center gap-2 text-slate-600">
                      <span className="font-medium">{selectedOrder.receiverName}</span>
                    </div>
                    <div className="flex items-center gap-2 text-slate-600">
                      <Phone className="w-3.5 h-3.5 text-slate-400" />
                      <span>{selectedOrder.phoneNumber}</span>
                    </div>
                  </div>
                </div>
                
                <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                  <h4 className="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2 border-b border-slate-200 pb-2">
                    <MapPin className="w-4 h-4 text-orange-500" /> Teslimat Adresi
                  </h4>
                  <p className="text-sm text-slate-600 leading-relaxed">
                    {selectedOrder.shippingAddress}
                  </p>
                </div>
              </div>

              {/* Ürün Listesi */}
              <h4 className="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2">
                <Package className="w-4 h-4 text-orange-500" /> Sipariş İçeriği
              </h4>
              <div className="border border-slate-100 rounded-xl overflow-hidden mb-6">
                <ul className="divide-y divide-slate-100">
                  {selectedOrder.items?.map((item) => (
                    <li key={item.productId} className="p-4 flex items-center justify-between hover:bg-slate-50 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className="w-12 h-12 bg-white border border-slate-200 rounded-lg flex items-center justify-center text-xl">
                          🛍️
                        </div>
                        <div>
                          <p className="font-medium text-slate-800 text-sm">Ürün ID: {item.productId}</p>
                          <p className="text-xs text-slate-500">Adet: {item.quantity}</p>
                        </div>
                      </div>
                      <span className="font-bold text-slate-800">{item.price * item.quantity} TL</span>
                    </li>
                  ))}
                  {(!selectedOrder.items || selectedOrder.items.length === 0) && (
                    <li className="p-4 text-center text-sm text-slate-500">Ürün detayı bulunamadı.</li>
                  )}
                </ul>
              </div>

              {/* Alt Toplam */}
              <div className="flex justify-between items-center bg-slate-50 p-4 rounded-xl border border-slate-200">
                <div>
                  <p className="text-sm font-medium text-slate-600">Sipariş Durumu: {getStatusBadge(selectedOrder.status)}</p>
                  <p className="text-xs text-slate-400 mt-1 flex items-center gap-1"><Calendar className="w-3 h-3"/> {formatDate(selectedOrder.createdAt)}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-slate-500">Genel Toplam</p>
                  <p className="text-2xl font-bold text-orange-500">{selectedOrder.totalPrice} TL</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
