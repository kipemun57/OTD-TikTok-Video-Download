package com.example.ui

object L10n {
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        FRENCH("fr", "Français"),
        SPANISH("es", "Español"),
        GERMAN("de", "Deutsch"),
        PORTUGUESE("pt", "Português"),
        ARABIC("ar", "العربية"),
        HINDI("hi", "हिन्दी"),
        INDONESIAN("id", "Bahasa Indonesia"),
        CHINESE("zh", "中文"),
        JAPANESE("ja", "日本語")
    }

    private val translations = mapOf(
        "app_title" to mapOf(
            Language.ENGLISH to "OTD",
            Language.FRENCH to "OTD",
            Language.SPANISH to "OTD",
            Language.GERMAN to "OTD",
            Language.PORTUGUESE to "OTD",
            Language.ARABIC to "OTD",
            Language.HINDI to "OTD",
            Language.INDONESIAN to "OTD",
            Language.CHINESE to "OTD",
            Language.JAPANESE to "OTD"
        ),
        "app_subtitle" to mapOf(
            Language.ENGLISH to "TikTok Video Downloader",
            Language.FRENCH to "Téléchargeur Vidéo TikTok",
            Language.SPANISH to "Descargador de Video TikTok",
            Language.GERMAN to "TikTok Video Downloader",
            Language.PORTUGUESE to "Baixador de Vídeo TikTok",
            Language.ARABIC to "محمل فيديوهات تيك توك",
            Language.HINDI to "टिकटॉक वीडियो डाउनलोडर",
            Language.INDONESIAN to "Pengunduh Video TikTok",
            Language.CHINESE to "TikTok 视频下载器",
            Language.JAPANESE to "TikTok 動画ダウンロード"
        ),
        "paste_hint" to mapOf(
            Language.ENGLISH to "Paste TikTok Video Link Here",
            Language.FRENCH to "Collez le lien TikTok ici",
            Language.SPANISH to "Pegue el enlace de TikTok aquí",
            Language.GERMAN to "TikTok-Videolink hier einfügen",
            Language.PORTUGUESE to "Cole o link do TikTok aqui",
            Language.ARABIC to "الصق رابط فيديو تيك توك هنا",
            Language.HINDI to "टिकटॉक वीडियो लिंक यहाँ पेस्ट करें",
            Language.INDONESIAN to "Tempel Tautan Video TikTok di Sini",
            Language.CHINESE to "在此粘贴 TikTok 视频链接",
            Language.JAPANESE to "ここにTikTok動画リンクを貼り付け"
        ),
        "btn_paste" to mapOf(
            Language.ENGLISH to "Paste",
            Language.FRENCH to "Coller",
            Language.SPANISH to "Pegar",
            Language.GERMAN to "Einfügen",
            Language.PORTUGUESE to "Colar",
            Language.ARABIC to "لصق",
            Language.HINDI to "पेस्ट",
            Language.INDONESIAN to "Tempel",
            Language.CHINESE to "粘贴",
            Language.JAPANESE to "貼り付け"
        ),
        "btn_clear" to mapOf(
            Language.ENGLISH to "Clear",
            Language.FRENCH to "Effacer",
            Language.SPANISH to "Limpiar",
            Language.GERMAN to "Löschen",
            Language.PORTUGUESE to "Limpar",
            Language.ARABIC to "مسح",
            Language.HINDI to "साफ़ करें",
            Language.INDONESIAN to "Bersihkan",
            Language.CHINESE to "清除",
            Language.JAPANESE to "クリア"
        ),
        "btn_analyze" to mapOf(
            Language.ENGLISH to "Analyze Video",
            Language.FRENCH to "Analyser la vidéo",
            Language.SPANISH to "Analizar Video",
            Language.GERMAN to "Video analysieren",
            Language.PORTUGUESE to "Analisar Vídeo",
            Language.ARABIC to "تحليل الفيديو",
            Language.HINDI to "वीडियो विश्लेषण करें",
            Language.INDONESIAN to "Analisis Video",
            Language.CHINESE to "分析视频",
            Language.JAPANESE to "動画を解析"
        ),
        "invalid_url" to mapOf(
            Language.ENGLISH to "Please enter a valid TikTok URL.",
            Language.FRENCH to "Veuillez entrer une URL TikTok valide.",
            Language.SPANISH to "Por favor, introduzca una URL de TikTok válida.",
            Language.GERMAN to "Bitte geben Sie eine gültige TikTok-URL ein.",
            Language.PORTUGUESE to "Por favor, insira um URL do TikTok válido.",
            Language.ARABIC to "يرجى إدخال رابط تيك توك صحيح.",
            Language.HINDI to "कृपया एक वैध टिकटॉक यूआरएल दर्ज करें।",
            Language.INDONESIAN to "Silakan masukkan URL TikTok yang valid.",
            Language.CHINESE to "请输入有效的 TikTok 链接。",
            Language.JAPANESE to "有効なTikTokのURLを入力してください。"
        ),
        "home" to mapOf(
            Language.ENGLISH to "Home",
            Language.FRENCH to "Accueil",
            Language.SPANISH to "Inicio",
            Language.GERMAN to "Startseite",
            Language.PORTUGUESE to "Início",
            Language.ARABIC to "الرئيسية",
            Language.HINDI to "होम",
            Language.INDONESIAN to "Beranda",
            Language.CHINESE to "主页",
            Language.JAPANESE to "ホーム"
        ),
        "downloads" to mapOf(
            Language.ENGLISH to "Downloads",
            Language.FRENCH to "Téléchargements",
            Language.SPANISH to "Descargas",
            Language.GERMAN to "Downloads",
            Language.PORTUGUESE to "Downloads",
            Language.ARABIC to "التنزيلات",
            Language.HINDI to "डाउनलोड",
            Language.INDONESIAN to "Unduhan",
            Language.CHINESE to "下载",
            Language.JAPANESE to "ダウンロード"
        ),
        "feedback" to mapOf(
            Language.ENGLISH to "Feedback",
            Language.FRENCH to "Retour d'information",
            Language.SPANISH to "Comentarios",
            Language.GERMAN to "Feedback",
            Language.PORTUGUESE to "Feedback",
            Language.ARABIC to "الآراء والملاحظات",
            Language.HINDI to "फीडबैक",
            Language.INDONESIAN to "Umpan Balik",
            Language.CHINESE to "反馈",
            Language.JAPANESE to "フィードバック"
        ),
        "privacy_policy" to mapOf(
            Language.ENGLISH to "Privacy Policy",
            Language.FRENCH to "Politique de confidentialité",
            Language.SPANISH to "Política de Privacidad",
            Language.GERMAN to "Datenschutzrichtlinie",
            Language.PORTUGUESE to "Política de Privacidade",
            Language.ARABIC to "سياسة الخصوصية",
            Language.HINDI to "गोपनीयता नीति",
            Language.INDONESIAN to "Kebijakan Privasi",
            Language.CHINESE to "隐私政策",
            Language.JAPANESE to "プライバシーポリシー"
        ),
        "language" to mapOf(
            Language.ENGLISH to "Language",
            Language.FRENCH to "Langue",
            Language.SPANISH to "Idioma",
            Language.GERMAN to "Sprache",
            Language.PORTUGUESE to "Idioma",
            Language.ARABIC to "اللغة",
            Language.HINDI to "भाषा",
            Language.INDONESIAN to "Bahasa",
            Language.CHINESE to "语言",
            Language.JAPANESE to "言語"
        ),
        "settings" to mapOf(
            Language.ENGLISH to "Settings",
            Language.FRENCH to "Paramètres",
            Language.SPANISH to "Ajustes",
            Language.GERMAN to "Einstellungen",
            Language.PORTUGUESE to "Configurações",
            Language.ARABIC to "الإعدادات",
            Language.HINDI to "सेटिंग्स",
            Language.INDONESIAN to "Pengaturan",
            Language.CHINESE to "设置",
            Language.JAPANESE to "設定"
        ),
        "rate_app" to mapOf(
            Language.ENGLISH to "Rate App",
            Language.FRENCH to "Evaluer l'application",
            Language.SPANISH to "Calificar Aplicación",
            Language.GERMAN to "App bewerten",
            Language.PORTUGUESE to "Avaliar App",
            Language.ARABIC to "تقييم التطبيق",
            Language.HINDI to "ऐप को रेट करें",
            Language.INDONESIAN to "Nilai Aplikasi",
            Language.CHINESE to "评分应用",
            Language.JAPANESE to "アプリを評価"
        ),
        "share_app" to mapOf(
            Language.ENGLISH to "Share App",
            Language.FRENCH to "Partager l'application",
            Language.SPANISH to "Compartir Aplicación",
            Language.GERMAN to "App teilen",
            Language.PORTUGUESE to "Compartilhar App",
            Language.ARABIC to "مشاركة التطبيق",
            Language.HINDI to "ऐप शेयर करें",
            Language.INDONESIAN to "Bagikan Aplikasi",
            Language.CHINESE to "分享应用",
            Language.JAPANESE to "アプリを共有"
        ),
        "about" to mapOf(
            Language.ENGLISH to "About",
            Language.FRENCH to "À propos",
            Language.SPANISH to "Acerca de",
            Language.GERMAN to "Über uns",
            Language.PORTUGUESE to "Sobre",
            Language.ARABIC to "حول التطبيق",
            Language.HINDI to "परिचय",
            Language.INDONESIAN to "Tentang",
            Language.CHINESE to "关于",
            Language.JAPANESE to "バージョン情報"
        ),
        "search_hint" to mapOf(
            Language.ENGLISH to "Search downloaded videos...",
            Language.FRENCH to "Rechercher des vidéos...",
            Language.SPANISH to "Buscar videos descargados...",
            Language.GERMAN to "Heruntergeladene Videos suchen...",
            Language.PORTUGUESE to "Buscar vídeos baixados...",
            Language.ARABIC to "البحث في الفيديوهات المحملة...",
            Language.HINDI to "डाउनलोड किए गए वीडियो खोजें...",
            Language.INDONESIAN to "Cari video yang diunduh...",
            Language.CHINESE to "搜索已下载的视频...",
            Language.JAPANESE to "ダウンロード済みの動画を検索..."
        ),
        "no_downloads" to mapOf(
            Language.ENGLISH to "No downloads found",
            Language.FRENCH to "Aucun téléchargement trouvé",
            Language.SPANISH to "No se encontraron descargas",
            Language.GERMAN to "Keine Downloads gefunden",
            Language.PORTUGUESE to "Nenhum download encontrado",
            Language.ARABIC to "لم يتم العثور على تنزيلات",
            Language.HINDI to "कोई डाउनलोड नहीं मिला",
            Language.INDONESIAN to "Tidak ada unduhan ditemukan",
            Language.CHINESE to "未找到下载内容",
            Language.JAPANESE to "ダウンロード履歴はありません"
        )
    )

    fun getString(key: String, language: Language): String {
        return translations[key]?.get(language) ?: translations[key]?.get(Language.ENGLISH) ?: key
    }
}
