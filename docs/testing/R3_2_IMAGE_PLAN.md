# R3.2 圖片切片進度

Base main8369da25fa54ab762dfceed457e71a08767a642e，Issue23，分支fix/r3-2-image-integrity。

- [x] 原13圖／Git／公開APK／66候選鑑識；原件保存。
- [x] 使用者授權無原件時自行編碼；沿用existing adaptive識別，不用inverse候選。
- [x] 原損壞RED、十launcher雙decoderGREEN、生成與來源manifest。
- [x] independent review兩P2 RED/GREEN；follow-up spec/quality PASS。
- [x] PNG由既有Roborazzi record修復；完整雙decoder與來源manifest、reviewer P2 RED/GREEN。
- [x] 全Python113、11contracts PASS。
- [x] 本機App JVM255PASS10SKIP／Host37PASS、Debug/Host/instrumentation建置完成；十圖與APK內bytes相同。
- [ ] PR最終HEAD CI：仍須exact HEAD結果，不把中間source build成功冒充runtime。
- [x] 獨立圖片Draft PR24／source checkpoints／handoff。

圖片本身不解除Release NO_GO；實機顯示未閉合，不製造假截圖。
