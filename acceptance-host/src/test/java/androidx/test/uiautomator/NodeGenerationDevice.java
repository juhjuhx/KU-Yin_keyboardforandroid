package androidx.test.uiautomator;

import android.view.accessibility.AccessibilityNodeInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Only root/idle/gesture transport is doubled; queries and UiObject2 getters are real. */
public final class NodeGenerationDevice {
    public static AccessibilityNodeInfo[] roots = new AccessibilityNodeInfo[0];
    public static Runnable onGesture = () -> {};
    public static int gestures;
    public static java.util.function.Supplier<AccessibilityNodeInfo[]> rootSupplier;

    @Implements(value = UiDevice.class, isInAndroidSdk = false)
    public static class DeviceShadow {
        @Implementation protected AccessibilityNodeInfo[] getWindowRoots() { return rootSupplier == null ? roots : rootSupplier.get(); }
        @Implementation protected void waitForIdle() {}
        @Implementation protected void waitForIdle(long timeout) {}
        @Implementation protected void runWatchers() {}
        @Implementation protected int getDisplayWidth() { return 1080; }
        @Implementation protected int getDisplayWidth(int id) { return 1080; }
        @Implementation protected int getDisplayHeight() { return 2400; }
        @Implementation protected int getDisplayHeight(int id) { return 2400; }
    }

    @Implements(value = GestureController.class, isInAndroidSdk = false)
    public static class GestureShadow {
        @Implementation protected void performGesture(PointerGesture... values) {
            gestures++;
            onGesture.run();
        }
    }
}
