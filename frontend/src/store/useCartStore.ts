import { create } from 'zustand';
import { fetchWithAuth } from '@/lib/fetcher';

export interface CartItem {
  productId: number;
  quantity: number;
  price: number;
  name?: string; 
}

export interface Cart {
  id: string;
  items: CartItem[];
  totalPrice: number;
}

interface CartStore {
  cart: Cart | null;
  isLoading: boolean;
  fetchCart: () => Promise<void>;
  addToCart: (productId: number, quantity: number) => Promise<void>;
  decrementFromCart: (productId: number) => Promise<void>;
  removeFromCart: (productId: number) => Promise<void>;
  clearCart: () => Promise<void>;
}

export const useCartStore = create<CartStore>((set) => ({
  cart: null,
  isLoading: false,

  fetchCart: async () => {
    set({ isLoading: true });
    try {
      const res = await fetchWithAuth("/carts");
      if (res.ok) {
        const json = await res.json();
        set({ cart: json.data });
      }
    } catch (error) {
      console.error("Sepet çekilemedi", error);
    } finally {
      set({ isLoading: false });
    }
  },

  addToCart: async (productId, quantity) => {
    try {
      const res = await fetchWithAuth("/carts/items", {
        method: "POST",
        body: JSON.stringify({ productId, quantity }),
      });
      
      let json;
      try {
        json = await res.json();
      } catch (err) {
        throw new Error(`HTTP Error ${res.status}`);
      }
      
      if (res.ok) {
        set({ cart: json.data });
        // Başarılı olduğunda kullanıcıya hissettir
        alert("🛒 Ürün sepete başarıyla eklendi!");
      } else {
        // Backend'in "Bu ürün için yeterli stok bulunmuyor!" mesajı buradan yakalanacak
        alert("❌ Sepete Eklenemedi: " + (json.message || "Bilinmeyen bir hata oluştu."));
      }
    } catch (error: any) {
      console.error("Sepete eklenemedi", error);
      alert("❌ Sunucu ile iletişim kurulamadı! Detay: " + error.message);
    }
  },

  decrementFromCart: async (productId) => {
    try {
      const res = await fetchWithAuth(`/carts/items/${productId}/decrement`, {
        method: "PATCH",
      });

      const json = await res.json();

      if (res.ok) {
        set({ cart: json.data });
      } else {
        alert("❌ Adet Azaltma Başarısız: " + (json.message || "Bilinmeyen hata."));
      }
    } catch (error) {
      console.error("Adet azaltılamadı", error);
    }
  },

  removeFromCart: async (productId) => {
    try {
      const res = await fetchWithAuth(`/carts/items/${productId}`, {
        method: "DELETE",
      });
      
      const json = await res.json();
      
      if (res.ok) {
        set({ cart: json.data });
      } else {
        alert("❌ Çıkarma İşlemi Başarısız: " + (json.message || "Bilinmeyen hata."));
      }
    } catch (error) {
      console.error("Sepetten çıkarılamadı", error);
    }
  },

  clearCart: async () => {
    try {
      const res = await fetchWithAuth("/carts", {
        method: "DELETE",
      });
      if (res.ok) {
        set({ cart: null });
        alert("🗑️ Sepet tamamen temizlendi.");
      }
    } catch (error) {
      console.error("Sepet temizlenemedi", error);
    }
  }
}));
