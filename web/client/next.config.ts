import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    // Dev proxies to the local Python server; production proxies to the
    // deployed Python API project on Vercel.
    const backend =
      process.env.NODE_ENV === "development"
        ? "http://localhost:8081"
        : "https://newsvisualizer-api.vercel.app";
    return [
      {
        source: "/api/:path*",
        destination: `${backend}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
