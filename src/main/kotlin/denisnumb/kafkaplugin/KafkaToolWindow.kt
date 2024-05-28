package denisnumb.kafkaplugin

import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.util.maximumHeight
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.awt.*
import java.util.*
import javax.swing.*

class KafkaToolWindow {
    val content = JPanel()

    init {
        content.layout = BorderLayout()
        // Создаем вкладки
        val tabbedPane = JTabbedPane()

        // Вкладка для получения данных из топика
        val receivePanel = JPanel()
        receivePanel.layout = BoxLayout(receivePanel, BoxLayout.Y_AXIS)

        val topicInputField = JTextField(20)
        topicInputField.maximumHeight = 30
        val getDataButton = JButton("Получить данные")

        // Минимальное пространство между элементами
        val smallGap = 5

        receivePanel.add(Box.createVerticalStrut(smallGap))
        receivePanel.add(JLabel("Введите имя топика:"))
        receivePanel.add(topicInputField)
        receivePanel.add(Box.createVerticalStrut(smallGap))  // Маленький промежуток
        receivePanel.add(getDataButton)

        tabbedPane.addTab("Получить", receivePanel)

        // Вкладка для отправки данных в топик
        val sendPanel = JPanel()
        sendPanel.layout = BoxLayout(sendPanel, BoxLayout.Y_AXIS)

        val topicSendField = JTextField(20)
        topicSendField.maximumHeight = 30
        val messageField = JTextField(20)
        messageField.maximumHeight = 30
        val headerField = JTextField(20)
        headerField.maximumHeight = 30
        val sendDataButton = JButton("Отправить")

        sendPanel.add(Box.createVerticalStrut(smallGap))
        sendPanel.add(JLabel("Введите имя топика:"))
        sendPanel.add(topicSendField)
        sendPanel.add(Box.createVerticalStrut(smallGap))  // Маленький промежуток
        sendPanel.add(JLabel("Введите заголовок:"))
        sendPanel.add(headerField)
        sendPanel.add(Box.createVerticalStrut(smallGap))  // Маленький промежуток
        sendPanel.add(JLabel("Введите сообщение:"))
        sendPanel.add(messageField)
        sendPanel.add(Box.createVerticalStrut(smallGap))  // Маленький промежуток
        sendPanel.add(sendDataButton)

        tabbedPane.addTab("Отправить", sendPanel)

        Thread.currentThread().contextClassLoader = null
        sendDataButton.addActionListener {
            val bootstrapServers = "185.84.163.145:9092"

            // Настройки для Kafka Producer
            val producerProps = Properties().apply {
                put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
                put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
                put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            }
            // Создание Kafka Producer
            val producer = KafkaProducer<String, String>(producerProps)
            // Название топика
            val topic = "my_topic"
            // Заголовок и сообщение
            val key = "my_key"
            val value = "my_message"
            val record = ProducerRecord(topic, key, value)
            // Отправка сообщения
            producer.send(record)
            // Закрытие продюсера после отправки
            producer.close()
        }

        // Добавляем вкладки на главный панель
        content.add(tabbedPane, BorderLayout.CENTER)
    }

    private fun showErrorDialog(text: String)
        = showDialog(text, Messages.getErrorIcon())

    private fun showInfoDialog(text: String)
        = showDialog(text, Messages.getInformationIcon())

    private fun showDialog(text: String, icon: Icon)
        = Messages.showMessageDialog(text, "Kafka Plugin", icon)
}