package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        var instance: JarvisAccessibilityService? = null
            private set

        fun isServiceEnabled(): Boolean = instance != null

        fun performBack() {
            instance?.performGlobalAction(GLOBAL_ACTION_BACK)
        }

        fun performHome() {
            instance?.performGlobalAction(GLOBAL_ACTION_HOME)
        }

        fun performRecents() {
            instance?.performGlobalAction(GLOBAL_ACTION_RECENTS)
        }

        fun openNotifications() {
            instance?.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        }

        fun openQuickSettings() {
            instance?.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        }

        fun lockScreen() {
            instance?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }

        fun takeScreenshot() {
            instance?.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        }

        fun scrollDown(): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            return scrollNode(root, AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }

        fun scrollUp(): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            return scrollNode(root, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        }

        fun clickTextOnScreen(text: String): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            val nodes = root.findAccessibilityNodeInfosByText(text)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    if (node.isClickable) {
                        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    } else {
                        var parent = node.parent
                        while (parent != null) {
                            if (parent.isClickable) {
                                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                            parent = parent.parent
                        }
                    }
                }
            }
            return false
        }

        fun readActiveScreenText(): String {
            val root = instance?.rootInActiveWindow ?: return "Screen context unavailable"
            val textBuilder = StringBuilder()
            extractNodeText(root, textBuilder)
            val result = textBuilder.toString().trim()
            return if (result.isBlank()) "No readable text detected on current screen." else result
        }

        private fun extractNodeText(node: AccessibilityNodeInfo?, sb: StringBuilder) {
            if (node == null) return
            if (!node.text.isNullOrBlank()) {
                sb.append(node.text).append(" | ")
            } else if (!node.contentDescription.isNullOrBlank()) {
                sb.append(node.contentDescription).append(" | ")
            }
            for (i in 0 until node.childCount) {
                extractNodeText(node.getChild(i), sb)
            }
        }

        private fun scrollNode(node: AccessibilityNodeInfo, action: Int): Boolean {
            if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction(action, null))) {
                return node.performAction(action)
            }
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null && scrollNode(child, action)) {
                    return true
                }
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
