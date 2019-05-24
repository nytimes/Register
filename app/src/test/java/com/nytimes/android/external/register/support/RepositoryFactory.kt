package com.nytimes.android.external.register.support

import com.nytimes.android.external.register.model.Repository
import java.util.*

object RepositoryFactory {

    private val RANDOM = Random()

    private val ORG_NAMES = arrayOf("nytimes", "google", "square", "usa", "poland", "newyork", "california", "dog")

    private val REPO_NAMES = arrayOf("store", "house", "care", "rv", "apartment", "rxjava", "glide", "picasso")

    private val LANGUAGES = arrayOf("Volt", "LSL", "eC", "CoffeeScript", "HTML", "Lex", "API Blueprint", "Swift", "C", "AutoHotkey", "Isabelle", "Metal", "Clarion", "JSONiq", "Boo", "AutoIt", "Clojure", "Rust", "Prolog")

    private val DESCRIPTION = "Lorem ipsum dolor sit amet, consectetur adipiscing elit," +
            " sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
            " quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute" +
            " irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur." +
            " Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit" +
            " anim id est laborum."

    private val randomStargazersCount: Int
        get() = RANDOM.nextInt(1000)

    private val randomForksCount: Int
        get() = RANDOM.nextInt(1000)

    private val randomRepoName: String
        get() = REPO_NAMES[RANDOM.nextInt(REPO_NAMES.size)]

    private val randomFullName: String
        get() = String.format("%s/%s",
                randomOrganizationName,
                randomRepoName)

    private val randomPushedAt: Date
        get() {
            val startTime = 100000000L + RANDOM.nextInt(100000)
            return Date(startTime)
        }

    private val randomDesc: String
        get() {
            val start = RANDOM.nextInt(DESCRIPTION.length / 2)
            val end = start + RANDOM.nextInt(DESCRIPTION.length / 2)
            return DESCRIPTION.substring(start, end)
        }

    private val randomLanguage: String
        get() = LANGUAGES[RANDOM.nextInt(LANGUAGES.size)]

    private val randomHtmlUrl: String
        get() = String.format("https://github.com/%s/%s",
                randomOrganizationName,
                randomRepoName)

    private val randomOrganizationName: String
        get() = ORG_NAMES[RANDOM.nextInt(ORG_NAMES.size)]

    fun create(): Repository = Repository(
                randomRepoName,
                randomFullName,
                randomDesc,
                randomPushedAt,
                randomStargazersCount,
                randomForksCount,
                randomLanguage,
                randomHtmlUrl)

}
