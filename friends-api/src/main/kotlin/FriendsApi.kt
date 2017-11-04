import de.bergwerklabs.atlantis.api.friends.*
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import de.bergwerklabs.atlas.api.AtlasClient
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Yannic Rieger on 01.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendsApi {
    
    companion object {
    
        private val client = AtlasClient()
        private val service = AtlantisPackageService()
        
        init {
            this.client.register()
        }
        
        @JvmStatic
        fun sendInvite(sender: UUID, receiver: UUID, callback: AtlantisPackageService.Callback<FriendInviteResponsePacket>) {
            this.client.sendPacket(FriendInviteRequestPacket(this.client.clientInformation, sender, receiver), FriendInviteResponsePacket::class.java, callback)
        }
        
        @JvmStatic
        fun removeFriend(from: UUID, toRemove: UUID) {
            this.service.sendPackage(RemoveFriendPacket(toRemove, from))
        }
        
        @JvmStatic
        fun retrieveFriendInfo(player: UUID): FriendInfo {
            val packet = this.service.sendRequestWithFuture(FriendInfoRequestPacket(player), FriendInfoResponsePacket::class.java)
                                     .get(4, TimeUnit.SECONDS)
            
            return FriendInfo(packet.friends, packet.pendingInvites)
        }
        
        fun follow(follower: UUID, toFollow: UUID) {
            // TODO:
        }
    }
}