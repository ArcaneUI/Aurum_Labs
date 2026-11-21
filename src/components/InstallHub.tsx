import { ArrowUpRight } from "lucide-react";
import { LINKS } from "../links";
import { useReveal } from "../hooks";
import { useLang } from "../i18n";

function InstallLinkCard({ n, title, desc, href }: { n: number; title: string; desc: string; href: string }) {
  return (
    <a
      href={href}
      target="_blank"
      rel="noreferrer"
      className="step-link group flex items-start gap-4 rounded-2xl border border-white/10 bg-white/[0.03] p-5 transition hover:-translate-y-0.5 hover:border-gold/35 hover:bg-white/[0.05]"
    >
      <div className="grid h-11 w-11 shrink-0 place-items-center rounded-xl border border-gold/25 bg-gold/10 font-display text-lg font-black text-gold">
        {n}
      </div>
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2">
          <div className="text-lg font-black text-white">{title}</div>
          <ArrowUpRight size={15} className="text-violet-300/45 transition group-hover:text-gold" />
        </div>
        <p className="mt-1 text-sm leading-relaxed text-violet-100/58">{desc}</p>
      </div>
    </a>
  );
}

export default function InstallHub() {
  const { t } = useLang();
  const [ref, shown] = useReveal<HTMLDivElement>(0.15);
  const [cardsRef, cardsShown] = useReveal<HTMLDivElement>(0.08);

  return (
    <section id="install" className="relative px-6 py-28 scroll-mt-24">
      <div className="relative mx-auto max-w-3xl">
        <div ref={ref} className={`reveal ${shown ? "in" : ""} mb-14 text-center`}>
          <div className="eyebrow mb-4 flex items-center justify-center gap-3">
            <span className="inline-block h-px w-8 bg-gold/60" />
            {t("install")}
            <span className="inline-block h-px w-8 bg-gold/60" />
          </div>
          <h2 className="font-display text-3xl font-black tracking-tight text-white sm:text-5xl">
            How to start in <span className="gold-text">2 minutes</span>
          </h2>
          <p className="mx-auto mt-5 max-w-xl text-base leading-relaxed text-violet-200/62 sm:text-lg">
            Click on the required item — откроется официальный сайт или страница загрузки.
          </p>
        </div>

        <div ref={cardsRef} className={`reveal ${cardsShown ? "in" : ""} space-y-4`}>
          <InstallLinkCard n={1} title="Fabric API" desc="Совместимая версия Fabric API для Minecraft 1.21.1. Откроется страница Modrinth с нужным релизом." href={LINKS.fabricApi} />
          <InstallLinkCard n={2} title="Java 21" desc="Если у вас нет подходящей Java, откроется страница Temurin / Adoptium с релизами Java 21." href={LINKS.java21} />
          <InstallLinkCard n={3} title="Инструкция" desc="Официальный гайд по добавлению модов в папку mods/." href={LINKS.fabricWiki} />
        </div>
      </div>
    </section>
  );
}
