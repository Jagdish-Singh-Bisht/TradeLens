import { useEffect, useRef } from 'react'
import { createChart, CandlestickSeries } from 'lightweight-charts'
import './CandlestickChart.css'


function CandlestickChart() {

    const chartContainerRef = useRef(null)

    useEffect(() => {

        const chart = createChart(chartContainerRef.current, {

            width: chartContainerRef.current.clientWidth,
            height: 450,
            layout: {
                background: { color: '#ffffff'},
                textColor: '#374151'
            },
            grid: {
                vertLines: { color: '#f0f0f0' },
                horLines: { color: '#f0f0f0' }
            },
            rightPriceScale: {
                broderColor: '#e5e7eb'
            },
            timeScale: {
                broderColor: '#e5e7eb',
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

        fetch('http://localhost:8080/api/market-candles/RELIANCE/NSE',
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
            window.removeEventListener('resize', handleResize)
            chart.remove()
        }

    }, [])

    return (
        <div className="candlestick-card">
            <div className="candlestick-heading">
                <h3>RELIANCE · NSE</h3>
                <p>Historical price movement</p>
            </div>

            <div ref={chartContainerRef} className="candlestick-chart" />
        </div>
    )

}

export default CandlestickChart