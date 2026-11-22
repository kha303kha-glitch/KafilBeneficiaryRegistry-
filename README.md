# Kafil Beneficiary Registry — Offline Android Jetpack Compose App

Complete offline-first Android application built in **Kotlin + Jetpack Compose (Material 3)** with full **Arabic RTL** support, **Room** SQLite persistence, instant multi-column search, and **Apache POI (.xlsx)** Excel import/export via Android **Storage Access Framework (SAF)**.

## Architecture & Key Files

- `app/src/main/java/com/kafil/beneficiaries/data/local/Beneficiary.kt`: Room `@Entity` (`beneficiaries`) with unique index on `uniqueCode` and search indices on `fullName`, `nationalId`, and `phoneNumber`.
- `app/src/main/java/com/kafil/beneficiaries/data/local/BeneficiaryDao.kt`: `Flow<List<Beneficiary>>` instant `LIKE` query filtering `fullName`, `nationalId`, `phoneNumber`, or `uniqueCode`, plus transactional `upsertByUniqueCode`.
- `app/src/main/java/com/kafil/beneficiaries/data/local/AppDatabase.kt`: Offline Room database (`kafil_beneficiaries_offline.db`).
- `app/src/main/java/com/kafil/beneficiaries/data/excel/ExcelHelper.kt`: Apache POI `XSSFWorkbook` `.xlsx` exporter (with `isRightToLeft = true` worksheet) and flexible header-mapping importer using Android `ContentResolver` + SAF document URIs.
- `app/src/main/java/com/kafil/beneficiaries/ui/BeneficiaryScreen.kt`: Material 3 RTL Compose screen with instant search bar, summary strip, `ActivityResultContracts.CreateDocument` & `OpenDocument` SAF launchers, add/edit form dialog, and delete confirmation.

## Requirements

- Android Studio Ladybug / Meerkat or newer
- JDK 17
- `minSdk = 26`, `compileSdk = 35`, `targetSdk = 35`
- Open the unzipped `android-project` folder directly in Android Studio and sync Gradle.
