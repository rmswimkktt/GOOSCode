package test.endtoend.auctionsniper;

import org.junit.After;
import org.junit.Test;

import auctionsniper.ApplicationRunner;
import auctionsniper.FakeAuctionServer;

public class AuctionSniperEndToEndTest {
	private final FakeAuctionServer auction = new FakeAuctionServer("item-54321");
	private final ApplicationRunner application = new ApplicationRunner();
	
	@Test
	public void sniperMakesAHigherBidButLoses() throws Exception{
		auction.startSellingItem();
		
		application.startBiddingIn(auction);
		auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
		
 		auction.reportPrice(1000, 98, "other bidder");
 		
 		application.hasShownSniperIsBidding();
 		auction.hasReceiveBid(1098, ApplicationRunner.SNIPER_XMPP_ID);
 		
 		auction.announceClosed();
 		application.showsSniperHasLostAuction();
		
	}
	@Test
	public void sniperJoinsAuctionUntilAuctionCloses() throws Exception{
		
		auction.startSellingItem();														//ステップ1
		application.startBiddingIn(auction);											//ステップ2
		auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);		//ステップ3
		auction.announceClosed();														//ステップ4
		application.showsSniperHasLostAuction();										//ステップ5
	}
	
	//追加のクリーンアップ
	@After
	public void stopAuction(){
		auction.stop();
	}
	@After
	public void stopApplication(){
		application.stop();
	}
}
