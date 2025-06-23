@file:Suppress(
  "KotlinRedundantDiagnosticSuppress",
  "MayBeConstant",
  "RedundantVisibilityModifier",
  "RemoveEmptyClassBody",
  "SpellCheckingInspection",
  "unused"
)
package connectors.default

import com.google.firebase.FirebaseApp
import com.google.firebase.dataconnect.ConnectorConfig
import com.google.firebase.dataconnect.DataConnectSettings
import com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
import com.google.firebase.dataconnect.FirebaseDataConnect
import com.google.firebase.dataconnect.generated.GeneratedConnector
import com.google.firebase.dataconnect.generated.GeneratedMutation
import com.google.firebase.dataconnect.generated.GeneratedOperation
import com.google.firebase.dataconnect.generated.GeneratedQuery
import com.google.firebase.dataconnect.getInstance
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

/**
 * A “default” implementation of a Firebase DataConnect connector.
 *
 * This interface and its generated implementations let you run queries and mutations
 * against your Firebase backend. The example below assumes you have configured
 * your `connector = "default"` in your DataConnect rules.
 */
public interface DefaultConnector : GeneratedConnector<DefaultConnector> {
  override val dataConnect: FirebaseDataConnect
  public companion object {
    @Suppress("MemberVisibilityCanBePrivate")
    public val config: ConnectorConfig = ConnectorConfig(
      connector = "default",
      location = "asia-east1",
      serviceId = "EClinic_App"
    )

    /**
     * Returns a singleton instance of `DefaultConnector` tied to the given `dataConnect`.
     */
    public fun getInstance(dataConnect: FirebaseDataConnect): DefaultConnector =
      synchronized(instances) {
        instances.getOrPut(dataConnect) {
          DefaultConnectorImpl(dataConnect)
        }
      }

    private val instances: java.util.WeakHashMap<FirebaseDataConnect, DefaultConnectorImpl> =
      java.util.WeakHashMap()
  }
}

/**
 * A convenience property that returns a `DefaultConnector` using the default FirebaseDataConnect singleton.
 */
public val DefaultConnector.Companion.instance: DefaultConnector
  get() = getInstance(FirebaseDataConnect.getInstance(config))

/**
 * Alternate overload: get an instance by just providing optional DataConnectSettings.
 */
public fun DefaultConnector.Companion.getInstance(
  settings: DataConnectSettings = DataConnectSettings()
): DefaultConnector =
  getInstance(FirebaseDataConnect.getInstance(config))

/**
 * Alternate overload: get an instance by providing a FirebaseApp and optional DataConnectSettings.
 */
public fun DefaultConnector.Companion.getInstance(
  app: FirebaseApp,
  settings: DataConnectSettings = DataConnectSettings()
): DefaultConnector =
  getInstance(FirebaseDataConnect.getInstance(config))

/**
 * The actual implementation of DefaultConnector.  GeneratedQuery and GeneratedMutation lists
 * will be empty unless you generate specific operations via DataConnect codegen.
 */
private class DefaultConnectorImpl(
  override val dataConnect: FirebaseDataConnect
) : DefaultConnector {

  @ExperimentalFirebaseDataConnect
  override fun operations(): List<GeneratedOperation<DefaultConnector, *, *>> =
    queries() + mutations()

  @ExperimentalFirebaseDataConnect
  override fun mutations(): List<GeneratedMutation<DefaultConnector, *, *>> =
    listOf()

  @ExperimentalFirebaseDataConnect
  override fun queries(): List<GeneratedQuery<DefaultConnector, *, *>> =
    listOf()

  @ExperimentalFirebaseDataConnect
  override fun copy(dataConnect: FirebaseDataConnect): DefaultConnector =
    DefaultConnectorImpl(dataConnect)

  override fun equals(other: Any?): Boolean =
    other is DefaultConnectorImpl && other.dataConnect == dataConnect

  override fun hashCode(): Int =
    java.util.Objects.hash("DefaultConnectorImpl", dataConnect)

  override fun toString(): String =
    "DefaultConnectorImpl(dataConnect=$dataConnect)"
}

/**
 * Base class for generated query implementations.  You can invoke
 * connector.query(…) to create instances of these at runtime.
 * The generated code will subclass this and fill in the operationName, serializers, etc.
 */
private open class DefaultConnectorGeneratedQueryImpl<Data, Variables>(
  override val connector: DefaultConnector,
  override val operationName: String,
  override val dataDeserializer: DeserializationStrategy<Data>,
  override val variablesSerializer: SerializationStrategy<Variables>,
) : GeneratedQuery<DefaultConnector, Data, Variables> {

  @ExperimentalFirebaseDataConnect
  override fun copy(
    connector: DefaultConnector,
    operationName: String,
    dataDeserializer: DeserializationStrategy<Data>,
    variablesSerializer: SerializationStrategy<Variables>,
  ) = DefaultConnectorGeneratedQueryImpl(
    connector,
    operationName,
    dataDeserializer,
    variablesSerializer
  )

  @ExperimentalFirebaseDataConnect
  override fun <NewVariables> withVariablesSerializer(
    variablesSerializer: SerializationStrategy<NewVariables>
  ): GeneratedQuery<DefaultConnector, Data, NewVariables> =
    DefaultConnectorGeneratedQueryImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  @ExperimentalFirebaseDataConnect
  override fun <NewData> withDataDeserializer(
    dataDeserializer: DeserializationStrategy<NewData>
  ): GeneratedQuery<DefaultConnector, NewData, Variables> =
    DefaultConnectorGeneratedQueryImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun equals(other: Any?): Boolean =
    other is DefaultConnectorGeneratedQueryImpl<*, *> &&
            other.connector == connector &&
            other.operationName == operationName &&
            other.dataDeserializer == dataDeserializer &&
            other.variablesSerializer == variablesSerializer

  override fun hashCode(): Int =
    java.util.Objects.hash(
      "DefaultConnectorGeneratedQueryImpl",
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun toString(): String =
    "DefaultConnectorGeneratedQueryImpl(" +
            "operationName=$operationName, " +
            "dataDeserializer=$dataDeserializer, " +
            "variablesSerializer=$variablesSerializer, " +
            "connector=$connector)"
}

/**
 * Base class for generated mutation implementations.  You can invoke
 * connector.mutation(…) to create instances of these at runtime.
 * The generated code will subclass this and fill in the operationName, serializers, etc.
 */
private open class DefaultConnectorGeneratedMutationImpl<Data, Variables>(
  override val connector: DefaultConnector,
  override val operationName: String,
  override val dataDeserializer: DeserializationStrategy<Data>,
  override val variablesSerializer: SerializationStrategy<Variables>,
) : GeneratedMutation<DefaultConnector, Data, Variables> {

  @ExperimentalFirebaseDataConnect
  override fun copy(
    connector: DefaultConnector,
    operationName: String,
    dataDeserializer: DeserializationStrategy<Data>,
    variablesSerializer: SerializationStrategy<Variables>,
  ) = DefaultConnectorGeneratedMutationImpl(
    connector, operationName, dataDeserializer, variablesSerializer
  )

  @ExperimentalFirebaseDataConnect
  override fun <NewVariables> withVariablesSerializer(
    variablesSerializer: SerializationStrategy<NewVariables>
  ): GeneratedMutation<DefaultConnector, Data, NewVariables> =
    DefaultConnectorGeneratedMutationImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  @ExperimentalFirebaseDataConnect
  override fun <NewData> withDataDeserializer(
    dataDeserializer: DeserializationStrategy<NewData>
  ): GeneratedMutation<DefaultConnector, NewData, Variables> =
    DefaultConnectorGeneratedMutationImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun equals(other: Any?): Boolean =
    other is DefaultConnectorGeneratedMutationImpl<*, *> &&
            other.connector == connector &&
            other.operationName == operationName &&
            other.dataDeserializer == dataDeserializer &&
            other.variablesSerializer == variablesSerializer

  override fun hashCode(): Int =
    java.util.Objects.hash(
      "DefaultConnectorGeneratedMutationImpl",
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun toString(): String =
    "DefaultConnectorGeneratedMutationImpl(" +
            "operationName=$operationName, " +
            "dataDeserializer=$dataDeserializer, " +
            "variablesSerializer=$variablesSerializer, " +
            "connector=$connector)"
}