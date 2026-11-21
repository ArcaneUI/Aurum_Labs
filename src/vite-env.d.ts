/// <reference types="vite/client" />

declare module "*.png" {
  const src: string;
  export default src;
}
declare module "*.jar?url" {
  const src: string;
  export default src;
}
declare module "*.zip?url" {
  const src: string;
  export default src;
}
