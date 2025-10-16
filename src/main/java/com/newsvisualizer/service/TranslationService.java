package com.newsvisualizer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for translation and dictionary functionality
 * Provides word definitions, translations, and language support
 */
public class TranslationService {
    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);
    
    // Cache for frequently looked up words to improve performance
    private final Map<String, String> definitionCache = new ConcurrentHashMap<>();
    private final Map<String, String> translationCache = new ConcurrentHashMap<>();
    
    // Built-in dictionary for common words
    private static final Map<String, String> BUILT_IN_DICTIONARY = new HashMap<String, String>() {{
        // News-related terms
        put("headline", "A heading at the top of an article or page in a newspaper or magazine");
        put("breaking news", "News that is happening right now and is being reported as it unfolds");
        put("correspondent", "A journalist employed to provide news stories for newspapers or broadcast media");
        put("editorial", "A newspaper article written by or on behalf of an editor that gives an opinion");
        put("byline", "A line in a newspaper naming the writer of an article");
        
        // Political terms
        put("parliament", "The highest legislature, consisting of the sovereign, the House of Lords, and the House of Commons");
        put("constituency", "A voting district; a body of voters in a specified area who elect a representative");
        put("manifesto", "A public declaration of policy and aims, especially one issued before an election");
        put("coalition", "An alliance for combined action, especially a temporary alliance of political parties");
        put("referendum", "A general vote by the electorate on a single political question");
        
        // Economic terms
        put("inflation", "A general increase in prices and fall in the purchasing value of money");
        put("gdp", "Gross Domestic Product - the total value of goods produced and services provided in a country");
        put("recession", "A period of temporary economic decline during which trade and industrial activity are reduced");
        put("stock market", "A market where shares of publicly held companies are issued and traded");
        put("cryptocurrency", "A digital or virtual currency that uses cryptography for security");
        
        // Technology terms
        put("artificial intelligence", "The simulation of human intelligence in machines that are programmed to think and learn");
        put("machine learning", "A type of artificial intelligence that allows software applications to predict outcomes");
        put("blockchain", "A system of recording information that makes it difficult or impossible to change or hack");
        put("cybersecurity", "The practice of protecting systems, networks, and programs from digital attacks");
        put("algorithm", "A set of rules or instructions given to a computer to help it solve problems");
        
        // Health terms
        put("pandemic", "A disease prevalent over a whole country or the world");
        put("epidemic", "A widespread occurrence of an infectious disease in a community at a particular time");
        put("vaccine", "A substance used to stimulate the production of antibodies and provide immunity");
        put("quarantine", "A state, period, or place of isolation to prevent the spread of disease");
        put("symptoms", "Physical or mental features that indicate a condition of disease");
        
        // Sports terms
        put("tournament", "A series of contests between a number of competitors, competing for an overall prize");
        put("championship", "A contest for the position of champion in a sport or game");
        put("playoffs", "An additional match played to decide the outcome of a contest");
        put("league", "A collection of people, countries, or groups that combine for mutual protection or cooperation");
        put("stadium", "A large venue for sports or other events, with tiers of seats for spectators");
        
        // General terms
        put("climate change", "Long-term shifts in global or regional climate patterns");
        put("sustainability", "The ability to maintain or support a process continuously over time");
        put("infrastructure", "The basic physical systems of a business, region, or nation");
        put("demographics", "Statistical data relating to the population and particular groups within it");
        put("globalization", "The process by which businesses develop international influence");
    }};
    
    /**
     * Get definition of a word or phrase
     */
    public String getDefinition(String word) {
        if (word == null || word.trim().isEmpty()) {
            return "Please enter a word or phrase to get its definition.";
        }
        
        String cleanWord = word.toLowerCase().trim();
        
        // Check cache first
        if (definitionCache.containsKey(cleanWord)) {
            logger.debug("Retrieved definition from cache for: {}", cleanWord);
            return definitionCache.get(cleanWord);
        }
        
        // Check built-in dictionary
        String builtInDefinition = findBuiltInDefinition(cleanWord);
        if (builtInDefinition != null) {
            definitionCache.put(cleanWord, builtInDefinition);
            return builtInDefinition;
        }
        
        // Try to get definition from online source (simplified approach)
        String onlineDefinition = getSimpleDefinition(cleanWord);
        if (onlineDefinition != null && !onlineDefinition.isEmpty()) {
            definitionCache.put(cleanWord, onlineDefinition);
            return onlineDefinition;
        }
        
        // Fallback response
        String fallback = "Definition not found. '" + word + "' might be a proper noun, technical term, or specialized word. " +
                         "Try searching for it online or breaking it into smaller parts.";
        return fallback;
    }
    
    /**
     * Translate text from one language to another
     */
    public String translateText(String text, String fromLanguage, String toLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return "Please enter text to translate.";
        }
        
        String cacheKey = text + "_" + fromLanguage + "_" + toLanguage;
        
        // Check cache first
        if (translationCache.containsKey(cacheKey)) {
            logger.debug("Retrieved translation from cache");
            return translationCache.get(cacheKey);
        }
        
        // Simple translation service (mock implementation)
        String translation = performSimpleTranslation(text, fromLanguage, toLanguage);
        
        if (translation != null && !translation.isEmpty()) {
            translationCache.put(cacheKey, translation);
            return translation;
        }
        
        return "Translation service temporarily unavailable. Please try again later.";
    }
    
    /**
     * Get explanation of complex phrases or sentences
     */
    public String explainPhrase(String phrase) {
        if (phrase == null || phrase.trim().isEmpty()) {
            return "Please enter a phrase or sentence to explain.";
        }
        
        String cleanPhrase = phrase.toLowerCase().trim();
        
        // Check for common phrases
        String explanation = explainCommonPhrases(cleanPhrase);
        if (explanation != null) {
            return explanation;
        }
        
        // Break down complex sentence
        return breakDownSentence(phrase);
    }
    
    /**
     * Get language detection
     */
    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Please enter text to detect its language.";
        }
        
        // Simple language detection based on common words and patterns
        String detected = performSimpleLanguageDetection(text);
        return "Detected language: " + detected;
    }
    
    /**
     * Get synonyms for a word
     */
    public String getSynonyms(String word) {
        if (word == null || word.trim().isEmpty()) {
            return "Please enter a word to find its synonyms.";
        }
        
        Map<String, String> synonymsMap = getSynonymsMap();
        String synonyms = synonymsMap.get(word.toLowerCase().trim());
        
        if (synonyms != null) {
            return "Synonyms for '" + word + "': " + synonyms;
        }
        
        return "No synonyms found for '" + word + "'. Try a more common word or check the spelling.";
    }
    
    // Helper methods
    
    private String findBuiltInDefinition(String word) {
        // Exact match
        if (BUILT_IN_DICTIONARY.containsKey(word)) {
            return BUILT_IN_DICTIONARY.get(word);
        }
        
        // Partial match for compound words
        for (Map.Entry<String, String> entry : BUILT_IN_DICTIONARY.entrySet()) {
            if (word.contains(entry.getKey()) || entry.getKey().contains(word)) {
                return entry.getValue() + " (Related term: " + entry.getKey() + ")";
            }
        }
        
        return null;
    }
    
    private String getSimpleDefinition(String word) {
        try {
            // Simple definition generator based on word analysis
            if (word.endsWith("ing")) {
                return "'" + word + "' appears to be a present participle or gerund form of a verb.";
            } else if (word.endsWith("ed")) {
                return "'" + word + "' appears to be a past tense or past participle form of a verb.";
            } else if (word.endsWith("ly")) {
                return "'" + word + "' appears to be an adverb, describing how something is done.";
            } else if (word.endsWith("tion") || word.endsWith("sion")) {
                return "'" + word + "' appears to be a noun expressing an action or process.";
            } else if (word.endsWith("able") || word.endsWith("ible")) {
                return "'" + word + "' appears to be an adjective meaning 'capable of' or 'having the quality of'.";
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error getting simple definition for: {}", word, e);
            return null;
        }
    }
    
    private String performSimpleTranslation(String text, String fromLang, String toLang) {
        Map<String, Map<String, String>> translations = getBasicTranslations();
        
        String fromLanguage = fromLang.toLowerCase().replaceAll("[()]", "").trim();
        String toLanguage = toLang.toLowerCase().replaceAll("[()]", "").trim();
        String inputText = text.trim();
        
        // Handle direct translations
        if (translations.containsKey(fromLanguage)) {
            Map<String, String> langMap = translations.get(fromLanguage);
            // First try exact match
            if (langMap.containsKey(inputText.toLowerCase())) {
                return langMap.get(inputText.toLowerCase());
            }
            // Then try partial match
            for (Map.Entry<String, String> entry : langMap.entrySet()) {
                if (inputText.toLowerCase().contains(entry.getKey()) || entry.getKey().contains(inputText.toLowerCase())) {
                    return entry.getValue();
                }
            }
        }
        
        // Handle English to other languages
        if (fromLanguage.contains("english") || fromLanguage.contains("auto")) {
            return translateFromEnglish(inputText, toLanguage);
        }
        
        // Handle automatic detection based on text content
        if (fromLanguage.contains("auto")) {
            if (containsChineseCharacters(inputText) && toLanguage.contains("english")) {
                return translateChineseToEnglish(inputText);
            } else if (containsJapaneseCharacters(inputText) && toLanguage.contains("english")) {
                return translateJapaneseToEnglish(inputText);
            }
        }
        
        return "Translation: '" + text + "' (" + fromLang + " → " + toLang + ") - Limited translation available. For full translation, please use Google Translate or similar service.";
    }
    
    private String explainCommonPhrases(String phrase) {
        Map<String, String> phraseExplanations = new HashMap<String, String>() {{
            put("breaking news", "This means news that is happening right now and is being reported immediately.");
            put("according to sources", "This means the information comes from people who claim to have knowledge about the topic.");
            put("in other news", "This is used to transition to a different news topic.");
            put("developing story", "This means the news event is still happening and more details are expected.");
            put("exclusive report", "This means the news outlet is the only one reporting this story.");
            put("on the record", "This means the person speaking is allowing their name to be used with their statements.");
            put("off the record", "This means the information is given privately and should not be published with attribution.");
        }};
        
        for (Map.Entry<String, String> entry : phraseExplanations.entrySet()) {
            if (phrase.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    private String breakDownSentence(String sentence) {
        int wordCount = sentence.split("\\s+").length;
        
        if (wordCount > 20) {
            return "This is a complex sentence with " + wordCount + " words. Try breaking it into smaller parts or looking up specific words that are confusing.";
        } else if (wordCount > 10) {
            return "This sentence has " + wordCount + " words. Look for the main subject and verb to understand the core meaning.";
        } else {
            return "This is a relatively simple sentence. Try looking up individual words if any are unfamiliar.";
        }
    }
    
    private String performSimpleLanguageDetection(String text) {
        text = text.toLowerCase();
        
        // Comprehensive pattern matching for all supported languages
        if (text.contains("the ") || text.contains(" and ") || text.contains(" is ") || text.contains(" are ")) {
            return "English";
        } else if (text.contains("और") || text.contains("में") || text.contains("के") || text.contains("की")) {
            return "Hindi";
        } else if (containsChineseCharacters(text)) {
            return "Chinese";
        } else if (containsJapaneseCharacters(text)) {
            return "Japanese";
        } else if (text.contains("le ") || text.contains("la ") || text.contains("les ") || text.contains("des ") || text.contains("du ") || text.contains("de ")) {
            return "French";
        } else if (text.contains("el ") || text.contains("la ") || text.contains("los ") || text.contains("las ") || text.contains("es ") || text.contains("por ")) {
            return "Spanish";
        } else if (text.contains("der ") || text.contains("die ") || text.contains("das ") || text.contains("und ") || text.contains("mit ")) {
            return "German";
        } else if (text.contains("il ") || text.contains("lo ") || text.contains("gli ") || text.contains("con ") || text.contains("per ")) {
            return "Italian";
        }
        
        return "Unknown (possibly English or another language)";
    }
    
    private Map<String, Map<String, String>> getBasicTranslations() {
        Map<String, Map<String, String>> translations = new HashMap<>();
        
        // English to Hindi basic translations
        Map<String, String> englishToHindi = new HashMap<String, String>() {{
            put("news", "समाचार");
            put("today", "आज");
            put("government", "सरकार");
            put("country", "देश");
            put("people", "लोग");
            put("time", "समय");
            put("world", "दुनिया");
            put("water", "पानी");
            put("food", "भोजन");
            put("school", "स्कूल");
            put("hello", "नमस्ते");
            put("good", "अच्छा");
            put("bad", "बुरा");
        }};
        
        // English to Chinese (Simplified) basic translations
        Map<String, String> englishToChinese = new HashMap<String, String>() {{
            put("news", "新闻");
            put("today", "今天");
            put("government", "政府");
            put("country", "国家");
            put("people", "人民");
            put("time", "时间");
            put("world", "世界");
            put("water", "水");
            put("food", "食物");
            put("school", "学校");
            put("hello", "你好");
            put("good", "好");
            put("bad", "坏");
        }};
        
        // English to Japanese basic translations
        Map<String, String> englishToJapanese = new HashMap<String, String>() {{
            put("news", "ニュース");
            put("today", "今日");
            put("government", "政府");
            put("country", "国");
            put("people", "人々");
            put("time", "時間");
            put("world", "世界");
            put("water", "水");
            put("food", "食べ物");
            put("school", "学校");
            put("hello", "こんにちは");
            put("good", "良い");
            put("bad", "悪い");
        }};
        
        // Chinese to English basic translations
        Map<String, String> chineseToEnglish = new HashMap<String, String>() {{
            put("你好", "hello");
            put("新闻", "news");
            put("今天", "today");
            put("中国", "China");
            put("美国", "America");
            put("日本", "Japan");
            put("好", "good");
            put("坏", "bad");
            put("人", "person");
            put("国家", "country");
        }};
        
        // Japanese to English basic translations
        Map<String, String> japaneseToEnglish = new HashMap<String, String>() {{
            put("こんにちは", "hello");
            put("ニュース", "news");
            put("今日", "today");
            put("日本", "Japan");
            put("アメリカ", "America");
            put("中国", "China");
            put("良い", "good");
            put("悪い", "bad");
            put("人", "person");
            put("国", "country");
        }};
        
        translations.put("english", englishToHindi);
        translations.put("chinese", chineseToEnglish);
        translations.put("chinese (simplified)", chineseToEnglish);
        translations.put("japanese", japaneseToEnglish);
        
        // English to Spanish basic translations
        Map<String, String> englishToSpanish = new HashMap<String, String>() {{
            put("news", "noticias");
            put("today", "hoy");
            put("government", "gobierno");
            put("country", "país");
            put("people", "gente");
            put("time", "tiempo");
            put("world", "mundo");
            put("water", "agua");
            put("food", "comida");
            put("school", "escuela");
            put("hello", "hola");
            put("good", "bueno");
            put("bad", "malo");
            put("yes", "sí");
            put("no", "no");
            put("thank you", "gracias");
        }};
        
        // English to French basic translations
        Map<String, String> englishToFrench = new HashMap<String, String>() {{
            put("news", "nouvelles");
            put("today", "aujourd'hui");
            put("government", "gouvernement");
            put("country", "pays");
            put("people", "gens");
            put("time", "temps");
            put("world", "monde");
            put("water", "eau");
            put("food", "nourriture");
            put("school", "école");
            put("hello", "bonjour");
            put("good", "bon");
            put("bad", "mauvais");
            put("yes", "oui");
            put("no", "non");
            put("thank you", "merci");
        }};
        
        // English to German basic translations
        Map<String, String> englishToGerman = new HashMap<String, String>() {{
            put("news", "Nachrichten");
            put("today", "heute");
            put("government", "Regierung");
            put("country", "Land");
            put("people", "Leute");
            put("time", "Zeit");
            put("world", "Welt");
            put("water", "Wasser");
            put("food", "Essen");
            put("school", "Schule");
            put("hello", "hallo");
            put("good", "gut");
            put("bad", "schlecht");
            put("yes", "ja");
            put("no", "nein");
            put("thank you", "danke");
        }};
        
        // English to Italian basic translations
        Map<String, String> englishToItalian = new HashMap<String, String>() {{
            put("news", "notizie");
            put("today", "oggi");
            put("government", "governo");
            put("country", "paese");
            put("people", "persone");
            put("time", "tempo");
            put("world", "mondo");
            put("water", "acqua");
            put("food", "cibo");
            put("school", "scuola");
            put("hello", "ciao");
            put("good", "buono");
            put("bad", "cattivo");
            put("yes", "sì");
            put("no", "no");
            put("thank you", "grazie");
        }};
        
        // Reverse translations (Foreign to English)
        Map<String, String> spanishToEnglish = new HashMap<String, String>() {{
            put("hola", "hello");
            put("noticias", "news");
            put("hoy", "today");
            put("gobierno", "government");
            put("país", "country");
            put("gente", "people");
            put("bueno", "good");
            put("malo", "bad");
            put("gracias", "thank you");
            put("sí", "yes");
            put("agua", "water");
            put("comida", "food");
        }};
        
        Map<String, String> frenchToEnglish = new HashMap<String, String>() {{
            put("bonjour", "hello");
            put("nouvelles", "news");
            put("aujourd'hui", "today");
            put("gouvernement", "government");
            put("pays", "country");
            put("gens", "people");
            put("bon", "good");
            put("mauvais", "bad");
            put("merci", "thank you");
            put("oui", "yes");
            put("non", "no");
            put("eau", "water");
            put("nourriture", "food");
        }};
        
        Map<String, String> germanToEnglish = new HashMap<String, String>() {{
            put("hallo", "hello");
            put("nachrichten", "news");
            put("heute", "today");
            put("regierung", "government");
            put("land", "country");
            put("leute", "people");
            put("gut", "good");
            put("schlecht", "bad");
            put("danke", "thank you");
            put("ja", "yes");
            put("nein", "no");
            put("wasser", "water");
            put("essen", "food");
        }};
        
        Map<String, String> italianToEnglish = new HashMap<String, String>() {{
            put("ciao", "hello");
            put("notizie", "news");
            put("oggi", "today");
            put("governo", "government");
            put("paese", "country");
            put("persone", "people");
            put("buono", "good");
            put("cattivo", "bad");
            put("grazie", "thank you");
            put("sì", "yes");
            put("acqua", "water");
            put("cibo", "food");
        }};
        
        // Chinese Traditional (basic support)
        Map<String, String> englishToChineseTraditional = new HashMap<String, String>() {{
            put("news", "新聞");
            put("today", "今天");
            put("government", "政府");
            put("country", "國家");
            put("people", "人民");
            put("hello", "你好");
            put("good", "好");
            put("bad", "壞");
            put("thank you", "謝謝");
        }};
        
        Map<String, String> chineseTraditionalToEnglish = new HashMap<String, String>() {{
            put("你好", "hello");
            put("新聞", "news");
            put("今天", "today");
            put("政府", "government");
            put("國家", "country");
            put("人民", "people");
            put("好", "good");
            put("壞", "bad");
            put("謝謝", "thank you");
        }};
        
        // Add all translations to the map
        translations.put("english", englishToHindi);
        translations.put("chinese", chineseToEnglish);
        translations.put("chinese (simplified)", chineseToEnglish);
        translations.put("chinese (traditional)", chineseTraditionalToEnglish);
        translations.put("japanese", japaneseToEnglish);
        translations.put("spanish", spanishToEnglish);
        translations.put("french", frenchToEnglish);
        translations.put("german", germanToEnglish);
        translations.put("italian", italianToEnglish);
        
        // Store English-to-other mappings for reverse lookup
        Map<String, Map<String, String>> englishTranslations = new HashMap<>();
        englishTranslations.put("hindi", englishToHindi);
        englishTranslations.put("chinese", englishToChinese);
        englishTranslations.put("chinese (simplified)", englishToChinese);
        englishTranslations.put("chinese (traditional)", englishToChineseTraditional);
        englishTranslations.put("japanese", englishToJapanese);
        englishTranslations.put("spanish", englishToSpanish);
        englishTranslations.put("french", englishToFrench);
        englishTranslations.put("german", englishToGerman);
        englishTranslations.put("italian", englishToItalian);
        
        // Note: English mappings are handled by individual methods for simplicity
        
        return translations;
    }
    
    private Map<String, String> getSynonymsMap() {
        return new HashMap<String, String>() {{
            put("good", "excellent, great, fine, nice, wonderful, superb");
            put("bad", "awful, terrible, poor, negative, harmful, wrong");
            put("big", "large, huge, enormous, massive, giant, vast");
            put("small", "tiny, little, mini, compact, minor, slight");
            put("fast", "quick, rapid, speedy, swift, hasty, prompt");
            put("slow", "sluggish, gradual, delayed, leisurely, unhurried");
            put("happy", "joyful, cheerful, pleased, content, delighted, glad");
            put("sad", "unhappy, sorrowful, depressed, gloomy, melancholy");
            put("important", "significant, crucial, vital, essential, major, key");
            put("easy", "simple, effortless, straightforward, basic, uncomplicated");
        }};
    }
    
    /**
     * Clear caches to free memory
     */
    public void clearCaches() {
        definitionCache.clear();
        translationCache.clear();
        logger.info("Translation service caches cleared");
    }
    
    /**
     * Check if text contains Chinese characters
     */
    private boolean containsChineseCharacters(String text) {
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if text contains Japanese characters (Hiragana, Katakana, or Kanji)
     */
    private boolean containsJapaneseCharacters(String text) {
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HIRAGANA ||
                Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA ||
                Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Translate from English to any supported language
     */
    private String translateFromEnglish(String text, String targetLanguage) {
        String lowerTarget = targetLanguage.toLowerCase().replaceAll("[()]", "").trim();
        
        // Route to appropriate translation method
        if (lowerTarget.contains("chinese")) {
            if (lowerTarget.contains("traditional")) {
                return translateToChineseTraditionalSimple(text);
            } else {
                return translateToChineseSimple(text);
            }
        } else if (lowerTarget.contains("japanese")) {
            return translateToJapaneseSimple(text);
        } else if (lowerTarget.contains("spanish")) {
            return translateToSpanishSimple(text);
        } else if (lowerTarget.contains("french")) {
            return translateToFrenchSimple(text);
        } else if (lowerTarget.contains("german")) {
            return translateToGermanSimple(text);
        } else if (lowerTarget.contains("italian")) {
            return translateToItalianSimple(text);
        } else if (lowerTarget.contains("hindi")) {
            return translateToHindiSimple(text);
        }
        
        return "Translation to " + targetLanguage + ": '" + text + "' (Limited vocabulary - use Google Translate for full translation)";
    }
    
    /**
     * Translate English text to Spanish
     */
    private String translateToSpanishSimple(String text) {
        Map<String, String> englishToSpanish = new HashMap<String, String>() {{
            put("hello", "hola");
            put("news", "noticias");
            put("today", "hoy");
            put("government", "gobierno");
            put("country", "país");
            put("people", "gente");
            put("good", "bueno");
            put("bad", "malo");
            put("thank you", "gracias");
            put("yes", "sí");
            put("no", "no");
        }};
        
        String lowerText = text.toLowerCase();
        return englishToSpanish.getOrDefault(lowerText, 
            "Translation to Spanish: " + text + " (Use Google Translate for better results)");
    }
    
    /**
     * Translate English text to French
     */
    private String translateToFrenchSimple(String text) {
        Map<String, String> englishToFrench = new HashMap<String, String>() {{
            put("hello", "bonjour");
            put("news", "nouvelles");
            put("today", "aujourd'hui");
            put("government", "gouvernement");
            put("country", "pays");
            put("people", "gens");
            put("good", "bon");
            put("bad", "mauvais");
            put("thank you", "merci");
            put("yes", "oui");
            put("no", "non");
        }};
        
        String lowerText = text.toLowerCase();
        return englishToFrench.getOrDefault(lowerText, 
            "Translation to French: " + text + " (Use Google Translate for better results)");
    }
    
    /**
     * Translate English text to German
     */
    private String translateToGermanSimple(String text) {
        Map<String, String> englishToGerman = new HashMap<String, String>() {{
            put("hello", "hallo");
            put("news", "Nachrichten");
            put("today", "heute");
            put("government", "Regierung");
            put("country", "Land");
            put("people", "Leute");
            put("good", "gut");
            put("bad", "schlecht");
            put("thank you", "danke");
            put("yes", "ja");
            put("no", "nein");
        }};
        
        String lowerText = text.toLowerCase();
        return englishToGerman.getOrDefault(lowerText, 
            "Translation to German: " + text + " (Use Google Translate for better results)");
    }
    
    /**
     * Translate English text to Italian
     */
    private String translateToItalianSimple(String text) {
        Map<String, String> englishToItalian = new HashMap<String, String>() {{
            put("hello", "ciao");
            put("news", "notizie");
            put("today", "oggi");
            put("government", "governo");
            put("country", "paese");
            put("people", "persone");
            put("good", "buono");
            put("bad", "cattivo");
            put("thank you", "grazie");
            put("yes", "sì");
            put("no", "no");
        }};
        
        String lowerText = text.toLowerCase();
        return englishToItalian.getOrDefault(lowerText, 
            "Translation to Italian: " + text + " (Use Google Translate for better results)");
    }
    
    /**
     * Translate English text to Hindi
     */
    private String translateToHindiSimple(String text) {
        Map<String, String> englishToHindi = new HashMap<String, String>() {{
            put("hello", "नमस्ते");
            put("news", "समाचार");
            put("today", "आज");
            put("government", "सरकार");
            put("country", "देश");
            put("people", "लोग");
            put("good", "अच्छा");
            put("bad", "बुरा");
            put("water", "पानी");
            put("food", "भोजन");
        }};
        
        String lowerText = text.toLowerCase();
        return englishToHindi.getOrDefault(lowerText, 
            "Translation to Hindi: " + text + " (Use Google Translate for better results)");
    }
    
    /**
     * Translate English text to Chinese Traditional
     */
    private String translateToChineseTraditionalSimple(String text) {
        Map<String, String> englishToChinese = new HashMap<String, String>() {{
            put("hello", "你好");
            put("news", "新聞");
            put("today", "今天");
            put("government", "政府");
            put("country", "國家");
            put("people", "人民");
            put("good", "好");
            put("bad", "壞");
            put("water", "水");
            put("food", "食物");
        }};
        
        String lowerText = text.toLowerCase();
        return englishToChinese.getOrDefault(lowerText, 
            "Translation to Chinese Traditional: " + text + " (Use Google Translate for better results)");
    }
    
    /**
     * Translate English text to Chinese (simplified approach)
     */
    private String translateToChineseSimple(String text) {
        Map<String, String> englishToChinese = new HashMap<String, String>() {{
            put("hello", "你好");
            put("news", "新闻");
            put("today", "今天");
            put("government", "政府");
            put("country", "国家");
            put("people", "人民");
            put("good", "好");
            put("bad", "坏");
            put("china", "中国");
            put("japan", "日本");
            put("america", "美国");
        }};
        
        String lowerText = text.toLowerCase();
        for (Map.Entry<String, String> entry : englishToChinese.entrySet()) {
            if (lowerText.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Translation to Chinese: " + text + " (Use Google Translate for better results)";
    }
    
    /**
     * Translate English text to Japanese (simplified approach)
     */
    private String translateToJapaneseSimple(String text) {
        Map<String, String> englishToJapanese = new HashMap<String, String>() {{
            put("hello", "こんにちは");
            put("news", "ニュース");
            put("today", "今日");
            put("government", "政府");
            put("country", "国");
            put("people", "人々");
            put("good", "良い");
            put("bad", "悪い");
            put("japan", "日本");
            put("china", "中国");
            put("america", "アメリカ");
        }};
        
        String lowerText = text.toLowerCase();
        for (Map.Entry<String, String> entry : englishToJapanese.entrySet()) {
            if (lowerText.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Translation to Japanese: " + text + " (Use Google Translate for better results)";
    }
    
    /**
     * Translate Chinese text to English (simplified approach)
     */
    private String translateChineseToEnglish(String text) {
        Map<String, String> chineseToEnglish = new HashMap<String, String>() {{
            put("你好", "hello");
            put("新闻", "news");
            put("今天", "today");
            put("政府", "government");
            put("国家", "country");
            put("人民", "people");
            put("好", "good");
            put("坏", "bad");
            put("中国", "China");
            put("日本", "Japan");
            put("美国", "America");
        }};
        
        for (Map.Entry<String, String> entry : chineseToEnglish.entrySet()) {
            if (text.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Translation from Chinese: " + text + " (Use Google Translate for better results)";
    }
    
    /**
     * Translate Japanese text to English (simplified approach)
     */
    private String translateJapaneseToEnglish(String text) {
        Map<String, String> japaneseToEnglish = new HashMap<String, String>() {{
            put("こんにちは", "hello");
            put("ニュース", "news");
            put("今日", "today");
            put("政府", "government");
            put("国", "country");
            put("人々", "people");
            put("良い", "good");
            put("悪い", "bad");
            put("日本", "Japan");
            put("中国", "China");
            put("アメリカ", "America");
        }};
        
        for (Map.Entry<String, String> entry : japaneseToEnglish.entrySet()) {
            if (text.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Translation from Japanese: " + text + " (Use Google Translate for better results)";
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return String.format("Definition cache: %d entries, Translation cache: %d entries", 
                           definitionCache.size(), translationCache.size());
    }
}