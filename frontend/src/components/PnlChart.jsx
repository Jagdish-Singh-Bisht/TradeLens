import './PnlChart.css'

import {useEffect, useState} from 'react'

import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    ReferenceLine
} from 'recharts'

function PnlChart() {

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
                <h3>P&L Performance</h3>
                <p>Cumulative profit and loss across completed trades</p>
            </div>

            <ResponsiveContainer width="100%" height={350}>

                <LineChart data={data}>

                    <CartesianGrid strokeDasharray="3 3" />

                    <ReferenceLine
                        y={0}
                        stroke="#6b7280"
                        strokeDasharray="4 4"
                    />

                    <XAxis
                        dataKey = "tradeNumber"
                        label={{
                            value: 'Trade',
                            position: 'insideBottom',
                            offset: -5
                        }}
                    />

                    <YAxis />

                    <Tooltip

                        formatter={(value) => [
                            `Rs.${Number(value).toLocaleString('en-IN', {
                                maximumFractionDigits: 2
                            })}`,
                            'Cumulative P&L'
                        ]}

                        labelFormatter={(label) => `Trade ${label}`}
                    />

                    <Line
                        type="monotone"
                        dataKey="cumulativeProfitLoss"
                        stroke="#2563eb"
                        strokeWidth={2.5}
                        dot={{ r: 4}}
                        activeDot={{r : 7}}
                    />

                </LineChart>

            </ResponsiveContainer>

        </div>
    )

}

export default PnlChart