"use client";

import Link from "next/link";
import { usePathname, useSearchParams } from "next/navigation";

interface PaginationProps {
  totalPages: number;
  currentPage: number; // 0-indexed from backend
}

export default function Pagination({ totalPages, currentPage }: PaginationProps) {
  const pathname = usePathname();
  const searchParams = useSearchParams();

  if (totalPages <= 1) return null;

  const createPageURL = (pageNumber: number) => {
    const params = new URLSearchParams(searchParams);
    params.set("page", pageNumber.toString());
    return `${pathname}?${params.toString()}`;
  };

  return (
    <div className="flex items-center justify-center space-x-2 mt-12">
      {/* Önceki Sayfa Butonu */}
      <Link
        href={createPageURL(currentPage - 1)}
        className={`px-4 py-2 border rounded-md transition-colors ${
          currentPage <= 0
            ? "pointer-events-none opacity-50 bg-slate-100 text-slate-400"
            : "bg-white text-slate-700 hover:bg-slate-50 hover:text-orange-600 border-slate-200"
        }`}
        aria-disabled={currentPage <= 0}
      >
        Önceki
      </Link>

      {/* Sayfa Numaraları */}
      <div className="flex space-x-1">
        {Array.from({ length: totalPages }, (_, i) => i).map((pageIndex) => {
          const isCurrentPage = pageIndex === currentPage;
          return (
            <Link
              key={pageIndex}
              href={createPageURL(pageIndex)}
              className={`w-10 h-10 flex items-center justify-center rounded-md border transition-colors ${
                isCurrentPage
                  ? "bg-orange-500 text-white border-orange-500 pointer-events-none shadow-sm"
                  : "bg-white text-slate-700 hover:bg-slate-50 hover:text-orange-600 border-slate-200"
              }`}
            >
              {pageIndex + 1}
            </Link>
          );
        })}
      </div>

      {/* Sonraki Sayfa Butonu */}
      <Link
        href={createPageURL(currentPage + 1)}
        className={`px-4 py-2 border rounded-md transition-colors ${
          currentPage >= totalPages - 1
            ? "pointer-events-none opacity-50 bg-slate-100 text-slate-400"
            : "bg-white text-slate-700 hover:bg-slate-50 hover:text-orange-600 border-slate-200"
        }`}
        aria-disabled={currentPage >= totalPages - 1}
      >
        Sonraki
      </Link>
    </div>
  );
}
