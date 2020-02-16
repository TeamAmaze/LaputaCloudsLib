package com.amaze.laputacloudslib

abstract class AbstractUser<Driver: AbstractFileStructureDriver> {
    abstract fun getFileStructureDriverAsync(callback: (Driver) -> Unit)
}

