# MoneyTrack (Android)

Aplikasi pencatat keuangan pribadi native Android, tema gelap, database SQLite lokal.

## Fitur
- Dashboard: saldo sepanjang waktu, pemasukan/pengeluaran bulan ini
- Catat pemasukan dan pengeluaran
- Riwayat harian dengan edit dan hapus + konfirmasi
- Kategori bawaan dan kategori buatan sendiri
- Ringkasan laporan bulanan dengan visual bar sederhana
- Pengingat harian dengan jam yang dapat diatur; menampilkan total pengeluaran hari ini
- Jadwal pengingat dijadwalkan ulang setelah perangkat reboot

## Build APK
1. Ekstrak ZIP.
2. Buka folder `MoneyTrack` di Android Studio.
3. Gunakan JDK 17 dan Android SDK Platform 35.
4. Tunggu Gradle Sync selesai.
5. Pilih Build > Build Bundle(s) / APK(s) > Build APK(s).
6. APK debug ada di `app/build/outputs/apk/debug/app-debug.apk`.

## Catatan
- Android 13+ meminta izin notifikasi saat pertama dibuka. Izinkan agar pengingat tampil.
- Android 12+ dapat membatasi alarm presisi. Jika akses alarm presisi tidak diizinkan, aplikasi memakai alarm inexact sebagai fallback; waktu dapat sedikit terlambat.
- Beberapa merek HP memiliki optimasi baterai agresif. Izinkan notifikasi dan pengecualian optimasi baterai bila jadwal sering tertunda.
- Data disimpan lokal di `moneytrack.db`; uninstall aplikasi dapat menghapus data. Buat backup sebelum reset perangkat.
- Laporan pada versi awal ini merangkum bulan berjalan. Grafik menggunakan bar perbandingan, bukan grafik harian.

## Build APK hanya dengan HP Android (tanpa laptop)
Cara termudah adalah memakai GitHub Actions sebagai komputer build di cloud:
1. Buat/login akun GitHub lewat browser HP.
2. Buat repository baru, misalnya `MoneyTrack`.
3. Upload seluruh isi folder proyek (termasuk folder tersembunyi `.github/workflows`) ke repository. Jika upload via browser tidak menampilkan folder tersembunyi, pastikan file `.github/workflows/build-apk.yml` ikut dibuat melalui menu Add file > Create new file dengan path tersebut.
4. Buka tab Actions di repository dan jalankan workflow `Build MoneyTrack APK` (Run workflow). Jika diminta, aktifkan Actions.
5. Setelah workflow sukses, buka hasil run dan unduh artifact `MoneyTrack-debug-apk`.
6. Ekstrak ZIP artifact, lalu buka `app-debug.apk` di HP dan izinkan instalasi dari sumber tersebut jika Android meminta.

Build berjalan di server GitHub; HP hanya dipakai untuk upload source, menjalankan workflow, dan mengunduh APK. Ini membutuhkan internet dan akun GitHub. Jangan unggah password, API key, atau data keuangan pribadi ke repository.
