import { useMemo, useState } from "react";
import {
  BookOpen,
  ChevronDown,
  Download,
  ExternalLink,
  Package,
  Sparkles,
  Star,
  Tag,
  Wrench,
} from "lucide-react";
import { MODS, STATUS_LABELS, type ModDef } from "../mods";
import { useReveal } from "../hooks";
import { useLang } from "../i18n";
import { MOD_JARS, SRC } from "../downloads";
import { BrandMark, ModGlyph, UpcomingIcons, ValueGlyph } from "./BrandSystem";

function MetaPill({ icon, text }: { icon: React.ReactNode; text: string }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/[0.04] px-3 py-1.5 text-[11px] font-bold text-violet-100/72">
      {icon}
      {text}
    </span>
  );
}

function FilterBar({ value, onChange }: { value: "all" | "released" | "beta" | "soon"; onChange: (v: "all" | "released" | "beta" | "soon") => void }) {
  const options = useMemo(
    () => [
      ["all", "Все", MODS.length],
      ["released", "Выпущенные", MODS.filter((m) => m.status === "released").length],
      
      ["soon", "Идеи / Soon", MODS.filter((m) => m.status === "soon").length],
    ] as const,
    []
  );

  return (
    <div className="mb-10 flex flex-wrap justify-center gap-2.5">
      {options.map(([key, label, count]) => (
        <button
          key={key}
          onClick={() => onChange(key)}
          className={`rounded-2xl border px-4 py-2.5 text-sm font-bold transition duration-300 ${
            value === key
              ? "border-gold bg-gold/12 text-gold shadow-[0_0_28px_-14px_rgba(246,198,24,.9)]"
              : "border-white/10 bg-white/[0.02] text-violet-200/65 hover:border-white/20 hover:bg-white/[0.05] hover:text-white"
          }`}
        >
          {label}
          <span className="ml-2 rounded-full bg-black/20 px-2 py-0.5 text-[10px] text-current/70">{count}</span>
        </button>
      ))}
    </div>
  );
}

function ModCard({ mod, index }: { mod: ModDef; index: number }) {
  const [expanded, setExpanded] = useState(false);
  const [ref, shown] = useReveal<HTMLDivElement>(0.12);
  const st = STATUS_LABELS[mod.status];
  const { t, lang } = useLang();

  return (
    <div ref={ref} className={`reveal ${shown ? "in" : ""}`} style={{ transitionDelay: `${index * 90}ms` }}>
      <div className="soft-panel group relative overflow-hidden rounded-[30px] border border-white/10 shadow-[0_40px_90px_-60px_rgba(0,0,0,.95)] card-frosted-bottom">
        <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/18 to-transparent" />
        <div className="absolute inset-0 opacity-60" style={{ background: `radial-gradient(640px circle at 100% 0%, ${mod.color}20, transparent 45%)` }} />
        <div className="absolute inset-0 opacity-0 transition-opacity duration-500 group-hover:opacity-100" style={{ background: `radial-gradient(480px circle at 15% 0%, ${mod.color}12, transparent 40%)` }} />

        <div className="relative z-10 p-6 sm:p-8 lg:p-9">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-start">
            <div className="flex min-w-0 flex-1 items-start gap-4 sm:gap-5">
              <div className="grid h-15 w-15 shrink-0 place-items-center rounded-[22px] border border-white/10 bg-white/[0.04] shadow-[inset_0_1px_0_rgba(255,255,255,.08)]" style={{ boxShadow: `inset 0 0 0 1px ${mod.color}22, 0 22px 40px -24px ${mod.color}44` }}>
                <ModGlyph id={mod.icon} color={mod.color} size={26} />
              </div>
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2.5">
                  <h3 className="font-display text-2xl font-black tracking-tight text-white sm:text-3xl">{mod.name}</h3>
                  <span className={`rounded-full border px-2.5 py-1 text-[11px] font-bold ${st.bg} ${st.color}`}>{lang === "RU" ? st.label : st.label_en}</span>
                </div>
                <p className="mt-1.5 text-base font-semibold" style={{ color: mod.color }}>
                  {lang === "RU" ? mod.tagline : mod.tagline_en}
                </p>
                <p className="mt-4 max-w-4xl text-[15px] leading-relaxed text-violet-100/62">{lang === "RU" ? mod.description : mod.description_en}</p>
              </div>
            </div>

            {mod.status === "released" && (
              <div className="lg:w-[280px]">
                <div className="rounded-[24px] border border-gold/18 bg-[linear-gradient(180deg,rgba(246,198,24,.16),rgba(246,198,24,.05))] p-4 shadow-[0_22px_50px_-28px_rgba(246,198,24,.45)]">
                  <div className="mb-3 flex items-center gap-3">
                    <BrandMark size={40} />
                    <div>
                      <div className="text-sm font-black text-white">{t("status_released")}</div>
                      <div className="text-[11px] font-bold uppercase tracking-[0.16em] text-gold/80">{mod.version}</div>
                    </div>
                  </div>
                  <div className="space-y-2.5">
                    <a href={MOD_JARS[mod.id]?.url ?? "#"} download={MOD_JARS[mod.id]?.name ?? ""} className="btn-orange w-full !justify-between">
                      <span className="flex items-center gap-2">
                        <Download size={18} />
                        {t("download_jar")}
                      </span>
                      <span className="text-xs font-semibold text-orange-200/60">{MOD_JARS[mod.id]?.size ?? ""}</span>
                    </a>
                    <a href={SRC.url} download={SRC.name} className="btn-ghost !flex !w-full !justify-between !rounded-2xl !px-4 !py-3 text-sm">
                      <span className="flex items-center gap-2.5"><ExternalLink size={15} className="text-gold" />{t("source_code")}</span>
                      <span className="text-xs text-violet-200/45">Gradle</span>
                    </a>
                  </div>
                </div>
              </div>
            )}
          </div>

          <div className="mt-5 flex flex-wrap gap-2.5">
            <MetaPill icon={<Package size={13} />} text={`v${mod.version}`} />
            <MetaPill icon={<Wrench size={13} />} text={mod.loader} />
            <MetaPill icon={<Star size={13} />} text={`MC ${mod.mc}`} />
          </div>

          <div className="mt-4 flex flex-wrap gap-2">
            {(lang === "RU" ? mod.tags : mod.tags_en).map((t) => (
              <span key={t} className="inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-[10px] font-bold uppercase tracking-[0.16em] text-violet-200/38">
                <Tag size={10} /> {t}
              </span>
            ))}
          </div>


          <button
            onClick={() => setExpanded((v) => !v)}
            className="mt-6 inline-flex items-center gap-2 rounded-xl px-1 text-left text-sm font-black transition hover:opacity-100"
            style={{ color: mod.color, opacity: 0.96 }}
          >
            <ChevronDown size={16} className={`transition-transform duration-300 ${expanded ? "rotate-180" : ""}`} />
            {expanded ? t("hide_features") : `${mod.features.length} ${t("features")}`}
          </button>

          <div className="grid transition-all duration-500" style={{ gridTemplateRows: expanded ? "1fr" : "0fr" }}>
            <div className="overflow-hidden">
              <div className="mt-4 flex flex-col items-start gap-2.5">
                {[...(lang === "RU" ? mod.features : mod.features_en)].sort((a, b) => b.length - a.length).map((f, i) => (
                  <div
                    key={f}
                    className="flex w-full items-start gap-3 rounded-2xl border border-white/5 bg-white/[0.02] px-4 py-2.5 text-[13px] font-medium leading-relaxed text-violet-100/75 transition-colors hover:bg-white/[0.04]"
                    style={{ animation: expanded ? `fade-up-soft .3s ${i * 30}ms both` : undefined }}
                  >
                    <span className="mt-2 h-1.5 w-1.5 shrink-0 rounded-full" style={{ background: mod.color }} />
                    {f}
                  </div>
                ))}
              </div>
            </div>
          </div>

          {mod.status !== "released" && (
            <div className="mt-6">
              <div className={`inline-flex items-center gap-2 rounded-2xl border px-4 py-3 text-sm font-bold ${st.bg} ${st.color}`}>
                <Sparkles size={16} />
                {mod.status === "beta" ? t("status_beta") : t("status_soon")}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function AboutSection() {
  const [ref, shown] = useReveal<HTMLDivElement>(0.18);

  const values = [
    {
      id: "performance" as const,
      title: "Производительность",
      desc: "Минимум лишней нагрузки, чистые миксины и аккуратная логика. Моды не должны ломать игру или сервер.",
      color: "#f6c618",
    },
    {
      id: "art" as const,
      title: "Визуальная цельность",
      desc: "Один общий язык интерфейсов, блоков и предметов, чтобы моды ощущались как части одной студии, а не случайный набор функций.",
      color: "#fb7185",
    },
    {
      id: "code" as const,
      title: "Открытый код",
      desc: "Сборка через Gradle, читаемая структура проекта, исходники и возможность быстро поддерживать моды после релиза.",
      color: "#38bdf8",
    },
    {
      id: "lang" as const,
      title: "Нормальная локализация",
      desc: "Русский и английский сразу из коробки, понятные тексты, аккуратные tooltips и интерфейс без каши из непонятных слов.",
      color: "#4ade80",
    },
  ];

  const soonIdeas = [
    { title: "Aurum: Machines", icon: UpcomingIcons.Blocks, color: "#94a3b8" },
    { title: "Aurum: Arcana", icon: UpcomingIcons.WalletCards, color: "#c084fc" },
    { title: "Aurum: Dungeons", icon: UpcomingIcons.Shield, color: "#60a5fa" },
    { title: "Aurum: Oceans", icon: UpcomingIcons.Rocket, color: "#38bdf8" },
    { title: "Aurum: Farming", icon: UpcomingIcons.TreePine, color: "#84cc16" },
    { title: "Aurum: Tools", icon: UpcomingIcons.Pickaxe, color: "#f59e0b" },
  ];

  return (
    <div id="about" className="mt-30 scroll-mt-24">
      <div ref={ref} className={`reveal ${shown ? "in" : ""} mb-14 text-center`}>
        <div className="eyebrow mb-4 flex items-center justify-center gap-3">
          <span className="inline-block h-px w-8 bg-gold/60" />
          О СТУДИИ
          <span className="inline-block h-px w-8 bg-gold/60" />
        </div>
        <h2 className="font-display text-3xl font-black tracking-tight text-white sm:text-5xl">
          Кто стоит за <span className="gold-text">Aurum Labs</span>
        </h2>
        <p className="mx-auto mt-5 max-w-3xl text-base leading-relaxed text-violet-200/60 sm:text-lg">
          Небольшая студия, которая делает не просто «ещё один мод», а линейку совместимых систем под одну экосистему: общий визуальный стиль, продуманные механики, нормальная локализация и аккуратный код.
        </p>
      </div>

      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
        {values.map((v, i) => {
          const [vRef, vShown] = useReveal<HTMLDivElement>(0.15);
          return (
            <div key={v.title} ref={vRef} className={`reveal ${vShown ? "in" : ""}`} style={{ transitionDelay: `${i * 70}ms` }}>
              <div className="soft-panel h-full rounded-[26px] border border-white/10 p-6">
                <div className="mb-4 grid h-14 w-14 place-items-center rounded-2xl border border-white/10 bg-white/[0.04]">
                  <ValueGlyph id={v.id} color={v.color} size={24} />
                </div>
                <h3 className="font-display text-lg font-black text-white">{v.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-violet-100/58">{v.desc}</p>
              </div>
            </div>
          );
        })}
      </div>

      <div className="mt-12 grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.05fr)]">
        {(() => {
          const [cRef, cShown] = useReveal<HTMLDivElement>(0.12);
          return (
            <div ref={cRef} className={`reveal ${cShown ? "in" : ""}`}>
              <div className="soft-panel h-full rounded-[28px] border border-white/10 p-8 sm:p-9">
                <div className="mb-3 text-sm font-black uppercase tracking-[0.18em] text-gold">Экосистема</div>
                <h3 className="font-display text-2xl font-black text-white sm:text-3xl">Моды, которые усиливают друг друга</h3>
                <p className="mt-3 text-sm leading-relaxed text-violet-100/60">
                  Waystones смогут использовать валюту из Economy, Farming — контракты и поставки через экономику, а Seasons — влиять на урожай и редкие ресурсы. Это не случайные идеи, а общий дизайн-план на несколько релизов вперёд.
                </p>
                <div className="mt-6 flex flex-wrap gap-2">
                  {["Minecraft 1.21.1", "Fabric API", "Java 21", "RU + EN"].map((x) => (
                    <MetaPill key={x} icon={<BookOpen size={13} />} text={x} />
                  ))}
                </div>
              </div>
            </div>
          );
        })()}

        {(() => {
          const [rRef, rShown] = useReveal<HTMLDivElement>(0.12);
          return (
            <div ref={rRef} className={`reveal ${rShown ? "in" : ""}`} style={{ transitionDelay: "80ms" }}>
              <div className="soft-panel rounded-[28px] border border-white/10 p-8 sm:p-9">
                <div className="mb-3 text-sm font-black uppercase tracking-[0.18em] text-violet-300">Идеи в очереди</div>
                <h3 className="font-display text-2xl font-black text-white sm:text-3xl">Дальше — ещё шире</h3>
                <div className="mt-6 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                  {soonIdeas.map((x, i) => {
                    const Icon = x.icon;
                    return (
                      <div key={x.title} className="rounded-2xl border border-white/8 bg-white/[0.03] p-4" style={{ animation: `fade-up-soft .45s ${i * 45}ms both` }}>
                        <div className="mb-3 grid h-11 w-11 place-items-center rounded-xl border border-white/10 bg-white/[0.05]">
                          <Icon size={20} style={{ color: x.color }} />
                        </div>
                        <div className="text-sm font-black text-white">{x.title}</div>
                        <div className="mt-1 text-[11px] font-semibold uppercase tracking-[0.16em] text-violet-200/42">concept / soon</div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          );
        })()}
      </div>
    </div>
  );
}

export default function ModCatalog() {
  const [filter, setFilter] = useState<"all" | "released" | "beta" | "soon">("all");
  const [search, setSearch] = useState("");
  const filtered = MODS.filter(m => (filter === "all" || m.status === filter) && (m.name.toLowerCase().includes(search.toLowerCase()) || m.tags.some(t => t.toLowerCase().includes(search.toLowerCase()))));
  const [headRef, headShown] = useReveal<HTMLDivElement>(0.2);
  const { t } = useLang();

  return (
    <section id="mods" className="relative px-6 py-26">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(900px_480px_at_50%_0%,rgba(246,198,24,.07),transparent_72%)]" />
      <div className="relative mx-auto max-w-6xl">
        <div ref={headRef} className={`reveal ${headShown ? "in" : ""} mb-14 text-center`}>
          <div className="eyebrow mb-4 flex items-center justify-center gap-3">
            <span className="inline-block h-px w-8 bg-gold/60" />
            {t("catalog")}
            <span className="inline-block h-px w-8 bg-gold/60" />
          </div>
          <h2 className="font-display text-4xl font-black tracking-tight text-white sm:text-6xl">
            {t("our_mods")} <span className="gold-text">{t("our_mods_2")}</span>
          </h2>
          <p className="mx-auto mt-5 max-w-3xl text-base leading-relaxed text-violet-200/60 sm:text-lg">
            {t("catalog_desc")}
          </p>
        </div>

        <FilterBar value={filter} onChange={setFilter} />
        <div className="mb-6 mx-auto max-w-md">
          <div className="relative">
            <input 
              type="text" 
              placeholder={t("search")} 
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="w-full rounded-2xl border border-white/10 bg-white/[0.03] px-5 py-3 text-sm text-white placeholder-violet-200/40 outline-none focus:border-gold/50 focus:bg-white/[0.06] transition"
            />
          </div>
        </div>


        <div className="space-y-7">
          {filtered.map((mod, i) => (
            <ModCard key={mod.id} mod={mod} index={i} />
          ))}
        </div>

        {filtered.length === 0 && <div className="py-20 text-center text-violet-200/40">Нет модов с таким статусом.</div>}

        <AboutSection />
      </div>
    </section>
  );
}
