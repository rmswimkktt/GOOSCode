package auctionsniper;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.text.Format;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import sun.org.mozilla.javascript.internal.ast.ThrowStatement;

import auctionsniper.ui.Main;

import com.objogate.wl.internal.Timeout;


public class FakeAuctionServer {
	private final SingleMessageListener messageListener = new SingleMessageListener();
	
	public static final String ITEM_ID_AS_LOGIN = "auction-%s";
	public static final String AUCTION_RESOURCE = "Auction";
	public static final String XMPP_HOSTNAME = "localhost";
	private static final String AUCTION_PASSWORD = "auction";
	
	private final String itemId;
	private final XMPPConnection connection;
	private Chat currentChat;
	
	public FakeAuctionServer(String itemId){
		this.itemId = itemId;
		this.connection = new XMPPConnection(XMPP_HOSTNAME);
	}
	
	//ステップ1
	public void startSellingItem() throws XMPPException{
		connection.connect();
		connection.login(String.format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD, AUCTION_RESOURCE);
		connection.getChatManager().addChatListener(
			new ChatManagerListener(){
				public void chatCreated(Chat chat, boolean createdLocally){
					currentChat = chat;
					chat.addMessageListener(messageListener);
				}
			}
		);
	}
	public String getItemId(){
		return itemId;
	}
	
	//ステップ3
	public void hasReceivedJoinRequestFromSniper(String sniperId) throws InterruptedException{
		receivesAMessageMatching(sniperId, equalTo(Main.JOIN_COMMOND_FORMAT));
	}
	
	//「入札」を受信したか
	public void hasReceiveBid(int bid, String sniperId) throws InterruptedException {
		receivesAMessageMatching(sniperId, equalTo(String.format(Main.BID_COMMOND_FORMAT, bid)));
	}
	
	private void receivesAMessageMatching(String sniperId, Matcher<? super String> messageMatcher) throws InterruptedException{
		messageListener.receivesAMessage(messageMatcher);
		assertThat(currentChat.getParticipant(), equalTo(sniperId));
	}
	
	//ステップ4
	public void announceClosed() throws XMPPException{
		currentChat.sendMessage("SQLVersion: 1.1; Event: CLOSE;");
	}
	
	public void stop(){
		connection.disconnect();
	}
	
	public class SingleMessageListener implements MessageListener{
		private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<Message>(1);

		public void processMessage(Chat chat, Message message){
			((BlockingQueue<Message>) messages).add(message);
		}
		
		public void receivesAMessage() throws InterruptedException{
			assertThat("Message", messages.poll(5, TimeUnit.SECONDS), is(notNullValue()));
		}
		@SuppressWarnings("unchecked")
		public void receivesAMessage(Matcher<? super String> messageMatcher) throws InterruptedException{
			final Message message = messages.poll(5, TimeUnit.SECONDS);
			assertThat("Message", messages, is(notNullValue()));
			assertThat(message.getBody(), messageMatcher);
		}
	}

	public void reportPrice(int price, int increment, String bidder) throws XMPPException{
		currentChat.sendMessage(String.format("SQLVersion: 1.1; Event: PRICE; CurrentPrice: %d; "
				+ "Increment: %d; Bidder: %s;", price, increment, bidder));
		
	}
	
}
