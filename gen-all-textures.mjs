import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';

// ---- Minimal PNG encoder ----
const CRC_TABLE = (() => { const t = new Uint32Array(256); for (let n=0;n<256;n++){let c=n;for(let k=0;k<8;k++)c=c&1?0xedb88320^(c>>>1):c>>>1;t[n]=c>>>0;}return t;})();
const crc32 = (buf) => { let c=0xffffffff; for(const b of buf)c=CRC_TABLE[(c^b)&255]^(c>>>8); return (c^0xffffffff)>>>0; };
const chunk = (type,data)=>{ const l=Buffer.alloc(4); l.writeUInt32BE(data.length); const td=Buffer.concat([Buffer.from(type,'ascii'),data]); const cr=Buffer.alloc(4); cr.writeUInt32BE(crc32(td)); return Buffer.concat([l,td,cr]); };
function png(w,h,pixels){
  const raw=Buffer.alloc((w*4+1)*h);
  for(let y=0;y<h;y++){raw[y*(w*4+1)]=0;for(let x=0;x<w;x++){const i=(y*w+x)*4;const o=y*(w*4+1)+1+x*4;raw[o]=pixels[i];raw[o+1]=pixels[i+1];raw[o+2]=pixels[i+2];raw[o+3]=pixels[i+3];}}
  const ihdr=Buffer.alloc(13);ihdr.writeUInt32BE(w,0);ihdr.writeUInt32BE(h,4);ihdr[8]=8;ihdr[9]=6;
  return Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',ihdr),chunk('IDAT',zlib.deflateSync(raw,{level:9})),chunk('IEND',Buffer.alloc(0))]);
}
function solidColor(w,h,r,g,b,a=255){
  const p=new Uint8Array(w*h*4);
  for(let i=0;i<w*h;i++){p[i*4]=r;p[i*4+1]=g;p[i*4+2]=b;p[i*4+3]=a;}
  return png(w,h,p);
}
function checkerboard(w,h,c1,c2){
  const p=new Uint8Array(w*h*4);
  for(let y=0;y<h;y++)for(let x=0;x<w;x++){
    const c=(x+y)%2===0?c1:c2;
    const i=(y*w+x)*4;p[i]=c[0];p[i+1]=c[1];p[i+2]=c[2];p[i+3]=c[3]||255;
  }
  return png(w,h,p);
}
function gradientV(w,h,top,bot){
  const p=new Uint8Array(w*h*4);
  for(let y=0;y<h;y++){const t=y/(h-1);for(let x=0;x<w;x++){const i=(y*w+x)*4;for(let c=0;c<3;c++)p[i+c]=Math.round(top[c]+(bot[c]-top[c])*t);p[i+3]=255;}}
  return png(w,h,p);
}
function saveTexture(modDir, subpath, data){
  const full=path.join(modDir,subpath);
  fs.mkdirSync(path.dirname(full),{recursive:true});
  fs.writeFileSync(full,data);
}
function saveModel(modDir, subpath, json){
  const full=path.join(modDir,subpath);
  fs.mkdirSync(path.dirname(full),{recursive:true});
  fs.writeFileSync(full,JSON.stringify(json,null,2));
}
function itemModel(modId,name){return{parent:"minecraft:item/generated",textures:{layer0:`${modId}:item/${name}`}};}
function blockModel(modId,name){return{parent:"minecraft:block/cube_all",textures:{all:`${modId}:block/${name}`}};}
function blockItemModel(modId,name){return{parent:`${modId}:block/${name}`};}
function blockstate(modId,name){return{variants:{"":{"model":`${modId}:block/${name}`}}};}
function icon64(r,g,b){return solidColor(64,64,r,g,b);}

const ROOT='/home/user/sandbox';

// ======= ECONOMY =======
const ecoDir=path.join(ROOT,'mod/src/main/resources');
// icon already exists, skip

// ======= SEASONS =======
const seasDir=path.join(ROOT,'mods/aurum-seasons/src/main/resources');
saveTexture(seasDir,'assets/aurum_seasons/icon.png',icon64(76,222,128));
saveTexture(seasDir,'assets/aurum_seasons/textures/item/season_calendar.png',gradientV(16,16,[60,180,100],[30,100,60]));
saveModel(seasDir,'assets/aurum_seasons/models/item/season_calendar.json',itemModel('aurum_seasons','season_calendar'));

// ======= BACKPACKS =======
const bpDir=path.join(ROOT,'mods/aurum-backpacks/src/main/resources');
saveTexture(bpDir,'assets/aurum_backpacks/icon.png',icon64(249,115,22));
for(const [name,c] of [['leather_backpack',[160,100,60]],['iron_backpack',[200,200,200]],['gold_backpack',[255,215,0]],['diamond_backpack',[0,220,220]],['netherite_backpack',[70,60,70]]]){
  saveTexture(bpDir,`assets/aurum_backpacks/textures/item/${name}.png`,gradientV(16,16,[c[0]+30,c[1]+30,c[2]+30],c));
  saveModel(bpDir,`assets/aurum_backpacks/models/item/${name}.json`,itemModel('aurum_backpacks',name));
}

// ======= FARMING =======
const farmDir=path.join(ROOT,'mods/aurum-farming/src/main/resources');
saveTexture(farmDir,'assets/aurum_farming/icon.png',icon64(132,204,22));
const farmItems={tomato_seeds:[60,120,30],corn_seeds:[200,180,50],strawberry_seeds:[180,40,60],onion_seeds:[180,160,100],rice_seeds:[200,200,180],tomato:[220,50,30],corn:[240,210,60],strawberry:[220,40,60],onion:[200,170,120],rice:[230,230,210],fertilizer:[100,80,50],tomato_soup:[200,70,40],roasted_corn:[220,180,40],strawberry_tart:[200,80,100],onion_rings:[200,170,100],farmers_paella:[180,140,60],delivery_contract:[230,220,180]};
for(const [n,c] of Object.entries(farmItems)){
  saveTexture(farmDir,`assets/aurum_farming/textures/item/${n}.png`,solidColor(16,16,...c));
  saveModel(farmDir,`assets/aurum_farming/models/item/${n}.json`,itemModel('aurum_farming',n));
}
saveTexture(farmDir,'assets/aurum_farming/textures/block/kitchen_table.png',checkerboard(16,16,[180,140,90,255],[140,100,60,255]));
saveModel(farmDir,'assets/aurum_farming/models/block/kitchen_table.json',blockModel('aurum_farming','kitchen_table'));
saveModel(farmDir,'assets/aurum_farming/models/item/kitchen_table.json',blockItemModel('aurum_farming','kitchen_table'));
saveModel(farmDir,'assets/aurum_farming/blockstates/kitchen_table.json',blockstate('aurum_farming','kitchen_table'));
// Crop blockstates/models  
for(const crop of ['tomato_crop','corn_crop','strawberry_crop','onion_crop','rice_crop']){
  saveTexture(farmDir,`assets/aurum_farming/textures/block/${crop}.png`,solidColor(16,16,40,120,30,200));
  saveModel(farmDir,`assets/aurum_farming/models/block/${crop}.json`,{parent:"minecraft:block/crop",textures:{crop:`aurum_farming:block/${crop}`}});
  saveModel(farmDir,`assets/aurum_farming/blockstates/${crop}.json`,blockstate('aurum_farming',crop));
}

// ======= OCEANS =======
const oceDir=path.join(ROOT,'mods/aurum-oceans/src/main/resources');
saveTexture(oceDir,'assets/aurum_oceans/icon.png',icon64(56,189,248));
const oceItems={pearl:[220,220,240],black_pearl:[40,30,50],shell:[230,200,170],starfish:[240,140,80],fishing_net:[180,170,140],crab_meat:[220,100,80],cooked_crab:[180,80,40],pearl_amulet:[200,200,230]};
for(const [n,c] of Object.entries(oceItems)){
  saveTexture(oceDir,`assets/aurum_oceans/textures/item/${n}.png`,solidColor(16,16,...c));
  saveModel(oceDir,`assets/aurum_oceans/models/item/${n}.json`,itemModel('aurum_oceans',n));
}
const oceBlocks={coral_lamp:[0,200,200],dock_planks:[120,80,50],barnacle_block:[160,160,150],sunken_chest:[100,80,60]};
for(const [n,c] of Object.entries(oceBlocks)){
  saveTexture(oceDir,`assets/aurum_oceans/textures/block/${n}.png`,checkerboard(16,16,[c[0]+20,c[1]+20,c[2]+20,255],c.concat([255])));
  saveModel(oceDir,`assets/aurum_oceans/models/block/${n}.json`,blockModel('aurum_oceans',n));
  saveModel(oceDir,`assets/aurum_oceans/models/item/${n}.json`,blockItemModel('aurum_oceans',n));
  saveModel(oceDir,`assets/aurum_oceans/blockstates/${n}.json`,blockstate('aurum_oceans',n));
}

// ======= MACHINES =======
const macDir=path.join(ROOT,'mods/aurum-machines/src/main/resources');
saveTexture(macDir,'assets/aurum_machines/icon.png',icon64(148,163,184));
const macItems={iron_gear:[180,180,180],gold_gear:[240,200,40],motor:[100,100,110],circuit:[30,120,60],copper_wire:[200,120,60],iron_dust:[200,200,200],gold_dust:[240,210,60],copper_dust:[210,130,70],iron_plate:[190,190,195],gold_plate:[240,210,50],copper_plate:[210,140,80]};
for(const [n,c] of Object.entries(macItems)){
  saveTexture(macDir,`assets/aurum_machines/textures/item/${n}.png`,solidColor(16,16,...c));
  saveModel(macDir,`assets/aurum_machines/models/item/${n}.json`,itemModel('aurum_machines',n));
}
const macBlocks={crusher:[100,100,110],press:[110,110,120],conveyor:[140,140,145],sorter:[120,120,130],pipe:[170,120,70],generator:[140,50,50]};
for(const [n,c] of Object.entries(macBlocks)){
  saveTexture(macDir,`assets/aurum_machines/textures/block/${n}.png`,checkerboard(16,16,[c[0]+30,c[1]+30,c[2]+30,255],c.concat([255])));
  saveModel(macDir,`assets/aurum_machines/models/block/${n}.json`,blockModel('aurum_machines',n));
  saveModel(macDir,`assets/aurum_machines/models/item/${n}.json`,blockItemModel('aurum_machines',n));
  saveModel(macDir,`assets/aurum_machines/blockstates/${n}.json`,blockstate('aurum_machines',n));
}

// Generator needs LIT blockstate
saveModel(macDir,'assets/aurum_machines/blockstates/generator.json',{variants:{"lit=false":{"model":"aurum_machines:block/generator"},"lit=true":{"model":"aurum_machines:block/generator"}}});

console.log('All textures, models, blockstates generated!');
