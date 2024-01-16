package com.matatkoj.nbaplayers.di

import com.matatkoj.nbaplayers.Constants
import com.matatkoj.nbaplayers.data.api.NbaApi
import com.matatkoj.nbaplayers.data.repository.NbaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideNbaRepository(
        api: NbaApi
    ): NbaRepository {
        return NbaRepository(api)
    }

    @Singleton
    @Provides
    fun provideNbaApi(): NbaApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(Constants.API_URL)
            .build()
            .create(NbaApi::class.java)
    }
}