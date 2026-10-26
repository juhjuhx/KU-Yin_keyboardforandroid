#!/usr/bin/env python3
"""Export original KU-Yin vector layouts; never produces device evidence.
Requires rsvg-convert. Original hero geometry is embedded unchanged; no fonts
or external raster assets are bundled. PNG pixels depend on installed fonts.
"""
import copy
import hashlib
import json
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/community/media/social'
HERO = ROOT / 'docs/community/media/kuyin-readme-hero.svg'
NS = 'http://www.w3.org/2000/svg'
ET.register_namespace('', NS)

SPECS = [
    ('cover-portrait', 1080, 1350, ['把日常注音輸入', '做成開源工程'], ['台灣大千 41 鍵', '離線中文組字與英文補全', 'Experimental Alpha · Debug APK']),
    ('story', 1080, 1920, ['KU-Yin 酷音', '一起測試與維護'], ['Android 大千注音鍵盤', 'Kotlin / Compose / libchewing', '先用合成文字，再回報問題']),
    ('cover-landscape', 1920, 1080, ['一套可以持續維護的', 'Android 注音鍵盤'], ['開源 · 離線 · 大千注音', 'Experimental Alpha，完整實機驗收仍未完成']),
    ('02-candidates', 1080, 1350, ['候選瀏覽', '不只是一排文字'], ['單列捲動、展開與上下頁', '用快照身分防止過期選取', '功能介紹；手機驗收待補']),
    ('03-architecture', 1080, 1350, ['分清組字、畫面', '與編輯器寫入'], ['Compose 呈現與事件', 'Session / JNI / libchewing', 'Service 統一處理 Editor effects']),
    ('04-offline', 1080, 1350, ['核心輸入在本機', '資料責任要清楚'], ['英文 10,000 詞字首補全', '敏感欄位採限制學習政策', '學習完整清除尚未支援；D2D 待驗']),
    ('05-maintenance', 1080, 1350, ['讓問題能重現', '讓來源能追溯'], ['原始碼、上游與建置文件', 'CI 綠燈不等於手機驗收', 'GitHub Issues 歡迎合成案例']),
]


def element(parent, name, attrs, text=None):
    e = ET.SubElement(parent, '{'+NS+'}'+name, {k:str(v) for k,v in attrs.items()})
    e.text = text
    return e


def make(name, width, height, title, body):
    root = ET.Element('{'+NS+'}svg', dict(width=str(width), height=str(height), viewBox=f'0 0 {width} {height}', role='img', **{'aria-labelledby':'social-title social-desc'}))
    element(root, 'title', {'id':'social-title'}, 'KU-Yin 酷音｜'+''.join(title))
    element(root, 'desc', {'id':'social-desc'}, '原創品牌與技術介紹，非Android操作截圖。Experimental Alpha，尚未完成完整實機驗收。')
    element(root, 'rect', {'width':width,'height':height,'fill':'#fbf9f4'})
    element(root, 'rect', {'x':0,'y':0,'width':18,'height':height,'fill':'#165950'})
    margin=72 if width==1080 else 96
    hero=copy.deepcopy(ET.parse(HERO).getroot());hero.set('x',str(margin));hero.set('y','72');hero.set('width',str(width-2*margin));hero.set('height',str((width-2*margin)*360/1280));root.append(hero)
    landscape=width>height
    y=675 if height==1920 else 455 if not landscape else 625
    size=78 if not landscape else 64
    group=element(root,'g',{'font-family':"'Noto Sans TC','PingFang TC','Microsoft JhengHei',sans-serif",'fill':'#153f3b'})
    for line in title:
        element(group,'text',{'x':margin,'y':y,'font-size':size,'font-weight':800},line);y+=size*1.28
    y+=65 if not landscape else 15
    for line in body:
        element(group,'circle',{'cx':margin+9,'cy':y-12,'r':7,'fill':'#9b8bb8'})
        element(group,'text',{'x':margin+34,'y':y,'font-size':36 if width==1080 else 38,'fill':'#315951'},line);y+=74 if not landscape else 52
    footer=height-175
    element(root,'rect',{'x':margin,'y':footer,'width':width-2*margin,'height':2,'fill':'#d6cfe4'})
    element(group,'text',{'x':margin,'y':footer+58,'font-size':27,'font-weight':700},'github.com/juhjuhx/KU-Yin_keyboardforandroid')
    element(group,'text',{'x':margin,'y':footer+108,'font-size':23,'fill':'#5c5569'},'品牌／技術介紹 · 非操作截圖 · v0.2.0-aie.1 Alpha')
    return ET.tostring(root, encoding='utf-8', xml_declaration=True)


def main():
    OUT.mkdir(parents=True,exist_ok=True)
    rows=[]
    version=subprocess.check_output(['rsvg-convert','--version'],text=True).strip()
    for name,w,h,title,body in SPECS:
        svg=OUT/(name+'.svg');png=OUT/(name+'.png');svg.write_bytes(make(name,w,h,title,body))
        subprocess.run(['rsvg-convert','-w',str(w),'-h',str(h),'-o',str(png),str(svg)],check=True)
        rows.append({'id':name,'category':'BRAND_RENDER_NOT_RUNTIME','width':w,'height':h,'svg':svg.name,'png':png.name,'svg_sha256':hashlib.sha256(svg.read_bytes()).hexdigest(),'png_sha256':hashlib.sha256(png.read_bytes()).hexdigest(),'alt':'KU-Yin 酷音：'+''.join(title)+'；品牌／技術示意，非操作截圖；Experimental Alpha。'})
    manifest={'schema':1,'source_commit_baseline':'08e4f69820e2ac9631f9fb1de7153824956d8f77','public_release':'v0.2.0-aie.1','hero_source':'../kuyin-readme-hero.svg','hero_sha256':hashlib.sha256(HERO.read_bytes()).hexdigest(),'generator':'scripts/generate_publication_media.py','renderer':version,'fonts':'system fallback only; no font binaries distributed; PNG pixel identity depends on rendering environment','origin':'original KU-Yin SVG geometry reused unchanged; new vector typography/layout; no third-party screenshot or foundation logo','license':'repository LGPL-2.1 applies to original vector sources; inherited hero source declaration; not store media-rights certification','screenshot_count':0,'assets':rows}
    (OUT/'ASSET_LICENSE_MANIFEST.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
    print('Exported',len(rows),'unique SVG/PNG pairs; five portrait cards share cover01, no duplicate copy.')

if __name__=='__main__':main()
