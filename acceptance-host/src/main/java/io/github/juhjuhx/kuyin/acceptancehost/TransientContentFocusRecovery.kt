package io.github.juhjuhx.kuyin.acceptancehost

/** One-shot test editor lifecycle policy; never interprets IME text or expected output. */
internal class TransientContentFocusRecovery {
    private var previouslyFocused = false
    var pending = false
        private set

    fun focusChanged(focused: Boolean, contentHasArea: Boolean, resumed: Boolean, windowFocused: Boolean) {
        if (focused) {
            previouslyFocused = true
            pending = false
        } else {
            pending = previouslyFocused && !contentHasArea && resumed && windowFocused
            previouslyFocused = false
        }
    }

    fun cancel() {
        pending = false
        previouslyFocused = false
    }

    fun layout(contentHasArea: Boolean, editorHasArea: Boolean, resumed: Boolean,
        windowFocused: Boolean, anotherViewFocused: Boolean, editorFocused: Boolean): Boolean {
        if (!pending) return false
        if (!resumed || !windowFocused) { cancel(); return false }
        if (!contentHasArea || !editorHasArea) return false
        pending = false // Consume before requesting focus, including rejection; never retry.
        return !anotherViewFocused && !editorFocused
    }
}
