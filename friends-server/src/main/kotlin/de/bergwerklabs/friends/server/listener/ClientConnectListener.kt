package de.bergwerklabs.friends.server.listener

import de.bergwerklabs.atlantis.api.corepackages.client.ClientConnectPackage
import de.bergwerklabs.atlantis.client.base.util.AtlantisListener

/**
 * Created by Yannic Rieger on 23.10.2017.
 *
 * @author Yannic Rieger
 */
class ClientConnectListener : AtlantisListener<ClientConnectPackage>() {
    
    override fun onResponse(pkg: ClientConnectPackage?) {
    
    }
}