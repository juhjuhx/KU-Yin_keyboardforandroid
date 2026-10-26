# KU-Yin 社群封面與圖卡

這裡都是**原創品牌／技術排版，非App操作截圖**。配色沿用KU-Yin青綠，搭配已批准暖白／淡紫；沒有複製其他鍵盤、基金會Logo或外部相片。

|用途|尺寸|PNG／可編輯SVG|
|---|---|---|
|主封面／IG第1張|1080×1350|[PNG](cover-portrait.png)／[SVG](cover-portrait.svg)|
|Story|1080×1920|[PNG](story.png)／[SVG](story.svg)|
|橫式封面|1920×1080|[PNG](cover-landscape.png)／[SVG](cover-landscape.svg)|
|IG第2張：候選|1080×1350|[PNG](02-candidates.png)／[SVG](02-candidates.svg)|
|IG第3張：架構|1080×1350|[PNG](03-architecture.png)／[SVG](03-architecture.svg)|
|IG第4張：離線|1080×1350|[PNG](04-offline.png)／[SVG](04-offline.svg)|
|IG第5張：維護|1080×1350|[PNG](05-maintenance.png)／[SVG](05-maintenance.svg)|

共7個唯一版型；主封面同時作圖卡1，不再複製一份。文案以[POSTS_READY](../../POSTS_READY.md)為唯一入口，發文前核對公開Release仍為v0.2.0-aie.1，不把CI測試包說成已上線新版。

來源、SHA、尺寸、Alt、授權與renderer見[ASSET_LICENSE_MANIFEST](ASSET_LICENSE_MANIFEST.json)。來源SVG由專案原創Hero幾何及文字排版組成；字型採系統fallback，不散布字型binary，PNG可視字形受render環境影響。授權沿用倉庫對原創vector source的LGPL-2.1及Hero來源聲明；這不是Google Play/store完整美術權利認證。

重建：安裝`rsvg-convert`後，在repo根執行`python3 scripts/generate_publication_media.py`。只匯出品牌圖，不產生裝置證據。
