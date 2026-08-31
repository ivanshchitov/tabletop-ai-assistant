package org.dishch.tabletopaiassistant.core.resources

/** Lets ViewModels resolve localized strings without depending on Android `Context` directly. */
interface ResourceProvider {

    fun getString(resId: Int): String

    fun getString(resId: Int, vararg formatArgs: Any): String
}
