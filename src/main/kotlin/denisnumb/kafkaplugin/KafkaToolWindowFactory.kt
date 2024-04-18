package denisnumb.kafkaplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class KafkaToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val kafkaToolWindow = KafkaToolWindow()
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(kafkaToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}