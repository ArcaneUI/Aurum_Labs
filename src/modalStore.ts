import { create } from 'zustand';

interface ModalState {
  isDownloadOpen: boolean;
  openDownload: () => void;
  closeDownload: () => void;
}

export const useModalStore = create<ModalState>((set) => ({
  isDownloadOpen: false,
  openDownload: () => set({ isDownloadOpen: true }),
  closeDownload: () => set({ isDownloadOpen: false }),
}));
