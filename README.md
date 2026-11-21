# Çizim Atölyesi

6–10 yaş arası çocuklar için yatay ekranda kullanılan bir Android çizim uygulaması. Solda araç paneli,
sağda renk paneli, ortada tuval bulunur. Tuval dolu olsa bile akıcı çalışması önceliklidir.

## Özellikler

### Çizim (1. aşama)

- Kurşun kalem, fırça, keçeli kalem, kaligrafi kalemi (yassı eğik uç; yöne göre kalınlaşıp incelir), pastel boya, sprey, sim tozu (seçili renkte ya da gökkuşağı renginde çizgi üzerinde parıltı taneleri ve küçük yıldızlar), silgi ve boya kovası
- Boya kovası düğmesine dokununca desen menüsü açılır: düz boya ya da seçili renk ile beyazdan oluşan dalgalı çizgiler, çapraz çizgiler, bulutlar, çizgili, puantiyeli, kareli, yıldızlar ve kalpler. Desenli bir alana yeniden dokunulunca kova desenin iki rengini birlikte tanır ve alanın tamamını yeni renge ya da desene boyar
- Altı fırça boyutu (çok inceden çok kalına); seçili boyut panelde görünür, diğerleri açılır menüden seçilir
- 24 renklik palet; seçilen renk kaleme veya tuvalin arka planına uygulanabilir
- Paletin başındaki 🎨 düğmesi sağdan kayan geniş renk panelini açar: tonlu renk ızgarası, renk tekerleği ve açıklık çubuğu. Panel seçim yapınca açık kalır, dışına dokununca kapanır; paletin dışından seçilen renk düğmenin içinde görünür
- Geniş paneldeki damlalık düğmesi tuvalden renk alır: düğmeye basınca panel kapanır, tuvale dokunup parmak gezdirilirken parmağın üstündeki büyüteç altındaki rengi gösterir. Parmak kalkınca o renk seçilir ve panel yeniden açılır
- 5 adımlık geri al ve yinele
- Çizim tuvalin dışına taşmaz; ekran dönse bile çizim kaybolmaz

### Galeri ve kayıt (2. aşama)

- Uygulama galeriyle açılır; resimlerin adı yoktur, en son düzenlenen en başta görünür
- Resimler otomatik kaydedilir: son değişiklikten 2 saniye sonra, galeriye dönerken ve uygulama arka plana geçerken
- Silinen resim çöp kutusuna gider, oradan geri getirilebilir; 24 saat sonra kalıcı olarak silinir
- 📤 düğmesiyle resim cihaz galerisine (`Pictures/Çizim Atölyesi`) PNG olarak aktarılır

### Boyama sayfaları (3. aşama)

- Galerinin üst satırında "Yeni resim" düğmesinin sağında, sola–sağa kaydırılan tek satırda 32 boyama sayfası bulunur (yeni eklenenler başta); kaydedilen resimler bunun altında "Resimlerim" başlığıyla listelenir: kedi, balık, ev, araba, çiçek, kelebek, dinozor, güneş, fayton, baykuş, kaplumbağa, roket, fil, yelkenli, tavşan, peri, midilli, bisikletli kız, prenses, balo elbiseli prenses, unicorn, köpek, kale, itfaiye aracı, tren, mandala, robot, astronot, ejderha, çiçek mandala, güneş mandala (oval), dalgalı mandala
- Sayfaya dokununca o sayfadan yeni bir resim açılır; sayfanın kendisi değişmez
- Resim ancak boyanınca kaydedilir; açılıp kapatılan sayfa galeride boş resim bırakmaz
- Çizgiler her zaman en üstte durur: fırça çizgileri örtmez, silgi çizgileri silmez
- Boya kovası çizgileri duvar sayar ve rengi çizgilerin yumuşak kenarlarının altına kadar doldurur
- Küçük resim ve dışa aktarılan PNG çizgileri de içerir

## Gereksinimler

- JDK 17 veya üstü (Android Studio'nun içindeki JBR yeterlidir)
- Android SDK: platform 37 (derleme için)
- Android 10 (API 29) veya üstü bir cihaz ya da emülatör

`local.properties` dosyasında `sdk.dir` ayarlı olmalıdır; Android Studio bu dosyayı kendisi oluşturur.

## Çalıştırma

Derleme:

```bash
./gradlew assembleDebug
```

Birim testleri:

```bash
./gradlew testDebugUnitTest
```

Bağlı cihaza yükleme:

```bash
./gradlew installDebug
```

Windows'ta `./gradlew` yerine `gradlew.bat` kullanılabilir.

## Yayın paketi (Google Play)

Play'e `.aab` paketi yüklenir. Paket bir **yükleme anahtarıyla** imzalanır; asıl imzayı Play App Signing ile
Google atar. Anahtar dosyası ve şifreleri repoya girmez (`.gitignore` içinde `*.jks`, `*.keystore`).

1. Anahtar bir kez oluşturulur ve repo dışında, yedeklenen bir yerde saklanır. Şifreyi `keytool` sorar:

   ```bash
   keytool -genkeypair -v -keystore ~/keys/drawing-workshop-upload.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
   ```

2. Bilgiler kullanıcının `~/.gradle/gradle.properties` dosyasına yazılır (proje içindeki dosyaya değil):

   ```properties
   drawingWorkshopUploadStoreFile=C:/Users/<kullanıcı>/keys/drawing-workshop-upload.jks
   drawingWorkshopUploadStorePassword=...
   drawingWorkshopUploadKeyAlias=upload
   drawingWorkshopUploadKeyPassword=...
   ```

   Aynı değerler `ORG_GRADLE_PROJECT_drawingWorkshopUploadStoreFile` gibi ortam değişkenleriyle de verilebilir.
   Bilgiler yoksa paket imzasız üretilir ve Play kabul etmez.

3. Paket üretilir: `app/build/outputs/bundle/release/app-release.aab`

   ```bash
   ./gradlew bundleRelease
   ```

Her yüklemede `app/build.gradle.kts` içindeki `versionCode` bir artırılır. Gizlilik politikası
`store/drawing-workshop/privacy.html` dosyasıdır ve GitHub Pages'te yayınlanır.

## Boyama sayfalarını üretme

Boyama sayfaları elle düzenlenen SVG kaynaklarından (`tools/coloring/pages/<id>.svg`) üretilir.
Betik, öndeki beyaz şekillerin arkasında kalan çizgileri kırpar ve sonucu Android vektörüne
(`res/drawable/coloring_<id>.xml`) yazar. Kaynak biçimi, ölçü kuralları ve daha önce düzeltilen hatalar
[`tools/coloring/DRAWING_GUIDE.md`](tools/coloring/DRAWING_GUIDE.md) dosyasındadır; yeni sayfaya başlamadan önce okunmalıdır.

```bash
pip install -r tools/coloring/requirements.txt
python tools/coloring/build.py            # tüm sayfalar
python tools/coloring/build.py cat fish   # yalnızca verilenler
```

Önizleme ve boya kovası bölge denetimi için `tools/coloring` klasöründe yerel bir sunucu açılır ve
`preview.html` tarayıcıda açılır (`?page=cat` ile tek sayfa). Kırmızı bölgeler çok küçüktür; yeni
sayfada çok küçük bölge kalmamalıdır.

```bash
cd tools/coloring && python -m http.server 8765
```

Yeni sayfa `data/coloring/BuiltInColoringPageCatalog.kt` listesine eklenir. Kayıtlar sayfa kimliğini
sakladığı için var olan kimlikler değiştirilmez.

## Proje yapısı

- `domain/` — Android'den bağımsız mantık: araç modelleri, geri al geçmişi, çizgi yumuşatma, boya kovası,
  çöp kutusu süresi (`domain/drawing`) ve ölçekleme hesabı (`domain/geometry`)
- `data/` — Room veritabanı, resim dosyalarının iç depoda saklanması, kayıt kuyruğu ve dışa aktarma
- `data/coloring/` — Boyama sayfası kataloğu ve sayfaların çizimi; sayfalar `res/drawable/coloring_*.xml` vektörleridir
- `ui/drawing/engine/` — Çizim motoru: ekran dışı bitmap, fırça türleri, bölge anlık görüntüleri
- `ui/drawing/` — Ekran, paneller, ViewModel, otomatik kayıt ve dokunmatik görünüm
- `ui/gallery/` — Galeri ve çöp kutusu ekranları
- `ui/navigation/` — Ekranlar arası gezinme
- `ui/components/` — Ortak arayüz bileşenleri
- `di/` — Hilt modülleri
