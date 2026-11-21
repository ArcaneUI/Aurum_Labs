import { useEffect, useRef, useState } from "react";

export default function CustomScrollbar({ containerRef, insetY = 0, isModal = false }: { containerRef?: React.RefObject<HTMLElement | null>, insetY?: number, isModal?: boolean }) {
  const [scrollProgress, setScrollProgress] = useState(0);
  const [thumbHeight, setThumbHeight] = useState(0);
  const [isHovered, setIsHovered] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const thumbRef = useRef<HTMLDivElement>(null);
  const trackRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const target = containerRef?.current || document.documentElement;
    const scrollTarget = containerRef?.current || window;
    
    const updateScroll = () => {
      const scrollY = containerRef?.current ? target.scrollTop : window.scrollY;
      const scrollHeight = target.scrollHeight;
      const clientHeight = target.clientHeight;
      
      if (scrollHeight <= clientHeight) {
        setThumbHeight(0);
        return;
      }
      
      const trackHeight = clientHeight - insetY * 2;
      let th = (clientHeight / scrollHeight) * trackHeight;
      const minThumbHeight = isModal ? 72 : 130;
      if (th < minThumbHeight) th = minThumbHeight;
      setThumbHeight(th);
      
      const maxScrollTop = scrollHeight - clientHeight;
      const progress = scrollY / maxScrollTop;
      setScrollProgress(progress);
    };

    updateScroll();
    scrollTarget.addEventListener("scroll", updateScroll, { passive: true });
    window.addEventListener("resize", updateScroll);
    
    const observer = new MutationObserver(updateScroll);
    observer.observe(target, { childList: true, subtree: true });

    return () => {
      scrollTarget.removeEventListener("scroll", updateScroll);
      window.removeEventListener("resize", updateScroll);
      observer.disconnect();
    };
  }, [containerRef, insetY, isModal]);

  const handlePointerDown = (e: React.PointerEvent) => {
    e.preventDefault();
    if (!trackRef.current || !thumbRef.current) return;
    
    setIsDragging(true);
    const target = containerRef?.current || document.documentElement;
    const clientHeight = target.clientHeight;
    const scrollHeight = target.scrollHeight;
    
    const trackHeight = clientHeight - insetY * 2;
    const maxScrollTop = scrollHeight - clientHeight;
    const maxThumbTop = trackHeight - thumbHeight;

    const startY = e.clientY;
    const startScrollProgress = scrollProgress;

    thumbRef.current.setPointerCapture(e.pointerId);

    const handlePointerMove = (moveEvent: PointerEvent) => {
      const deltaY = moveEvent.clientY - startY;
      const deltaProgress = deltaY / maxThumbTop;
      let newProgress = startScrollProgress + deltaProgress;
      if (newProgress < 0) newProgress = 0;
      if (newProgress > 1) newProgress = 1;
      
      setScrollProgress(newProgress);
      const newScrollTop = newProgress * maxScrollTop;
      
      if (containerRef?.current) {
        containerRef.current.scrollTop = newScrollTop;
      } else {
        window.scrollTo(0, newScrollTop);
      }
    };

    const handlePointerUp = (upEvent: PointerEvent) => {
      setIsDragging(false);
      thumbRef.current?.releasePointerCapture(upEvent.pointerId);
      window.removeEventListener("pointermove", handlePointerMove);
      window.removeEventListener("pointerup", handlePointerUp);
    };

    window.addEventListener("pointermove", handlePointerMove);
    window.addEventListener("pointerup", handlePointerUp);
  };

  if (thumbHeight === 0) return null;

  const trackHeight = (containerRef?.current?.clientHeight || window.innerHeight) - insetY * 2;
  const maxThumbTop = trackHeight - thumbHeight;
  const top = insetY + scrollProgress * maxThumbTop;

  return (
    <div
      ref={trackRef}
      className={`fixed right-0 ${isModal ? "z-[100]" : "z-[1000]"} w-3`}
      style={{ top: insetY, bottom: insetY }}
    >
      <div
        ref={thumbRef}
        onPointerEnter={() => setIsHovered(true)}
        onPointerLeave={() => setIsHovered(false)}
        onPointerDown={handlePointerDown}
        className="absolute right-0 rounded-l-full cursor-pointer"
        style={{
          height: thumbHeight,
          transform: `translate3d(0, ${top - insetY}px, 0)`,
          width: isHovered || isDragging ? 10 : 4,
          backgroundColor: isDragging ? "#ffe27a" : isHovered ? "#f6c618" : "rgba(167, 139, 250, 0.44)",
          boxShadow: isDragging ? "-3px 0 18px rgba(246, 198, 24, 0.8)" : isHovered ? "-3px 0 18px rgba(246, 198, 24, 0.55)" : "-1px 0 8px rgba(139, 92, 246, 0.22)",
          transition: "width 0.28s cubic-bezier(0.22, 1, 0.36, 1), background-color 0.3s ease, box-shadow 0.3s ease",
          touchAction: "none"
        }}
      />
    </div>
  );
}
