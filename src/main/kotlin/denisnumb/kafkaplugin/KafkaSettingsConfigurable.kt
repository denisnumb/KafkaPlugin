package denisnumb.kafkaplugin

import com.intellij.openapi.options.Configurable
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*


class KafkaSettingsConfigurable : Configurable {
    private val serverIpField = JTextField(20)
    private val serverPortField = JTextField(20)
    private val defaultTopicsField = JTextArea(5, 20)
    private val defaultMessagesField = JTextArea(5, 20)
    private val mainPanel = JPanel()

    init {
        mainPanel.layout = BorderLayout()
        val panel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints().apply {
            insets = JBUI.insets(5)
            fill = GridBagConstraints.HORIZONTAL
        }

        constraints.gridx = 0
        constraints.gridy = 0
        panel.add(JLabel("IP-Адрес сервера Kafka:"), constraints)

        constraints.gridx = 1
        panel.add(serverIpField, constraints)

        constraints.gridx = 0
        constraints.gridy = 1
        panel.add(JLabel("Порт сервера Kafka:"), constraints)

        constraints.gridx = 1
        panel.add(serverPortField, constraints)

        constraints.gridx = 0
        constraints.gridy = 2
        panel.add(JLabel("Пользовательские топики:"), constraints)

        constraints.gridx = 1
        panel.add(JScrollPane(defaultTopicsField), constraints)

        constraints.gridx = 0
        constraints.gridy = 3
        panel.add(JLabel("Пользовательские сообщения:"), constraints)

        constraints.gridx = 1
        panel.add(JScrollPane(defaultMessagesField), constraints)

        mainPanel.add(panel, BorderLayout.PAGE_START)
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
                defaultMessagesField.text != settings.defaultMessages.joinToString("\n") ||
                defaultTopicsField.text != settings.defaultTopics.joinToString("\n")
    }

    override fun apply() {
        val settings: KafkaSettings = KafkaSettings.instance
        settings.serverIp = serverIpField.text
        settings.serverPort = serverPortField.text.toInt()
        settings.defaultMessages = defaultMessagesField.text.split("\n")
        settings.defaultTopics = defaultTopicsField.text.split("\n")
    }

    override fun reset() {
        val settings: KafkaSettings = KafkaSettings.instance
        serverIpField.text = settings.serverIp
        serverPortField.text = settings.serverPort.toString()
        defaultMessagesField.text = settings.defaultMessages.joinToString("\n")
        defaultTopicsField.text = settings.defaultTopics.joinToString("\n")
    }

    override fun disposeUIResources() {
        // Clean up resources if necessary
    }
}