package auctionsniper;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.text.Format;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

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
	public void hasReceivedJoinRequestFromSniper() throws InterruptedException{
		messageListener.receivesAMessage();
	}
	
	//ステップ4
	public void announceClosed() throws XMPPException{
		currentChat.sendMessage(new Message());
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
	}
	
}
