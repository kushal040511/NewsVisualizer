"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import Reveal from "@/components/Reveal";

const modules = [
  {
    code: "00",
    title: "Dashboard",
    body: "The command center. Feed health, urgency, duplicate pressure and source balance at a glance.",
    href: "/dashboard",
    wide: true,
  },
  {
    code: "03",
    title: "Story Radar",
    body: "Live narrative clusters with confidence scoring.",
    href: "/story-radar",
  },
  {
    code: "05",
    title: "Breaking Watch",
    body: "Urgency-scored headlines from recency, tone and alert keywords.",
    href: "/breaking-watch",
  },
  {
    code: "09",
    title: "AI Summary",
    body: "Machine-written briefs for any article or the entire feed.",
    href: "/ai-summary",
    wide: true,
  },
];

export default function LandingModules() {
  return (
    <section id="modules" className="relative py-32 md:py-44">
      <div className="mx-auto max-w-7xl px-6 md:px-10">
        <Reveal>
          <p className="font-mono text-xs uppercase tracking-[0.4em] text-dim">
            01 / Modules
          </p>
          <h2 className="mt-6 max-w-2xl font-display text-4xl font-semibold tracking-tight text-snow md:text-6xl">
            Not a feed.
            <br />
            <span className="text-fog">An instrument.</span>
          </h2>
        </Reveal>

        <div className="mt-16 grid grid-cols-1 gap-5 md:grid-cols-3">
          {modules.map((m, i) => (
            <Reveal
              key={m.code}
              delay={i * 0.06}
              className={m.wide ? "md:col-span-2" : ""}
            >
              <motion.div
                whileHover={{ y: -4 }}
                transition={{ type: "spring", stiffness: 300, damping: 24 }}
                className="group relative h-full overflow-hidden rounded-3xl border border-line bg-coal p-8 shadow-card transition-colors duration-300 hover:border-accent/30"
              >
                <Link href={m.href} className="block">
                  <div className="pointer-events-none absolute -right-20 -top-20 h-48 w-48 rounded-full bg-accent/0 blur-3xl transition-all duration-500 group-hover:bg-accent/10" />
                  <p className="font-mono text-[10px] uppercase tracking-[0.3em] text-dim">
                    MOD.{m.code}
                  </p>
                  <h3 className="mt-5 font-display text-xl font-semibold tracking-tight text-snow md:text-2xl">
                    {m.title}
                  </h3>
                  <p className="mt-3 max-w-sm text-sm leading-relaxed text-fog">
                    {m.body}
                  </p>
                  <p className="mt-8 font-mono text-[10px] uppercase tracking-[0.25em] text-dim transition-colors duration-300 group-hover:text-accent">
                    Open Module ↗
                  </p>
                </Link>
              </motion.div>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  );
}
