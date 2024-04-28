package denisnumb.kafkaplugin

import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import com.intellij.ui.components.JBTabbedPane
import javax.swing.*

class KafkaToolWindow {
    val content = JPanel()
    private val tabbedPane = JBTabbedPane()

    init {
        tabbedPane.addTab("Получить", initGetDataPane())
        tabbedPane.addTab("Отправить", initSendDataPane())

        content.add(tabbedPane)
    }

    private fun initGetDataPane(): JPanel {
        val panel = JPanel()

        return panel
    }

    private fun initSendDataPane(): JPanel {
        val panel = JPanel()

        val textField = JTextField(20)
        textField.text = "Title"

        val button = JButton("Send")

        val textFieldConstraints = GridBagConstraints()
        textFieldConstraints.gridx = 0
        textFieldConstraints.gridy = 0
        textFieldConstraints.fill = GridBagConstraints.HORIZONTAL
        textFieldConstraints.weightx = 1.0
        textFieldConstraints.insets = JBUI.insets(5)

        val buttonConstraints = GridBagConstraints()
        buttonConstraints.gridx = 1
        buttonConstraints.gridy = 0
        buttonConstraints.insets = JBUI.insets(5)


        button.addActionListener { _ ->
            if (textField.text.isBlank())
                showErrorDialog("Enter title!")
            else
                showInfoDialog(textField.text)
        }

        panel.add(textField, textFieldConstraints)
        panel.add(button, buttonConstraints)

        return panel
    }

    private fun showErrorDialog(text: String)
        = showDialog(text, Messages.getErrorIcon())

    private fun showInfoDialog(text: String)
        = showDialog(text, Messages.getInformationIcon())

    private fun showDialog(text: String, icon: Icon)
        = Messages.showMessageDialog(text, "Kafka Plugin", icon)
}