import type { NextAuthOptions } from "next-auth";
import KeycloakProvider from "next-auth/providers/keycloak";

export const authOptions: NextAuthOptions = {
  providers: [
    KeycloakProvider({
      clientId: process.env.KEYCLOAK_CLIENT_ID || "",
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET || "",
      issuer: process.env.KEYCLOAK_ISSUER || "",
    }),
  ],
  callbacks: {
    async jwt({ token, account }) {
      if (account) {
        token.accessToken = account.access_token;
        try {
          if (account.access_token) {
            const base64Payload = account.access_token.split('.')[1];
            const payloadBuffer = Buffer.from(base64Payload, 'base64');
            const payload = JSON.parse(payloadBuffer.toString('utf-8'));
            token.roles = payload.realm_access?.roles || [];
          }
        } catch (error) {
          console.error("Error parsing JWT payload:", error);
          token.roles = [];
        }
      }
      return token;
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken as string;
      session.user = {
        ...session.user,
        roles: (token.roles as string[]) || [],
      } as any;
      return session;
    },
  },
  secret: process.env.NEXTAUTH_SECRET,
};
