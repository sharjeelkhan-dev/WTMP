package com.sharjeel.wtmp.di

import com.sharjeel.wtmp.data.repository.SecurityRepositoryImpl
import com.sharjeel.wtmp.domain.repository.SecurityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module responsible for binding Repository interface abstractions
 * to their concrete data layer implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // =================================================================
    // REPOSITORY BINDINGS
    // =================================================================

    @Binds
    @Singleton
    abstract fun bindSecurityRepository(
        impl: SecurityRepositoryImpl
    ): SecurityRepository
}