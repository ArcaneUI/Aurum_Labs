import {
  Armchair,
  Backpack,
  Blocks,
  Castle,
  Code2,
  Coins,
  Cpu,
  FlaskConical,
  Gauge,
  Gem,
  Languages,
  Leaf,
  LucideIcon,
  MapPinned,
  Palette,
  Pickaxe,
  Rocket,
  Shield,
  Sprout,
  TreePine,
  Waves,
  WalletCards,
} from "lucide-react";
import type { CSSProperties } from "react";

export type ModIconId =
  | "economy"
  | "seasons"
  | "backpacks"
  | "waystones"
  | "furniture"
  | "dungeons"
  | "alchemy"
  | "farming"
  | "machines"
  | "oceans"
  | "magic";

export type ValueIconId = "performance" | "art" | "code" | "lang";

const MOD_ICONS: Record<ModIconId, LucideIcon> = {
  economy: Coins,
  seasons: Leaf,
  backpacks: Backpack,
  waystones: MapPinned,
  furniture: Armchair,
  dungeons: Castle,
  alchemy: FlaskConical,
  farming: Sprout,
  machines: Cpu,
  oceans: Waves,
  magic: Gem,
};

const VALUE_ICONS: Record<ValueIconId, LucideIcon> = {
  performance: Gauge,
  art: Palette,
  code: Code2,
  lang: Languages,
};

/**
 * Новый логотип Aurum Labs: не «просто молния», а стилизованная монета/ядро мода.
 * Снаружи — тёмный защитный щиток, внутри — золотое «ядро» с руноподобной A-формой.
 */
export function BrandMark({ size = 44, className = "" }: { size?: number; className?: string }) {
  return (
    <span
      className={`relative inline-grid place-items-center ${className}`}
      style={{ width: size, height: size }}
      aria-hidden
    >
      <span
        className="absolute inset-0 rounded-2xl"
        style={{
          background:
            "linear-gradient(145deg, rgba(255,255,255,.12), rgba(255,255,255,.03))",
          border: "1px solid rgba(255,255,255,.08)",
          boxShadow: "0 16px 40px -16px rgba(246,198,24,.45), inset 0 1px 0 rgba(255,255,255,.15)",
          backdropFilter: "blur(8px)",
        }}
      />
      <svg viewBox="0 0 100 100" className="relative z-10" style={{ width: size * 0.78, height: size * 0.78 }}>
        <defs>
          <linearGradient id="aurumCore" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#fff2a8" />
            <stop offset="45%" stopColor="#f6c618" />
            <stop offset="100%" stopColor="#b77400" />
          </linearGradient>
          <linearGradient id="aurumEdge" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#2c2348" />
            <stop offset="100%" stopColor="#171022" />
          </linearGradient>
        </defs>

        <path
          d="M50 6 83 24v36L50 94 17 76V40Z"
          fill="url(#aurumEdge)"
          stroke="rgba(255,255,255,.18)"
          strokeWidth="3"
        />
        <path d="M50 17 73 30v25L50 70 27 57V32Z" fill="url(#aurumCore)" />
        <path d="M50 17 73 30 50 43 27 32Z" fill="rgba(255,255,255,.22)" />
        <path
          d="M51 28 62 56h-8l-3-9h-7l-4 9h-8l12-28zm-1 13-3-8-3 8z"
          fill="#2a1a00"
        />
      </svg>
    </span>
  );
}

export function ModGlyph({ id, color, size = 22, className = "" }: { id: ModIconId; color: string; size?: number; className?: string }) {
  const Icon = MOD_ICONS[id];
  return <Icon size={size} strokeWidth={2.2} className={className} style={{ color }} aria-hidden />;
}

export function ValueGlyph({ id, color, size = 22 }: { id: ValueIconId; color: string; size?: number }) {
  const Icon = VALUE_ICONS[id];
  return <Icon size={size} strokeWidth={2.2} style={{ color }} aria-hidden />;
}

export function OrbitChip({ icon, color, title, subtitle, style }: { icon: ModIconId; color: string; title: string; subtitle: string; style?: CSSProperties }) {
  return (
    <div
      className="absolute rounded-2xl border border-white/10 bg-white/[0.05] px-4 py-3 backdrop-blur-md shadow-[0_20px_40px_-24px_rgba(0,0,0,.7)]"
      style={style}
    >
      <div className="flex items-center gap-3">
        <div className="grid h-10 w-10 place-items-center rounded-xl" style={{ background: `${color}18`, boxShadow: `inset 0 0 0 1px ${color}33` }}>
          <ModGlyph id={icon} color={color} size={18} />
        </div>
        <div>
          <div className="text-sm font-bold text-white">{title}</div>
          <div className="text-[11px] font-semibold uppercase tracking-[0.16em] text-violet-200/45">{subtitle}</div>
        </div>
      </div>
    </div>
  );
}

export function SpecChip({ icon, text }: { icon: React.ReactNode; text: string }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border border-white/10 bg-white/[0.04] px-3 py-1.5 text-[11px] font-bold text-violet-100/75">
      {icon}
      {text}
    </span>
  );
}

export const UpcomingIcons = {
  Shield,
  Pickaxe,
  TreePine,
  WalletCards,
  Rocket,
  Blocks,
};
