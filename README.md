# GoalApp

Modern bir Android hedef takip uygulaması. Günlük hedeflerinizi oluşturun, ilerleyişinizi takip edin ve başarılarınızı görselleştirin.

## 🎯 Özellikler

### ✅ Temel Özellikler
- **Günlük Hedef Yönetimi**: Hedefler oluşturun, güncelleyin ve silin
- **İlerleme Takibi**: Slider ile kolayca ilerleme güncelleyin
- **Otomatik Arşivleme**: Önceki günlere ait hedefler otomatik olarak arşivlenir
- **Bildirim Desteği**: Hedefleriniz için hatırlatıcı kurabilirsiniz
- **Tarih Seçici**: Gelecek günler için hedef oluşturabilirsiniz

### 📊 Görselleştirme ve Analiz
- **Haftalık Özet Grafiği**: Son 7 günün üretkenliğini görselleştiren ısı haritası
  - Her günün toplam hedef sayısı
  - Tamamlanma oranına göre renklendirme (Yeşil: %80+, Turuncu: %50+, Kırmızı: %50-)
  - Dinamik bar yükseklikleri
  
- **Günlük Özet Kartı**: Seçili gün için toplam hedefler, tamamlananlar ve ortalama ilerleme

### 📝 Hedef Detayları ve Notlar
- **Not Ekleme**: Her hedefe özel notlar ekleyebilir ve düzenleyebilirsiniz
  - Hedefe neden ulaşamadığınızı kaydedin
  - Motivasyon notları ekleyin
  - İlerleyişinizi detaylandırın

### 🎨 UI/UX
- **Material Design 3**: Modern ve temiz arayüz
- **Dark Mode Desteği**: Sistem temasına uyum
- **Renk Kişiselleştirmesi**: Her hedef için farklı renk seçimi
- **Animasyonlar**: Progress güncellemelerinde smooth animasyonlar

## 🏗️ Teknik Detaylar

### Mimari
- **MVVM Pattern**: ViewModel ile UI katmanının ayrılması
- **Clean Architecture**: Repository pattern ile data katmanı soyutlaması
- **UI State Management**: Combine ile reactive state yönetimi

### Teknolojiler
- **Jetpack Compose**: Modern declarative UI
- **Room Database**: Local data persistence
- **Hilt**: Dependency injection
- **Kotlin Coroutines & Flow**: Asenkron operasyonlar
- **WorkManager**: Bildirim zamanlama
- **Material 3**: UI komponenleri

### Proje Yapısı
```
app/
├── data/
│   ├── GoalEntity.kt         # Room entity ve model
│   ├── GoalDao.kt            # Database operations
│   ├── GoalRepository.kt     # Data layer abstraction
│   └── SampleGoalsProvider.kt # Test data
├── ui/
│   ├── home/                 # Anasayfa - Günlük hedefler
│   ├── archive/              # Arşiv ekranı - Haftalık grafik
│   ├── detail/               # Hedef detayı - Notlar
│   ├── add/                  # Hedef ekleme
│   └── components/           # Reusable components
├── notification/             # Bildirim sistemi
└── util/                     # Yardımcı fonksiyonlar
```

## 🚀 Kurulum

### Gereksinimler
- Android Studio Hedgehog | 2023.1.1 veya üzeri
- JDK 17
- Android SDK 34
- Gradle 8.14.3

### Build
```bash
./gradlew assembleDebug
```

### Yükleme
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 📱 Ekran Görüntüleri

### Anasayfa
- Günlük hedeflerin listesi
- İlerleme yüzdeleri
- Tamamlanma durumu

### Arşiv
- Haftalık üretkenlik grafiği
- Geçmiş hedeflerin listesi
- Günlük istatistikler

### Hedef Detayı
- Dairesel progress indicator
- İlerleme güncelleyici slider
- Not ekleme/düzenleme

## 🔄 Veritabanı Migrations

Proje versiyonlar arası migration'ları destekler:
- **v1 → v2**: isArchived, archivedAt alanları
- **v2 → v3**: Bildirim alanları (notificationEnabled, notificationHour, notificationMinute)
- **v3 → v4**: notes alanı

## 📝 Lisans

Bu proje eğitim amaçlıdır.

## 🎓 Öğrenilen Konular

- Jetpack Compose ile modern UI geliştirme
- Room Database ile local storage
- WorkManager ile arka plan işlemleri
- Hilt ile dependency injection
- Coroutines ve Flow ile reactive programming
- Material Design 3 prensipleri
- MVVM ve Clean Architecture

