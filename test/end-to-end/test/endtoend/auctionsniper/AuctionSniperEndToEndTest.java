package test.endtoend.auctionsniper;

import org.junit.After;
import org.junit.Test;

import auctionsniper.ApplicationRunner;
import auctionsniper.FakeAuctionServer;

public class AuctionSniperEndToEndTest {
	private final FakeAuctionServer auction = new FakeAuctionServer("item-54321");
	private final ApplicationRunner application = new ApplicationRunner();
	
	@Test
	public void sniperJoinsAuctionUntilAuctionCloses() throws Exception{
		
		auction.startSellingItem();						//ステップ1
		application.startBiddingIn(auction);			//ステップ2
		auction.hasReceivedJoinRequestFromSniper();		//ステップ3
		auction.announceClosed();						//ステップ4
		application.showsSniperHasLostAuction();		//ステップ5
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
