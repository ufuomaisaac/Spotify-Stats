package com.efedaniel.spotifystats.network.di

import com.efedaniel.spotifystats.network.interceptors.AuthorizationInterceptor
import com.efedaniel.spotifystats.network.interceptors.ErrorInterceptor
import com.efedaniel.spotifystats.network.interceptors.TokenAuthenticator
import com.efedaniel.spotifystats.utility.constants.Constants.SPOTIFY_AUTH_BASE_URL
import com.efedaniel.spotifystats.utility.constants.Constants.SPOTIFY_BASE_URL
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [
    NetworkModule::class
])
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    internal fun provideRetrofitBuilder(
        callAdapterFactory: CallAdapter.Factory,
        converterFactory: Converter.Factory,
    ): Retrofit.Builder = Retrofit
        .Builder()
        .addCallAdapterFactory(callAdapterFactory)
        .addConverterFactory(converterFactory)

    @Provides
    @Singleton
    @Named("Auth_Retrofit")
    internal fun provideAuthRetrofit(
        builder: Retrofit.Builder,
        @Named("BaseOkHttp") okhttp: OkHttpClient,
    ): Retrofit = builder
        .client(okhttp)
        .baseUrl(SPOTIFY_AUTH_BASE_URL)
        .build()

    @Provides
    @Singleton
    internal fun provideRetrofit(
        builder: Retrofit.Builder,
        @Named("InterceptedOkHttp") okhttp: OkHttpClient,
    ): Retrofit = builder
        .client(okhttp)
        .baseUrl(SPOTIFY_BASE_URL)
        .build()

    @Provides
    @Singleton
    @Named("InterceptedOkHttp")
    internal fun provideOkHttpClient(
        @Named("BaseOkHttp") okhttp: OkHttpClient,
        tokenAuthenticator: TokenAuthenticator,
        errorInterceptor: ErrorInterceptor,
        curlInterceptor: CurlInterceptor
    ): OkHttpClient = okhttp
        .newBuilder()
        .authenticator(tokenAuthenticator)
//        .addInterceptor(errorInterceptor)
        .addInterceptor(curlInterceptor)
        .build()

    @Provides
    @Singleton
    @Named("BaseOkHttp")
    internal fun provideBaseOkHttp(
        authInterceptor: AuthorizationInterceptor,
        logger: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logger)
        .build()

    @Provides
    @Singleton
    internal fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor()
            .also { it.level = BODY }

    @Provides
    @Singleton
    internal fun provideCurlInterceptor(): CurlInterceptor =
        CurlInterceptor(object : Logger {
            override fun log(message: String) {
                Timber.d(message)
            }
        })

    @Provides
    @Singleton
    internal fun provideConverterFactory(): Converter.Factory =
        GsonConverterFactory.create()

    @Provides
    @Singleton
    internal fun provideCallAdapterFactory(): CallAdapter.Factory =
        RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io())

    @Provides
    @Singleton
    internal fun provideGson(): Gson =
        GsonBuilder().create()
}