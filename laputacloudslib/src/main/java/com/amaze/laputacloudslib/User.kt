package com.amaze.laputacloudslib

abstract class AbstractUser<Driver: AbstractFileStructureDriver> {
    abstract suspend fun getFileStructureDriverAsync(callback: suspend (Driver) -> Unit)
}

