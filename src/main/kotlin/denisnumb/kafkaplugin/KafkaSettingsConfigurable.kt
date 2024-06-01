package denisnumb.kafkaplugin

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.*


class KafkaSettingsConfigurable : Configurable {
    private val serverIpField = JTextField()
    private val serverPortField = JTextField()
    private val userFilesPathField = JTextField()
    private val mainPanel = JPanel()

    init {
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.add(JLabel("Server IP:"))
        mainPanel.add(serverIpField)
        mainPanel.add(JLabel("Server Port:"))
        mainPanel.add(serverPortField)
        mainPanel.add(JLabel("User Files Path:"))
        mainPanel.add(userFilesPathField)
    }

    @Nls
    override fun getDisplayName(): String {
        return "Kafka Settings"
    }

    override fun createComponent(): JComponent? {
        return mainPanel
    }

    override fun isModified(): Boolean {
        val settings: KafkaSettings = KafkaSettings.instance
        return serverIpField.text != settings.serverIp ||
                serverPortField.text != settings.serverPort.toString() ||
                userFilesPathField.text != settings.userFilesPath
    }

    override fun apply() {
        val settings: KafkaSettings = KafkaSettings.instance
        settings.serverIp = serverIpField.text
        settings.serverPort = serverPortField.text.toInt()
        settings.userFilesPath = userFilesPathField.text
    }

    override fun reset() {
        val settings: KafkaSettings = KafkaSettings.instance
        serverIpField.text = settings.serverIp
        serverPortField.text = settings.serverPort.toString()
        userFilesPathField.text = settings.userFilesPath
    }

    override fun disposeUIResources() {
        // Clean up resources if necessary
    }
}