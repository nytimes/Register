package com.nytimes.android.external.playbillingtester.support;

import com.nytimes.android.external.playbillingtester.model.ImmutableRepository;
import com.nytimes.android.external.playbillingtester.model.Repository;

import java.util.Date;
import java.util.Random;

public final class RepositoryFactory {

    private static final Random RANDOM = new Random();

    private static final String[] ORG_NAMES = new String[]{
            "nytimes", "google", "square", "usa", "poland", "newyork", "california", "dog"
    };

    private static final String[] REPO_NAMES = new String[]{
            "store", "house", "care", "rv", "apartment", "rxjava", "glide", "picasso"
    };

    private static final String[] LANGUAGES = new String[]{
            "Volt", "LSL", "eC", "CoffeeScript", "HTML", "Lex", "API Blueprint", "Swift", "C",
            "AutoHotkey", "Isabelle", "Metal", "Clarion", "JSONiq", "Boo", "AutoIt", "Clojure",
            "Rust", "Prolog"
    };

    private static final String DESCRIPTION = "Lorem ipsum dolor sit amet, consectetur adipiscing elit," +
            " sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
            " quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute" +
            " irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur." +
            " Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit" +
            " anim id est laborum.";

    public static Repository create() {
        return create(
                getRandomRepoName(),
                getRandomFullName(),
                getRandomPushedAt(),
                getRandomDesc(),
                getRandomStargazersCount(),
                getRandomForksCount(),
                getRandomLanguage(),
                getRandomHtmlUrl());
    }

    public static Repository create(String name, String fullName, Date pushedAt,
                                    String description, int stargazersCount, int forksCount,
                                    String language, String htmlUrl) {
        return ImmutableRepository.builder()
                .name(name)
                .pushedAt(pushedAt)
                .description(description)
                .stargazersCount(stargazersCount)
                .forksCount(forksCount)
                .fullName(fullName)
                .language(language)
                .htmlUrl(htmlUrl).build();
    }

    private static int getRandomStargazersCount() {
        return RANDOM.nextInt(1000);
    }

    private static int getRandomForksCount() {
        return RANDOM.nextInt(1000);
    }

    private static String getRandomRepoName() {
        return REPO_NAMES[RANDOM.nextInt(REPO_NAMES.length)];
    }

    private static String getRandomFullName() {
        return String.format("%s/%s",
                getRandomOrganizationName(),
                getRandomRepoName());
    }

    private static Date getRandomPushedAt() {
        long startTime = 100000000L + RANDOM.nextInt(100000);
        return new Date(startTime);
    }

    private static String getRandomDesc() {
        int start = RANDOM.nextInt(DESCRIPTION.length() / 2);
        int end = start + RANDOM.nextInt(DESCRIPTION.length() / 2);
        return DESCRIPTION.substring(start, end);
    }

    private static String getRandomLanguage() {
        return LANGUAGES[RANDOM.nextInt(LANGUAGES.length)];
    }

    private static String getRandomHtmlUrl() {
        return String.format("https://github.com/%s/%s",
                getRandomOrganizationName(),
                getRandomRepoName());
    }

    private static String getRandomOrganizationName() {
        return ORG_NAMES[RANDOM.nextInt(ORG_NAMES.length)];
    }

    private RepositoryFactory () {}

}
