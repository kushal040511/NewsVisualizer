import Reveal from "@/components/Reveal";
import MagneticButton from "@/components/ui/MagneticButton";

export default function LandingFooter() {
  return (
    <footer className="relative overflow-hidden border-t border-line">
      {/* CTA block */}
      <div className="relative py-36 text-center md:py-48">
        <div
          aria-hidden
          className="pointer-events-none absolute left-1/2 top-1/2 h-[50vmin] w-[70vmin] -translate-x-1/2 -translate-y-1/2 rounded-full bg-accent/[0.07] blur-[140px]"
        />
        <Reveal>
          <p className="font-mono text-xs uppercase tracking-[0.4em] text-dim">
            02 / Access
          </p>
          <h2 className="mx-auto mt-8 max-w-4xl font-display text-5xl font-bold leading-[0.95] tracking-tight text-snow md:text-8xl">
            ENTER THE
            <br />
            <span className="text-accent drop-shadow-[0_0_30px_rgba(0,240,255,0.35)]">
              NEWSROOM
            </span>
          </h2>
          <p className="mx-auto mt-8 max-w-sm text-sm leading-relaxed text-fog">
            Fetch a live feed and watch fourteen instruments light up at once.
          </p>
          <div className="mt-12 flex justify-center">
            <MagneticButton href="/dashboard">
              Launch Console <span aria-hidden>↗</span>
            </MagneticButton>
          </div>
        </Reveal>
      </div>

      {/* Giant wordmark */}
      <div aria-hidden className="select-none overflow-hidden">
        <p className="translate-y-[18%] text-center font-display text-[17vw] font-bold leading-none tracking-tight text-graphite">
          NEWSVIZ
        </p>
      </div>

      {/* Bottom bar */}
      <div className="border-t border-line">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 px-6 py-6 font-mono text-[10px] uppercase tracking-[0.25em] text-dim md:flex-row md:px-10">
          <span>© 2026 NewsVisualizer</span>
          <span className="flex items-center gap-2">
            <span className="h-1.5 w-1.5 rounded-full bg-accent animate-pulse-dot" />
            All systems operational
          </span>
          <span>Signal over noise</span>
        </div>
      </div>
    </footer>
  );
}
