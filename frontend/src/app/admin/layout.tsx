"use client";

import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Link from "next/link";
import { useTranslation } from "react-i18next";
import { Package, ShieldAlert } from "lucide-react";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { data: session, status } = useSession();
  const router = useRouter();
  const { t } = useTranslation();
  const [isAuthorized, setIsAuthorized] = useState(false);

  useEffect(() => {
    if (status === "loading") return;

    if (!session) {
      router.push("/");
      return;
    }

    const roles = session?.user?.roles || [];
    if (!roles.includes("ADMIN")) {
      router.push("/");
    } else {
      setIsAuthorized(true);
    }
  }, [session, status, router]);

  if (status === "loading" || !isAuthorized) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="animate-pulse flex flex-col items-center">
          <ShieldAlert className="w-12 h-12 text-slate-300 mb-4" />
          <p className="text-slate-500 font-medium">Yetki kontrol ediliyor...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 flex">
      {/* Admin Sidebar */}
      <aside className="w-64 bg-slate-900 text-white flex flex-col">
        <div className="p-6 border-b border-slate-800">
          <h2 className="text-xl font-bold flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-orange-500" />
            N11 Admin
          </h2>
        </div>
        <nav className="flex-1 p-4">
          <ul className="space-y-2">
            <li>
              <Link
                href="/admin"
                className="flex items-center gap-3 px-4 py-3 rounded-lg bg-slate-800 text-orange-500 font-medium"
              >
                <Package className="w-5 h-5" />
                Ürün Yönetimi
              </Link>
            </li>
          </ul>
        </nav>
        <div className="p-4 border-t border-slate-800 text-sm text-slate-400">
          Giriş yapan: <br />
          <span className="font-semibold text-white">{session?.user?.name}</span>
        </div>
      </aside>

      {/* Admin Content */}
      <main className="flex-1 overflow-auto">
        {/* Admin Header */}
        <header className="bg-white shadow-sm px-8 py-4 flex justify-between items-center sticky top-0 z-10">
          <h1 className="text-2xl font-semibold text-slate-800">Yönetim Paneli</h1>
          <Link href="/" className="text-sm font-medium text-slate-500 hover:text-orange-500 transition-colors">
            &larr; Vitrine Dön
          </Link>
        </header>

        <div className="p-8">
          {children}
        </div>
      </main>
    </div>
  );
}
