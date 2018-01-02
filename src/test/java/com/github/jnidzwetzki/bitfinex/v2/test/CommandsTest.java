package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexOrderBuilder;
import com.github.jnidzwetzki.bitfinex.v2.commands.AbstractAPICommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.AuthCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.CancelOrderCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.CancelOrderGroupCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.CommandException;
import com.github.jnidzwetzki.bitfinex.v2.commands.OrderCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.PingCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeCandlesCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTickerCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTradingOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookFrequency;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookPrecision;
import com.github.jnidzwetzki.bitfinex.v2.entity.Timeframe;
import com.github.jnidzwetzki.bitfinex.v2.entity.TradeOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCurrencyPair;

public class CommandsTest {

	/**
	 * Call all commands and check for excepion
	 * @throws CommandException
	 */
	@Test
	public void testCommandsJSON() throws CommandException {
		
		final BitfinexOrder order 
			= BitfinexOrderBuilder.create(
					BitfinexCurrencyPair.BCH_USD, BitfinexOrderType.EXCHANGE_STOP, 2).build();
		
		final BitfinexCandlestickSymbol candleSymbol 
			= new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BCH_USD, Timeframe.HOUR_1);
		
		final TradeOrderbookConfiguration orderbookConfiguration 
			= new TradeOrderbookConfiguration(BitfinexCurrencyPair.BCH_USD, 
					OrderBookPrecision.P0, OrderBookFrequency.F0	, 50);
		
		final List<AbstractAPICommand> commands = Arrays.asList(
				new AuthCommand(), 
				new CancelOrderCommand(123),
				new CancelOrderGroupCommand(1),
				new OrderCommand(order),
				new PingCommand(), 
				new SubscribeCandlesCommand(candleSymbol),
				new SubscribeTickerCommand(BitfinexCurrencyPair.BCH_USD),
				new SubscribeTradingOrderbookCommand(orderbookConfiguration),
				new UnsubscribeChannelCommand(12));
		
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getApiKey()).thenReturn("abc");
		Mockito.when(bitfinexApiBroker.getApiSecret()).thenReturn("123");
		
		for(final AbstractAPICommand command : commands) {
			final String commandValue = command.getCommand(bitfinexApiBroker);
			Assert.assertNotNull(commandValue);
			Assert.assertTrue(commandValue.length() > 10);
		}
	}
}
