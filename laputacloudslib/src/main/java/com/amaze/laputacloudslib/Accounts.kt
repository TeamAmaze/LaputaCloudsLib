package com.amaze.laputacloudslib


abstract class AbstractAccount {
    abstract suspend fun tryLogInAsync(callback: suspend (AbstractUser<out AbstractFileStructureDriver>) -> Unit)
}
