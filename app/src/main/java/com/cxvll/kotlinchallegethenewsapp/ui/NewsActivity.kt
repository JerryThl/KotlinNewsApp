package com.cxvll.kotlinchallegethenewsapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cxvll.kotlinchallegethenewsapp.R
import com.cxvll.kotlinchallegethenewsapp.ViewModel.NewsViewModel
import com.cxvll.kotlinchallegethenewsapp.ViewModel.NewsViewModelProviderFactory
import com.cxvll.kotlinchallegethenewsapp.databinding.ActivityNewsBinding
import com.cxvll.kotlinchallegethenewsapp.db.ArticleDatabase
import com.cxvll.kotlinchallegethenewsapp.repository.NewsRepository

class NewsActivity : AppCompatActivity() {

    lateinit var binding: ActivityNewsBinding
    lateinit var newsViewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        newsViewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
    }
}