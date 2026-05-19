package com.basitborsa.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Static alias map for matching free-text news articles to known stock symbols + sectors.
 * Aliases are matched case-insensitively against title + summary.
 */
@Component
public class CompanyAliasRegistry {

    private static final Map<String, List<String>> SYMBOL_ALIASES = Map.of(
            "THYAO", List.of(
                    "thyao",
                    "thyao.is",
                    "thy ",
                    "türk hava yolları",
                    "turk hava yollari",
                    "türk hava",
                    "turkish airlines ao",
                    "turkish airlines"
            ),
            "ASELS", List.of("aselsan", "asels"),
            "BIMAS", List.of("bim birleşik", "bim birlesik", "bim mağazalar", "bim magazalar", "bimas", "bim a.ş.", "bim a.s."),
            "SISE",  List.of("şişecam", "sisecam", "şişe cam", "sise cam", "türkiye şişe ve cam", "turkiye sise ve cam"),
            "TUPRS", List.of("tüpraş", "tupras", "tuprs", "türkiye petrol rafinerileri", "turkiye petrol rafinerileri"),
            "KCHOL", List.of("koç holding", "koc holding", "kchol"),
            "GARAN", List.of("garanti bbva", "garanti bankası", "garanti bankasi", "garan "),
            "FROTO", List.of("ford otosan", "ford otomotiv", "froto")
    );

    private static final Map<String, List<String>> SECTOR_KEYWORDS = Map.ofEntries(
            Map.entry("Havacılık & Ulaşım", List.of("havacılık", "havayolu", "hava yolu", "uçuş", "yolcu sayısı", "havalimanı")),
            Map.entry("Savunma & Teknoloji", List.of("savunma sanayi", "savunma", "askeri", "radar", "ihracat sözleşmesi")),
            Map.entry("Perakende & Gıda", List.of("perakende", "indirim market", "gıda", "süpermarket", "mağaza zinciri")),
            Map.entry("Cam & Sanayi", List.of("cam", "düzcam", "şişe", "ambalaj cam")),
            Map.entry("Enerji & Petrokimya", List.of("petrol", "rafineri", "petrokimya", "akaryakıt", "doğalgaz")),
            Map.entry("Holding & Sanayi", List.of("holding", "iştirak", "konglomera")),
            Map.entry("Bankacılık", List.of("banka", "bankacılık", "kredi büyümesi", "faiz marjı", "merkez bankası")),
            Map.entry("Otomotiv & Sanayi", List.of("otomotiv", "araç", "binek araç", "ticari araç", "elektrikli araç"))
    );

    public Map<String, List<String>> symbolAliases() { return SYMBOL_ALIASES; }

    public Map<String, List<String>> sectorKeywords() { return SECTOR_KEYWORDS; }

    public static String normalize(String s) {
        if (s == null) return "";
        String lower = s.toLowerCase(Locale.forLanguageTag("tr-TR"));
        StringBuilder out = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            switch (c) {
                case 'ı' -> out.append('i');
                case 'i' -> out.append('i');
                case 'ü' -> out.append('u');
                case 'ö' -> out.append('o');
                case 'ç' -> out.append('c');
                case 'ş' -> out.append('s');
                case 'ğ' -> out.append('g');
                case 'â' -> out.append('a');
                case 'î' -> out.append('i');
                case 'û' -> out.append('u');
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
