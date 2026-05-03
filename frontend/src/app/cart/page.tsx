"use client";

import { useEffect } from "react";
import { useCartStore } from "@/store/useCartStore";
import Navbar from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { Trash2, CreditCard, Minus, Plus } from "lucide-react";
import Link from "next/link";
import { useSession, signIn } from "next-auth/react";
import { useTranslation } from "react-i18next";

export default function CartPage() {
  const { t } = useTranslation();
  const { cart, fetchCart, addToCart, decrementFromCart, removeFromCart, clearCart, isLoading } = useCartStore();
  const { status, data: session } = useSession();

  useEffect(() => {
    if (session?.accessToken) {
      fetchCart();
    }
  }, [session, fetchCart]);

  if (status === "loading") {
    return (
      <div className="min-h-screen bg-slate-50">
        <Navbar />
        <div className="container mx-auto px-6 py-12 text-center text-slate-500">Oturum açılıyor...</div>
      </div>
    );
  }

  if (status === "unauthenticated") {
    return (
      <div className="min-h-screen bg-slate-50">
        <Navbar />
        <div className="container mx-auto px-6 py-20 text-center">
          <h2 className="text-2xl font-bold mb-4">Sepetinizi Görmek İçin Giriş Yapmalısınız</h2>
          <Button onClick={() => signIn("keycloak")} className="bg-orange-500">Giriş Yap</Button>
        </div>
      </div>
    );
  }

  const hasItems = cart && cart.items && cart.items.length > 0;

  return (
    <div className="min-h-screen bg-slate-100">
      <Navbar />

      <main className="container mx-auto px-6 py-12 max-w-5xl text-slate-900">
        <h2 className="text-3xl font-bold text-slate-800 mb-8">{t('navbar.cart')}</h2>

        {isLoading ? (
          <p className="text-center text-slate-500 py-8">Sepet yükleniyor...</p>
        ) : null}

        {!isLoading && !hasItems ? (
          <div className="bg-white p-12 text-center rounded-2xl shadow-sm border border-slate-200">
            <h3 className="text-xl font-medium text-slate-600 mb-4">{t('checkout.empty_cart_warning')}</h3>
            <Link href="/">
              <Button className="bg-slate-800">{t('cart.products')}</Button>
            </Link>
          </div>
        ) : null}

        {!isLoading && hasItems ? (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Sol: Ürün Listesi */}
            <div className="lg:col-span-2 space-y-4">
              <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
                <div className="flex justify-between items-center border-b border-slate-200 pb-4 mb-4">
                  <span className="font-medium text-slate-800">{t('cart.products')}</span>
                  <Button variant="ghost" className="text-red-500 hover:text-red-600 hover:bg-red-50 h-8" onClick={() => clearCart()}>
                    <Trash2 className="w-4 h-4 mr-2" /> {t('cart.clear_cart')}
                  </Button>
                </div>
                
                {cart.items.map((item) => (
                  <div key={item.productId} className="flex justify-between items-center py-4 border-b border-slate-50 last:border-0">
                    <div className="flex items-center gap-4">
                      <div className="w-16 h-16 bg-slate-100 rounded-lg flex items-center justify-center">
                        📦
                      </div>
                      <div>
                        <h4 className="font-semibold text-slate-800">Ürün ID: {item.productId}</h4>
                        <p className="text-sm text-slate-700">{t('cart.quantity')}: {item.quantity}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-6">
                      <div className="flex items-center gap-2 border border-slate-300 rounded-lg p-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8 text-slate-900 hover:bg-slate-200"
                          onClick={() => decrementFromCart(item.productId)}
                        >
                          <Minus className="w-4 h-4" />
                        </Button>
                        <span className="w-7 text-center text-sm font-semibold">{item.quantity}</span>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8 text-slate-900 hover:bg-slate-200"
                          onClick={() => addToCart(item.productId, 1)}
                        >
                          <Plus className="w-4 h-4" />
                        </Button>
                      </div>
                      <span className="font-bold text-slate-900 whitespace-nowrap">{item.price * item.quantity} TL</span>
                      <Button variant="ghost" size="icon" className="text-slate-400 hover:text-red-500" onClick={() => removeFromCart(item.productId)}>
                        <Trash2 className="w-5 h-5" />
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Sağ: Sipariş Özeti */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 sticky top-24">
                <h3 className="text-lg font-bold text-slate-900 border-b border-slate-200 pb-4 mb-4">{t('checkout.order_summary')}</h3>
                
                <div className="flex justify-between items-center mb-2">
                  <span className="text-slate-700">{t('checkout.subtotal')}</span>
                  <span className="font-medium">{cart.totalPrice} TL</span>
                </div>
                <div className="flex justify-between items-center mb-6">
                  <span className="text-slate-700">{t('checkout.shipping')}</span>
                  <span className="text-green-500 font-medium">{t('checkout.free')}</span>
                </div>
                
                <div className="flex justify-between items-center border-t border-slate-200 pt-4 mb-8">
                  <span className="font-bold text-lg">{t('checkout.total')}</span>
                  <span className="font-bold text-2xl text-orange-500">{cart.totalPrice} TL</span>
                </div>
                
                <Link href="/checkout">
                  <Button className="w-full bg-orange-500 hover:bg-orange-600 h-12 text-lg">
                    <CreditCard className="w-5 h-5 mr-2" />
                    {t('cart.buy')}
                  </Button>
                </Link>
              </div>
            </div>
          </div>
        ) : null}
      </main>
    </div>
  );
}
