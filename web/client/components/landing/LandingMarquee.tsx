const items = [
  "NARRATIVE CLUSTERS",
  "BREAKING SIGNALS",
  "SOURCE BALANCE",
  "AI SUMMARIES",
  "STORY TIMELINES",
  "DUPLICATE DETECTION",
];

export default function LandingMarquee() {
  const row = [...items, ...items];
  return (
    <div
      aria-hidden
      className="relative overflow-hidden border-y border-line bg-coal py-5"
    >
      <div className="flex w-max animate-marquee gap-12 whitespace-nowrap">
        {row.map((item, i) => (
          <span
            key={i}
            className="flex items-center gap-12 font-mono text-xs uppercase tracking-[0.3em] text-dim"
          >
            {item}
            <span className="text-accent">⬡</span>
          </span>
        ))}
      </div>
    </div>
  );
}
