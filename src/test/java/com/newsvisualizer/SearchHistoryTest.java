package com.newsvisualizer;

import com.newsvisualizer.model.SearchHistory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchHistoryTest {

    @Test
    void headlineSearchUsesFriendlyDisplayTitle() {
        SearchHistory history = new SearchHistory();
        history.setSearchType("headlines");
        history.setCountry("in");
        history.setArticlesFound(5);

        assertEquals("Headlines from IN", history.getDisplayTitle());
        assertEquals("5 articles found", history.getSearchDescription());
    }

    @Test
    void translationActionsUseTranslationHelperLabels() {
        SearchHistory history = new SearchHistory();
        history.setSearchType("translation");

        assertEquals("Translation Helper", history.getDisplayTitle());
        assertEquals("Language result generated", history.getSearchDescription());
    }

    @Test
    void newsroomBriefingUsesDedicatedLabels() {
        SearchHistory history = new SearchHistory();
        history.setSearchType("briefing");

        assertEquals("Newsroom Briefing", history.getDisplayTitle());
        assertEquals("Feed briefing generated", history.getSearchDescription());
    }
}
