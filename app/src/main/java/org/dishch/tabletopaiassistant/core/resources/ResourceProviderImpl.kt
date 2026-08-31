package org.dishch.tabletopaiassistant.core.resources

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ResourceProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ResourceProvider {

    override fun getString(resId: Int): String = context.getString(resId)

    override fun getString(resId: Int, vararg formatArgs: Any): String = context.getString(resId, *formatArgs)
}
