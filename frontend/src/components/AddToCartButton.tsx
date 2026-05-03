"use client";

import { Button } from "@/components/ui/button";
import { useCartStore } from "@/store/useCartStore";
import { useSession, signIn } from "next-auth/react";
import { ShoppingBag } from "lucide-react";
import { useTranslation } from "react-i18next";

export default function AddToCartButton({ productId }: { productId: number }) {
  const { t } = useTranslation();
  const { data: session } = useSession();
  const { addToCart } = useCartStore();

  const handleAdd = () => {
    if (!session) {
      // Kullanıcı giriş yapmamışsa Keycloak login'e gönder
      signIn("keycloak");
      return;
    }
    // Giriş yapmışsa Backend'deki sepete 1 adet ekle
    addToCart(productId, 1);
  };

  return (
    <Button 
      onClick={handleAdd} 
      className="w-full bg-orange-500 hover:bg-orange-600 text-white mt-4 font-semibold transition-all shadow-sm"
    >
      <ShoppingBag className="w-4 h-4 mr-2" />
      {t('cart.add_to_cart')}
    </Button>
  );
}
