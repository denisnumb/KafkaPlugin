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
    var userFilesPath: String = ""

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