package com.amaze.laputacloudslib


abstract class AbstractAccount {
    abstract fun tryLogInAsync(callback: (AbstractUser<out AbstractFileStructureDriver>) -> Unit)
}

