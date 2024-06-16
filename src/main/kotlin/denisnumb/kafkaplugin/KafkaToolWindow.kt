package denisnumb.kafkaplugin

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.errors.TimeoutException
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.awt.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.swing.*



class KafkaToolWindow {
    private var gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val tempDir = Files.createTempDirectory("kafka_messages")
    private val userSettings = KafkaSettings.instance
    private val address: String get() = "${userSettings.serverIp}:${userSettings.serverPort}"

    val content = JPanel()

    init {
        content.layout = BorderLayout()
        val tabbedPane = JTabbedPane()

        val receivePanel = createReceivePanel()
        tabbedPane.addTab("Получить", receivePanel)
        val sendPanel = createSendPanel()
        tabbedPane.addTab("Отправить", sendPanel)

        content.add(tabbedPane, BorderLayout.PAGE_START)

        val buttonPanel = JPanel()
        buttonPanel.layout = FlowLayout(FlowLayout.CENTER)

        val openFolderButton = JButton("Открыть папку с сообщениями")
        openFolderButton.addActionListener {
            openFolderInExplorer(tempDir)
        }
        buttonPanel.add(openFolderButton)

        val openSettingsButton = JButton("Открыть настройки плагина")
        openSettingsButton.addActionListener {
            ShowSettingsUtil.getInstance().showSettingsDialog(ProjectManager.getInstance().openProjects.firstOrNull(), "KafkaPlugin Config")
        }
        buttonPanel.add(openSettingsButton)

        content.add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun createReceivePanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints().apply {
            insets = JBUI.insets(5)
            fill = GridBagConstraints.HORIZONTAL
        }

        val topicInputFieldGet = JTextField(20)
        val getDataButton = JButton("Получить данные")

        constraints.gridx = 0
        constraints.gridy = 0
        panel.add(JLabel("Введите имя топика:"), constraints)

        constraints.gridx = 1
        panel.add(topicInputFieldGet, constraints)

        constraints.gridx = 0
        constraints.gridy = 1
        constraints.gridwidth = 2
        constraints.anchor = GridBagConstraints.CENTER
        panel.add(getDataButton, constraints)

        getDataButton.addActionListener {
            val topic = topicInputFieldGet.text
            if (topic.isEmpty())
                showErrorDialog("Введите топик")
            else
                fetchKafkaData(topic)
        }

        return panel
    }

    private fun createSendPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints().apply {
            insets = JBUI.insets(5)
            fill = GridBagConstraints.HORIZONTAL
        }

        val topicsComboBox = ComboBox(userSettings.defaultTopics.toTypedArray())
        val topicSendField = JTextField(20)
        val headerField = JTextField(20)
        val keyField = JTextField(20)
        val messagesComboBox = ComboBox(userSettings.defaultMessages.toTypedArray())
        val messageField = JTextArea(10, 40)
        messageField.lineWrap = true
        messageField.wrapStyleWord = true
        val sendDataButton = JButton("Отправить")
        val messageFromFileButton = JButton("Сообщение из файла")
        val contentTypeComboBox = ComboBox(arrayOf("text/plain", "application/json", "application/xml"))

        fun addLabeledComponent(label: String, component: JComponent, gridx: Int, gridy: Int) {
            constraints.gridx = gridx
            constraints.gridy = gridy
            panel.add(JLabel(label), constraints)
            constraints.gridx = gridx + 1
            panel.add(component, constraints)
        }

        addLabeledComponent("Пользовательские топики:", topicsComboBox, 0, 0)
        addLabeledComponent("Введите имя топика:", topicSendField, 0, 1)
        addLabeledComponent("Введите заголовок:", headerField, 0, 2)
        addLabeledComponent("Введите ключ:", keyField, 0, 3)
        addLabeledComponent("Пользовательские сообщения:", messagesComboBox, 0, 4)
        addLabeledComponent("Введите сообщение:", JScrollPane(messageField), 0, 5)
        addLabeledComponent("Content-Type:", contentTypeComboBox, 0, 6)

        constraints.gridx = 0
        constraints.gridy = 7
        constraints.gridwidth = 2
        constraints.anchor = GridBagConstraints.CENTER
        panel.add(messageFromFileButton, constraints)

        constraints.gridy = 8
        panel.add(sendDataButton, constraints)

        messageFromFileButton.addActionListener {
            val fileChooser = JFileChooser()
            val result = fileChooser.showOpenDialog(panel)
            if (result == JFileChooser.APPROVE_OPTION) {
                val selectedFile = fileChooser.selectedFile
                messageField.text = selectedFile.readText()
            }
        }

        sendDataButton.addActionListener {
            sendKafkaMessage(
                topic = topicSendField.text,
                headers = headerField.text,
                key = keyField.text,
                message = messageField.text,
                contentType = contentTypeComboBox.selectedItem?.toString() ?: "text/plain"
            )
        }

        topicsComboBox.addActionListener {
            topicSendField.text = topicsComboBox.selectedItem?.toString()
        }

        messagesComboBox.addActionListener {
            messageField.text = messagesComboBox.selectedItem?.toString()
        }

        return panel
    }

    private fun fetchKafkaData(topic: String) {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, address)
            put(ConsumerConfig.GROUP_ID_CONFIG, "your_group_id")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    Thread.currentThread().contextClassLoader = null
                    KafkaConsumer<String, String>(props).use { consumer ->
                        consumer.subscribe(listOf(topic))

                        val records = consumer.poll(Duration.ofMillis(1000))
                        if (records.isEmpty) {
                            SwingUtilities.invokeLater {
                                showInfoDialog("Для топика \"$topic\" нет новых сообщений")
                            }
                        } else {
                            val fileName = "$topic ${getCurrentDateTime()}.txt"
                            val file = File("${tempDir.toFile().absolutePath}/$fileName")

                            val jsonRecords = records.map { record ->
                                val headersMap = record.headers().associate { header ->
                                    header.key() to String(header.value())
                                }
                                mapOf(
                                    "topic" to record.topic(),
                                    "partition" to record.partition(),
                                    "offset" to record.offset(),
                                    "key" to record.key(),
                                    "value" to record.value(),
                                    "headers" to headersMap
                                )
                            }

                            file.printWriter().use { out ->
                                out.println(gson.toJson(jsonRecords))
                            }

                            SwingUtilities.invokeLater {
                                showInfoDialog("Данные записаны в файл: $fileName")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                showErrorDialog("Ошибка при выполнении операции: ${e.message}")
            }
        }
    }

    private fun sendKafkaMessage(topic: String, headers: String, key: String, message: String, contentType: String) {
        if (topic.isEmpty() || message.isEmpty()) {
            showErrorDialog("Заполните поля")
            return
        }

        val producerProps = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, address)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 2000)
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    Thread.currentThread().contextClassLoader = null
                    val producer = KafkaProducer<String, String>(producerProps)

                    val recordHeaders = headers.split(";")
                        .mapNotNull {
                            val keyValue = it.split(":", limit = 2)
                            if (keyValue.size == 2) {
                                RecordHeader(keyValue[0].trim(), keyValue[1].trim().toByteArray())
                            } else null
                        }.toMutableList()

                    recordHeaders.add(RecordHeader("content-type", contentType.toByteArray()))

                    val record = ProducerRecord(topic, null, null, key, message, recordHeaders)

                    try {
                        val future: Future<RecordMetadata> = producer.send(record)
                        future[2, TimeUnit.SECONDS]
                        SwingUtilities.invokeLater {
                            showInfoDialog("Сообщение успешно отправлено")
                        }
                    } catch (e: Exception) {
                        SwingUtilities.invokeLater {
                            if (e.cause is TimeoutException)
                                showErrorDialog("Превышено время ожидания")
                            else
                                showErrorDialog("Ошибка при отправке сообщения: ${e.message}")
                        }
                    } finally {
                        producer.close()
                    }
                }
            } catch (e: Exception) {
                showErrorDialog("Ошибка при выполнении операции: ${e.message}")
            }
        }
    }

    private fun getCurrentDateTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")
        return current.format(formatter)
    }

    private fun openFolderInExplorer(folder: Path) {
        try {
            Desktop.getDesktop().open(folder.toFile())
        } catch (e: IOException) {
            showErrorDialog("Не удалось открыть папку: ${e.message}")
        }
    }

    private fun showErrorDialog(text: String)
        = showDialog(text, Messages.getErrorIcon())

    private fun showInfoDialog(text: String)
        = showDialog(text, Messages.getInformationIcon())

    private fun showDialog(text: String, icon: Icon)
        = Messages.showMessageDialog(text, "Kafka Plugin", icon)
}