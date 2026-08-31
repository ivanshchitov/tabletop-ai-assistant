package org.dishch.tabletopaiassistant.navigation

import kotlinx.serialization.Serializable

sealed interface NavigationConfig {

    @Serializable
    data object Chat : NavigationConfig

    @Serializable
    data object Settings : NavigationConfig
}
