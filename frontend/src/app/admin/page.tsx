"use client";

import { useState, useEffect } from "react";
import { fetchWithAuth } from "@/lib/fetcher";
import { Plus, Edit, Trash2, Box, Package } from "lucide-react";
import { Button } from "@/components/ui/button";

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl: string;
  stockQuantity: number | null;
}

export default function AdminProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [isProductModalOpen, setIsProductModalOpen] = useState(false);
  const [isStockModalOpen, setIsStockModalOpen] = useState(false);
  
  const [currentProduct, setCurrentProduct] = useState<Partial<Product>>({});
  const [stockUpdateQuantity, setStockUpdateQuantity] = useState<number>(0);

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    setLoading(true);
    try {
      const res = await fetchWithAuth("/products?size=100");
      if (res.ok) {
        const json = await res.json();
        setProducts(json.data?.content || []);
      }
    } catch (error) {
      console.error("Failed to load products", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    const isEditing = !!currentProduct.id;
    const method = isEditing ? "PUT" : "POST";
    const url = isEditing ? `/products/${currentProduct.id}` : `/products`;

    try {
      const res = await fetchWithAuth(url, {
        method,
        body: JSON.stringify({
          name: currentProduct.name,
          description: currentProduct.description,
          price: currentProduct.price,
          category: currentProduct.category,
          imageUrl: currentProduct.imageUrl
        }),
      });

      if (res.ok) {
        alert(`Ürün başarıyla ${isEditing ? 'güncellendi' : 'eklendi'}`);
        setIsProductModalOpen(false);
        loadProducts();
      } else {
        const err = await res.json();
        alert("Hata: " + (err.message || "İşlem başarısız"));
      }
    } catch (error) {
      alert("Hata: Sunucu ile iletişim kurulamadı.");
    }
  };

  const handleDeleteProduct = async (id: number) => {
    if (!confirm("Bu ürünü silmek istediğinize emin misiniz?")) return;

    try {
      const res = await fetchWithAuth(`/products/${id}`, { method: "DELETE" });
      if (res.ok) {
        alert("Ürün başarıyla silindi");
        loadProducts();
      } else {
        alert("Silme işlemi başarısız");
      }
    } catch (error) {
      alert("Hata oluştu");
    }
  };

  const handleUpdateStock = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentProduct.id) return;

    try {
      const res = await fetchWithAuth(`/inventory`, {
        method: "POST",
        body: JSON.stringify({
          productId: currentProduct.id,
          quantity: stockUpdateQuantity,
        }),
      });

      if (res.ok) {
        alert("Stok başarıyla güncellendi");
        setIsStockModalOpen(false);
        loadProducts(); // Reload to get updated stock if possible, or fetch individual
      } else {
        const err = await res.json();
        alert("Hata: " + (err.message || "İşlem başarısız"));
      }
    } catch (error) {
      alert("Hata: Sunucu ile iletişim kurulamadı.");
    }
  };

  const openNewProductModal = () => {
    setCurrentProduct({ category: "Elektronik", price: 0 });
    setIsProductModalOpen(true);
  };

  const openEditProductModal = (product: Product) => {
    setCurrentProduct(product);
    setIsProductModalOpen(true);
  };

  const openStockModal = async (product: Product) => {
    setCurrentProduct(product);
    setStockUpdateQuantity(0);
    // Fetch current stock from inventory service
    try {
      const res = await fetchWithAuth(`/inventory/${product.id}`);
      if (res.ok) {
        const json = await res.json();
        setStockUpdateQuantity(json.data?.quantity || 0);
      }
    } catch (error) {
      console.error("Could not fetch stock", error);
    }
    setIsStockModalOpen(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center bg-white p-6 rounded-xl shadow-sm border border-slate-100">
        <div>
          <h2 className="text-xl font-bold text-slate-800">Ürün Listesi</h2>
          <p className="text-sm text-slate-500">Tüm ürünlerinizi buradan yönetebilir, stok güncelleyebilirsiniz.</p>
        </div>
        <Button onClick={openNewProductModal} className="bg-orange-500 hover:bg-orange-600 text-white flex items-center gap-2">
          <Plus className="w-4 h-4" /> Yeni Ürün Ekle
        </Button>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-slate-500">Yükleniyor...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 text-slate-600 text-sm border-b border-slate-100">
                  <th className="p-4 font-semibold">ID</th>
                  <th className="p-4 font-semibold">Görsel</th>
                  <th className="p-4 font-semibold">Ürün Adı</th>
                  <th className="p-4 font-semibold">Kategori</th>
                  <th className="p-4 font-semibold">Fiyat</th>
                  <th className="p-4 font-semibold text-right">İşlemler</th>
                </tr>
              </thead>
              <tbody>
                {products.map((p) => (
                  <tr key={p.id} className="border-b border-slate-50 hover:bg-slate-50/50 transition-colors">
                    <td className="p-4 text-slate-500">#{p.id}</td>
                    <td className="p-4">
                      <div className="w-12 h-12 rounded bg-slate-100 flex items-center justify-center overflow-hidden">
                        {p.imageUrl ? (
                          <img src={p.imageUrl} alt={p.name} className="w-full h-full object-cover" />
                        ) : (
                          <Package className="w-5 h-5 text-slate-300" />
                        )}
                      </div>
                    </td>
                    <td className="p-4">
                      <div className="font-medium text-slate-800">{p.name}</div>
                    </td>
                    <td className="p-4 text-slate-600">{p.category}</td>
                    <td className="p-4 font-semibold text-slate-800">{p.price} TL</td>
                    <td className="p-4 text-right space-x-2">
                      <Button variant="outline" size="sm" onClick={() => openStockModal(p)} className="text-blue-600 border-blue-200 hover:bg-blue-50">
                        <Box className="w-4 h-4 mr-1" /> Stok
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => openEditProductModal(p)} className="text-slate-600 hover:text-slate-900">
                        <Edit className="w-4 h-4" />
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => handleDeleteProduct(p.id)} className="text-red-500 border-red-200 hover:bg-red-50">
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </td>
                  </tr>
                ))}
                {products.length === 0 && (
                  <tr>
                    <td colSpan={6} className="p-8 text-center text-slate-500">Kayıtlı ürün bulunamadı.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Product Modal */}
      {isProductModalOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center">
              <h3 className="text-xl font-bold text-slate-800">{currentProduct.id ? 'Ürünü Düzenle' : 'Yeni Ürün Ekle'}</h3>
              <button onClick={() => setIsProductModalOpen(false)} className="text-slate-400 hover:text-slate-600">&times;</button>
            </div>
            <form onSubmit={handleSaveProduct} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Ürün Adı</label>
                <input required type="text" value={currentProduct.name || ''} onChange={e => setCurrentProduct({...currentProduct, name: e.target.value})} className="w-full p-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none" />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Açıklama</label>
                <textarea required value={currentProduct.description || ''} onChange={e => setCurrentProduct({...currentProduct, description: e.target.value})} className="w-full p-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none" rows={3}></textarea>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Fiyat (TL)</label>
                  <input required type="number" step="0.01" min="0" value={currentProduct.price || 0} onChange={e => setCurrentProduct({...currentProduct, price: parseFloat(e.target.value)})} className="w-full p-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Kategori</label>
                  <input required type="text" value={currentProduct.category || ''} onChange={e => setCurrentProduct({...currentProduct, category: e.target.value})} className="w-full p-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Görsel URL</label>
                <input type="text" value={currentProduct.imageUrl || ''} onChange={e => setCurrentProduct({...currentProduct, imageUrl: e.target.value})} className="w-full p-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" placeholder="https://..." />
              </div>
              <div className="pt-4 flex justify-end gap-3">
                <Button type="button" variant="outline" onClick={() => setIsProductModalOpen(false)}>İptal</Button>
                <Button type="submit" className="bg-orange-500 hover:bg-orange-600 text-white">Kaydet</Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Stock Modal */}
      {isStockModalOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm overflow-hidden">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center">
              <h3 className="text-lg font-bold text-slate-800">Stok Güncelle</h3>
              <button onClick={() => setIsStockModalOpen(false)} className="text-slate-400 hover:text-slate-600">&times;</button>
            </div>
            <form onSubmit={handleUpdateStock} className="p-6 space-y-4">
              <div>
                <p className="text-sm text-slate-500 mb-4"><strong>{currentProduct.name}</strong> için stok miktarını girin:</p>
                <label className="block text-sm font-medium text-slate-700 mb-1">Stok Adedi</label>
                <input required type="number" min="0" value={stockUpdateQuantity} onChange={e => setStockUpdateQuantity(parseInt(e.target.value))} className="w-full p-3 text-lg font-bold border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" />
              </div>
              <div className="pt-4 flex justify-end gap-3">
                <Button type="button" variant="outline" onClick={() => setIsStockModalOpen(false)}>İptal</Button>
                <Button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white">Stok Güncelle</Button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
