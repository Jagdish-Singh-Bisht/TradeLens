import { useEffect, useRef, useState } from 'react'
import { createChart, CandlestickSeries } from 'lightweight-charts'
import './CandlestickChart.css'


function CandlestickChart() {

    const chartContainerRef = useRef(null)

    const[instruments, setInstruments] = useState([])

    const[selectedInstrument, setSelectedInstrument] = useState(null)

    const [noData, setNoData] = useState(false)

    const [hoveredCandle, setHoveredCandle] = useState(null)


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

        if(!selectedInstrument) {
            return
        }

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

            if(!param.time) {
                setHoveredCandle(null)
                return
            }

            const candle = param.seriesData.get(candlestickSeries)

            if(!candle){
                setHoveredCandle(null)
                return
            }

            setHoveredCandle({
                time: param.time,
                open: candle.open,
                high: candle.high,
                low: candle.low,
                close: candle.close
            })

        }

        chart.subscribeCrosshairMove(handleCrosshairMove)

        fetch(`http://localhost:8080/api/market-candles/${selectedInstrument.symbol}/${selectedInstrument.exchange}`,
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

                if (candles.length === 0) {
                    setNoData(true)
                    return
                }

                setNoData(false)

                const chartData = candles.map(candle => {
                    const [date, time] = candle.candleTime.split('T')
                    const [year, month, day] = date.split('-').map(Number)
                    const [hour, minute, second = 0] = time.split(':').map(Number)

                    return {
                        time: Math.floor(
                            Date.UTC(year, month - 1, day, hour, minute, second) / 1000
                        ),
                        open: Number(candle.open),
                        high: Number(candle.high),
                        low: Number(candle.low),
                        close: Number(candle.close)
                    }
                })

                candlestickSeries.setData(chartData)
                chart.timeScale().fitContent()
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

    }, [selectedInstrument])

    return (
        <div className="candlestick-card">
            <div className="candlestick-heading">
                <h3>
                    { selectedInstrument
                        ? `${selectedInstrument.symbol} . ${selectedInstrument.exchange}`
                        : 'Loading instruments...'
                    }
                </h3>

                <p>Historical price movement</p>
            </div>

            <select
                value= {
                    selectedInstrument
                        ? `${selectedInstrument.symbol}|${selectedInstrument.exchange}`
                        : ''
                }

                onChange = { event => {
                    const [symbol, exchange] = event.target.value.split('|')

                    const instrument = instruments.find(
                        item =>
                            item.symbol === symbol &&
                            item.exchange === exchange
                    )

                    if(instrument) {
                        setSelectedInstrument(instrument)
                    }

                }}
                disabled = {instruments.length === 0}
            >
                {instruments.map(instrument => (
                    <option
                        key={ `${instrument.symbol}|${instrument.exchange}` }
                        value={ `${instrument.symbol}|${instrument.exchange}` }
                    >
                        {instrument.symbol} . {instrument.exchange}
                    </option>
                ))}

            </select>

            <div className="candlestick-chart-container">

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
                        No historical candle data available for this instrument.
                    </p>
                )}

                <div ref={chartContainerRef} className="candlestick-chart" />
            </div>

        </div>
    )

}

export default CandlestickChart