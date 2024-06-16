package denisnumb.kafkaplugin

import com.intellij.openapi.options.Configurable
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*


class KafkaSettingsConfigurable : Configurable {
    private val serverIpField = JTextField(40)
    private val serverPortField = JTextField(40)
    private val groupField = JTextField(40)
    private val userNameField = JTextField(40)
    private val userPasswordField = JPasswordField(40)
    private val pathToSSLField = JTextField(40)
    private val passwordToSSLField = JPasswordField(40)
    private val defaultTopicsField = JTextArea(5, 40)
    private val defaultMessagesField = JTextArea(5, 40)
    private val mainPanel = JPanel()

    init {
        mainPanel.layout = BorderLayout()
        val panel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints().apply {
            insets = JBUI.insets(5)
            fill = GridBagConstraints.HORIZONTAL
        }

        fun addLabeledComponent(label: String, component: JComponent, gridx: Int, gridy: Int) {
            constraints.gridx = gridx
            constraints.gridy = gridy
            panel.add(JLabel(label), constraints)
            constraints.gridx = gridx + 1
            panel.add(component, constraints)
        }

        addLabeledComponent("IP-Адрес сервера Kafka:", serverIpField, 0, 0)
        addLabeledComponent("Порт сервера Kafka:", serverPortField, 0, 1)
        addLabeledComponent("Group ID:", groupField, 0, 2)
        addLabeledComponent("Имя пользователя:", userNameField, 0, 3)
        addLabeledComponent("Пароль:", userPasswordField, 0, 4)
        addLabeledComponent("Путь к SSL-сертификату:", pathToSSLField, 0, 5)
        addLabeledComponent("Пароль к SSL-сертификату:", passwordToSSLField, 0, 6)
        addLabeledComponent("Пользовательские топики:", JScrollPane(defaultTopicsField), 0, 7)
        addLabeledComponent("Пользовательские сообщения:", JScrollPane(defaultMessagesField), 0, 8)


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
                groupField.text != settings.groupId ||
                userNameField.text != settings.userName ||
                userPasswordField.text != settings.userPassword ||
                pathToSSLField.text != settings.pathToSSL ||
                passwordToSSLField.text != settings.passwordToSSL ||
                defaultMessagesField.text != settings.defaultMessages.joinToString("\n") ||
                defaultTopicsField.text != settings.defaultTopics.joinToString("\n")
    }

    override fun apply() {
        val settings: KafkaSettings = KafkaSettings.instance
        settings.serverIp = serverIpField.text
        settings.serverPort = serverPortField.text.toInt()
        settings.groupId = groupField.text
        settings.userName = userNameField.text
        settings.userPassword = userPasswordField.password.joinToString()
        settings.pathToSSL = pathToSSLField.text
        settings.passwordToSSL = passwordToSSLField.password.joinToString()
        settings.defaultMessages = defaultMessagesField.text.split("\n")
        settings.defaultTopics = defaultTopicsField.text.split("\n")
    }

    override fun reset() {
        val settings: KafkaSettings = KafkaSettings.instance
        serverIpField.text = settings.serverIp
        serverPortField.text = settings.serverPort.toString()
        groupField.text = settings.groupId
        userNameField.text = ""
        userPasswordField.text = ""
        pathToSSLField.text = ""
        passwordToSSLField.text = ""
        defaultMessagesField.text = settings.defaultMessages.joinToString("\n")
        defaultTopicsField.text = settings.defaultTopics.joinToString("\n")
    }

    override fun disposeUIResources() {
        // Clean up resources if necessary
    }
}