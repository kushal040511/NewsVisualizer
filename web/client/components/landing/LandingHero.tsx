"use client";

import dynamic from "next/dynamic";
import { motion, useScroll, useTransform } from "framer-motion";
import { useRef } from "react";
import MagneticButton from "@/components/ui/MagneticButton";

const CoreScene = dynamic(() => import("@/components/three/CoreScene"), {
  ssr: false,
  loading: () => (
    <div className="flex h-full w-full items-center justify-center">
      <span className="font-mono text-xs uppercase tracking-[0.3em] text-dim">
        Acquiring signal…
      </span>
    </div>
  ),
});

const ease = [0.16, 1, 0.3, 1] as const;

export default function LandingHero() {
  const ref = useRef<HTMLElement>(null);
  const { scrollYProgress } = useScroll({
    target: ref,
    offset: ["start start", "end start"],
  });
  const fade = useTransform(scrollYProgress, [0, 0.8], [1, 0]);
  const rise = useTransform(scrollYProgress, [0, 1], [0, -120]);
  const drift = useTransform(scrollYProgress, [0, 1], [0, 160]);

  return (
    <section
      ref={ref}
      id="signal"
      className="relative flex min-h-screen flex-col justify-center overflow-hidden bg-grid"
    >
      {/* Ambient glow */}
      <div
        aria-hidden
        className="pointer-events-none absolute left-1/2 top-1/2 h-[60vmin] w-[60vmin] -translate-x-1/2 -translate-y-1/2 rounded-full bg-accent/10 blur-[120px]"
      />
      {/* Vignette */}
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_at_center,transparent_40%,#050505_95%)]"
      />

      {/* 3D layer */}
      <motion.div style={{ y: drift }} className="absolute inset-0 z-10">
        <CoreScene />
      </motion.div>

      {/* Type layer */}
      <motion.div
        style={{ opacity: fade, y: rise }}
        className="pointer-events-none relative z-20 mx-auto w-full max-w-7xl px-6 md:px-10"
      >
        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.4, ease }}
          className="mb-6 font-mono text-xs uppercase tracking-[0.4em] text-accent"
        >
          ⬡ Live Feed — All Systems Online
        </motion.p>

        <h1 className="font-display font-bold leading-[0.85] tracking-tight">
          <motion.span
            initial={{ opacity: 0, y: 60 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 1, delay: 0.55, ease }}
            className="block text-[13vw] text-snow md:text-[9vw]"
          >
            INTELLIGENCE
          </motion.span>
          <motion.span
            initial={{ opacity: 0, y: 60 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 1, delay: 0.7, ease }}
            className="block text-[13vw] text-stroke md:text-[9vw]"
          >
            RENDERED
          </motion.span>
        </h1>

        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.9, delay: 0.95, ease }}
          className="mt-10 flex flex-col items-start gap-8 md:flex-row md:items-center md:justify-between"
        >
          <p className="max-w-xs text-sm leading-relaxed text-fog">
            NewsVisualizer turns the world&apos;s feed into one instrument —
            clusters, signals, timelines and source intelligence in real time.
          </p>
          <div className="pointer-events-auto flex items-center gap-4">
            <MagneticButton href="/dashboard">
              Launch Console <span aria-hidden>↗</span>
            </MagneticButton>
            <MagneticButton href="#modules" variant="ghost">
              Explore Modules
            </MagneticButton>
          </div>
        </motion.div>
      </motion.div>

      {/* HUD footer strip */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 1, delay: 1.3 }}
        style={{ opacity: fade }}
        className="absolute inset-x-0 bottom-0 z-20 border-t border-line"
      >
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4 font-mono text-[10px] uppercase tracking-[0.25em] text-dim md:px-10">
          <span>14 Modules</span>
          <span className="hidden md:inline">Live Clustering</span>
          <span className="hidden md:inline">Source Graph</span>
          <span className="flex items-center gap-2">
            <span className="h-1.5 w-1.5 rounded-full bg-accent animate-pulse-dot" />
            Scroll to Initialize
          </span>
        </div>
      </motion.div>
    </section>
  );
}
