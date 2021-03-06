package com.icecreamqaq.yuq.event

import com.IceCreamQAQ.Yu.event.events.CancelEvent
import com.IceCreamQAQ.Yu.event.events.Event
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.*
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.MessageSource

open class MessageEvent(open val sender: Contact, val message: Message) : Event(), CancelEvent {
    override fun cancelAble() = true
}

open class GroupMessageEvent(override val sender: Member, val group: Group, message: Message) : MessageEvent(sender, message)
open class PrivateMessageEvent(sender: Contact, message: Message) : MessageEvent(sender, message) {
    open class FriendMessage(override val sender: Friend, message: Message) : PrivateMessageEvent(sender, message)
    open class TempMessage(override val sender: Member, message: Message) : PrivateMessageEvent(sender, message)
}

open class MessageRecallEvent(open val sender: Contact, open val operator: Contact, val messageId: Int) : Event()
open class PrivateRecallEvent(sender: Contact, operator: Contact, messageId: Int) : MessageRecallEvent(sender, operator, messageId)
open class GroupRecallEvent(val group: Group, override val sender: Member, override val operator: Member, messageId: Int) : MessageRecallEvent(sender, operator, messageId)

open class FriendListEvent : Event()
open class FriendAddEvent(val friend: Friend) : FriendListEvent()
open class FriendDeleteEvent(val friend: Friend) : FriendListEvent()

open class GroupListEvent : Event()
open class BotJoinGroupEvent(val group: Group) : GroupListEvent()

/***
 * Bot 从某个群离开。
 * 当事件响应前，group 就已经从列表中被移出。
 */
open class BotLeaveGroupEvent(val group: Group) : GroupListEvent() {
    /***
     * Bot 主动退出某群。
     */
    open class Leave(group: Group) : BotLeaveGroupEvent(group)

    /***
     * Bot 因为某些特殊原因离开某群（其他客户端主动退出，群解散，群被强制解散等等）
     */
    open class Other(group: Group) : BotLeaveGroupEvent(group)

    /***
     * Bot 被某群移出。
     */
    open class Kick(val operator: Member) : BotLeaveGroupEvent(operator.group)
}

open class NewRequestEvent(val message: String) : Event(), CancelEvent {
    override fun cancelAble() = true
    var accept: Boolean? = null
    var rejectMessage: String = ""
}

open class NewFriendRequestEvent(val qq: UserInfo, val group: Group?, message: String) : NewRequestEvent(message)
open class GroupInviteEvent(val group: GroupInfo, val qq: UserInfo, message: String) : NewRequestEvent(message)
open class GroupMemberRequestEvent(val group: Group, val qq: UserInfo, message: String) : NewRequestEvent(message), CancelEvent {
    override fun cancelAble() = true
    val blackList = false
}

open class GroupMemberEvent(val group: Group, val member: Member) : Event()
open class GroupMemberJoinEvent(group: Group, member: Member) : GroupMemberEvent(group, member) {
    open class Join(group: Group, member: Member) : GroupMemberJoinEvent(group, member)
    open class Invite(group: Group, member: Member, val inviter: Member) : GroupMemberJoinEvent(group, member)
}

@Deprecated("群事件结构调整，使得命名语义更加清晰。", ReplaceWith("GroupMemberJoinEvent.Invite"))
open class GroupMemberInviteEvent(group: Group, member: Member, inviter: Member) : GroupMemberJoinEvent.Invite(group, member, inviter)


open class GroupMemberLeaveEvent(group: Group, member: Member) : GroupMemberEvent(group, member) {
    open class Leave(group: Group, member: Member) : GroupMemberLeaveEvent(group, member)
    open class Kick(group: Group, member: Member, val operator: Member) : GroupMemberLeaveEvent(group, member)
}

@Deprecated("群事件结构调整，使得命名语义更加清晰。", ReplaceWith("GroupMemberLeaveEvent.Kick"))
open class GroupMemberKickEvent(group: Group, member: Member, operator: Member) : GroupMemberLeaveEvent.Kick(group, member, operator)

open class GroupBanMemberEvent(group: Group, member: Member, val operator: Member, val time: Int) : GroupMemberEvent(group, member)
open class GroupUnBanMemberEvent(group: Group, member: Member, val operator: Member) : GroupMemberEvent(group, member)
open class GroupBanBotEvent(group: Group, member: Member, val operator: Member, val time: Int) : GroupMemberEvent(group, member)
open class GroupUnBanBotEvent(group: Group, member: Member, val operator: Member) : GroupMemberEvent(group, member)

open class ContextSessionCreateEvent(session: ContextSession) : Event()
open class ActionContextInvokeEvent(val context: BotActionContext) : Event(), CancelEvent {
    override fun cancelAble() = true
    open class Per(context: BotActionContext) : ActionContextInvokeEvent(context) {

    }

    open class Post(context: BotActionContext) : ActionContextInvokeEvent(context) {
    }
}

open class SendMessageEvent(val sendTo: Contact, val message: Message) : Event() {
    open class Per(sendTo: Contact, message: Message) : SendMessageEvent(sendTo, message), CancelEvent {
        override fun cancelAble() = true
    }

    open class Post(sendTo: Contact, message: Message, val messageSource: MessageSource) : SendMessageEvent(sendTo, message)
}

open class ClickEvent(open val operator: Contact, val action:String,val suffix:String) : Event()
open class ClickBotEvent(operator: Contact, action: String, suffix: String) : ClickEvent(operator, action, suffix) {
    open class Private(operator: Contact, action: String, suffix: String) : ClickBotEvent(operator, action, suffix) {
        open class FriendClick(override val operator: Friend, action: String, suffix: String) : Private(operator, action, suffix)
        open class TempClick(override val operator: Member, action: String, suffix: String) : Private(operator, action, suffix)
    }

    open class Group(override val operator: Member, action: String, suffix: String) : ClickBotEvent(operator, action, suffix)
}

open class ClickSomeBodyEvent(operator: Contact, open val target: Contact, action: String, suffix: String) : ClickEvent(operator, action, suffix) {
    open class Private(operator: Contact, target: Contact, action: String, suffix: String) : ClickSomeBodyEvent(operator, target, action, suffix)
    open class Group(override val operator: Member, override val target: Member, action: String, suffix: String) : ClickSomeBodyEvent(operator, target, action, suffix)
}
