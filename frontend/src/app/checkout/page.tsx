"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useSession, signIn } from "next-auth/react";
import Link from "next/link";
import Navbar from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { useCartStore } from "@/store/useCartStore";
import { fetchWithAuth } from "@/lib/fetcher";
import { useTranslation } from "react-i18next";

interface OrderSummary {
  orderNumber: string;
  status: string;
  paymentFailureReason?: string | null;
}

interface CheckoutForm {
  shippingAddress: string;
  receiverName: string;
  phoneNumber: string;
  cardHolderName: string;
  cardNumber: string;
  expireMonth: string;
  expireYear: string;
  cvc: string;
}

const initialForm: CheckoutForm = {
  shippingAddress: "",
  receiverName: "",
  phoneNumber: "",
  cardHolderName: "",
  cardNumber: "",
  expireMonth: "",
  expireYear: "",
  cvc: "",
};

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export default function CheckoutPage() {
  const { t } = useTranslation();
  const { status, data: session } = useSession();
  const { cart, fetchCart } = useCartStore();
  const [form, setForm] = useState<CheckoutForm>(initialForm);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [createdOrderNumber, setCreatedOrderNumber] = useState<string | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  useEffect(() => {
    if (session?.accessToken) {
      fetchCart();
    }
  }, [session, fetchCart]);

  const hasItems = (cart?.items?.length ?? 0) > 0;
  const totalItems = useMemo(() => cart?.items?.reduce((sum, item) => sum + item.quantity, 0) ?? 0, [cart]);

  const fetchOrderStatus = async (orderNumber: string): Promise<OrderSummary | null> => {
    const response = await fetchWithAuth("/orders");
    if (!response.ok) {
      return null;
    }
    const json = await response.json();
    const orders = (json?.data ?? []) as OrderSummary[];
    return orders.find((order) => order.orderNumber === orderNumber) ?? null;
  };

  const waitForOrderCompletion = async (
    orderNumber: string
  ): Promise<{ completed: boolean; failedReason?: string }> => {
    for (let attempt = 0; attempt < 8; attempt++) {
      const order = await fetchOrderStatus(orderNumber);
      if (order?.status === "COMPLETED") {
        return { completed: true };
      }
      if (order?.status === "FAILED") {
        return {
          completed: false,
          failedReason: order.paymentFailureReason || "Ödeme başarısız oldu.",
        };
      }
      await sleep(1500);
    }
    return { completed: false };
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSuccessMessage(null);
    setCreatedOrderNumber(null);
    setStatusMessage(null);

    if (!hasItems) {
      setError("Sepetiniz boş olduğu için ödeme başlatılamaz.");
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await fetchWithAuth("/orders", {
        method: "POST",
        body: JSON.stringify(form),
      });

      const json = await response.json();
      if (!response.ok) {
        setError(json.message || "Sipariş sırasında bir hata oluştu.");
        return;
      }

      const orderNumber = json?.data?.orderNumber as string | undefined;
      setCreatedOrderNumber(orderNumber || null);
      setStatusMessage("Sipariş alındı, ödeme sonucu doğrulanıyor...");

      if (orderNumber) {
        const result = await waitForOrderCompletion(orderNumber);
        if (result.completed) {
          setSuccessMessage("Ödeme onaylandı, siparişiniz tamamlandı.");
          setStatusMessage(null);
          setForm(initialForm);
          await fetchCart();
        } else {
          setError(result.failedReason || "Ödeme henüz onaylanmadı veya başarısız oldu. Lütfen tekrar deneyin.");
          setStatusMessage(null);
        }
      } else {
        setSuccessMessage(json.message || "Siparişiniz alındı.");
      }
    } catch (requestError) {
      console.error("Checkout error:", requestError);
      setError("Sunucu ile iletişim kurulamadı. Lütfen tekrar deneyin.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (status === "loading") {
    return (
      <div className="min-h-screen bg-slate-100">
        <Navbar />
        <div className="container mx-auto px-6 py-12 text-center text-slate-500">Yükleniyor...</div>
      </div>
    );
  }

  if (status === "unauthenticated") {
    return (
      <div className="min-h-screen bg-slate-50">
        <Navbar />
        <div className="container mx-auto px-6 py-20 text-center">
          <h2 className="text-2xl font-bold mb-4">Ödeme yapabilmek için giriş yapmalısınız</h2>
          <Button onClick={() => signIn("keycloak")} className="bg-orange-500">Giriş Yap</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main className="container mx-auto px-6 py-12 max-w-6xl text-slate-900">
        <h2 className="text-3xl font-bold text-slate-800 mb-8">{t('checkout.title')}</h2>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <form onSubmit={handleSubmit} className="lg:col-span-2 bg-white rounded-2xl shadow-sm border border-slate-200 p-6 space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-slate-800 mb-3">{t('checkout.delivery_info')}</h3>
              <div className="space-y-4">
                <input
                  required
                  value={form.receiverName}
                  onChange={(e) => setForm((prev) => ({ ...prev, receiverName: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 p-3 outline-none text-slate-900 placeholder:text-slate-500 focus:ring-2 focus:ring-orange-300"
                  placeholder="Alıcı Adı Soyadı"
                />
                <input
                  required
                  type="tel"
                  value={form.phoneNumber}
                  onChange={(e) => setForm((prev) => ({ ...prev, phoneNumber: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 p-3 outline-none text-slate-900 placeholder:text-slate-500 focus:ring-2 focus:ring-orange-300"
                  placeholder="Telefon Numarası (Örn: 05551234567)"
                />
                <textarea
                  required
                  rows={3}
                  value={form.shippingAddress}
                  onChange={(e) => setForm((prev) => ({ ...prev, shippingAddress: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 p-3 outline-none text-slate-900 placeholder:text-slate-500 focus:ring-2 focus:ring-orange-300"
                  placeholder="Açık Adresinizi Girin"
                />
              </div>
            </div>

            <div>
              <h3 className="text-lg font-semibold text-slate-800 mb-3">{t('checkout.card_info')}</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <input
                  required
                  value={form.cardHolderName}
                  onChange={(e) => setForm((prev) => ({ ...prev, cardHolderName: e.target.value }))}
                  className="rounded-lg border border-slate-300 p-3 outline-none text-slate-900 placeholder:text-slate-500 focus:ring-2 focus:ring-orange-300"
                  placeholder="Kart üzerindeki isim"
                />
                <input
                  required
                  inputMode="numeric"
                  value={form.cardNumber}
                  onChange={(e) => setForm((prev) => ({ ...prev, cardNumber: e.target.value.replace(/\D/g, "") }))}
                  className="rounded-lg border border-slate-300 p-3 outline-none text-slate-900 placeholder:text-slate-500 focus:ring-2 focus:ring-orange-300"
                  placeholder="Kart numarası"
                  maxLength={19}
                />
                <input
                  required
                  inputMode="numeric"
                  value={form.expireMonth}
                  onChange={(e) => setForm((prev) => ({ ...prev, expireMonth: e.target.value.replace(/\D/g, "").slice(0, 2) }))}
                  className="rounded-lg border border-slate-300 p-3 outline-none text-slate-900 placeholder:text-slate-500 focus:ring-2 focus:ring-orange-300"
                  placeholder="Ay (MM)"
                  maxLength={2}
                />
                <input
                  required
                  inputMode="numeric"
                  value={form.expireYear}
                  onChange={(e) => setForm((prev) => ({ ...prev, expireYear: e.target.value.replace(/\D/g, "").slice(0, 4) }))}
                  className="rounded-lg border border-slate-300 p-3 outline-none text-slate-900 placeholder:text-slate-500 focus:ring-2 focus:ring-orange-300"
                  placeholder="Yıl (YYYY)"
                  maxLength={4}
                />
                <input
                  required
                  inputMode="numeric"
                  value={form.cvc}
                  onChange={(e) => setForm((prev) => ({ ...prev, cvc: e.target.value.replace(/\D/g, "").slice(0, 4) }))}
                  className="rounded-lg border border-slate-300 p-3 outline-none text-slate-900 placeholder:text-slate-500 focus:ring-2 focus:ring-orange-300 md:col-span-2"
                  placeholder="CVC"
                  maxLength={4}
                />
              </div>
            </div>

            {error ? <p className="text-sm text-red-600">{error}</p> : null}
            {statusMessage ? <p className="text-sm text-slate-700">{statusMessage}</p> : null}
            {successMessage ? (
              <div className="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-700">
                <p>{successMessage}</p>
                {createdOrderNumber ? <p className="mt-1 font-medium">Sipariş No: {createdOrderNumber}</p> : null}
              </div>
            ) : null}

            <div className="flex flex-col sm:flex-row gap-3 pt-2">
              <Button type="submit" disabled={isSubmitting || !hasItems} className="bg-orange-500 hover:bg-orange-600 h-11">
                {isSubmitting ? "Ödeme İşleniyor..." : t('checkout.complete_payment')}
              </Button>
              <Link href="/cart">
                <Button type="button" variant="outline" className="h-11 w-full sm:w-auto">{t('checkout.back_to_cart')}</Button>
              </Link>
            </div>
          </form>

          <aside className="lg:col-span-1">
            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 sticky top-24">
              <h3 className="text-lg font-bold text-slate-800 border-b border-slate-100 pb-4 mb-4">{t('checkout.order_summary')}</h3>
              <div className="space-y-2 text-sm text-slate-800">
                <div className="flex justify-between">
                  <span className="text-slate-700">{t('cart.quantity')}</span>
                  <span className="font-medium">{totalItems}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-700">{t('checkout.subtotal')}</span>
                  <span className="font-medium">{cart?.totalPrice ?? 0} TL</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-700">{t('checkout.shipping')}</span>
                  <span className="font-medium text-green-600">{t('checkout.free')}</span>
                </div>
              </div>
              <div className="flex justify-between items-center border-t border-slate-100 pt-4 mt-6">
                <span className="font-bold text-lg">{t('checkout.total')}</span>
                <span className="font-bold text-2xl text-orange-500">{cart?.totalPrice ?? 0} TL</span>
              </div>
              {!hasItems ? (
                <p className="text-sm text-slate-500 mt-4">Sipariş oluşturmak için sepete ürün ekleyin.</p>
              ) : null}
            </div>
          </aside>
        </div>
      </main>
    </div>
  );
}
