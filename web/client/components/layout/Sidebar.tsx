"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutGrid,
  Newspaper,
  BarChart3,
  Radar,
  Eye,
  BellRing,
  Copy,
  Scale,
  Clock,
  Sparkles,
  Languages,
  PanelsTopLeft,
  History,
  Settings,
} from "lucide-react";
import { cn } from "@/lib/utils";

const nav = [
  { href: "/dashboard", label: "Dashboard", code: "00", icon: LayoutGrid },
  { href: "/news-fetch", label: "News Fetch", code: "01", icon: Newspaper },
  { href: "/analytics", label: "Analytics", code: "02", icon: BarChart3 },
  { href: "/story-radar", label: "Story Radar", code: "03", icon: Radar },
  { href: "/source-monitor", label: "Source Monitor", code: "04", icon: Eye },
  { href: "/breaking-watch", label: "Breaking Watch", code: "05", icon: BellRing },
  { href: "/duplicates", label: "Duplicates", code: "06", icon: Copy },
  { href: "/source-balance", label: "Source Balance", code: "07", icon: Scale },
  { href: "/story-timeline", label: "Timeline", code: "08", icon: Clock },
  { href: "/ai-summary", label: "AI Summary", code: "09", icon: Sparkles },
  { href: "/translation", label: "Translation", code: "10", icon: Languages },
  { href: "/newsapp", label: "NewsApp", code: "11", icon: PanelsTopLeft },
  { href: "/search-history", label: "History", code: "12", icon: History },
  { href: "/settings", label: "Settings", code: "13", icon: Settings },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="fixed inset-y-0 left-0 z-40 flex w-60 flex-col border-r border-line bg-coal/70 backdrop-blur-xl">
      {/* Brand */}
      <Link
        href="/"
        className="flex h-16 items-center gap-2 border-b border-line px-6"
      >
        <span className="font-display text-sm font-bold tracking-[0.3em] text-snow">
          NEWSVIZ
        </span>
        <span className="h-1.5 w-1.5 rounded-full bg-accent animate-pulse-dot" />
      </Link>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto px-3 py-4">
        <p className="px-3 pb-3 font-mono text-[9px] uppercase tracking-[0.35em] text-dim">
          Modules
        </p>
        <ul className="space-y-0.5">
          {nav.map((item) => {
            const active = pathname === item.href;
            const Icon = item.icon;
            return (
              <li key={item.href}>
                <Link
                  href={item.href}
                  className={cn(
                    "group flex items-center gap-3 rounded-lg px-3 py-2 transition-all duration-200",
                    active
                      ? "bg-accent-dim text-accent shadow-glow"
                      : "text-fog hover:bg-graphite hover:text-snow"
                  )}
                >
                  <Icon className="h-4 w-4 shrink-0" />
                  <span className="flex-1 text-[13px] font-medium tracking-wide">
                    {item.label}
                  </span>
                  <span
                    className={cn(
                      "font-mono text-[9px] tracking-widest",
                      active ? "text-accent/80" : "text-dim/60"
                    )}
                  >
                    {item.code}
                  </span>
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      {/* Status */}
      <div className="border-t border-line px-6 py-4">
        <p className="flex items-center gap-2 font-mono text-[9px] uppercase tracking-[0.25em] text-dim">
          <span className="h-1.5 w-1.5 rounded-full bg-accent animate-pulse-dot" />
          Feed Online
        </p>
      </div>
    </aside>
  );
}
