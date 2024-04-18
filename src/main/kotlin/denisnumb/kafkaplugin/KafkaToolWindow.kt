package denisnumb.kafkaplugin

import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField

class KafkaToolWindow {
    val content = JPanel()

    init {
        val textField = JTextField(20) // Устанавливаем начальную ширину поля ввода
        textField.text = "Title" // Placeholder text

        val button = JButton("Send")

        // Настраиваем GridBagConstraints для textField
        val textFieldConstraints = GridBagConstraints()
        textFieldConstraints.gridx = 0
        textFieldConstraints.gridy = 0
        textFieldConstraints.fill = GridBagConstraints.HORIZONTAL
        textFieldConstraints.weightx = 1.0
        textFieldConstraints.insets = JBUI.insets(5) // Отступы

        // Настраиваем GridBagConstraints для button
        val buttonConstraints = GridBagConstraints()
        buttonConstraints.gridx = 1
        buttonConstraints.gridy = 0
        buttonConstraints.insets = JBUI.insets(5) // Отступы


        button.addActionListener { _ ->
            if (textField.text.isBlank())
                showErrorDialog("Enter title!")
            else
                showInfoDialog(textField.text)
        }

        content.add(textField, textFieldConstraints)
        content.add(button, buttonConstraints)
    }

    private fun showErrorDialog(text: String)
        = showDialog(text, Messages.getErrorIcon())

    private fun showInfoDialog(text: String)
        = showDialog(text, Messages.getInformationIcon())

    private fun showDialog(text: String, icon: Icon)
        = Messages.showMessageDialog(text, "Kafka Plugin", icon)
}