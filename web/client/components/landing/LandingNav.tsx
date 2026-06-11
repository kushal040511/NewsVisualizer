"use client";

import Link from "next/link";
import { motion, useScroll, useMotionValueEvent } from "framer-motion";
import { useState } from "react";

const links = [
  { label: "Modules", href: "#modules" },
  { label: "Signal", href: "#signal" },
];

export default function LandingNav() {
  const { scrollY } = useScroll();
  const [scrolled, setScrolled] = useState(false);

  useMotionValueEvent(scrollY, "change", (y) => {
    setScrolled(y > 32);
  });

  return (
    <motion.header
      initial={{ y: -80, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.8, delay: 0.2, ease: [0.16, 1, 0.3, 1] }}
      className={`fixed inset-x-0 top-0 z-50 transition-all duration-500 ${
        scrolled ? "glass" : "border-b border-transparent"
      }`}
    >
      <nav className="mx-auto flex h-16 max-w-7xl items-center justify-between px-6 md:px-10">
        <Link
          href="/"
          className="font-display text-lg font-bold tracking-[0.25em] text-snow"
        >
          NEWSVIZ
          <span className="ml-1 inline-block h-1.5 w-1.5 rounded-full bg-accent align-middle animate-pulse-dot" />
        </Link>

        <div className="hidden items-center gap-8 md:flex">
          {links.map((l) => (
            <a
              key={l.href}
              href={l.href}
              className="font-mono text-xs uppercase tracking-[0.2em] text-fog transition-colors duration-200 hover:text-accent"
            >
              {l.label}
            </a>
          ))}
        </div>

        <Link
          href="/dashboard"
          className="rounded-full border border-line px-5 py-2 font-mono text-xs uppercase tracking-[0.15em] text-snow transition-all duration-300 hover:border-accent/70 hover:text-accent hover:shadow-glow"
        >
          Launch Console
        </Link>
      </nav>
    </motion.header>
  );
}
