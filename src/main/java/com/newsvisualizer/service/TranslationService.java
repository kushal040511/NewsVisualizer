package com.newsvisualizer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI-backed language assistant for translation, definition, explanation, and language detection.
 */
public class TranslationService {
    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);
    private static final Map<String, String> FILE_CONFIG = loadEnvFiles();

    private static final String OPENAI_API_KEY = readConfig("OPENAI_API_KEY");
    private static final String OPENAI_MODEL = readConfig("OPENAI_TRANSLATION_MODEL", "gpt-4.1-mini");
    private static final String OPENAI_RESPONSES_URL = readConfig("OPENAI_RESPONSES_URL", "https://api.openai.com/v1/responses");

    private static final String GEMINI_API_KEY = readConfig("GEMINI_API_KEY");
    private static final String GEMINI_MODEL = readConfig("GEMINI_TRANSLATION_MODEL", "gemini-2.0-flash");
    private static final String GEMINI_API_URL = readConfig("GEMINI_API_URL", "https://generativelanguage.googleapis.com/v1beta/models");

    private static final String DEEPL_API_KEY = readConfig("DEEPL_API_KEY");
    private static final String DEEPL_API_URL = readConfig("DEEPL_API_URL", "https://api-free.deepl.com/v2/translate");
    private static final String LIBRETRANSLATE_API_URL = readConfig("LIBRETRANSLATE_API_URL", "https://libretranslate.com");

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration PROVIDER_COOLDOWN = Duration.ofMinutes(15);

    private final Map<String, String> responseCache = new ConcurrentHashMap<>();
    private final Map<String, String> lastProviderErrors = new ConcurrentHashMap<>();
    private final Map<String, Long> providerCooldownUntil = new ConcurrentHashMap<>();
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TranslationService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public String getDefinition(String word) {
        if (isBlank(word)) {
            return "Please enter a word or phrase to define.";
        }

        LanguageTaskResponse response = runLanguageTask(
            "define_word",
            word,
            "Auto-detect",
            null,
            "Return a concise dictionary-style definition, one simple example sentence, and optional usage notes."
        );
        return formatDefinitionResponse(response, word);
    }

    public String translateText(String text, String fromLanguage, String toLanguage) {
        if (isBlank(text)) {
            return "Please enter text to translate.";
        }
        if (isBlank(toLanguage) || "Auto-detect".equalsIgnoreCase(toLanguage)) {
            return "Please choose a target language for translation.";
        }

        String cacheKey = buildCacheKey("translate", text, fromLanguage, toLanguage);
        String cached = responseCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String deeplTranslation = tryDeepLTranslation(text, fromLanguage, toLanguage);
        if (deeplTranslation != null) {
            String formatted = formatTranslationResponse(
                new LanguageTaskResponse("translation", detectLanguageNameFallback(text, fromLanguage), toLanguage, deeplTranslation, null, null)
            );
            responseCache.put(cacheKey, formatted);
            return formatted;
        }

        LanguageTaskResponse response = runLanguageTask(
            "translate_text",
            text,
            fromLanguage,
            toLanguage,
            "Translate accurately while preserving tone, names, and meaning. If the input is already in the target language, say so and return the original text."
        );
        String formatted = formatTranslationResponse(response);
        responseCache.put(cacheKey, formatted);
        return formatted;
    }

    public String explainPhrase(String phrase) {
        if (isBlank(phrase)) {
            return "Please enter a sentence or paragraph to explain.";
        }

        LanguageTaskResponse response = runLanguageTask(
            "explain_sentence",
            phrase,
            "Auto-detect",
            null,
            "Explain the text in simpler language. Preserve important entities, dates, and context. Use plain wording."
        );
        return formatExplanationResponse(response);
    }

    public String detectLanguage(String text) {
        if (isBlank(text)) {
            return "Please enter text to detect its language.";
        }

        LanguageTaskResponse response = runLanguageTask(
            "detect_language",
            text,
            "Auto-detect",
            null,
            "Identify the primary language of the text. If the text is mixed-language, mention the dominant language and note that it is mixed."
        );
        return formatDetectionResponse(response);
    }

    public String getSynonyms(String word) {
        if (isBlank(word)) {
            return "Please enter a word or phrase.";
        }

        LanguageTaskResponse response = runLanguageTask(
            "synonyms",
            word,
            "Auto-detect",
            null,
            "Return a few useful synonyms or related alternatives and explain subtle differences briefly."
        );

        StringBuilder builder = new StringBuilder();
        builder.append("Synonyms and Related Alternatives:\n\n");
        builder.append(response.primaryText()).append('\n');
        if (!isBlank(response.notes())) {
            builder.append("\nNotes:\n").append(response.notes());
        }
        return builder.toString();
    }

    public void clearCaches() {
        responseCache.clear();
    }

    private LanguageTaskResponse runLanguageTask(String mode, String input, String sourceLanguage, String targetLanguage, String taskInstruction) {
        String cacheKey = buildCacheKey(mode, input, sourceLanguage, targetLanguage);
        LanguageTaskResponse cached = parseCachedResponse(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<String> providerErrors = new ArrayList<>();

        LanguageTaskResponse openAiResponse = tryOpenAiTask(mode, input, sourceLanguage, targetLanguage, taskInstruction);
        if (openAiResponse != null) {
            responseCache.put(cacheKey, writeResponse(openAiResponse));
            return openAiResponse;
        }
        captureProviderError("OpenAI", providerErrors);

        LanguageTaskResponse geminiResponse = tryGeminiTask(mode, input, sourceLanguage, targetLanguage, taskInstruction);
        if (geminiResponse != null) {
            responseCache.put(cacheKey, writeResponse(geminiResponse));
            return geminiResponse;
        }
        captureProviderError("Gemini", providerErrors);

        if ("detect_language".equals(mode)) {
            String libreDetected = tryLibreTranslateDetect(input);
            if (!isBlank(libreDetected)) {
                LanguageTaskResponse response = new LanguageTaskResponse(mode, libreDetected, normalizeLanguage(targetLanguage), libreDetected, "", "Detected using LibreTranslate fallback.");
                responseCache.put(cacheKey, writeResponse(response));
                return response;
            }
            String googleDetected = tryGoogleDetectedLanguage(input);
            if (!isBlank(googleDetected)) {
                LanguageTaskResponse response = new LanguageTaskResponse(mode, googleDetected, normalizeLanguage(targetLanguage), googleDetected, "", "Detected using Google Translate fallback.");
                responseCache.put(cacheKey, writeResponse(response));
                return response;
            }
        }

        if ("translate_text".equals(mode)) {
            String libreTranslation = tryLibreTranslateTranslation(input, sourceLanguage, targetLanguage);
            if (!isBlank(libreTranslation)) {
                LanguageTaskResponse response = new LanguageTaskResponse(mode, detectLanguageNameFallback(input, sourceLanguage), normalizeLanguage(targetLanguage), libreTranslation, "", "Translated using LibreTranslate fallback.");
                responseCache.put(cacheKey, writeResponse(response));
                return response;
            }

            String googleTranslation = tryGoogleTranslateTranslation(input, sourceLanguage, targetLanguage);
            if (!isBlank(googleTranslation)) {
                LanguageTaskResponse response = new LanguageTaskResponse(mode, detectLanguageNameFallback(input, sourceLanguage), normalizeLanguage(targetLanguage), googleTranslation, "", "Translated using Google Translate fallback.");
                responseCache.put(cacheKey, writeResponse(response));
                return response;
            }

            String myMemoryTranslation = tryMyMemoryTranslation(input, sourceLanguage, targetLanguage);
            if (!isBlank(myMemoryTranslation)) {
                LanguageTaskResponse response = new LanguageTaskResponse(mode, detectLanguageNameFallback(input, sourceLanguage), normalizeLanguage(targetLanguage), myMemoryTranslation, "", "Translated using MyMemory fallback.");
                responseCache.put(cacheKey, writeResponse(response));
                return response;
            }
        }

        if ("define_word".equals(mode)) {
            LanguageTaskResponse response = tryFreeDictionaryDefinition(input, sourceLanguage);
            if (response != null) {
                responseCache.put(cacheKey, writeResponse(response));
                return response;
            }
        }

        if ("explain_sentence".equals(mode)) {
            LanguageTaskResponse response = buildHeuristicExplanation(input, sourceLanguage);
            responseCache.put(cacheKey, writeResponse(response));
            return response;
        }

        if ("detect_language".equals(mode)) {
            LanguageTaskResponse response = new LanguageTaskResponse(
                mode,
                detectLanguageNameFallback(input, sourceLanguage),
                normalizeLanguage(targetLanguage),
                "",
                "",
                "Detected using local fallback heuristics."
            );
            responseCache.put(cacheKey, writeResponse(response));
            return response;
        }

        if (providerErrors.isEmpty() && isBlank(OPENAI_API_KEY) && isBlank(GEMINI_API_KEY)) {
            return fallbackResponse("AI language service is not configured. Set OPENAI_API_KEY or GEMINI_API_KEY to enable smart translation and explanation.");
        }

        if (!providerErrors.isEmpty()) {
            return fallbackResponse(buildFailureMessage(providerErrors));
        }

        if ("define_word".equals(mode)) {
            return fallbackResponse("No definition was found for that input.");
        }

        if ("translate_text".equals(mode)) {
            return fallbackResponse("Translation is temporarily unavailable. Please try again with a shorter input or another target language.");
        }

        if ("synonyms".equals(mode)) {
            return fallbackResponse("Synonym suggestions are temporarily unavailable.");
        }

        if ("detect_language".equals(mode)) {
            return fallbackResponse("Language detection could not determine the input language.");
        }

        if ("explain_sentence".equals(mode)) {
            return fallbackResponse("Explanation is temporarily unavailable.");
        }

        return fallbackResponse("All configured AI providers failed. Check your API keys, quota, or network access.");
    }

    private LanguageTaskResponse tryOpenAiTask(String mode, String input, String sourceLanguage, String targetLanguage, String taskInstruction) {
        if (isBlank(OPENAI_API_KEY)) {
            return null;
        }
        if (isProviderCoolingDown("OpenAI")) {
            return null;
        }

        try {
            ObjectNode schema = objectMapper.createObjectNode();
            schema.put("type", "object");

            ObjectNode properties = schema.putObject("properties");
            properties.putObject("mode").put("type", "string");
            properties.putObject("detected_language").put("type", "string");
            properties.putObject("target_language").put("type", "string");
            properties.putObject("primary_text").put("type", "string");
            properties.putObject("example").put("type", "string");
            properties.putObject("notes").put("type", "string");

            ArrayNode required = schema.putArray("required");
            required.add("mode");
            required.add("detected_language");
            required.add("target_language");
            required.add("primary_text");
            required.add("example");
            required.add("notes");
            schema.put("additionalProperties", false);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", OPENAI_MODEL);
            requestBody.put("instructions",
                "You are a multilingual language assistant for a news analysis platform. " +
                "Be accurate, concise, and reliable across any language. " +
                "Always return a JSON object that matches the provided schema. " +
                taskInstruction
            );

            String prompt = String.format(
                "Mode: %s%nSource language: %s%nTarget language: %s%nInput:%n%s",
                mode,
                normalizeLanguage(sourceLanguage),
                normalizeLanguage(targetLanguage),
                input
            );
            requestBody.put("input", prompt);

            ObjectNode textNode = requestBody.putObject("text");
            ObjectNode formatNode = textNode.putObject("format");
            formatNode.put("type", "json_schema");
            formatNode.put("name", "language_helper_response");
            formatNode.put("strict", true);
            formatNode.set("schema", schema);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_RESPONSES_URL))
                .timeout(HTTP_TIMEOUT)
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                logger.error("OpenAI request failed: {} {}", response.statusCode(), response.body());
                rememberProviderError("OpenAI", extractApiErrorMessage(response.body(), "OpenAI request failed."));
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            String outputText = json.path("output_text").asText();
            if (isBlank(outputText)) {
                return fallbackResponse("OpenAI returned an empty response. Please try again.");
            }

            return parseModelResponse(outputText, mode, sourceLanguage, targetLanguage);
        } catch (Exception e) {
            logger.error("Error running OpenAI language task", e);
            rememberProviderError("OpenAI", e.getMessage());
            return null;
        }
    }

    private LanguageTaskResponse tryGeminiTask(String mode, String input, String sourceLanguage, String targetLanguage, String taskInstruction) {
        if (isBlank(GEMINI_API_KEY)) {
            return null;
        }
        if (isProviderCoolingDown("Gemini")) {
            return null;
        }

        try {
            ObjectNode schema = objectMapper.createObjectNode();
            schema.put("type", "OBJECT");

            ObjectNode properties = schema.putObject("properties");
            properties.putObject("mode").put("type", "STRING");
            properties.putObject("detected_language").put("type", "STRING");
            properties.putObject("target_language").put("type", "STRING");
            properties.putObject("primary_text").put("type", "STRING");
            properties.putObject("example").put("type", "STRING");
            properties.putObject("notes").put("type", "STRING");

            ArrayNode required = schema.putArray("required");
            required.add("mode");
            required.add("detected_language");
            required.add("target_language");
            required.add("primary_text");
            required.add("example");
            required.add("notes");

            String prompt = String.format(
                "You are a multilingual language assistant for a news analysis platform. %s%n%n" +
                "Return only valid JSON with keys: mode, detected_language, target_language, primary_text, example, notes.%n" +
                "Mode: %s%nSource language: %s%nTarget language: %s%nInput:%n%s",
                taskInstruction,
                mode,
                normalizeLanguage(sourceLanguage),
                normalizeLanguage(targetLanguage),
                input
            );

            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = requestBody.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", prompt);

            ObjectNode generationConfig = requestBody.putObject("generationConfig");
            generationConfig.put("temperature", 0.2);
            generationConfig.put("responseMimeType", "application/json");
            generationConfig.set("responseSchema", schema);

            String url = String.format("%s/%s:generateContent?key=%s",
                GEMINI_API_URL,
                GEMINI_MODEL,
                URLEncoder.encode(GEMINI_API_KEY, StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(HTTP_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                logger.error("Gemini request failed: {} {}", response.statusCode(), response.body());
                rememberProviderError("Gemini", extractApiErrorMessage(response.body(), "Gemini request failed."));
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode candidates = json.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return null;
            }

            String outputText = candidates.get(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();

            if (isBlank(outputText)) {
                return null;
            }

            return parseModelResponse(outputText, mode, sourceLanguage, targetLanguage);
        } catch (Exception e) {
            logger.error("Error running Gemini language task", e);
            rememberProviderError("Gemini", e.getMessage());
            return null;
        }
    }

    private String tryDeepLTranslation(String text, String fromLanguage, String toLanguage) {
        if (isBlank(DEEPL_API_KEY)) {
            return null;
        }

        try {
            StringBuilder form = new StringBuilder();
            form.append("text=").append(URLEncoder.encode(text, StandardCharsets.UTF_8));
            form.append("&target_lang=").append(URLEncoder.encode(toDeepLCode(toLanguage), StandardCharsets.UTF_8));

            String sourceCode = toDeepLCode(fromLanguage);
            if (!isBlank(sourceCode) && !"AUTO".equals(sourceCode)) {
                form.append("&source_lang=").append(URLEncoder.encode(sourceCode, StandardCharsets.UTF_8));
            }

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPL_API_URL))
                .timeout(HTTP_TIMEOUT)
                .header("Authorization", "DeepL-Auth-Key " + DEEPL_API_KEY)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                logger.warn("DeepL translation failed: {} {}", response.statusCode(), response.body());
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode translations = json.path("translations");
            if (translations.isArray() && !translations.isEmpty()) {
                return translations.get(0).path("text").asText(null);
            }
        } catch (Exception e) {
            logger.warn("DeepL translation attempt failed", e);
        }

        return null;
    }

    private String tryLibreTranslateTranslation(String text, String fromLanguage, String toLanguage) {
        try {
            String sourceCode = toLibreCode(fromLanguage);
            String targetCode = toLibreCode(toLanguage);
            if (isBlank(targetCode)) {
                return null;
            }

            ObjectNode body = objectMapper.createObjectNode();
            body.put("q", text);
            body.put("source", isBlank(sourceCode) ? "auto" : sourceCode);
            body.put("target", targetCode);
            body.put("format", "text");

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LIBRETRANSLATE_API_URL + "/translate"))
                .timeout(HTTP_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                logger.warn("LibreTranslate translation failed: {} {}", response.statusCode(), response.body());
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            return json.path("translatedText").asText(null);
        } catch (Exception e) {
            logger.warn("LibreTranslate translation attempt failed", e);
            return null;
        }
    }

    private String tryMyMemoryTranslation(String text, String fromLanguage, String toLanguage) {
        try {
            String sourceCode = toMyMemoryCode(fromLanguage, text, true);
            String targetCode = toMyMemoryCode(toLanguage, text, false);
            if (isBlank(sourceCode) || isBlank(targetCode)) {
                return null;
            }

            String url = String.format(
                "https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s",
                URLEncoder.encode(text, StandardCharsets.UTF_8),
                URLEncoder.encode(sourceCode, StandardCharsets.UTF_8),
                URLEncoder.encode(targetCode, StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                logger.warn("MyMemory translation failed: {} {}", response.statusCode(), response.body());
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode matches = json.path("matches");
            if (matches.isArray()) {
                for (JsonNode match : matches) {
                    String translation = match.path("translation").asText("");
                    double score = match.path("match").asDouble(0.0);
                    if (score >= 0.60 && isLikelyUsefulTranslation(text, translation)) {
                        return translation.trim();
                    }
                }
            }

            String translatedText = json.path("responseData").path("translatedText").asText("");
            if (isLikelyUsefulTranslation(text, translatedText)) {
                return translatedText.trim();
            }
        } catch (Exception e) {
            logger.warn("MyMemory translation attempt failed", e);
        }

        return null;
    }

    private String tryGoogleTranslateTranslation(String text, String fromLanguage, String toLanguage) {
        try {
            String sourceCode = toGoogleCode(fromLanguage, text, true);
            String targetCode = toGoogleCode(toLanguage, text, false);
            if (isBlank(targetCode)) {
                return null;
            }

            String url = String.format(
                "https://translate.googleapis.com/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s",
                URLEncoder.encode(sourceCode, StandardCharsets.UTF_8),
                URLEncoder.encode(targetCode, StandardCharsets.UTF_8),
                URLEncoder.encode(text, StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                logger.warn("Google translate fallback failed: {} {}", response.statusCode(), response.body());
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode translations = json.path(0);
            if (!translations.isArray() || translations.isEmpty()) {
                return null;
            }

            StringBuilder translated = new StringBuilder();
            for (JsonNode segment : translations) {
                translated.append(segment.path(0).asText(""));
            }
            String finalText = translated.toString().trim();
            return isBlank(finalText) ? null : finalText;
        } catch (Exception e) {
            logger.warn("Google translate fallback attempt failed", e);
            return null;
        }
    }

    private String tryLibreTranslateDetect(String text) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("q", text);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LIBRETRANSLATE_API_URL + "/detect"))
                .timeout(HTTP_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                logger.warn("LibreTranslate detect failed: {} {}", response.statusCode(), response.body());
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            if (json.isArray() && !json.isEmpty()) {
                String code = json.get(0).path("language").asText();
                return fromLanguageCode(code);
            }
        } catch (Exception e) {
            logger.warn("LibreTranslate detect attempt failed", e);
        }

        return null;
    }

    private String tryGoogleDetectedLanguage(String text) {
        try {
            String url = String.format(
                "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=en&dt=t&q=%s",
                URLEncoder.encode(text, StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                logger.warn("Google detect fallback failed: {} {}", response.statusCode(), response.body());
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            String code = json.path(2).asText("");
            return fromLanguageCode(code);
        } catch (Exception e) {
            logger.warn("Google detect fallback attempt failed", e);
            return null;
        }
    }

    private LanguageTaskResponse tryFreeDictionaryDefinition(String text, String sourceLanguage) {
        String candidate = extractDictionaryCandidate(text);
        if (isBlank(candidate)) {
            return null;
        }

        String englishWord = candidate;
        if (!looksEnglish(candidate)) {
            String translated = tryLibreTranslateTranslation(candidate, sourceLanguage, "English");
            if (isBlank(translated)) {
                translated = tryGoogleTranslateTranslation(candidate, sourceLanguage, "English");
            }
            if (isBlank(translated)) {
                translated = tryMyMemoryTranslation(candidate, sourceLanguage, "English");
            }
            if (!isBlank(translated)) {
                englishWord = translated;
            }
        }

        try {
            String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" +
                URLEncoder.encode(englishWord.trim(), StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                logger.warn("Dictionary API definition lookup failed: {} {}", response.statusCode(), response.body());
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            if (!json.isArray() || json.isEmpty()) {
                return null;
            }

            JsonNode entry = json.get(0);
            JsonNode meanings = entry.path("meanings");
            if (!meanings.isArray() || meanings.isEmpty()) {
                return null;
            }

            JsonNode firstMeaning = meanings.get(0);
            JsonNode definitions = firstMeaning.path("definitions");
            if (!definitions.isArray() || definitions.isEmpty()) {
                return null;
            }

            JsonNode firstDefinition = definitions.get(0);
            String definition = firstDefinition.path("definition").asText("");
            if (isBlank(definition)) {
                return null;
            }

            String example = firstDefinition.path("example").asText("");
            String partOfSpeech = firstMeaning.path("partOfSpeech").asText("");
            String notes = isBlank(partOfSpeech)
                ? "Provided by dictionary fallback."
                : "Part of speech: " + partOfSpeech + ". Provided by dictionary fallback.";

            return new LanguageTaskResponse(
                "define_word",
                detectLanguageNameFallback(text, sourceLanguage),
                "English",
                definition,
                example,
                notes
            );
        } catch (Exception e) {
            logger.warn("Dictionary API definition attempt failed", e);
            return null;
        }
    }

    private LanguageTaskResponse buildHeuristicExplanation(String text, String sourceLanguage) {
        String cleaned = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            return fallbackResponse("Please enter a sentence or paragraph to explain.");
        }

        String translatedToEnglish = tryLibreTranslateTranslation(cleaned, sourceLanguage, "English");
        if (isBlank(translatedToEnglish)) {
            translatedToEnglish = tryGoogleTranslateTranslation(cleaned, sourceLanguage, "English");
        }
        if (isBlank(translatedToEnglish)) {
            translatedToEnglish = tryMyMemoryTranslation(cleaned, sourceLanguage, "English");
        }

        String explanationSource = isBlank(translatedToEnglish) ? cleaned : translatedToEnglish;
        String leadSentence = explanationSource.split("(?<=[.!?])\\s+", 2)[0].trim();
        String explanation = "This text means that " + decapitalizeSentence(leadSentence);
        if (!explanation.endsWith(".")) {
            explanation += ".";
        }

        String notes = isBlank(translatedToEnglish)
            ? "Generated using local explanation fallback because AI providers were unavailable."
            : "Best-effort explanation based on translation fallback.";

        return new LanguageTaskResponse(
            "explain_sentence",
            detectLanguageNameFallback(cleaned, sourceLanguage),
            "English",
            explanation,
            "",
            notes
        );
    }

    private LanguageTaskResponse parseModelResponse(String outputText, String mode, String sourceLanguage, String targetLanguage) {
        try {
            JsonNode node = objectMapper.readTree(outputText);
            return new LanguageTaskResponse(
                node.path("mode").asText(mode),
                node.path("detected_language").asText(detectLanguageNameFallback("", sourceLanguage)),
                node.path("target_language").asText(normalizeLanguage(targetLanguage)),
                node.path("primary_text").asText(""),
                node.path("example").asText(""),
                node.path("notes").asText("")
            );
        } catch (Exception e) {
            logger.warn("Failed to parse model response as JSON, using raw output", e);
            return new LanguageTaskResponse(mode, normalizeLanguage(sourceLanguage), normalizeLanguage(targetLanguage), outputText, "", "");
        }
    }

    private String formatTranslationResponse(LanguageTaskResponse response) {
        StringBuilder builder = new StringBuilder();
        builder.append("Detected Language: ").append(nonBlank(response.detectedLanguage(), "Unknown")).append("\n");
        builder.append("Target Language: ").append(nonBlank(response.targetLanguage(), "Requested language")).append("\n\n");
        builder.append("Translation:\n").append(response.primaryText());
        if (!isBlank(response.notes())) {
            builder.append("\n\nNotes:\n").append(response.notes());
        }
        return builder.toString();
    }

    private String formatDefinitionResponse(LanguageTaskResponse response, String originalWord) {
        StringBuilder builder = new StringBuilder();
        builder.append("Definition:\n").append(response.primaryText());
        if (!isBlank(response.example())) {
            builder.append("\n\nExample:\n").append(response.example());
        }
        if (!isBlank(response.notes())) {
            builder.append("\n\nNotes:\n").append(response.notes());
        }
        if (builder.length() == 0) {
            return "No definition available for '" + originalWord + "'.";
        }
        return builder.toString();
    }

    private String formatExplanationResponse(LanguageTaskResponse response) {
        StringBuilder builder = new StringBuilder();
        builder.append("Explanation:\n").append(response.primaryText());
        if (!isBlank(response.notes())) {
            builder.append("\n\nKey Insight:\n").append(response.notes());
        }
        return builder.toString();
    }

    private String formatDetectionResponse(LanguageTaskResponse response) {
        StringBuilder builder = new StringBuilder();
        builder.append("Detected Language: ").append(nonBlank(response.detectedLanguage(), "Unknown"));
        if (!isBlank(response.notes())) {
            builder.append("\n\nDetails:\n").append(response.notes());
        }
        return builder.toString();
    }

    private LanguageTaskResponse fallbackResponse(String message) {
        return new LanguageTaskResponse("fallback", "Unknown", "", message, "", "");
    }

    private LanguageTaskResponse parseCachedResponse(String key) {
        String cached = responseCache.get(key);
        if (cached == null) {
            return null;
        }
        try {
            return objectMapper.readValue(cached, LanguageTaskResponse.class);
        } catch (Exception e) {
            logger.debug("Failed to parse cached translation response", e);
            return null;
        }
    }

    private String writeResponse(LanguageTaskResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.debug("Failed to serialize language task response", e);
            return "";
        }
    }

    private static String buildCacheKey(String mode, String input, String sourceLanguage, String targetLanguage) {
        return mode + "::" + normalizeLanguage(sourceLanguage) + "::" + normalizeLanguage(targetLanguage) + "::" + input.trim();
    }

    private static String normalizeLanguage(String language) {
        if (isBlank(language)) {
            return "Auto-detect";
        }
        return language.trim();
    }

    private static String nonBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static String readConfig(String key) {
        return readConfig(key, "");
    }

    private static String readConfig(String key, String fallback) {
        String env = System.getenv(key);
        if (!isBlank(env)) {
            return env.trim();
        }
        String fileValue = FILE_CONFIG.get(key);
        if (!isBlank(fileValue)) {
            return fileValue.trim();
        }
        String property = System.getProperty(key);
        if (!isBlank(property)) {
            return property.trim();
        }
        return fallback;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static Map<String, String> loadEnvFiles() {
        Map<String, String> values = new ConcurrentHashMap<>();
        List<Path> candidates = List.of(
            Path.of(".env"),
            Path.of(".env.local")
        );

        for (Path candidate : candidates) {
            try {
                if (!Files.exists(candidate)) {
                    continue;
                }
                for (String line : Files.readAllLines(candidate, StandardCharsets.UTF_8)) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                        continue;
                    }
                    int split = trimmed.indexOf('=');
                    String key = trimmed.substring(0, split).trim();
                    String value = trimmed.substring(split + 1).trim();
                    value = stripQuotes(value);
                    if (!key.isEmpty() && !value.isEmpty()) {
                        values.putIfAbsent(key, value);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to read env file {}", candidate, e);
            }
        }

        return values;
    }

    private static String stripQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private String extractApiErrorMessage(String responseBody, String fallback) {
        try {
            JsonNode json = objectMapper.readTree(responseBody);
            String message = json.path("error").path("message").asText();
            if (!isBlank(message)) {
                return message;
            }
        } catch (Exception e) {
            logger.debug("Failed to parse API error response", e);
        }
        return fallback;
    }

    private void rememberProviderError(String provider, String message) {
        if (!isBlank(provider) && !isBlank(message)) {
            lastProviderErrors.put(provider, message.trim());
            if (isQuotaError(message)) {
                providerCooldownUntil.put(provider, System.currentTimeMillis() + PROVIDER_COOLDOWN.toMillis());
            }
        }
    }

    private boolean isProviderCoolingDown(String provider) {
        Long until = providerCooldownUntil.get(provider);
        return until != null && until > System.currentTimeMillis();
    }

    private void captureProviderError(String provider, List<String> providerErrors) {
        String error = lastProviderErrors.remove(provider);
        if (!isBlank(error)) {
            providerErrors.add(provider + ": " + error);
        }
    }

    private String buildFailureMessage(List<String> providerErrors) {
        return "Providers failed: " + String.join(" | ", providerErrors);
    }

    private static boolean isQuotaError(String message) {
        if (isBlank(message)) {
            return false;
        }
        String normalized = message.toLowerCase();
        return normalized.contains("insufficient_quota")
            || normalized.contains("quota exceeded")
            || normalized.contains("resource_exhausted")
            || normalized.contains("billing details");
    }

    private static String detectLanguageNameFallback(String text, String requestedLanguage) {
        if (!isBlank(requestedLanguage) && !"Auto-detect".equalsIgnoreCase(requestedLanguage)) {
            return requestedLanguage;
        }
        if (text == null) {
            return "Unknown";
        }
        String normalized = text.toLowerCase();
        if (text.chars().anyMatch(c -> c >= 0x0900 && c <= 0x097F)) {
            return "Hindi";
        }
        if (text.chars().anyMatch(c -> c >= 0x3040 && c <= 0x30FF)) {
            return "Japanese";
        }
        if (text.chars().anyMatch(c -> c >= 0x4E00 && c <= 0x9FFF)) {
            return "Chinese";
        }
        if (text.chars().anyMatch(c -> c >= 0x0600 && c <= 0x06FF)) {
            return "Arabic";
        }
        if (normalized.matches(".*\\b(hola|gracias|amigo|buenos|dias)\\b.*")) {
            return "Spanish";
        }
        if (normalized.matches(".*\\b(bonjour|merci|salut)\\b.*")) {
            return "French";
        }
        if (normalized.matches(".*\\b(hallo|danke|guten)\\b.*")) {
            return "German";
        }
        if (normalized.matches(".*\\b(hello|thanks|good morning)\\b.*")) {
            return "English";
        }
        return "Unknown";
    }

    private static String toDeepLCode(String language) {
        if (isBlank(language) || "Auto-detect".equalsIgnoreCase(language)) {
            return "AUTO";
        }

        Map<String, String> codes = Map.ofEntries(
            Map.entry("english", "EN"),
            Map.entry("hindi", "HI"),
            Map.entry("spanish", "ES"),
            Map.entry("french", "FR"),
            Map.entry("german", "DE"),
            Map.entry("italian", "IT"),
            Map.entry("japanese", "JA"),
            Map.entry("portuguese", "PT-PT"),
            Map.entry("russian", "RU"),
            Map.entry("chinese (simplified)", "ZH"),
            Map.entry("dutch", "NL"),
            Map.entry("polish", "PL"),
            Map.entry("korean", "KO"),
            Map.entry("turkish", "TR"),
            Map.entry("ukrainian", "UK")
        );

        return codes.getOrDefault(language.toLowerCase().trim(), "AUTO");
    }

    private static String toLibreCode(String language) {
        if (isBlank(language) || "Auto-detect".equalsIgnoreCase(language)) {
            return "auto";
        }
        Map<String, String> codes = Map.ofEntries(
            Map.entry("english", "en"),
            Map.entry("hindi", "hi"),
            Map.entry("spanish", "es"),
            Map.entry("french", "fr"),
            Map.entry("german", "de"),
            Map.entry("italian", "it"),
            Map.entry("portuguese", "pt"),
            Map.entry("japanese", "ja"),
            Map.entry("korean", "ko"),
            Map.entry("chinese (simplified)", "zh"),
            Map.entry("dutch", "nl"),
            Map.entry("polish", "pl"),
            Map.entry("russian", "ru"),
            Map.entry("turkish", "tr"),
            Map.entry("ukrainian", "uk"),
            Map.entry("arabic", "ar")
        );
        return codes.getOrDefault(language.toLowerCase().trim(), "");
    }

    private static String toMyMemoryCode(String language, String text, boolean allowAutoDetect) {
        String code = toLibreCode(language);
        if (!isBlank(code) && !"auto".equals(code)) {
            return code;
        }
        if (!allowAutoDetect) {
            return "";
        }
        return switch (detectLanguageNameFallback(text, language).toLowerCase()) {
            case "english" -> "en";
            case "hindi" -> "hi";
            case "spanish" -> "es";
            case "french" -> "fr";
            case "german" -> "de";
            case "italian" -> "it";
            case "japanese" -> "ja";
            case "korean" -> "ko";
            case "chinese" -> "zh";
            case "portuguese" -> "pt";
            case "russian" -> "ru";
            case "turkish" -> "tr";
            case "arabic" -> "ar";
            default -> "en";
        };
    }

    private static String toGoogleCode(String language, String text, boolean allowAutoDetect) {
        String code = toLibreCode(language);
        if (!isBlank(code) && !"auto".equals(code)) {
            return code;
        }
        return allowAutoDetect ? "auto" : "";
    }

    private static String fromLanguageCode(String code) {
        if (isBlank(code)) {
            return "Unknown";
        }
        return switch (code.toLowerCase()) {
            case "en" -> "English";
            case "hi" -> "Hindi";
            case "es" -> "Spanish";
            case "fr" -> "French";
            case "de" -> "German";
            case "it" -> "Italian";
            case "pt" -> "Portuguese";
            case "ja" -> "Japanese";
            case "ko" -> "Korean";
            case "zh" -> "Chinese";
            case "nl" -> "Dutch";
            case "pl" -> "Polish";
            case "ru" -> "Russian";
            case "tr" -> "Turkish";
            case "uk" -> "Ukrainian";
            case "ar" -> "Arabic";
            default -> code.toUpperCase();
        };
    }

    private static String extractDictionaryCandidate(String text) {
        if (isBlank(text)) {
            return "";
        }
        String candidate = text.trim();
        if (candidate.contains(" ")) {
            return "";
        }
        return candidate.replaceAll("^[^\\p{L}]+|[^\\p{L}]+$", "");
    }

    private static boolean looksEnglish(String text) {
        return !isBlank(text) && text.chars().allMatch(c -> Character.isLetter(c) && c < 128);
    }

    private static boolean isLikelyUsefulTranslation(String source, String translation) {
        if (isBlank(translation)) {
            return false;
        }
        String cleaned = translation.trim();
        if (cleaned.equalsIgnoreCase(source == null ? "" : source.trim())) {
            return false;
        }
        if (cleaned.matches("(?i)test\\d*")) {
            return false;
        }
        return cleaned.length() >= 2;
    }

    private static String decapitalizeSentence(String text) {
        if (isBlank(text)) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.length() == 1) {
            return trimmed.toLowerCase();
        }
        return Character.toLowerCase(trimmed.charAt(0)) + trimmed.substring(1);
    }

    public record LanguageTaskResponse(
        String mode,
        String detectedLanguage,
        String targetLanguage,
        String primaryText,
        String example,
        String notes
    ) {
    }
}
