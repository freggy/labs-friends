import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import java.util.*

/**
 * Created by Yannic Rieger on 01.11.2017.
 *
 * @author Yannic Rieger
 */
data class FriendInfo(val friendList: Set<FriendEntry>, val pendingInvites: Set<RequestEntry>)