import { useEffect, useState } from "react";
import { Menu, X, Package } from "lucide-react";
import { BrandMark } from "./BrandSystem";
import { useModalStore } from "../modalStore";
import { useLang } from "../i18n";

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [open, setOpen] = useState(false);
  const { lang, setLang, t } = useLang();

  const LINKS: [string, string][] = [
    ["mods", t("mods")],
    ["install", t("install")],
    ["about", t("about")],
  ];

  useEffect(() => {
    const fn = () => setScrolled(window.scrollY > 200);
    fn();
    window.addEventListener("scroll", fn, { passive: true });
    return () => window.removeEventListener("scroll", fn);
  }, []);

  return (
    <header className={`fixed inset-x-0 top-0 z-50 transition-all duration-500 ${scrolled || open ? "border-b border-white/10 bg-[#0b0815]/78 backdrop-blur-2xl shadow-[0_10px_40px_-24px_rgba(0,0,0,.85)]" : ""}`}>
      <nav className="mx-auto flex h-18 max-w-7xl items-center px-6 py-3">
        <a
          href="#top"
          className={`flex items-center gap-3 transition-all duration-500 ${scrolled ? "opacity-100 translate-y-0" : "opacity-0 -translate-y-4 pointer-events-none"}`}
        >
          <BrandMark size={40} />
          <span className="font-display text-lg font-black tracking-wide text-white sm:text-xl">
            AURUM<span className="gold-text"> LABS</span>
          </span>
        </a>

        <div className="absolute left-1/2 top-1/2 hidden -translate-x-1/2 -translate-y-1/2 items-center gap-2 md:flex">
          {LINKS.map(([id, label]) => (
            <a
              key={id}
              href={`#${id}`}
              className="rounded-xl px-4 py-2 text-sm font-semibold text-violet-100/68 transition hover:bg-white/[0.05] hover:text-white"
            >
              {label}
            </a>
          ))}
        </div>

        <div className="ml-auto flex items-center gap-3">
          <button onClick={() => useModalStore.getState().openDownload()} className="btn-orange hidden sm:flex !py-2 !px-4 text-sm">
            <Package size={16} /> {t("download_pack")}
          </button>
          
          <button onClick={() => setLang(lang === 'RU' ? 'EN' : 'RU')} className="rounded-xl border border-white/10 bg-white/[0.04] px-3 py-2 text-xs font-bold text-white transition hover:bg-white/10">
            {lang}
          </button>
    
          <button aria-label="Меню" onClick={() => setOpen(!open)} className="rounded-xl border border-white/10 bg-white/[0.04] p-2.5 text-white md:hidden">
            {open ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>
      </nav>

      {open && (
        <div className="border-t border-white/10 px-6 pb-6 pt-3 md:hidden">
          {LINKS.map(([id, label]) => (
            <a key={id} href={`#${id}`} onClick={() => setOpen(false)} className="block rounded-xl px-3 py-3 font-semibold text-violet-100/78 hover:bg-white/[0.05] hover:text-gold">
              {label}
            </a>
          ))}
        </div>
      )}
    </header>
  );
}
