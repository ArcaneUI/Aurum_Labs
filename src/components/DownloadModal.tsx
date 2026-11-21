import { useState, useRef } from "react";
import JSZip from "jszip";
import { Download, X, CheckSquare, Square, Package, Info } from "lucide-react";
import { MODS } from "../mods";
import { MOD_JARS } from "../downloads";
import CustomScrollbar from "./CustomScrollbar";
import { BrandMark } from "./BrandSystem";
import { useLang } from "../i18n";

export default function DownloadModal({ isOpen, onClose }: { isOpen: boolean, onClose: () => void }) {
  const [selected, setSelected] = useState<Set<string>>(new Set(MODS.filter(m => m.status === 'released').map(m => m.id)));
  const [isDownloading, setIsDownloading] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);
  const { lang } = useLang();

  if (!isOpen) return null;

  const toggle = (id: string) => {
    const next = new Set(selected);
    if (next.has(id)) next.delete(id);
    else next.add(id);
    setSelected(next);
  };

  const handleDownload = async () => {
    if (selected.size === 0) return;
    setIsDownloading(true);
    const zip = new JSZip();
    const modsFolder = zip.folder("mods");
    
    zip.file("HOW_TO_INSTALL.txt", "Put the jar files from the 'mods' folder into your .minecraft/mods folder.\nMake sure you have Fabric Loader and Fabric API installed.");
    
    for (const id of selected) {
      const jar = MOD_JARS[id];
      if (jar && jar.url !== "#") {
        try {
          const res = await fetch(jar.url);
          const blob = await res.blob();
          modsFolder?.file(jar.name, blob);
        } catch (e) {
          console.error("Failed to fetch", jar.name, e);
        }
      }
    }
    
    const content = await zip.generateAsync({ type: "blob" });
    const url = URL.createObjectURL(content);
    const a = document.createElement("a");
    a.href = url;
    a.download = "Aurum-Labs-Pack-1.21.1.zip";
    a.click();
    URL.revokeObjectURL(url);
    setIsDownloading(false);
    onClose();
  };

  const releasedMods = MODS.filter(m => m.status === 'released');

  return (
    <div className="fixed inset-0 z-[2000] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative flex max-h-[85vh] w-full max-w-lg flex-col overflow-hidden rounded-3xl border border-white/10 bg-[#0b0815] shadow-2xl">
        <div className="flex items-center justify-between border-b border-white/10 p-5">
          <div className="flex items-center gap-3">
            <BrandMark size={32} />
            <span className="font-display text-lg font-black text-white">Aurum Labs Pack</span>
          </div>
          <button onClick={onClose} className="rounded-xl p-2 text-violet-200/50 hover:bg-white/5 hover:text-white">
            <X size={20} />
          </button>
        </div>
        
        <div ref={scrollRef} className="flex-1 overflow-y-auto p-5 relative">
          <CustomScrollbar containerRef={scrollRef} insetY={22} isModal={true} />
          <div className="mb-5 rounded-2xl border border-emerald-500/20 bg-emerald-500/10 p-4 text-sm text-emerald-100/90 flex gap-3">
            <Info size={18} className="shrink-0 text-emerald-400 mt-0.5" />
            <span>{lang === "RU" ? "Выберите моды для скачивания. Они будут упакованы в один ZIP-архив с инструкцией." : "Select mods to download. They will be packed into a single ZIP archive with instructions."}</span>
          </div>
          
          <div className="space-y-3">
            {releasedMods.map(mod => {
              const isSel = selected.has(mod.id);
              return (
                <div key={mod.id} onClick={() => toggle(mod.id)} className={`flex cursor-pointer items-center gap-4 rounded-2xl border p-4 transition-colors ${isSel ? 'border-gold/40 bg-gold/10' : 'border-white/10 bg-white/[0.03] hover:border-white/20 hover:bg-white/[0.06]'}`}>
                  <div className={`text-2xl transition-colors ${isSel ? 'text-gold' : 'text-violet-200/40'}`}>
                    {isSel ? <CheckSquare size={24} /> : <Square size={24} />}
                  </div>
                  <div className="flex-1">
                    <div className="font-display font-black text-white">{mod.name}</div>
                    <div className="text-xs text-violet-200/60">{lang === "RU" ? mod.tagline : mod.tagline_en}</div>
                  </div>
                  {MOD_JARS[mod.id] && (
                    <div className="text-xs font-bold text-violet-200/40">{MOD_JARS[mod.id].size}</div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
        
        <div className="border-t border-white/10 p-5">
          <button onClick={handleDownload} disabled={isDownloading || selected.size === 0} className="btn-orange flex w-full items-center justify-center gap-2 !rounded-2xl !py-3.5 opacity-100 disabled:opacity-50 disabled:cursor-not-allowed">
            {isDownloading ? <Package className="animate-spin" size={18} /> : <Download size={18} />}
            {isDownloading ? (lang === "RU" ? "Упаковка архива..." : "Packing archive...") : (lang === "RU" ? `Скачать пак (${selected.size})` : `Download pack (${selected.size})`)}
          </button>
        </div>
      </div>
    </div>
  );
}
