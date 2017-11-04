import java.util.*

/**
 * Created by Yannic Rieger on 01.11.2017.
 * <p>
 * @author Yannic Rieger
 */
data class FriendInfo(val friendList: Set<UUID>, val pendingInvites: Set<UUID>)