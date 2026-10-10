import {useEffect, useState} from 'react'

import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    ReferenceLine
} from 'recharts'

import './PnlChart.css'

function TradePnlChart() {

    const [data, setData] = useState([])

    useEffect(() => {
        fetch('http://localhost:8080/api/broker-accounts/1/analytics/pnl-trend',
            {
                credentials: 'include'
            })
            .then(response => {
                if(!response.ok) {
                    throw new Error('Failed to fetch P&L data')
                }

                return response.json()
            })
            .then(result => setData(result))
            .catch(error => console.error(error))

    }, [])

    return (

        <div className="pnl-chart-card">

            <div className="pnl-chart-heading">
                <h3>Trade-by-Trade P&L</h3>
                <p>Profit or Loss from each completed trade</p>
            </div>

            <ResponsiveContainer width="100%" height={300}>
                <BarChart data={data}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="tradeNumber" />
                    <YAxis />

                    <Tooltip

                        content={({ active, payload, label }) => {
                            if(!active || !payload || !payload.length) {
                                return null
                            }

                            const trade = payload[0].payload

                            return (
                                <div className="pnl-tooltip">
                                    <strong>Trade {label}</strong>

                                    <p>
                                        Trade P&L: Rs.{Number(trade.profitLoss).toLocaleString('en-IN')}
                                    </p>

                                    <p>
                                        Cumulative P&L: Rs.{Number(trade.cumulativeProfitLoss).toLocaleString('en-IN')}
                                    </p>

                                </div>
                            )
                        }}

                        // formatter={(value) => [
                        //     `Rs.${Number(value).toLocaleString('en-IN', {
                        //         maximumFractionDigits: 2
                        //     })}`,
                        //     'Trade P&L'
                        // ]}
                        // labelFormatter={label => `Trade ${label}`}

                    />

                    <ReferenceLine y={0} stroke="#6b7280" />

                    <Bar
                        dataKey="profitLoss"
                        shape={(props) => {
                            const { x, y, width, height, payload } = props

                            return (
                                <rect
                                    x={x}
                                    y={height < 0 ? y + height : y}
                                    width={width}
                                    height={Math.abs(height)}
                                    rx={3}
                                    fill={
                                        payload.profitLoss >= 0
                                            ? '#16a34a'
                                            : '#ef4444'
                                    }
                                />
                            )
                        }}
                    />

                </BarChart>
            </ResponsiveContainer>

        </div>
    )

}

export default TradePnlChart