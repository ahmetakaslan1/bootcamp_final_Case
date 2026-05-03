/** @type {import('next').NextConfig} */
const nextConfig = {
    async rewrites() {
        return [
            {
                source: '/api/v1/:path*',
                destination: `${process.env.API_GATEWAY_URL || 'http://localhost:8080'}/api/v1/:path*`
            }
        ];
    },
    output: 'standalone'
};

export default nextConfig;
