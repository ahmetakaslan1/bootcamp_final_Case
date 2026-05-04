"use client";

import { useEffect } from "react";
import { signIn, signOut, useSession } from "next-auth/react";
import { Button } from "@/components/ui/button";
import { useCartStore } from "@/store/useCartStore";
import { ShoppingCart } from "lucide-react";
import Link from "next/link";
import { useTranslation } from "react-i18next";

export default function Navbar() {
  const { t, i18n } = useTranslation();
  const { data: session, status } = useSession();
  const { cart, fetchCart } = useCartStore();

  // Oturum açıldığında ilk in mevcut sepetini getir
  useEffect(() => {
    if (session?.accessToken) {
      fetchCart();
    }
  }, [session, fetchCart]);

  // Sepetteki toplam ürün sayısı
  const itemCount = cart?.items?.reduce((acc, item) => acc + item.quantity, 0) || 0;

  return (
    <nav className="w-full bg-white shadow-sm px-6 py-4 flex justify-between items-center sticky top-0 z-50">
      <Link href="/">
        <h1 className="text-2xl font-bold text-slate-800 cursor-pointer">
          {t('navbar.title').split(' ')[0]} <span className="text-orange-500">{t('navbar.title').split(' ')[1] || 'Vitrin'}</span>
        </h1>
      </Link>
      
      <div className="flex items-center gap-6">
        {/* Language Switcher */}
        <div className="flex space-x-2 text-sm font-semibold">
          <button 
            onClick={() => i18n.changeLanguage('tr')}
            className={`transition-colors ${i18n.language === 'tr' ? 'text-orange-500' : 'text-slate-400 hover:text-slate-700'}`}
          >
            TR
          </button>
          <span className="text-slate-300">|</span>
          <button 
            onClick={() => i18n.changeLanguage('en')}
            className={`transition-colors ${i18n.language === 'en' ? 'text-orange-500' : 'text-slate-400 hover:text-slate-700'}`}
          >
            EN
          </button>
        </div>
        {/* Sepet İkonu ve Rozeti */}
        <Link href="/cart">
          <div className="relative cursor-pointer hover:bg-slate-50 p-2 rounded-full transition-colors">
            <ShoppingCart className="w-6 h-6 text-slate-700" />
            {itemCount > 0 && (
              <span className="absolute top-0 right-0 -mt-1 -mr-1 flex h-5 w-5 items-center justify-center rounded-full bg-orange-500 text-[10px] font-bold text-white shadow-sm ring-2 ring-white">
                {itemCount}
              </span>
            )}
          </div>
        </Link>

        {/* Kullanıcı Giriş Durumu */}
        {status === "loading" ? (
          <div className="h-10 w-24 bg-slate-100 animate-pulse rounded-md"></div>
        ) : session ? (
          <div className="flex items-center gap-4 border-l pl-6 border-slate-200">
            {session.user?.roles?.includes("ADMIN") && (
              <Link href="/admin" className="text-sm font-bold text-orange-500 hover:text-orange-600 transition-colors bg-orange-50 px-3 py-1 rounded-full">
                Admin Panel
              </Link>
            )}
            <Link href="/orders" className="text-sm font-medium text-slate-600 hover:text-orange-500 transition-colors">
              {t('navbar.orders')}
            </Link>
            <span className="text-sm font-medium text-slate-600 hidden sm:block border-l pl-4 border-slate-200">
              {t('navbar.welcome').replace('{name}', '')} <span className="font-bold text-slate-800">{session.user?.name}</span>
            </span>
            <Button onClick={() => signOut()} variant="outline" className="border-slate-200 text-black/40 font-medium hover:text-black/60 transition-color">
              {t('navbar.logout')}
            </Button>
          </div>
        ) : (
          <div className="border-l pl-6 border-slate-200">
            <Button onClick={() => signIn("keycloak")} className="bg-slate-800 hover:bg-slate-900 text-white shadow-md transition-all">
              {t('navbar.login')}
            </Button>
          </div>
        )}
      </div>
    </nav>
  );
}
