"use client";

import { motion } from "framer-motion";
import type { ReactNode } from "react";

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  action?: ReactNode;
  children?: ReactNode;
  className?: string;
}

export function PageHeader({
  title,
  subtitle,
  action,
  className = "",
}: PageHeaderProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
      className={className}
    >
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1
            className="font-display text-3xl font-semibold tracking-tight text-snow"
            style={{ textShadow: "0 0 30px rgba(0, 240, 255, 0.12)" }}
          >
            {title}
          </h1>
          <div
            className="mt-2.5 h-px w-12 bg-gradient-to-r from-accent to-transparent"
            aria-hidden="true"
          />
          {subtitle && (
            <p className="mt-3 max-w-2xl text-sm leading-relaxed text-fog">
              {subtitle}
            </p>
          )}
        </div>
        {action && <div className="flex items-center gap-3">{action}</div>}
      </div>
    </motion.div>
  );
}

interface SectionHeaderProps {
  title: string;
  description?: string;
  action?: ReactNode;
}

export function SectionHeader({
  title,
  description,
  action,
}: SectionHeaderProps) {
  return (
    <div className="mb-4 flex items-start justify-between gap-4">
      <div>
        <h2 className="font-display text-lg font-medium tracking-tight text-snow">
          {title}
        </h2>
        <div
          className="mt-1.5 h-px w-8 bg-gradient-to-r from-accent/70 to-transparent"
          aria-hidden="true"
        />
        {description && <p className="mt-1.5 text-sm text-dim">{description}</p>}
      </div>
      {action && <div>{action}</div>}
    </div>
  );
}
