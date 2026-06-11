"use client";

import { motion, useMotionValue, useSpring } from "framer-motion";
import { useRef, type ReactNode, type MouseEvent } from "react";

type Props = {
  children: ReactNode;
  href?: string;
  variant?: "solid" | "ghost";
  className?: string;
};

/** Button that magnetically follows the cursor and glows on hover. */
export default function MagneticButton({
  children,
  href = "#",
  variant = "solid",
  className = "",
}: Props) {
  const ref = useRef<HTMLAnchorElement>(null);
  const x = useMotionValue(0);
  const y = useMotionValue(0);
  const sx = useSpring(x, { stiffness: 200, damping: 18, mass: 0.4 });
  const sy = useSpring(y, { stiffness: 200, damping: 18, mass: 0.4 });

  const onMouseMove = (e: MouseEvent) => {
    const el = ref.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    x.set((e.clientX - rect.left - rect.width / 2) * 0.3);
    y.set((e.clientY - rect.top - rect.height / 2) * 0.3);
  };

  const reset = () => {
    x.set(0);
    y.set(0);
  };

  const styles =
    variant === "solid"
      ? "bg-snow text-base hover:bg-accent hover:shadow-glow"
      : "border border-line text-snow hover:border-accent/60 hover:text-accent";

  return (
    <motion.a
      ref={ref}
      href={href}
      onMouseMove={onMouseMove}
      onMouseLeave={reset}
      style={{ x: sx, y: sy }}
      className={`inline-flex items-center gap-2 rounded-full px-7 py-3.5 font-display text-sm font-medium tracking-wide transition-colors duration-300 ${styles} ${className}`}
    >
      {children}
    </motion.a>
  );
}
