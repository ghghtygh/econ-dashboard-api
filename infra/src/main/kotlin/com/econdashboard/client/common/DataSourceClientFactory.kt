package com.econdashboard.client.common

import com.econdashboard.enums.DataSource
import org.springframework.stereotype.Component

@Component
class DataSourceClientFactory(
    clients: List<DataSourceClient>
) {

    private val clientMap: Map<DataSource, DataSourceClient> =
        clients.associateBy { it.source }

    fun getClient(source: DataSource): DataSourceClient {
        return clientMap[source]
            ?: throw UnsupportedOperationException("No client for data source: $source")
    }
}
