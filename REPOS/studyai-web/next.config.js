/** @type {import('next').NextConfig} */
const nextConfig = {
  async headers() {
    return [
      {
        // AASA は application/json で配信しないと Apple CDN が無視する。
        source: "/.well-known/apple-app-site-association",
        headers: [{ key: "Content-Type", value: "application/json" }],
      },
    ];
  },
};
module.exports = nextConfig;
