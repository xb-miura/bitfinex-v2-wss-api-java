package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.CandlestickHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.Timeframe;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;


public class CandlestickHandlerTest {

	/**
	 * The delta for double compares
	 */
	private static final double DELTA = 0.001;

	/**
	 * Test the parsing of one candlestick
	 * @throws APIException
	 */
	@Test
	public void testCandlestickUpdateAndNotify() throws APIException {
		
		final String callbackValue = "[15134900000,15996,15997,16000,15980,318.5139342]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		
		final BitfinexCandlestickSymbol symbol 
			= new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BTC_USD, Timeframe.MINUTES_1);
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
		final QuoteManager tickerManager = new QuoteManager(bitfinexApiBroker);
		Mockito.when(bitfinexApiBroker.getquoteManager()).thenReturn(tickerManager);

		final AtomicInteger counter = new AtomicInteger(0);

		tickerManager.registerCandlestickCallback(symbol, (s, c) -> {
			counter.incrementAndGet();
			Assert.assertEquals(symbol, s);
			Assert.assertEquals(15996, c.getOpenPrice().toDouble(), DELTA);
			Assert.assertEquals(15997, c.getClosePrice().toDouble(), DELTA);
			Assert.assertEquals(16000, c.getMaxPrice().toDouble(), DELTA);
			Assert.assertEquals(15980, c.getMinPrice().toDouble(), DELTA);
			Assert.assertEquals(318.5139342, c.getVolume().toDouble(), DELTA);
		});
						
		final CandlestickHandler candlestickHandler = new CandlestickHandler();
		candlestickHandler.handleChannelData(bitfinexApiBroker, symbol, jsonArray);
		
		Assert.assertEquals(1, counter.get());
	}
	
	
	/**
	 * Test the parsing of a candlestick snapshot
	 * @throws APIException
	 */
	@Test
	public void testCandlestickSnapshotUpdateAndNotify() throws APIException {
		
		final String callbackValue = "[[15134900000,15996,15997,16000,15980,318.5139342],[15135100000,15899,15996,16097,15890,1137.180342268]]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		
		final BitfinexCandlestickSymbol symbol 
			= new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BTC_USD, Timeframe.MINUTES_1);
			
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
		final QuoteManager tickerManager = new QuoteManager(bitfinexApiBroker);
		Mockito.when(bitfinexApiBroker.getquoteManager()).thenReturn(tickerManager);

		final AtomicInteger counter = new AtomicInteger(0);
		
		tickerManager.registerCandlestickCallback(symbol, (s, c) -> {
			Assert.assertEquals(symbol, s);
			final int counterValue = counter.getAndIncrement();
			if(counterValue == 0) {
				Assert.assertEquals(15996, c.getOpenPrice().toDouble(), DELTA);
				Assert.assertEquals(15997, c.getClosePrice().toDouble(), DELTA);
				Assert.assertEquals(16000, c.getMaxPrice().toDouble(), DELTA);
				Assert.assertEquals(15980, c.getMinPrice().toDouble(), DELTA);
				Assert.assertEquals(318.5139342, c.getVolume().toDouble(), DELTA);
			} else if(counterValue == 1) {
				Assert.assertEquals(15899, c.getOpenPrice().toDouble(), DELTA);
				Assert.assertEquals(15996, c.getClosePrice().toDouble(), DELTA);
				Assert.assertEquals(16097, c.getMaxPrice().toDouble(), DELTA);
				Assert.assertEquals(15890, c.getMinPrice().toDouble(), DELTA);
				Assert.assertEquals(1137.180342268, c.getVolume().toDouble(), DELTA);
			} else {
				throw new IllegalArgumentException("Illegal call, expected 2 candlesticks");
			}
		});
						
		final CandlestickHandler candlestickHandler = new CandlestickHandler();
		candlestickHandler.handleChannelData(bitfinexApiBroker, symbol, jsonArray);
		
		Assert.assertEquals(2, counter.get());
	}
	
	/**
	 * Test the symbol encoding and decoding
	 */
	@Test
	public void testCandlestickSymbolEncoding1() {
		final BitfinexCandlestickSymbol symbol1 
			= new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BCH_USD, Timeframe.MINUTES_15);
		
		final BitfinexCandlestickSymbol symbol2
			= new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BTC_USD, Timeframe.MINUTES_15);
	
		Assert.assertFalse(symbol1.equals(symbol2));
		
		final String symbol1String = symbol1.toBifinexCandlestickString();
		final String symbol2String = symbol2.toBifinexCandlestickString();
		
		Assert.assertEquals(symbol1, BitfinexCandlestickSymbol.fromBitfinexString(symbol1String));
		Assert.assertEquals(symbol2, BitfinexCandlestickSymbol.fromBitfinexString(symbol2String));
	}
		
	/**
	 * Test the symbol encoding and decoding
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testCandlestickSymbolEncoding2() {
		final String symbol = "dffdsf:dfsfd:dsdfd";
		BitfinexCandlestickSymbol.fromBitfinexString(symbol);
	}
	
	/**
	 * Test the symbol encoding and decoding
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testCandlestickSymbolEncoding3() {
		final String symbol = "trading:";
		BitfinexCandlestickSymbol.fromBitfinexString(symbol);
	}
}
