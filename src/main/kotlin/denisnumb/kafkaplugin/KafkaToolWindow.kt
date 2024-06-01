package denisnumb.kafkaplugin

import com.intellij.openapi.ui.Messages
import com.intellij.ui.util.maximumHeight
import io.ktor.utils.io.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.awt.*
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.swing.*


class KafkaToolWindow {
    val content = JPanel()
    val userSettings = KafkaSettings.instance
    val address: String get() = "${userSettings.serverIp}:${userSettings.serverPort}"

    init {

        content.layout = BorderLayout()
        // Создаем вкладки
        val tabbedPane = JTabbedPane()

        // Вкладка для получения данных из топика
        val receivePanel = JPanel()
        receivePanel.layout = BoxLayout(receivePanel, BoxLayout.Y_AXIS)

        val topicInputFieldGet = JTextField(20)
        topicInputFieldGet.maximumHeight = 30
        val getDataButton = JButton("Получить данные")

        // Минимальное пространство между элементами
        val smallGap = 5

        receivePanel.add(Box.createVerticalStrut(smallGap))
        receivePanel.add(JLabel("Введите имя топика:"))
        receivePanel.add(topicInputFieldGet)
        receivePanel.add(Box.createVerticalStrut(smallGap))
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
        sendPanel.add(Box.createVerticalStrut(smallGap))
        sendPanel.add(JLabel("Введите заголовок:"))
        sendPanel.add(headerField)
        sendPanel.add(Box.createVerticalStrut(smallGap))
        sendPanel.add(JLabel("Введите сообщение:"))
        sendPanel.add(messageField)
        sendPanel.add(Box.createVerticalStrut(smallGap))
        sendPanel.add(sendDataButton)

        tabbedPane.addTab("Отправить", sendPanel)

        Thread.currentThread().contextClassLoader = null


        getDataButton.addActionListener {
            if (topicInputFieldGet.text.isEmpty())
                showErrorDialog("Введите топик")
            else {
                val props = Properties().apply {
                    put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, address)
                    put(ConsumerConfig.GROUP_ID_CONFIG, "your_group_id")
                    put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
                    put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
                    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                }

                val consumer = KafkaConsumer<String, String>(props)
                val topics = listOf(topicInputFieldGet.text)
                consumer.subscribe(topics)

                val records = consumer.poll(java.time.Duration.ofMillis(1000))
                if (records.isEmpty) {
                    showInfoDialog("Нет данных")
                } else {
                    val fileName = userSettings.userFilesPath + "/" + topicInputFieldGet.text + " " + getCurrentDateTime() + ".txt"
                    val f = File(fileName)

                    f.printWriter().use { out ->
                        records.forEach { record ->
                            out.println("Получено сообщение: (key: ${record.key()}, value: ${record.value()}) at offset ${record.offset()}")
                        }
                    }

                    showInfoDialog("Данные записаны в файл ${f.absolutePath}")
                }
                consumer.close()
            }
        }


        sendDataButton.addActionListener {
            // Настройки для Kafka Producer
            val producerProps = Properties().apply {
                put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, address)
                put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
                put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
                put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 2000)
            }
            // Создание Kafka Producer
            val producer = KafkaProducer<String, String>(producerProps)

            if (topicSendField.text.isEmpty() || headerField.text.isEmpty() || messageField.text.isEmpty())
                showErrorDialog("Заполните поля")
            else {
                val record = ProducerRecord(topicSendField.text, headerField.text, messageField.text)

                try {
                    val future: Future<RecordMetadata> = producer.send(record)
                    val metadata = future[2, TimeUnit.SECONDS]
                    showInfoDialog("Message sent successfully")
                } catch (e: Exception) {
                    showErrorDialog(e.message.orEmpty())
                } finally {
                    producer.close()
                }
            }
        }

        // Добавляем вкладки на главный панель
        content.add(tabbedPane, BorderLayout.CENTER)
    }

    private fun getCurrentDateTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")
        return current.format(formatter)
    }

    private fun showErrorDialog(text: String)
        = showDialog(text, Messages.getErrorIcon())

    private fun showInfoDialog(text: String)
        = showDialog(text, Messages.getInformationIcon())

    private fun showDialog(text: String, icon: Icon)
        = Messages.showMessageDialog(text, "Kafka Plugin", icon)
}