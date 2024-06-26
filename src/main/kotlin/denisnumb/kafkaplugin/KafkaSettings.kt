package denisnumb.kafkaplugin

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


@State(name = "KafkaSettings", storages = [Storage("KafkaSettings.xml")])
class KafkaSettings : PersistentStateComponent<KafkaSettings> {
    var serverIp: String = "localhost"
    var serverPort: Int = 9092
    var groupId: String = "group_id"
    var userName: String = "User"
    var userPassword: String = "Password"
    var pathToSSL: String = ""
    var passwordToSSL: String = ""
    var defaultMessages: List<String> = emptyList()
    var defaultTopics: List<String> = emptyList()

    override fun getState(): KafkaSettings {
        return this
    }

    override fun loadState(state: KafkaSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: KafkaSettings
            get() = ServiceManager.getService(KafkaSettings::class.java)
    }
}