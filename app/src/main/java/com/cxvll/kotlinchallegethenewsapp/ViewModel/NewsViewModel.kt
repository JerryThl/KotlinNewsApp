package com.cxvll.kotlinchallegethenewsapp.ViewModel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cxvll.kotlinchallegethenewsapp.models.Article
import com.cxvll.kotlinchallegethenewsapp.models.NewsResponse
import com.cxvll.kotlinchallegethenewsapp.repository.NewsRepository
import com.cxvll.kotlinchallegethenewsapp.utils.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(app: Application, val newsRepository: NewsRepository) : AndroidViewModel(app) {

    val headLines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headLinesPage = 1
    var headLinesResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newsSearchQuery: String? = null
    var oldSearchQuery: String? = null

    init{
        getHeadlines(countryCode = "us")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headLinesPage++
                if (headLinesResponse == null) {
                    headLinesResponse = resultResponse
                } else {
                    val oldArticles = headLinesResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }

                return Resource.Success(headLinesResponse ?: resultResponse)
            }
        }

        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (searchNewsResponse == null || newsSearchQuery != oldSearchQuery) {
                    searchNewsPage = 1
                    oldSearchQuery = newsSearchQuery
                    searchNewsResponse = resultResponse
                } else {
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }

                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }

        return Resource.Error(response.message())
    }

    fun addToFavourites(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getFavouriteNews() = newsRepository.getFavouriteNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    fun internetConnection(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }

    private suspend fun headlinesInternet(countryCode:String){
        headLines.postValue(Resource.Loading())
        try{
            if(internetConnection(this.getApplication())){
                val response = newsRepository.getHeadlines(countryCode, headLinesPage)
                headLines.postValue(handleHeadlinesResponse(response))
            }else{
                headLines.postValue(Resource.Error("No internet connection"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException -> headLines.postValue(Resource.Error("Unable to connect internet"))
                else -> headLines.postValue(Resource.Error("No internet connection"))
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery:String){
        newsSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())
        try{
            if(internetConnection(this.getApplication())){
                val response = newsRepository.searchForNews(searchQuery,searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            }else{
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        }catch (t:Throwable){
            when(t){
                is IOException -> searchNews.postValue(Resource.Error("Unable to connect internet"))
                else -> searchNews.postValue(Resource.Error("No internet connection"))
            }
        }
    }
}