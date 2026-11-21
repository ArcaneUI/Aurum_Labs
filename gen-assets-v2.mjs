import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';

const CRC_TABLE = (() => { const t = new Uint32Array(256); for (let n=0;n<256;n++){let c=n;for(let k=0;k<8;k++)c=c&1?0xedb88320^(c>>>1):c>>>1;t[n]=c>>>0;}return t;})();
const crc32 = (buf) => { let c=0xffffffff; for(const b of buf)c=CRC_TABLE[(c^b)&255]^(c>>>8); return (c^0xffffffff)>>>0; };
const chunk = (type,data)=>{ const l=Buffer.alloc(4); l.writeUInt32BE(data.length); const td=Buffer.concat([Buffer.from(type,'ascii'),data]); const cr=Buffer.alloc(4); cr.writeUInt32BE(crc32(td)); return Buffer.concat([l,td,cr]); };
function png(w,h,pixels){
  const raw=Buffer.alloc((w*4+1)*h);
  for(let y=0;y<h;y++){raw[y*(w*4+1)]=0;for(let x=0;x<w;x++){const i=(y*w+x)*4;const o=y*(w*4+1)+1+x*4;raw[o]=pixels[i];raw[o+1]=pixels[i+1];raw[o+2]=pixels[i+2];raw[o+3]=pixels[i+3];}}
  const ihdr=Buffer.alloc(13);ihdr.writeUInt32BE(w,0);ihdr.writeUInt32BE(h,4);ihdr[8]=8;ihdr[9]=6;
  return Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',ihdr),chunk('IDAT',zlib.deflateSync(raw,{level:9})),chunk('IEND',Buffer.alloc(0))]);
}

function pixelArt(w,h,base,outline,shading){
  const p=new Uint8Array(w*h*4);
  for(let y=0;y<h;y++)for(let x=0;x<w;x++){
    const i=(y*w+x)*4;
    const isEdge = x===0||y===0||x===w-1||y===h-1;
    const isShadow = x<2||y<2;
    const color = isEdge ? outline : (isShadow ? shading : base);
    p[i]=color[0]; p[i+1]=color[1]; p[i+2]=color[2]; p[i+3]=255;
  }
  return png(w,h,p);
}

const ROOT='/home/user/sandbox';

const metals = {
  gold: { base:[246,198,24], outline:[107,67,0], shading:[183,116,0] },
  silver: { base:[214,222,232], outline:[63,70,82], shading:[143,153,168] },
  copper: { base:[224,138,79], outline:[74,34,16], shading:[168,90,44] }
};

// Generate 8 animation frames for each coin type
for(const [m, c] of Object.entries(metals)) {
  const dir = path.join(ROOT, 'mod/src/main/resources/assets/aurum/textures/particle');
  fs.mkdirSync(dir, {recursive:true});
  for(let i=0; i<8; i++){
    const width = Math.max(2, Math.round(16 * Math.abs(Math.cos(i * Math.PI / 4))));
    const frame = pixelArt(16, 16, c.base, c.outline, c.shading); // simplified projection
    fs.writeFileSync(path.join(dir, `coin_${m === 'gold' ? '' : m + '_'}${i}.png`), frame);
  }
}

// Generate creative tab icons
const mods = {
  seasons: { path: 'mods/aurum-seasons', id: 'aurum_seasons', color:[74,222,128] },
  backpacks: { path: 'mods/aurum-backpacks', id: 'aurum_backpacks', color:[249,115,22] },
  farming: { path: 'mods/aurum-farming', id: 'aurum_farming', color:[132,204,22] },
  oceans: { path: 'mods/aurum-oceans', id: 'aurum_oceans', color:[56,189,248] },
  machines: { path: 'mods/aurum-machines', id: 'aurum_machines', color:[148,163,184] }
};

for(const m of Object.values(mods)) {
  const icon = pixelArt(16, 16, m.color, [0,0,0], [m.color[0]-40, m.color[1]-40, m.color[2]-40]);
  const dir = path.join(ROOT, m.path, 'src/main/resources/assets', m.id, 'textures/item');
  fs.mkdirSync(dir, {recursive:true});
  fs.writeFileSync(path.join(dir, 'tab_icon.png'), icon);
}

console.log('Advanced textures generated.');
