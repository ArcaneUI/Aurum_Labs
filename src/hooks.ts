import { useEffect, useRef, useState } from "react";

/** Срабатывает один раз, когда элемент появляется в окне. */
export function useReveal<T extends HTMLElement>(threshold = 0.15) {
  const ref = useRef<T>(null);
  const [shown, setShown] = useState(false);
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const io = new IntersectionObserver(
      ([e]) => {
        if (e.isIntersecting) {
          setShown(true);
          io.disconnect();
        }
      },
      { threshold }
    );
    io.observe(el);
    return () => io.disconnect();
  }, [threshold]);
  return [ref, shown] as const;
}

/** Плавный разгон числа до target, когда active === true. */
export function useCountUp(target: number, active: boolean, duration = 1400) {
  const [v, setV] = useState(0);
  useEffect(() => {
    if (!active) return;
    let raf = 0;
    const t0 = performance.now();
    const tick = (t: number) => {
      const p = Math.min(1, (t - t0) / duration);
      const e = 1 - Math.pow(1 - p, 3);
      setV(Math.round(target * e));
      if (p < 1) raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [target, active, duration]);
  return v;
}

/**
 * «Накрутка» числа, как у HUD в моде (BalanceHud): каждый тик приближаемся к цели.
 */
export function useRolling(target: number) {
  const [shown, setShown] = useState(target);
  const targetRef = useRef(target);
  targetRef.current = target;
  useEffect(() => {
    const id = window.setInterval(() => {
      setShown((s) => {
        const diff = targetRef.current - s;
        if (Math.abs(diff) < 1) return targetRef.current;
        return s + Math.sign(diff) * Math.max(1, Math.abs(diff) * 0.2);
      });
    }, 40);
    return () => window.clearInterval(id);
  }, []);
  return shown;
}
