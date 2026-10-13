import { useEffect, useRef, useState } from 'react'
import { createChart, CandlestickSeries, createSeriesMarkers } from 'lightweight-charts'
import './CandlestickChart.css'



function CandlestickChart({
                              symbol: tradeSymbol,
                              exchange: tradeExchange,
                              entryTime,
                              exitTime,
                              entryPrice,
                              exitPrice
                          }) {

    const chartContainerRef = useRef(null)

    const[instruments, setInstruments] = useState([])

    const[selectedInstrument, setSelectedInstrument] = useState(null)

    const [noData, setNoData] = useState(false)

    const [hoveredCandle, setHoveredCandle] = useState(null)

    const chartSymbol = tradeSymbol || selectedInstrument?.symbol;
    const chartExchange = tradeExchange || selectedInstrument?.exchange;

    const [tradeMarkerTooltip, setTradeMarkerTooltip] = useState(null);

    const tradeMarkerTimes = useRef({
        entry: null,
        exit: null
    });

    const [tooltipPosition, setTooltipPosition] = useState({
        x: 12,
        y: 12
    });

    useEffect(() => {
        fetch('http://localhost:8080/api/market-candles/instruments', {
            credentials: 'include'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load instruments')
                }
                return response.json()
            })
            .then(data => {
                setInstruments(data)

                if (data.length > 0) {
                    setSelectedInstrument(data[0])
                }
            })
            .catch(error => {
                console.error('Error loading instruments:', error)
            })
    }, [])

    useEffect(() => {

        if (!chartSymbol || !chartExchange) return;

        const chart = createChart(chartContainerRef.current, {

            width: chartContainerRef.current.clientWidth,
            height: 450,

            crosshair: {mode: 0},

            layout: {
                background: { color: '#ffffff'},
                textColor: '#374151'
            },
            grid: {
                vertLines: { color: '#f0f0f0' },
                horLines: { color: '#f0f0f0' }
            },
            rightPriceScale: {
                borderColor: '#e5e7eb'
            },
            timeScale: {
                borderColor: '#e5e7eb',
                timeVisible: true,
                secondsVisible: false
            }
        })

        const candlestickSeries = chart.addSeries(CandlestickSeries, {
            upColor: '#16a34a',
            downColor: '#ef4444',
            borderUpColor: '#16a34a',
            borderDownColor: '#ef4444',
            wickUpColor: '#16a34a',
            wickDownColor: '#ef4444'
        })

        const handleCrosshairMove = (param) => {
            if (param.time == null) {
                setHoveredCandle(null);
                setTradeMarkerTooltip(null);
                return;
            }

            const candle = param.seriesData.get(candlestickSeries);

            if (!candle) {
                setHoveredCandle(null);
                setTradeMarkerTooltip(null);
                return;
            }

            setHoveredCandle({
                time: param.time,
                open: candle.open,
                high: candle.high,
                low: candle.low,
                close: candle.close
            });

            const hoveredTime =
                typeof param.time === 'number' ? param.time : null;

            if (param.point) {
                setTooltipPosition({
                    x: Math.max(8, Math.min(param.point.x + 14, chartContainerRef.current.clientWidth - 230)),
                    y: Math.max(8, param.point.y - 45)
                });
            }

            const { entry, exit } = tradeMarkerTimes.current;

            if (hoveredTime === entry && entry !== null) {
                setTradeMarkerTooltip({
                    action: 'BUY',
                    price: entryPrice,
                    time: entryTime
                });
            } else if (hoveredTime === exit && exit !== null) {
                setTradeMarkerTooltip({
                    action: 'SELL',
                    price: exitPrice,
                    time: exitTime
                });
            } else {
                setTradeMarkerTooltip(null);
            }
        };

        chart.subscribeCrosshairMove(handleCrosshairMove)

        fetch(`http://localhost:8080/api/market-candles/${chartSymbol}/${chartExchange}`,
            {
                credentials: 'include'
            })
            .then(response => {
                if(!response.ok) {
                    throw new Error('Failed to fetch market candles')
                }
                return response.json()
            })

            .then(candles => {
                // Clear details from the previously selected trade.
                setHoveredCandle(null);
                setTradeMarkerTooltip(null);
                tradeMarkerTimes.current = { entry: null, exit: null };

                if (!candles.length) {
                    setNoData(true);
                    return;
                }

                // Convert the trade's date and time into Unix seconds.
                const toChartTime = (dateTime) => {
                    if (!dateTime) return null;

                    const [datePart, timePart] = dateTime.split('T');
                    if (!timePart) return null;

                    const [year, month, day] = datePart.split('-').map(Number);
                    const [hour, minute, second = 0] = timePart.split(':').map(Number);

                    return Math.floor(
                        Date.UTC(year, month - 1, day, hour, minute, second) / 1000
                    );
                };

                const entryTimestamp = toChartTime(entryTime);
                const exitTimestamp = toChartTime(exitTime);

                // Show only candles on the selected trade's date.
                // Include 30 minutes before entry and 15 minutes after exit.
                const tradeDate = entryTime?.split('T')[0];

                const rangeStart = entryTimestamp == null
                    ? null
                    : entryTimestamp - 30 * 60;

                // const rangeEnd = exitTimestamp == null
                //     ? null
                //     : exitTimestamp + 15 * 60;

                const rangeEnd = exitTimestamp == null
                    ? rangeStart + 6 * 60 * 60
                    : toChartTime(
                        `${exitTime.slice(0, 10)}T23:59:00`
                    );

                const relevantCandles = tradeDate
                    ? candles.filter(candle => {
                        const timestamp = toChartTime(candle.candleTime);

                        return (
                            candle.candleTime.startsWith(tradeDate) &&
                            (rangeStart == null || timestamp >= rangeStart) &&
                            (rangeEnd == null || timestamp <= rangeEnd)
                        );
                    })
                    : candles;

                if (relevantCandles.length === 0) {
                    setNoData(true);
                    return;
                }

                setNoData(false);

                const chartData = relevantCandles
                    .map(candle => ({
                        time: toChartTime(candle.candleTime),
                        open: Number(candle.open),
                        high: Number(candle.high),
                        low: Number(candle.low),
                        close: Number(candle.close)
                    }))
                    .sort((a, b) => a.time - b.time);

                candlestickSeries.setData(chartData);

                // A marker is allowed only when a candle exists at
                // the exact trade timestamp. Never use a different date's candle.
                const candleTimes = new Set(chartData.map(candle => candle.time));

                const markers = [];

                const nearestCandle = (timestamp) => {
                    if(timestamp == null || chartData.length === 0) return null;

                    const nearest = chartData.reduce((best, candle) =>
                            Math.abs(candle.time - timestamp) <
                            Math.abs(best.time - timestamp)
                                ? candle
                                : best
                    );

                    return Math.abs(nearest.time - timestamp) <= 120
                        ? nearest
                        : null;

                };

                const entryCandle = nearestCandle(entryTimestamp);
                const exitCandle = nearestCandle(exitTimestamp);

                if (entryCandle) {
                    markers.push({
                        time: entryCandle.time,
                        position: 'atPriceMiddle',
                        price: Number(entryPrice),
                        color: '#006400', // #16a34a
                        shape: 'arrowUp',
                        // text: '▲',
                        size: 0.1
                    });
                }

                if (exitCandle) {
                    markers.push({
                        time: exitCandle.time,
                        position: 'atPriceMiddle',
                        price: Number(exitPrice),
                        color: '#dc2626',
                        shape: 'arrowDown',
                        // text: '▼',
                        size: 0.1
                    });
                }


                markers.sort((a, b) => a.time - b.time);

                createSeriesMarkers(candlestickSeries, markers);

                tradeMarkerTimes.current = {
                    entry: entryCandle?.time ?? null,
                    exit: exitCandle?.time ?? null
                };

                chart.timeScale().fitContent();
            })

            .catch(error => console.error('Candlestick Chart error: ', error))

        const handleResize = () => {
            chart.applyOptions({
                width: chartContainerRef.current.clientWidth
            })
        }

        window.addEventListener('resize', handleResize)

        return () => {
            chart.unsubscribeCrosshairMove(handleCrosshairMove)
            window.removeEventListener('resize', handleResize)
            chart.remove()
        }

    }, [
        chartSymbol,
        chartExchange,
        entryTime,
        exitTime,
        entryPrice,
        exitPrice
    ])

    return (

        <div className="candlestick-card">
            <div className="candlestick-heading">
                <h3>
                    {chartSymbol && chartExchange
                        ? `${chartSymbol} · ${chartExchange}`
                        : 'Loading chart...'}
                </h3>

                <p>Historical price movement</p>
            </div>

            {!tradeSymbol && !tradeExchange && (
                <select
                    value={
                        selectedInstrument
                            ? `${selectedInstrument.symbol}|${selectedInstrument.exchange}`
                            : ''
                    }
                    onChange={(event) => {
                        const instrument = instruments.find(
                            (item) =>
                                `${item.symbol}|${item.exchange}` === event.target.value
                        );

                        setSelectedInstrument(instrument || null);
                    }}
                >
                    {instruments.map((instrument) => (
                        <option
                            key={`${instrument.symbol}|${instrument.exchange}`}
                            value={`${instrument.symbol}|${instrument.exchange}`}
                        >
                            {instrument.symbol} · {instrument.exchange}
                        </option>
                    ))}
                </select>
            )}

            <div className="candlestick-chart-container">

                {tradeMarkerTooltip && (
                    <div
                        className={`trade-marker-tooltip ${
                            tradeMarkerTooltip.action === 'BUY' ? 'buy-tooltip' : 'sell-tooltip'
                        }`}
                        style={{
                            left: `${tooltipPosition.x}px`,
                            top: `${tooltipPosition.y}px`
                        }}
                    >
                        <strong>{tradeMarkerTooltip.action}</strong>
                        <span>₹{Number(tradeMarkerTooltip.price).toFixed(2)}</span>
                        <span>{tradeMarkerTooltip.time?.replace('T', ' ')}</span>
                    </div>
                )}

                {hoveredCandle && (
                    <div className="ohlc-details">
                        <span>
                            Time: {
                            typeof hoveredCandle.time === 'number'
                                ? new Date(hoveredCandle.time * 1000)
                                    .toISOString()
                                    .slice(11, 16)
                                : '—'
                        }
                        </span>

                        <span>O: {hoveredCandle.open.toFixed(2)}</span>

                        <span className="ohlc-high">
                            H: {hoveredCandle.high.toFixed(2)}
                        </span>

                        <span className="ohlc-low">
                            L: {hoveredCandle.low.toFixed(2)}
                        </span>

                        <span className={
                            hoveredCandle.close >= hoveredCandle.open
                                ? 'ohlc-high'
                                : 'ohlc-low'
                        }>
                            C: {hoveredCandle.close.toFixed(2)}
                        </span>
                    </div>
                )}

                {noData && (
                    <p className="no-candle-data">
                        No historical candle data available for this trade's date
                        and time range. Import candle data covering the trade period.
                    </p>
                )}

                <div ref={chartContainerRef} className="candlestick-chart" />
            </div>

        </div>
    )

}

export default CandlestickChart