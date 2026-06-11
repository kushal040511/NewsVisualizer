import type { ReactNode } from "react";
import { Sidebar } from "@/components/layout/Sidebar";

export default function WorkspaceLayout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen bg-base bg-grid">
      <Sidebar />
      <main className="ml-60 min-h-screen px-8 py-8 lg:px-12">
        <div className="mx-auto max-w-7xl space-y-6">{children}</div>
      </main>
    </div>
  );
}
