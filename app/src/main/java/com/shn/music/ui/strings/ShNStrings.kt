package com.shn.music.ui.strings

import com.shn.music.core.settings.AppLanguage

class ShNStrings(
    val language: AppLanguage
) {

    val isArabic: Boolean
        get() = language == AppLanguage.ARABIC

    val appName: String
        get() = if (isArabic) "موسيقى شهاب" else "SHN Music"

    val home: String
        get() = if (isArabic) "الرئيسية" else "Home"

    val songs: String
        get() = if (isArabic) "الأغاني" else "Songs"

    val albums: String
        get() = if (isArabic) "الألبومات" else "Albums"

    val artists: String
        get() = if (isArabic) "الفنانون" else "Artists"

    val genres: String
        get() = if (isArabic) "الأنواع" else "Genres"

    val folders: String
        get() = if (isArabic) "المجلدات" else "Folders"

    val playlists: String
        get() = if (isArabic) "قوائم التشغيل" else "Playlists"

    val favorites: String
        get() = if (isArabic) "المفضلة" else "Favorites"

    val recent: String
        get() = if (isArabic) "الأخيرة" else "Recent"

    val mostPlayed: String
        get() = if (isArabic) "الأكثر تشغيلًا" else "Most played"

    val searchHint: String
        get() = if (isArabic) {
            "ابحث عن أغنية أو فنان أو ألبوم"
        } else {
            "Search songs, artists, albums"
        }

    val search: String
        get() = if (isArabic) "بحث" else "Search"

    val settings: String
        get() = if (isArabic) "الإعدادات" else "Settings"

    val queue: String
        get() = if (isArabic) "قائمة الانتظار" else "Queue"

    val nowPlaying: String
        get() = if (isArabic) "التشغيل الآن" else "Now Playing"

    val refresh: String
        get() = if (isArabic) "تحديث المكتبة" else "Refresh library"

    val ready: String
        get() = if (isArabic) "جاهز للتشغيل" else "Ready to play"

    val chooseSong: String
        get() = if (isArabic) {
            "اختر أغنية من مكتبتك"
        } else {
            "Choose a song from your library"
        }

    val recentlyPlayed: String
        get() = if (isArabic) "تم تشغيلها مؤخرًا" else "Recently Played"

    val recentlyAdded: String
        get() = if (isArabic) "أضيفت مؤخرًا" else "Recently Added"

    val library: String
        get() = if (isArabic) "المكتبة" else "Library"

    val seeAll: String
        get() = if (isArabic) "عرض الكل" else "See all"

    val emptyQueue: String
        get() = if (isArabic) "قائمة الانتظار فارغة" else "Queue is empty"

    val clear: String
        get() = if (isArabic) "مسح" else "Clear"

    val nothingPlaying: String
        get() = if (isArabic) {
            "لا توجد أغنية قيد التشغيل"
        } else {
            "Nothing is playing"
        }

    val unknownArtist: String
        get() = if (isArabic) "فنان غير معروف" else "Unknown artist"

    val noResults: String
        get() = if (isArabic) "لا توجد نتائج" else "No results"

    val tryAnother: String
        get() = if (isArabic) {
            "جرّب كلمة بحث أخرى"
        } else {
            "Try another search"
        }

    val languageLabel: String
        get() = if (isArabic) "اللغة" else "Language"

    val arabic: String
        get() = "العربية"

    val english: String
        get() = "English"

    val appearance: String
        get() = if (isArabic) "المظهر" else "Appearance"

    val system: String
        get() = if (isArabic) "النظام" else "System"

    val light: String
        get() = if (isArabic) "فاتح" else "Light"

    val dark: String
        get() = if (isArabic) "داكن" else "Dark"

    val savePosition: String
        get() = if (isArabic) {
            "حفظ موضع التشغيل"
        } else {
            "Save playback position"
        }

    val savePositionDesc: String
        get() = if (isArabic) {
            "استئناف الأغنية من آخر موضع"
        } else {
            "Resume songs from their last position"
        }

    val about: String
        get() = if (isArabic) "حول SHN Music" else "About SHN Music"

    val version: String
        get() = if (isArabic) "الإصدار 1.0.0" else "Version 1.0.0"
}

