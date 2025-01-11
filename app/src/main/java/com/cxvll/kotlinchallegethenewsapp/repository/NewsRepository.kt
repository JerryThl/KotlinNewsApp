package com.cxvll.kotlinchallegethenewsapp.repository

import androidx.lifecycle.LiveData
import com.cxvll.kotlinchallegethenewsapp.api.RetrofitInstance
import com.cxvll.kotlinchallegethenewsapp.db.ArticleDatabase
import com.cxvll.kotlinchallegethenewsapp.models.Article

class NewsRepository(val db: ArticleDatabase) {

    suspend fun getHeadlines(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getHeadlines(countryCode, pageNumber)

    suspend fun searchForNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)


    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)


    fun getFavouriteNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)


}