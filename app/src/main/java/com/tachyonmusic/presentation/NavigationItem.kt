package com.tachyonmusic.presentation

abstract class NavigationItem(private val route: String) {
    open fun route(args: Map<String, String> = emptyMap()) = if (args.isEmpty()) route else {
        var filledRoute = route
        for(item in args) {
            filledRoute = filledRoute.replace("{${item.key}}", item.value)
        }
        filledRoute
    }
}
