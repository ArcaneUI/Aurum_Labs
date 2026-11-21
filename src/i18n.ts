import { create } from 'zustand';

type Lang = 'RU' | 'EN';

interface LangState {
  lang: Lang;
  setLang: (l: Lang) => void;
  t: (key: string) => string;
}

const DICT: Record<string, Record<Lang, string>> = {
  "mods": { RU: "Моды", EN: "Mods" },
  "install": { RU: "Установка", EN: "Install" },
  "about": { RU: "О нас", EN: "About" },
  "studio": { RU: "Студия модов для Minecraft", EN: "Minecraft Mod Studio" },
  "we_make": { RU: "Мы делаем моды, которые добавляют в Minecraft", EN: "We make mods that add to Minecraft" },
  "we_desc": { RU: "Качественный код, пиксельный арт и внимание к деталям.", EN: "High-quality code, pixel art, and attention to detail." },
  "view_mods": { RU: "Смотреть моды", EN: "View Mods" },
  "download_pack": { RU: "Скачать пак", EN: "Download Pack" },
  "about_studio": { RU: "О студии", EN: "About Studio" },
  "stats_mods": { RU: "модов", EN: "mods" },
  "stats_version": { RU: "версия MC", EN: "MC version" },
  "stats_loader": { RU: "загрузчик", EN: "loader" },
  // catalog
  "catalog": { RU: "КАТАЛОГ", EN: "CATALOG" },
  "our_mods": { RU: "Наши", EN: "Our" },
  "our_mods_2": { RU: "моды", EN: "mods" },
  "catalog_desc": { RU: "Не лендинг одного релиза, а настоящая витрина студии: текущие проекты, бета-версии, идеи в проработке и центральный мод, вокруг которого строится вся линейка.", EN: "A true showcase of our studio: current projects, betas, upcoming ideas, and the core mod that powers the entire ecosystem." },
  "all": { RU: "Все", EN: "All" },
  "released": { RU: "Выпущенные", EN: "Released" },
  "beta": { RU: "Бета", EN: "Beta" },
  "soon": { RU: "Скоро", EN: "Soon" },
  "search": { RU: "Поиск по модам и тегам...", EN: "Search mods and tags..." },
  "features": { RU: "возможностей", EN: "features" },
  "hide_features": { RU: "Скрыть возможности", EN: "Hide features" },
  "download_jar": { RU: "Скачать .jar", EN: "Download .jar" },
  "source_code": { RU: "Исходники", EN: "Source Code" },
  "status_released": { RU: "Выпущен", EN: "Released" },
  "status_beta": { RU: "Бета-версия — активная разработка", EN: "Beta - Active development" },
  "status_soon": { RU: "Концепт — идёт проработка дизайна и механик", EN: "Concept - Design in progress" },
  // ... we'll add more later if needed or just use Russian for now in some places if too big. Let's translate MODS directly in mods.ts.
};

export const useLang = create<LangState>((set, get) => ({
  lang: 'EN',
  setLang: (lang) => set({ lang }),
  t: (key) => DICT[key]?.[get().lang] || key,
}));
