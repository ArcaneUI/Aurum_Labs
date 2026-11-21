import { BrandMark } from "./BrandSystem";

export default function Footer2() {
  return (
    <footer className="relative border-t border-white/10 px-6 py-12">
      <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-6 sm:flex-row">
        <div className="flex items-center gap-3">
          <BrandMark size={42} />
          <div>
            <div className="font-display text-lg font-black text-white">
              AURUM<span className="gold-text"> LABS</span>
            </div>
            <div className="text-xs text-violet-200/40">Студия модов для Minecraft Fabric · MIT License</div>
          </div>
        </div>
        <div className="text-center text-xs leading-relaxed text-violet-200/40 sm:text-right">
          Проект не связан с Mojang Studios и Microsoft.
          <br />
          Minecraft — торговая марка Mojang AB.
        </div>
      </div>
    </footer>
  );
}
