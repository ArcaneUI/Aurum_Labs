import { type CSSProperties, type MouseEvent, type ReactNode } from "react";
import { useReveal } from "../hooks";

/** Появление блока при прокрутке. */
export function Reveal({ children, delay = 0, className = "" }: { children: ReactNode; delay?: number; className?: string }) {
  const [ref, shown] = useReveal<HTMLDivElement>();
  return (
    <div ref={ref} className={`reveal ${shown ? "in" : ""} ${className}`} style={{ transitionDelay: `${delay}ms` }}>
      {children}
    </div>
  );
}

/** Карточка со свечением, которое следует за курсором. */
export function GlowCard({ children, className = "", style }: { children: ReactNode; className?: string; style?: CSSProperties }) {
  const onMove = (e: MouseEvent<HTMLDivElement>) => {
    const r = e.currentTarget.getBoundingClientRect();
    e.currentTarget.style.setProperty("--mx", `${e.clientX - r.left}px`);
    e.currentTarget.style.setProperty("--my", `${e.clientY - r.top}px`);
  };
  return (
    <div onMouseMove={onMove} className={`glow-card ${className}`} style={style}>
      {children}
    </div>
  );
}

export function SectionTitle({ eyebrow, title, sub }: { eyebrow: string; title: ReactNode; sub?: ReactNode }) {
  return (
    <Reveal className="mb-14 text-center mx-auto max-w-3xl">
      <div className="eyebrow mb-4 flex items-center gap-3 justify-center">
        <span className="inline-block h-px w-8 bg-gold/60" />
        {eyebrow}
        <span className="inline-block h-px w-8 bg-gold/60" />
      </div>
      <h2 className="font-display text-3xl font-black leading-tight tracking-tight text-white sm:text-5xl">{title}</h2>
      {sub && <p className="mt-5 text-base leading-relaxed text-violet-200/70 sm:text-lg">{sub}</p>}
    </Reveal>
  );
}
