import { useEffect, useState } from "react";
import { Layers3 } from "lucide-react";
import { MODS } from "../mods";
import { useReveal } from "../hooks";
import { BrandMark } from "./BrandSystem";

import { useLang } from "../i18n";

function Particles() {
  const dots = Array.from({ length: 24 }, (_, i) => ({
    left: `${(i * 17) % 100}%`,
    top: `${(i * 31) % 100}%`,
    size: 4 + ((i * 7) % 7),
    color: ["#f6c618", "#a78bfa", "#4ade80", "#38bdf8", "#fb7185"][i % 5],
    delay: `${(i % 7) * 0.7}s`,
    dur: `${10 + (i % 6) * 3}s`,
  }));

  return (
    <div className="pointer-events-none absolute inset-0 overflow-hidden">
      {dots.map((d, i) => (
        <span
          key={i}
          className="absolute rounded-full particle-soft"
          style={{
            left: d.left,
            top: d.top,
            width: d.size,
            height: d.size,
            background: d.color,
            animationDelay: d.delay,
            animationDuration: d.dur,
          }}
        />
      ))}
    </div>
  );
}

function Metric({ title, value, accent }: { title: string; value: string; accent?: boolean }) {
  return (
    <div className="rounded-2xl border border-white/8 bg-white/[0.03] px-5 py-4 text-left shadow-[0_14px_40px_-28px_rgba(0,0,0,.9)] backdrop-blur-md">
      <div className={`font-display text-3xl font-black ${accent ? "gold-text" : "text-white"}`}>{value}</div>
      <div className="mt-1 text-[11px] font-bold uppercase tracking-[0.18em] text-violet-200/45">{title}</div>
    </div>
  );
}

export default function Hero2() {
  const [ref, shown] = useReveal<HTMLDivElement>(0.15);
  const [word, setWord] = useState(0);
  const { t, lang } = useLang();
  const wordsRU = ["экономику", "сезоны", "рюкзаки", "телепорт", "мебель"];
  const wordsEN = ["an economy", "seasons", "backpacks", "teleporters", "furniture"];
  const words = lang === 'RU' ? wordsRU : wordsEN;
  const colors = ["text-yellow-400", "text-emerald-400", "text-orange-400", "text-violet-400", "text-rose-400"];

  useEffect(() => {
    const id = setInterval(() => setWord((w) => (w + 1) % words.length), 2600);
    return () => clearInterval(id);
  }, [words.length]);

  return (
    <section id="top" className="relative flex min-h-[100svh] max-h-[1080px] flex-col items-center justify-center overflow-hidden px-6 pb-28 pt-28 sm:pt-32">
      {/* Фон */}
      <div className="hero-bg absolute inset-0" style={{ maskImage: "linear-gradient(180deg, #000 0%, #000 76%, transparent 100%)", WebkitMaskImage: "linear-gradient(180deg, #000 0%, #000 76%, transparent 100%)" }} />
      <div className="bg-grid absolute inset-0" />
      <Particles />

      <div className="relative z-10 mx-auto max-w-4xl text-center">
        <BrandMark size={72} className="mx-auto mb-8" />

        <h1 className="font-display text-5xl font-black leading-[1.02] tracking-tight text-white sm:text-7xl lg:text-8xl" style={{ animation: "pop-in .7s .1s both" }}>
          AURUM<span className="gold-text"> LABS</span>
        </h1>

        <p className="mx-auto mt-8 max-w-2xl text-lg leading-relaxed text-violet-100/70 sm:text-xl" style={{ animation: "pop-in .7s .25s both" }}>
          {t("we_make")}{" "}
          <span key={word + lang} className={`inline-block font-bold ${colors[word]}`} style={{ animation: "word-swap .6s cubic-bezier(.4,0,.2,1) both" }}>
            {words[word]}
          </span>
          .<br className="hidden sm:block" />
          {t("we_desc")}
        </p>

        <div className="mt-10 flex flex-wrap justify-center gap-4" style={{ animation: "pop-in .7s .4s both" }}>
          <a href="#mods" className="btn-orange text-base">
            <Layers3 size={18} /> {t("view_mods")}
          </a>


        </div>

        <div ref={ref} className={`reveal ${shown ? "in" : ""} mx-auto mt-20 grid max-w-xl grid-cols-3 gap-8 border-t border-white/10 pt-10`}>
          <Metric title={t("stats_mods")} value={String(MODS.length)} />
          <Metric title={t("stats_version")} value="1.21.1" accent />
          <Metric title={t("stats_loader")} value="Fabric" />
        </div>
      </div>

      <div className="pointer-events-none absolute left-[15%] top-[25%] h-[380px] w-[380px] rounded-full bg-yellow-500/10 blur-[120px]" />
      <div className="pointer-events-none absolute right-[15%] top-[45%] h-[280px] w-[280px] rounded-full bg-violet-500/10 blur-[100px]" />
      <div className="pointer-events-none absolute inset-x-0 bottom-0 h-32 bg-gradient-to-t from-[#0b0815] to-transparent" />
    </section>
  );
}
