package com.example.ime.service

object EditorDeletion {
    sealed interface Plan {
        data object DeleteSelection : Plan
        data object DeleteCodePointBeforeCursor : Plan
    }

    fun plan(hasSelection: Boolean): Plan =
        if (hasSelection) Plan.DeleteSelection else Plan.DeleteCodePointBeforeCursor
}
