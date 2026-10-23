# R3.4 後續核對

本文件下方保存R3.2交接時間點。最終CI39515675883對應source43aff1d、synthetic merged423a7e：blocking build SUCCESS，experimental Runtime FAILURE，App5PASS／Host15PASS9FAIL8SKIP。不是Runtime全綠。latest-main整合另見 [R3.4報告](R3_4_IMAGE_INTEGRATION.md)，不覆寫原始鑑識與舊Alpha。

# KU-Yin R3.2 圖片交接

Base main `8369da25fa54ab762dfceed457e71a08767a642e`；branch `fix/r3-2-image-integrity`；核心Launcher/tool checkpoint `2322bedde224aa5172b3d725d3b9c85f0cb83762`；CI dependency checkpoint `545210bc1cf8801f6b0c5d0a5d4d976b93ada7fc`。Draft [PR24](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/pull/24)，未合併。

## 已完成

十Launcher fallback由原adaptive資產生成；PNG由原GreetingScreenshotTest／Roborazzi重建；舊資產仍在Git/external evidence，不採不可靠inverse候選。[來源與驗證](R3_2_IMAGE_INTEGRITY.md)／[生成manifest](../community/media/GENERATED_LAUNCHER_MANIFEST.json)／[PNG manifest](../community/media/ROBORAZZI_RENDER_MANIFEST.json)。Runtime宣傳圖仍0，不把PNG充當手機操作截圖。

Fresh本機：全Python113 PASS（48，PNG/provenance增量後）；11contracts PASS；4focused PASS（46）。PNG RED40b（原bytes完全解碼失敗）→既有Roborazzi真實record36→GREEN41b；provenance舊test誤收RED45→固定allowlist/hash/classification/metadata assertions GREEN46，47四種負向變異均拒絕。

Native bootstrap用新獨立cache、同prebuilt3587ba3355711f0aca50136e787719f6562676b8／libchewing a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c。Gradle24 fresh執行exit0：App265總數=255PASS/10SKIP、Host37PASS；43後續相同source JVM結果FROM-CACHE，未冒稱重執行；XML另保存。Debug/兩 instrumentation/Host APK建置成功。十輸出bytes全部對應APK內同SHA資源。

本機驗證APK（非發行候選）：20003／0.2.0-aie.1，SHA32624b76b12f07f8019d053feb35c9294ffa80477f03a45ba937eac118009310，Debug cert46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94。未安裝／發布，不冒稱新的可升級RC；舊公開Alpha不動。

Reviewer對十圖/two P2 follow-up spec/quality PASS；PNG增量及provenance P2亦複核關閉，兩軸PASS，僅限圖片切片。CI39514430936@545210bc blocking build SUCCESS，experimental runtime尚未取得最終結果；這是PNG增量前的source，不作最終HEAD全綠。最終推送後以PR24 exact HEAD CI為準，不把build當runtime。

## 保護與下一步

無Kotlin/JNI/native pin、Dachen、candidate/editor contract、簽章、version或資料更改。QR／實機／H1／key custody／完整license仍OPEN；RC／Stable NO_GO。沒有合併PR10/12/20或建立Release。

外部原始證據：`/Users/huang/Desktop/KU-Yin-R3.2-20261021-k4xtcv1s`。下一步只讀fresh CI及review；原R3.1／其他工區保留，不重跑全套Binding A/B。
