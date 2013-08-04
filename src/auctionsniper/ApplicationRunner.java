package auctionsniper;

import auctionsniper.ui.Main;
import auctionsniper.ui.Main.MainWindow;

public class ApplicationRunner {
	public static final String SNIPER_ID = "sniper";
	public static final String SNIPER_PASSWORD = "sniper";
	private AuctionSniperDriver driver;
	
	//ステップ2
	public void startBiddingIn(final FakeAuctionServer auction){
		Thread thread = new Thread("Test Application"){
			@Override public void run(){
				try{
					Main.main(FakeAuctionServer.XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD, auction.getItemId());
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
		driver = new AuctionSniperDriver(1000);
		driver.showsSniperStatus(Main.STATUS_JOINING);
	}
	//ステップ5
	public void showsSniperHasLostAuction(){
		driver.showsSniperStatus(MainWindow.STATUS_LOST);
	}
	
	public void stop(){
		if(driver != null){
			driver.dispose();
		}
	}
}
