package auctionsniper;

import java.util.EventListener;

public interface AuctionEventListener extends EventListener{

	enum PriceSource{
		FromSniper, FromOtherBidder, Other;
	};
	
	public void auctionClosed();

	public void currentPrice(int i, int j, PriceSource priceSource);

}
