/** @type {import('next').NextConfig} */
const nextConfig = {
  async headers() {
    return [
      {
        // AASA は application/json で配信しないと Apple CDN が無視する。
        source: "/.well-known/apple-app-site-association",
        headers: [{ key: "Content-Type", value: "application/json" }],
      },
      {
        source: "/auth/callback",
        headers: [
          { key: "Referrer-Policy", value: "no-referrer" },
          { key: "Cache-Control", value: "no-store" },
          { key: "X-Robots-Tag", value: "noindex, nofollow" },
        ],
      },
    ];
  },
};
module.exports = nextConfig;
