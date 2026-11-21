import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App";
import DownloadModal from "./components/DownloadModal";
import { useModalStore } from "./modalStore";
import { initSmoothScroll } from "./smoothScroll";
initSmoothScroll();

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
    <DownloadWrapper />
  </StrictMode>
);

function DownloadWrapper() {
  const { isDownloadOpen, closeDownload } = useModalStore();
  return <DownloadModal isOpen={isDownloadOpen} onClose={closeDownload} />;
}
